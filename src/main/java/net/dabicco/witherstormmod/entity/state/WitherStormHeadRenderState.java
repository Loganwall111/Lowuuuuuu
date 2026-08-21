package net.dabicco.witherstormmod.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class WitherStormHeadRenderState extends EntityRenderState {
   public float jawAngle;
   public boolean damaged;
   public float idleTimeTicks;
   public float spawnElapsedTicks = Float.MAX_VALUE;
   public float fireElapsedTicks = -1.0F;
   public float hurtElapsedTicks = -1.0F;
   public float roarElapsedTicks = -1.0F;
   public float headScale = 1.0F;
   public boolean devourer;
   public boolean beamActive;
   public float beamDX;
   public float beamDY;
   public float beamDZ;
}
