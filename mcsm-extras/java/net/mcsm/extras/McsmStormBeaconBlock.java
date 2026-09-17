package net.mcsm.extras;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCSM - the storm beacon block. A command-block-antenna the player can
 * place anywhere: right-click it and it fires the same relay burst a lit
 * beacontown beacon produces (shockwave + the mod's own summon path).
 * Registered into the mod's own registries during their ModBlocks.
 * initialize(), so no separate mod and no creative-tab dependency; give it
 * with /give @s dabywitherstormmod:storm_beacon.
 */
public class McsmStormBeaconBlock extends Block {

    public static Block BLOCK;
    private static final Map<BlockPos, Long> COOLDOWN = new ConcurrentHashMap<>();

    public McsmStormBeaconBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static void mcsm$register() {
        McsmExtrasConfig.load();
        if (!McsmExtrasConfig.enableBeaconBlock || BLOCK != null) return;
        BLOCK = net.dabicco.witherstormmod.ModBlocks.register("storm_beacon", McsmStormBeaconBlock::new,
                BlockBehaviour.Properties.of().strength(4.0f).lightLevel(s -> 12));
        net.dabicco.witherstormmod.ModItems.register("storm_beacon",
                (java.util.function.Function<Item.Properties, Item>) p -> new BlockItem(BLOCK, p),
                new Item.Properties());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                           Player player, InteractionHand hand, BlockHitResult hit) {
        McsmExtrasConfig.load();
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        long gt = level.getGameTime();
        long cool = (long) (McsmExtrasConfig.beaconCooldownSeconds * 20.0);
        Long last = COOLDOWN.get(pos);
        if (last != null && gt - last < cool) return InteractionResult.CONSUME;
        COOLDOWN.put(pos, gt);
        McsmStormFx.fire(level, pos);
        return InteractionResult.CONSUME;
    }
}
