package net.mcsm.sift.block;

import net.mcsm.sift.McsmVoidTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Broken Fabric of Reality - how you enter the well
 * Like invisible block, doesn't have stand so you just go through it and layers of that block there are surrounding it
 * At very bottom sponge acts like glue between regular universe and outside fabric of reality
 * 
 * Two types:
 * - Fabric of Reality Stone: solid fibre from reality itself, pitch black with stars, cosmic purple
 * - Broken Fabric: invisible, no collision, portal - how you enter
 * 
 * In survival only way through is Reality Knife, in creative you can just breakthrough causing whole thing to break open
 */
public class BrokenFabricOfRealityBlock extends Block {

    public BrokenFabricOfRealityBlock(Properties props) {
        super(props);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE; // invisible block
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        // No collision - you just go through it
        if (ctx instanceof EntityCollisionContext ecc) {
            Entity entity = ecc.getEntity();
            if (entity instanceof Player player) {
                // In creative, still no collision but allow breaking
                if (player.isCreative()) {
                    return Shapes.empty();
                }
            }
        }
        return Shapes.empty(); // invisible, no stand
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true; // light passes through
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof Player player) {
            // Check if player has Reality Knife or is in creative
            boolean canPass = player.isCreative() || hasRealityKnife(player);
            
            if (!canPass) {
                // Push back slightly, need knife to cut through
                Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.multiply(0.3, 0.5, 0.3));
                if (level.isClientSide && level.random.nextFloat() < 0.1f) {
                    player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§5The fabric resists... You need a Reality Knife to cut through").withStyle(s -> s.withColor(0xFFAA55FF)),
                        true
                    );
                }
                return;
            }

            // Allow passage - dump directly into liquid of layer one where floats
            if (!level.isClientSide && player.getY() < pos.getY() + 0.5) {
                // Entering the well - transition to Sift
                if (pos.getY() > -300) { // top fabric - entering Sift
                    // Trigger emptiness fall - pitch black void of stars, 40-50 sec fall
                    player.teleportTo(player.getX(), -350, player.getZ());
                    player.setDeltaMovement(new Vec3(0, -1.2, 0));
                    player.fallDistance = 0;
                    level.playSound(null, pos, 
                        net.minecraft.sounds.SoundEvents.PORTAL_TRAVEL, 
                        net.minecraft.sounds.SoundSource.BLOCKS, 1f, 0.3f);
                    
                    // Fireworks for 40-50 sec fall visualization
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.FIREWORK,
                            player.getX(), player.getY() - 5, player.getZ(),
                            30, 1, 1, 1, 0.2);
                    }
                    
                    // Break open whole thing in creative - causing whole thing to break open
                    if (player.isCreative()) {
                        breakFabricAround(level, pos, 8);
                    }
                } else { // bottom fabric - leading to Unknown dimension
                    // Rainbow one at very bottom - color of skybox of brand new sift void dementia
                    // When breakthrough or fall through you end up in Unknown
                    player.teleportTo(player.getX(), McsmVoidTiers.TOP_BOUNDARY - 100, player.getZ());
                    player.setDeltaMovement(new Vec3(0, -0.5, 0));
                    level.playSound(null, pos,
                        net.minecraft.sounds.SoundEvents.END_PORTAL_SPAWN,
                        net.minecraft.sounds.SoundSource.BLOCKS, 1f, 0.8f);
                }
            }
        }
    }

    private boolean hasRealityKnife(Player player) {
        // Check if player has reality knife in inventory or hand
        return player.getMainHandItem().getDescriptionId().contains("reality_knife") ||
               player.getOffhandItem().getDescriptionId().contains("reality_knife") ||
               player.getInventory().contains(
                   stack -> stack.getDescriptionId().contains("reality_knife")
               );
    }

    private void breakFabricAround(Level level, BlockPos center, int radius) {
        // In creative, breakthrough causes whole thing to break open
        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos p = center.offset(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (s.getBlock() instanceof FabricOfRealityBlock || s.getBlock() instanceof BrokenFabricOfRealityBlock) {
                        if (level.random.nextFloat() < 0.7f) {
                            level.destroyBlock(p, false);
                            if (level instanceof ServerLevel sl) {
                                sl.sendParticles(ParticleTypes.PORTAL,
                                    p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                                    10, 0.3, 0.3, 0.3, 0.2);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Invisible but with subtle reality distortion particles
        if (random.nextFloat() < 0.08f) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
            double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
            
            level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 10; // subtle glow even though invisible - reality cracks glowing
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }
}
