package net.dabicco.devouringstorms.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class WitherStormHeadRenderState extends EntityRenderState {
   public float upsideDown;
   public double modelOffX;
   public double modelOffY = -1.25;
   public double modelOffZ;
   public float yRot;
   public float xRot;
   public float zRot;
   public float jawAngle = 0.0F;
   public float jawLagYaw = 0.0F;
   public float jawLagPitch = 0.0F;
   public float jawLagRoll = 0.0F;
   public boolean devourer;
   public boolean earlyPhase;
   public float headScale = 6.0F;
   public float lit = 1.0F;
   public float spawnElapsedTicks = Float.MAX_VALUE;
   public float fireElapsedTicks = -1.0F;
   public float hurtElapsedTicks = -1.0F;
   public float roarElapsedTicks = -1.0F;
   public float idleTimeTicks;
   public boolean damaged;
   public boolean eyeDark;
   public boolean hasAttach = false;
   public double attachDX;
   public double attachDY;
   public double attachDZ;
   public int headId;
   public float beamScale = 1.0F;
   public boolean beamActive = false;
   public double beamDX;
   public double beamDY;
   public double beamDZ;
}
