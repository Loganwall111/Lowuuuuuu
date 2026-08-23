package net.dabicco.devouringstorms.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({RenderPipelines.class})
public interface RenderPipelinesAccessor {
   @Accessor("ENTITY_SNIPPET")
   static Snippet dabyws$entitySnippet() {
      throw new AssertionError();
   }

   @Accessor("POST_PROCESSING_SNIPPET")
   static Snippet dabyws$postProcessingSnippet() {
      throw new AssertionError();
   }

   @Accessor("GLOBALS_SNIPPET")
   static Snippet dabyws$globalsSnippet() {
      throw new AssertionError();
   }

   @Accessor("ENTITY_EMISSIVE_SNIPPET")
   static Snippet dabyws$entityEmissiveSnippet() {
      throw new AssertionError();
   }
}
