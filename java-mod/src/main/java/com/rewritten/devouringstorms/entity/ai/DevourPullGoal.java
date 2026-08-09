package com.rewritten.devouringstorms.entity.ai;

import com.rewritten.devouringstorms.entity.MassgEntity;
import com.rewritten.devouringstorms.storm.MassgPhase;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * The tractor pull. Every so often the storm chooses up to three living things and
 * drags them, screaming, towards its core. Anything that reaches the mouth is devoured.
 */
public class DevourPullGoal extends Goal {

    private static final int BEAM_DURATION = 80;   // ticks a victim is dragged
    private static final int BEAM_COOLDOWN = 240;  // ticks between pulls (12 s)

    private final MassgEntity massg;
    private final List<LivingEntity> victims = new ArrayList<>();
    private int cooldown = BEAM_COOLDOWN;
    private int beamTicks;

    public DevourPullGoal(MassgEntity massg) {
        this.massg = massg;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.massg.getPhase().atLeast(MassgPhase.HUNGER) && this.massg.getDeadTicks() < 0;
    }

    @Override
    public void tick() {
        if (!(this.massg.level() instanceof ServerLevel level)) return;

        // ---- reeling in current victims ----
        if (this.beamTicks > 0) {
            this.beamTicks--;
            Vec3 mouth = this.massg.mouthPosition();
            this.victims.removeIf(v -> !v.isAlive());
            for (LivingEntity victim : this.victims) {
                Vec3 pull = mouth.subtract(victim.position());
                double dist = pull.length();
                if (dist < 4.5) {
                    this.massg.devour(victim);
                    continue;
                }
                victim.setDeltaMovement(victim.getDeltaMovement().add(pull.normalize().scale(0.09)));
                victim.hurtMarked = true;
                victim.fallDistance = 0.0f;
                if (this.beamTicks % 3 == 0) {
                    spawnBeam(level, victim, mouth);
                }
            }
            if (this.victims.isEmpty() || this.beamTicks == 0) {
                this.victims.clear();
                this.cooldown = BEAM_COOLDOWN;
            }
            return;
        }

        // ---- choosing new victims ----
        if (--this.cooldown > 0) return;
        var inRange = level.getEntitiesOfClass(LivingEntity.class,
            this.massg.getBoundingBox().inflate(64.0),
            e -> e != this.massg && e.isAlive() && !(e instanceof MassgEntity));
        if (inRange.isEmpty()) {
            this.cooldown = 100;
            return;
        }
        this.victims.clear();
        var random = this.massg.getRandom();
        int count = Math.min(1 + random.nextInt(3), inRange.size());
        for (int i = 0; i < count; i++) {
            var pick = inRange.remove(random.nextInt(inRange.size()));
            this.victims.add(pick);
        }
        this.beamTicks = BEAM_DURATION;
        level.playSound(null, this.massg, com.rewritten.devouringstorms.registry.ModSounds.MASSG_PULL_LOOP,
            net.minecraft.sounds.SoundSource.HOSTILE, 3.0f, 1.0f);
        level.sendParticles(ParticleTypes.FLASH,
            this.massg.getX(), this.massg.getY() + 2.0, this.massg.getZ(), 1, 0, 0, 0, 0);
    }

    /** A visible beam of streaming particles from victim to mouth — the devouring itself. */
    private void spawnBeam(ServerLevel level, LivingEntity victim, Vec3 mouth) {
        Vec3 from = victim.getEyePosition();
        Vec3 diff = mouth.subtract(from);
        int points = 8;
        for (int i = 0; i <= points; i++) {
            Vec3 p = from.add(diff.scale(i / (double) points));
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        level.sendParticles(ParticleTypes.ASH, victim.getX(), victim.getY() + 0.5, victim.getZ(), 2, 0.3, 0.3, 0.3, 0.01);
    }
}
