package com.rewritten.devouringstorms.entity;

import com.rewritten.devouringstorms.registry.ModBlocks;
import com.rewritten.devouringstorms.registry.ModParticles;
import com.rewritten.devouringstorms.util.ModTexts;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * THE FORGER. "spits tentacles out of the sky and opens doors nobody asked for."
 * It hangs at the ceiling of anything that deserves a ceiling: tentacle rain on your head,
 * and — at intervals the quarantine redacted — it opens GIGANTIC RIFTS to the Multiverse
 * and to the Plague dimension. It forges passages. It never asks first.
 */
public class ForgerEntity extends Monster {

    private int nextTentacleAt = 200;
    private int nextRiftAt = 900;
    private int nextGrumbleAt = 800;

    public ForgerEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createForgerAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 160.0)
            .add(Attributes.MOVEMENT_SPEED, 0.22)
            .add(Attributes.ATTACK_DAMAGE, 10.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.7)
            .add(Attributes.FOLLOW_RANGE, 56.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;
        var random = this.getRandom();

        // ---- tentacle rain from the sky ----
        if (this.tickCount >= nextTentacleAt) {
            nextTentacleAt = this.tickCount + 300 + random.nextInt(260);
            var prey = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(48.0));
            for (Player p : prey.stream().limit(3).toList()) {
                for (int i = 0; i < 3; i++) {
                    BlockPos sky = p.blockPosition().offset(random.nextInt(9) - 4,
                        22 + random.nextInt(8), random.nextInt(9) - 4);
                    var tentacle = ModEntities.SKY_TENTACLE.create(level);
                    if (tentacle == null) continue;
                    tentacle.moveTo(sky.getX() + 0.5, sky.getY(), sky.getZ() + 0.5, random.nextInt(360), 0.0f);
                    tentacle.setDeltaMovement(0, -0.5 - random.nextDouble() * 0.3, 0);
                    level.addFreshEntity(tentacle);
                }
            }
            if (!prey.isEmpty()) {
                broadcastNear(level, prey.get(random.nextInt(prey.size())),
                    ModTexts.FORGER_LINES.get(random.nextInt(ModTexts.FORGER_LINES.size())));
            }
            level.sendParticles(ModParticles.GLITCH, this.getX(), this.getY() + 2.4, this.getZ(),
                30, 2.4, 0.4, 2.4, 0.04);
        }

        // ---- the gigantic rifts: a vertical seam, a door to elsewhere ----
        if (this.tickCount >= nextRiftAt) {
            nextRiftAt = this.tickCount + 1400 + random.nextInt(1200);
            var nearPlayers = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(64.0));
            for (Player p : nearPlayers.stream().limit(2).toList()) {
                BlockPos base = p.blockPosition().offset(random.nextInt(17) - 8, 3, random.nextInt(17) - 8);
                for (int dy = 0; dy < 6; dy++) {
                    level.setBlock(base.above(dy), ModBlocks.RIFT_PORTAL.defaultBlockState(), 3);
                }
                p.sendSystemMessage(Component.literal(
                    "§d§oA gigantic rift tears open nearby. §r§7§othe multiverse does not knock.§r"));
            }
        }

        if (this.tickCount >= nextGrumbleAt) {
            nextGrumbleAt = this.tickCount + 1100 + random.nextInt(900);
        }

        // slow upward drift, so it forgés along the skyline
        if (this.tickCount % 40 == 0) {
            this.setDeltaMovement(
                random.nextGaussian() * 0.03,
                0.05 + random.nextGaussian() * 0.02,
                random.nextGaussian() * 0.03);
        }
    }

    private static void broadcastNear(ServerLevel level, Player center, String text) {
        for (Player p : level.getEntitiesOfClass(Player.class, center.getBoundingBox().inflate(48.0))) {
            p.sendSystemMessage(Component.literal(text));
        }
    }
}
