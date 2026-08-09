package com.rewritten.devouringstorms.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

/**
 * WITHERED SYMBIONT. A living thing the storm half-ate and half-digested and gave back.
 * It follows the storm's hunger. Vanilla zombie instincts; storm-fed body.
 * (Sun sensitivity intentionally untouched — the Decayed Reality is eternal night,
 * and that is where these belong.)
 */
public class WitheredSymbiontEntity extends Zombie {

    public WitheredSymbiontEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createSymbiontAttributes() {
        return Zombie.createAttributes()
            .add(Attributes.MAX_HEALTH, 42.0)
            .add(Attributes.ATTACK_DAMAGE, 7.0)
            .add(Attributes.MOVEMENT_SPEED, 0.27)
            .add(Attributes.ARMOR, 4.0)
            .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0)
            .add(Attributes.FOLLOW_RANGE, 48.0);
    }
}
