package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.mcsm.extras.McsmDynamicLights;

/**
 * Devouring Storms: coloured dynamic lights, phase 2b - the glow lights
 * the GROUND. A dropped luminous item places the engine's own invisible
 * light block (Blocks.LIGHT at the item's glow level) through the real
 * light engine, so terrain, mobs and particles around the drop are lit
 * exactly like a placed torch would light them.
 *
 * Self-cleaning by construction: the light follows the item as it slides
 * (old spot restored first), and Entity#onRemoval restores the spot when
 * the item is picked up, merges or despawns. Only air is ever replaced -
 * the mixin never destroys a real block, and never stacks a second light
 * on top of another glowing drop's light.
 *
 * Every name here was verified against the 26.2 jar by the CI probe:
 * Blocks.LIGHT, LightBlock.LEVEL, Level.setBlock(BlockPos, BlockState, int),
 * Entity.onRemoval(RemovalReason), ItemEntity.tick, isClientSide().
 */
@Mixin(ItemEntity.class)
public abstract class McsmItemGroundLightMixin {

    @Unique
    private BlockPos dabyws$litPos;
    @Unique
    private BlockState dabyws$litOriginal;

    @Inject(method = "tick", at = @At("TAIL"))
    private void dabyws$groundLight(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }
        if (self.tickCount % 10 != 0) {
            return;
        }
        ItemStack stack = self.getItem();
        int light = McsmDynamicLights.lightLevel(stack);
        if (light <= 0) {
            dabyws$restoreLight();
            return;
        }
        BlockPos pos = self.blockPosition();
        if (pos.equals(dabyws$litPos)) {
            return; // already carrying our light here
        }
        dabyws$restoreLight();
        Level lvl = self.level();
        BlockState cur = lvl.getBlockState(pos);
        if (!cur.isAir()) {
            return; // never displace a real block (or another drop's light)
        }
        dabyws$litOriginal = cur;
        dabyws$litPos = pos;
        lvl.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, light), 3);
    }

    @Inject(method = "onRemoval", at = @At("HEAD"))
    private void dabyws$cleanupLight(Entity.RemovalReason reason, CallbackInfo ci) {
        dabyws$restoreLight();
    }

    @Unique
    private void dabyws$restoreLight() {
        if (dabyws$litPos == null) {
            return;
        }
        ItemEntity self = (ItemEntity) (Object) this;
        Level lvl = self.level();
        BlockState cur = lvl.getBlockState(dabyws$litPos);
        if (cur.getBlock() == Blocks.LIGHT) {
            BlockState back = dabyws$litOriginal != null
                    ? dabyws$litOriginal
                    : Blocks.AIR.defaultBlockState();
            lvl.setBlock(dabyws$litPos, back, 3);
        }
        dabyws$litPos = null;
        dabyws$litOriginal = null;
    }
}
