package net.mcsm.sift.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Fabric of Reality Stone - pitch black with stars, animated End Gateway style, cosmic purple
 * Almost pitch black but has stars, very animated style similar to End Gateway, cosmic purple
 * Infinite gigantic ground that appears as first layer before Sift - acts as glue between regular universe and outside fabric
 * Reality cracks glowing on it - animated reality cracks
 * 
 * When stepped on: world distorts, gravitational scratches, trampoline effect - world herds inwards creating distortion
 * Jumping stretches fabric and warps like walking on gigantic trampoline, footless feel
 */
public class FabricOfRealityBlock extends Block {

    public FabricOfRealityBlock(Properties props) {
        super(props);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // custom model with animated starfield
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Reality cracks glowing - little reality cracks glowing
        if (random.nextFloat() < 0.15f) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 1.01;
            double z = pos.getZ() + random.nextDouble();
            
            // Cosmic purple particles + star sparkles
            level.addParticle(ParticleTypes.PORTAL, x, y, z, 
                (random.nextDouble() - 0.5) * 0.2, 
                random.nextDouble() * 0.3, 
                (random.nextDouble() - 0.5) * 0.2);
            
            if (random.nextFloat() < 0.3f) {
                level.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0.05, 0);
            }
        }
        
        // End Gateway style animated stars inside block
        if (random.nextFloat() < 0.05f) {
            level.addParticle(ParticleTypes.REVERSE_PORTAL,
                pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8,
                pos.getY() + 0.5,
                pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8,
                0, 0, 0);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        
        if (entity instanceof LivingEntity living) {
            // Trampoline effect - world distorts, gravitational scratches
            // Bouncy but not explosive, distortion effect
            Vec3 motion = living.getDeltaMovement();
            
            // If falling onto fabric, bounce with distortion
            if (motion.y < -0.1) {
                double bounceFactor = 0.6 + level.random.nextDouble() * 0.2;
                Vec3 bounced = new Vec3(motion.x * 0.9, -motion.y * bounceFactor, motion.z * 0.9);
                living.setDeltaMovement(bounced);
                living.hasImpulse = true;
                living.fallDistance = 0;
                
                // Trigger client distortion renderer
                if (level.isClientSide) {
                    net.mcsm.sift.client.FabricDistortionRenderer.triggerDistortion(pos, 1.0f);
                } else {
                    // Server particles - reality stretch effect
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.PORTAL,
                            pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                            20, 0.5, 0.1, 0.5, 0.3);
                    }
                }
                
                level.playSound(null, pos, 
                    net.minecraft.sounds.SoundEvents.SLIME_BLOCK_FALL, 
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.6f, 0.3f + level.random.nextFloat() * 0.4f);
            }
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        // No fall damage - trampoline absorbs
        entity.causeFallDamage(fallDistance, 0f, level.damageSources().fall());
        // Instead, distort world inwards - amazing exploring shameless effect
        if (entity instanceof LivingEntity) {
            if (level.isClientSide) {
                net.mcsm.sift.client.FabricDistortionRenderer.triggerDistortion(pos, 
                    Mth.clamp(fallDistance / 20f, 0.5f, 2.0f));
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // Slight slowdown when standing - like walking on gigantic plastic/trampoline
        if (entity instanceof LivingEntity && entity.onGround()) {
            Vec3 motion = entity.getDeltaMovement();
            // Stretch effect - footless look, radical
            entity.setDeltaMovement(motion.multiply(0.85, 1.0, 0.85));
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true; // for animated cracks
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Occasionally spawn reality crack - glowing crack that spreads
        if (random.nextFloat() < 0.01f) {
            // Would place BrokenFabric block nearby as crack
            BlockPos crackPos = pos.offset(
                random.nextInt(3) - 1,
                0,
                random.nextInt(3) - 1
            );
            if (level.getBlockState(crackPos).is(this)) {
                // Crack glow pulse
                level.sendParticles(ParticleTypes.GLOW,
                    crackPos.getX() + 0.5, crackPos.getY() + 1, crackPos.getZ() + 0.5,
                    5, 0.2, 0.1, 0.2, 0.05);
            }
        }
    }

    // Light emission - cosmic purple glow, not yellow like regular sponge
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        // Animated light - pulsing like End Gateway
        return 8; // base, shader will make it pulse
    }
}
