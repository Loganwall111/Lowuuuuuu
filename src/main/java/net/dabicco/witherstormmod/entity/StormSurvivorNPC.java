package net.dabicco.witherstormmod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Npc;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * StormSurvivorNPC — Devouring Storms NPC entity.
 * Survivors who provide lore, quests, and dialogue.
 */
public class StormSurvivorNPC extends PathfinderMob implements Npc {

    private String dialogueId = "default";
    private int dialogueProgress = 0;
    private boolean hasGivenQuest = false;

    public StormSurvivorNPC(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        String[] dialogues = {
            "\u00a76[Survivor]\u00a7f You... you're still alive? I thought the storm took everyone.",
            "\u00a76[Survivor]\u00a7f The world isn't what it used to be. The Devouring Storm changed everything.",
            "\u00a76[Survivor]\u00a7f Be careful out there. The corruption spreads further every day.",
            "\u00a76[Survivor]\u00a7f I've seen things... things that shouldn't exist. Reality itself is breaking.",
            "\u00a76[Survivor]\u00a7f If you find any \u00a7dReality Shards\u00a7f, bring them to me. I can use them."
        };

        String dialogue = dialogues[dialogueProgress % dialogues.length];
        player.sendSystemComponent(Component.literal(dialogue));
        dialogueProgress++;

        return InteractionResult.CONSUME;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("DialogueId", dialogueId);
        tag.putInt("DialogueProgress", dialogueProgress);
        tag.putBoolean("HasGivenQuest", hasGivenQuest);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("DialogueId")) {
            this.dialogueId = tag.getString("DialogueId");
        }
        if (tag.contains("DialogueProgress")) {
            this.dialogueProgress = tag.getInt("DialogueProgress");
        }
        if (tag.contains("HasGivenQuest")) {
            this.hasGivenQuest = tag.getBoolean("HasGivenQuest");
        }
    }
}
