package net.dabicco.devouringstorms.entity.state;

import net.dabicco.devouringstorms.client.PreviewScene;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.phys.Vec3;

public class WitherStormRenderState extends LivingEntityRenderState {
   public boolean phase4;
   public boolean devourer;
   public float collapseTicks = -1.0F;
   public float slopePitch;
   public float slopeRoll;
   public final float[] groundBias = new float[8];
   public boolean playingSpawnAnimation;
   public double phase;
   public Vec3[] portalCorners;
   public boolean portalOpen;
   public float spawnElapsedTicks;
   public float changeover;
   public float hatch = 1.0F;
   public float miniHeadElapsedTicks = -1.0F;
   public float frontTentacleElapsedTicks = -1.0F;
   public float idleTimeTicks;
   public float phase5ElapsedTicks = -1.0F;
   public float phase58ElapsedTicks = -1.0F;
   public float collapseWhiteout;
   public float collapseFade = 1.0F;
   public float[] headXRot = new float[2];
   public float[] headYRot = new float[2];
   public float bodyRoll = 0.0F;
   public float nightFactor = 0.0F;
   public double worldX;
   public double worldY;
   public double worldZ;
   public int stormId;
   public double velX;
   public double velY;
   public double velZ;
   public boolean underSiege;
   public float siegeProgress;
   public boolean snatchActive;
   public double snatchRelX;
   public double snatchRelY;
   public double snatchRelZ;
   public PreviewScene preview;
}
