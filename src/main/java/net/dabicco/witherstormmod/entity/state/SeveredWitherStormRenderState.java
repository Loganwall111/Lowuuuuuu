package net.dabicco.witherstormmod.entity.state;

import net.dabicco.witherstormmod.client.PreviewScene;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class SeveredWitherStormRenderState extends LivingEntityRenderState {
   public float bodyRoll;
   public float phase;
   public boolean mirrored;
   public float idleTimeTicks;
   public int stormId;
   public int bodyLight;
   public float collapseTicks = -1.0F;
   public float droop;
   public int side = 1;
   public float slopePitch;
   public float slopeRoll;
   public final float[] groundBias = new float[8];
   public PreviewScene preview;
}
