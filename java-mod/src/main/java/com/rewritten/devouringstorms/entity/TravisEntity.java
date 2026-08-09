package com.rewritten.devouringstorms.entity;

import com.rewritten.devouringstorms.registry.ModItems;
import com.rewritten.devouringstorms.util.ModTexts;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathAwareEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * TRAVIS. "Anna, Travis, and Tonya are trapped in Decayed Reality."
 * The one who stayed behind at the Fray waystation to mind the tear. Talks about the bell,
 * about EAOIN, and about Tonya, who followed Anna into somewhere neither of them can clearly
 * remember anymore. Show him a Memory Fragment — proof the others were real — and he'll
 * trade away the spare field tape he keeps for company.
 */
public class TravisEntity extends PathAwareEntity {

    private int nextChatAt = 260;
    private boolean tapeGifted = false;

    public TravisEntity(EntityType<? extends PathAwareEntity> type, Level level) {
        super(type, level);
        this.setCustomName(Component.literal("§9Travis"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createTravisAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 30.0)
            .add(Attributes.MOVEMENT_SPEED, 0.26)
            .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.tickCount < nextChatAt) return;
        nextChatAt = this.tickCount + 340 + this.getRandom().nextInt(400);
        var near = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(10.0));
        if (near.isEmpty()) return;
        String line = ModTexts.TRAVIS_LINES.get(this.getRandom().nextInt(ModTexts.TRAVIS_LINES.size()));
        for (Player p : near) p.sendSystemMessage(Component.literal(line));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(this.level() instanceof ServerLevel level)) return InteractionResult.PASS;
        if (stack.is(ModItems.MEMORY_FRAGMENT) && !tapeGifted) {
            tapeGifted = true;
            this.spawnAtLocation(ModItems.AUDIO_LOG_2);
            player.sendSystemMessage(Component.literal(
                "§9Travis§r ⟸ §o...she was real. Here — the plague log. I kept a spare for company. Listen with the volume down.§r"));
            level.playSound(null, this, net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
                net.minecraft.sounds.SoundSource.NEUTRAL, 0.8f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
