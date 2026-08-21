package net.dabicco.witherstormmod.entity.state;

import net.dabicco.witherstormmod.client.PreviewScene;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.EntityType;

public class WitherStormRenderState extends EntityRenderState {
   public EntityType<?> entityType;
   public PreviewScene preview;
   public int stormId;
   public double phase;
   public boolean phase4;
   public boolean devourer;
   public float hatch = 1.0F;
   public float changeover;
   public float collapseTicks = -1.0F;
   public boolean snatchActive;
   public boolean playingSpawnAnimation;
   public float spawnElapsedTicks = Float.MAX_VALUE;
   public float phase5ElapsedTicks = -1.0F;
   public float phase58ElapsedTicks = -1.0F;
   public float frontTentacleElapsedTicks = -1.0F;
   public float miniHeadElapsedTicks = -1.0F;
   public float ageInTicks;
   public float idleTimeTicks;
   public float bodyRot;
   public float yRot;
   public float xRot;
   public float bodyRoll;
   public float slopePitch;
   public float slopeRoll;
   public float nightFactor;
   public double worldX;
   public double worldY;
   public double worldZ;
   public double velX;
   public double velY;
   public double velZ;
   public double snatchRelX;
   public double snatchRelY;
   public double snatchRelZ;
   public final float[] headYRot = new float[3];
   public final float[] headXRot = new float[3];
   public final float[] groundBias = new float[3];
}
