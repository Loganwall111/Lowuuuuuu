package com.rewritten.devouringstorms.block;

import com.rewritten.devouringstorms.registry.ModItems;
import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.util.ModTexts;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * THE SEALED VAULT. The ARG vault from across the updates.
 * Seven schedules were scattered to the four winds of the quarantine — town caches, dead
 * machines, a companion's trust, the storm's own corpse. Bring all seven to a sealed vault
 * and it accepts the code the schedules assemble: M.A.S.S.G.O.O.S.
 * What the vault pays out stays classified until 2027.
 */
public class SealedVaultBlock extends Block {

    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    public SealedVaultBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    /** Any-hand use: schedules may be in hand or pocket when it opens. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        return handle(state, level, pos, player);
    }

    /** Empty-hand use: same inquiry. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        return handle(state, level, pos, player);
    }

    private InteractionResult handle(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (state.getValue(OPEN)) {
            player.sendSystemMessage(Component.literal(
                "§8[§dVAULT§8] §7Archive open. The payload remains classified until §02027§7."));
            return InteractionResult.CONSUME;
        }

        int owned = 0;
        for (Item schedule : ModItems.SCHEDULES) {
            if (player.getInventory().hasAnyMatching(stack -> stack.is(schedule))) owned++;
        }

        if (owned < ModItems.SCHEDULES.size()) {
            level.playSound(null, pos, ModSounds.TERMINAL_BOOT, SoundSource.BLOCKS, 0.6f, 0.4f);
            player.sendSystemMessage(Component.literal(
                "§8[§dVAULT§8] §7SEALED. §8schedules located: §5" + owned + "§8/§57"));
            player.sendSystemMessage(Component.literal(
                "§8[§dVAULT§8] §8there are seven. the town, the ruin, the shrine, the apparition, the companion, the storm, the severed."));
            return InteractionResult.CONSUME;
        }

        // ---- the seven assemble: M.A.S.S.G.O.O.S ----
        if (!player.isCreative()) {
            for (Item schedule : ModItems.SCHEDULES) {
                player.getInventory().clearOrCountMatchingItems(stack -> stack.is(schedule), 1, player.inventoryMenu.getCraftSlots());
            }
        }
        level.setBlock(pos, state.setValue(OPEN, true), 3);
        level.playSound(null, pos, ModSounds.TERMINAL_BOOT, SoundSource.BLOCKS, 2.0f, 0.8f);
        level.playSound(null, pos, ModSounds.RIFT_OPEN, SoundSource.BLOCKS, 1.6f, 0.7f);

        for (String line : ModTexts.VAULT_PAYLOAD) {
            player.sendSystemMessage(Component.literal(line));
        }

        // the payout — tangible before it goes classified
        grant(player, ModItems.CLASSIFIED_PAYLOAD);
        grant(player, ModItems.COMMANDED_STAR);
        grant(player, ModItems.MUSIC_DISC_CHANGED);
        grant(player, net.minecraft.world.item.Items.ECHO_SHARD, 6);
        grant(player, net.minecraft.world.item.Items.DIAMOND, 5);
        return InteractionResult.CONSUME;
    }

    private static void grant(Player player, Item item) {
        grant(player, item, 1);
    }

    private static void grant(Player player, Item item, int count) {
        ItemStack stack = new ItemStack(item, count);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
