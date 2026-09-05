package net.dabicco.witherstormmod.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class SuperTntRenderState extends EntityRenderState {
   public final ItemStackRenderState item = new ItemStackRenderState();
   public float flash;
   public int fuse;
}
