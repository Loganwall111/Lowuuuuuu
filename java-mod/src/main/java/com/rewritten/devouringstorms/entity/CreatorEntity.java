package com.rewritten.devouringstorms.entity;

import com.rewritten.devouringstorms.registry.ModEntities;
import com.rewritten.devouringstorms.registry.ModParticles;
import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.util.ModTexts;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * THE CREATOR. "It's like the entire world itself."
 * A cosmos that took shape over the Cosmic Abyss: tall enough that your eyes file it under
 * 'weather', red eyes that file YOU under 'arrival'. It speaks ordinary human language in
 * a register your chest cavity feels before your ears do — and it has A HAND. People keep
 * flying up to it. People keep coming back down.
 */
public class CreatorEntity extends Monster {

    private int nextSpeechAt = 400;
    private int nextHandAt = 600;

    public CreatorEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setCustomName(Component.literal("§4§lTHE CREATOR"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createCreatorAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 900.0)
            .add(Attributes.MOVEMENT_SPEED, 0.02)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
            .add(Attributes.FOLLOW_RANGE, 128.0)
            .add(Attributes.ARMOR, 12.0)
            .add(Attributes.SCALE, 8.0);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;

        // starfall: hair made of static
        if (this.tickCount % 2 == 0) {
            level.sendParticles(ParticleTypes.END_ROD,
                this.getX() + (this.getRandom().nextGaussian()) * 3.5,
                this.getY() + this.getBbHeight() * (0.3 + this.getRandom().nextDouble() * 0.7),
                this.getZ() + this.getRandom().nextGaussian() * 3.5,
                1, 0.08, 0.05, 0.08, 0.005);
            if (this.tickCount % 10 == 0) {
                level.sendParticles(ModParticles.GLITCH,
                    this.getX(), this.getY() + this.getBbHeight() * 0.9, this.getZ(),
                    2, 1.6, 0.5, 1.6, 0.03);
            }
        }
        if (this.tickCount % 61 == 0) {
            level.playSound(null, this, ModSounds.AMBIENT_RIFT_HUM, SoundSource.HOSTILE, 3.0f, 0.25f);
        }

        LivingEntity target = this.getTarget();
        if (target == null) {
            var near = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(96.0));
            for (Player p : near) {
                if (!p.isCreative() && !p.isSpectator()) { this.setTarget(p); target = p; break; }
            }
        }

        // ---- THE HAND. You came up to it. It answers personally. ----
        if (this.tickCount >= nextHandAt && target != null && target.isAlive()) {
            nextHandAt = this.tickCount + 400 + this.getRandom().nextInt(260);
            spawnHand(level, target.position());
            Vec3 eye = this.getEyePosition();
            level.playSound(null, this, ModSounds.MASSG_ROAR, SoundSource.HOSTILE, 4.0f, 0.35f);
            broadcastSpec(level, "§4§lCREATOR: §r§4§oHere. I made this for you. The bend, not the hand.",
                120.0);
        }

        // ---- the speech. plain words, deep register, no shouting required ----
        if (this.tickCount >= nextSpeechAt) {
            nextSpeechAt = this.tickCount + 600 + this.getRandom().nextInt(700);
            var hearers = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(72.0));
            if (!hearers.isEmpty()) {
                String line = ModTexts.CREATOR_LINES.get(this.getRandom().nextInt(ModTexts.CREATOR_LINES.size()));
                for (Player p : hearers) {
                    p.sendSystemMessage(Component.literal("§4§lCREATOR: §r§4" + line));
                }
                level.playSound(null, this, ModSounds.TERMINAL_TRANSMISSION, SoundSource.HOSTILE, 1.6f, 0.5f);
            }
        }

        // it never looks away. that is its one trick.
        if (target != null) {
            Vec3 look = target.position().subtract(this.position());
            this.setYRot((float) (Math.atan2(-look.x, look.z) * 180.0 / Math.PI));
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
        }
    }

    private void spawnHand(ServerLevel level, Vec3 at) {
        CreatorHandEntity hand = ModEntities.CREATOR_HAND.create(level);
        if (hand == null) return;
        hand.moveTo(at.x, at.y + 11.0, at.z, 0.0f, 0.0f);
        hand.setOwner(this.getUUID());
        hand.setStrikeAt(at);
        level.addFreshEntity(hand);
    }

    private void broadcastSpec(ServerLevel level, String text, double radius) {
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(radius))) {
            p.sendSystemMessage(Component.literal(text));
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // dramatic chip: stars ring off it
        level.sendParticles(ParticleTypes.END_ROD,
            this.getX(), this.getY() + this.getBbHeight() * 0.5, this.getZ(), 24, 2.0, 2.0, 2.0, 0.06);
        return super.hurtServer(level, source, amount * 0.35f);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }
}
