package net.dabicco.devouringstorms.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({LevelRenderer.class})
public interface LevelRendererTargetsAccessor {
   @Accessor("targets")
   LevelTargetBundle dabyws$targets();
}
