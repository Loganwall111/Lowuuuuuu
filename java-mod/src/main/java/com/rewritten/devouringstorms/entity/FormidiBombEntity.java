package com.rewritten.devouringstorms.entity;

import com.rewritten.devouringstorms.registry.ModEntities;
import com.rewritten.devouringstorms.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * THE FORMIDIBOMB. Super-TNT from the old world's last desperate plan.
 * The only weapon that can permanently end a playing-dead MASSG.
 */
public class FormidiBombEntity extends ThrowableItemProjectile {

    public FormidiBombEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public FormidiBombEntity(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.FORMIDI_BOMB, owner, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.FORMIDI_BOMB;
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!(this.level() instanceof ServerLevel level)) return;
        var target = hit.getEntity();
        var explosionSource = this.damageSources().explosion(this, this.getOwner());

        if (target instanceof MassgEntity massg) {
            // This is the kill condition: hard damage through the corrupted hide.
            massg.hurtServer(level, explosionSource, 150.0f);
        } else if (target instanceof LivingEntity living) {
            living.hurtServer(level, explosionSource, 30.0f);
        }
        detonate(level, 2.5f);
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!(this.level() instanceof ServerLevel level)) return;
        var explosionSource = this.damageSources().explosion(this, this.getOwner());
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0))) {
            entity.hurtServer(level, explosionSource, entity instanceof MassgEntity ? 150.0f : 25.0f);
        }
        detonate(level, 2.0f);
    }

    /** Visual detonation only (block-safe) — the Formidibomb's work is done through its payload. */
    private void detonate(ServerLevel level, float radius) {
        level.explode(this, null, null, this.getX(), this.getY(), this.getZ(), radius, false,
            Level.ExplosionInteraction.NONE);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 4, 0.5, 0.5, 0.5, 0.01);
        this.discard();
    }
}
