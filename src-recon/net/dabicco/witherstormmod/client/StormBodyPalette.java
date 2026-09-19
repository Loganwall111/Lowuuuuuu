package net.dabicco.witherstormmod.client;

import net.minecraft.resources.Identifier;

/**
 * MCSM 1.9.201 -- master colour boundary key for the storm body blocks.
 *
 * The uploaded split texture sheet is registered in the mod namespace as
 * {@code dabywitherstormmod:textures/misc/body_palette_key.png}: a two-block
 * atlas whose LEFT half is the dark navy-black boundary (#0A0E14) and whose
 * RIGHT half is the absolute void-black boundary (#000000). The custom entity
 * pipelines (FoglessRenderTypes, define MCSM_VOID_BODY) lock their fragment
 * shading to exactly these two constants: inner creases, ambient-occlusion
 * shadows and structural block layers output solid cinematic void-black,
 * while facet mid-tones may rise no higher than the navy key. Legacy
 * light-gray / brown vanilla-style sheets are explicitly not sampled.
 */
public final class StormBodyPalette {
   /** Registered split-key asset (navy-black | void-black). */
   public static final Identifier KEY = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/body_palette_key.png");
   /** Left key block, #0A0E14 -- the brightest tone a body facet may reach. */
   public static final int NAVY_BLACK = 0xFF0A0E14;
   /** Right key block, #000000 -- creases, AO shadows, structural layers. */
   public static final int VOID_BLACK = 0xFF000000;
   /** Normalised components of {@link #NAVY_BLACK}, matching fogless_entity.fsh. */
   public static final float NAVY_R = 0.0392F;
   public static final float NAVY_G = 0.0549F;
   public static final float NAVY_B = 0.0784F;

   private StormBodyPalette() {
   }

   /**
    * True when a packed ARGB body tone is inside the keyed boundary band and
    * therefore legal for a storm facet; anything brighter is clamped down by
    * the shader and anything between the keys is a crease (void-black).
    */
   public static boolean inBoundary(int argb) {
      int r = argb >> 16 & 0xFF;
      int g = argb >> 8 & 0xFF;
      int b = argb & 0xFF;
      return r <= 0x0A + 6 && g <= 0x0E + 6 && b <= 0x14 + 8;
   }
}
