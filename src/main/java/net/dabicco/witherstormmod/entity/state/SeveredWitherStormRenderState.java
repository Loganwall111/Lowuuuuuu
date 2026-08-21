package net.dabicco.witherstormmod.entity.state;

import net.dabicco.witherstormmod.client.PreviewScene;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.EntityType;

public class SeveredWitherStormRenderState extends EntityRenderState {
   public EntityType<?> entityType;
   public PreviewScene preview;
   public int stormId;
   public float phase;
   public boolean mirrored;
   public int side = 1;
   public float collapseTicks = -1.0F;
   public float droop;
   public float slopePitch;
   public float slopeRoll;
   public float bodyRot;
   public float yRot;
   public float xRot;
   public float bodyRoll;
   public float idleTimeTicks;
   public float ageInTicks;
   public int bodyLight;
   public final float[] groundBias = new float[3];
}
