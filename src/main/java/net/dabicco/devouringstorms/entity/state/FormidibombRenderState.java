package net.dabicco.devouringstorms.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class FormidibombRenderState extends EntityRenderState {
   public final ItemStackRenderState item = new ItemStackRenderState();
   public boolean morphed;
   public float spin;
   public float whiteout;
   public float crackGlow;
   public float shake;
   public int ticks;
   public long seed;
}
