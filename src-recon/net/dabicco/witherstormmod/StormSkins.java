package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.resources.Identifier;

public final class StormSkins {
   private static final Identifier LEGACY_CLASSIC = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/wither_storm.png");
   private static final Identifier LEGACY_OG = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/wither_storm_og.png");
   private static final Identifier PHASE4_CLASSIC = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/phase_4_assets.png");
   private static final Identifier PHASE4_OG = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/phase_4_assets_og.png");
   private static final Identifier DEVOURER_CLASSIC = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/devourer_assets.png");
   private static final Identifier DEVOURER_OG = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/devourer_assets_og.png");

   private StormSkins() {
   }

   public static boolean og() {
      return Math.round(DabyWSClientConfig.stormSkin) >= 1L;
   }

   public static Identifier legacy() {
      return og() ? LEGACY_OG : LEGACY_CLASSIC;
   }

   public static Identifier phase4() {
      return og() ? PHASE4_OG : PHASE4_CLASSIC;
   }

   public static Identifier devourer() {
      return og() ? DEVOURER_OG : DEVOURER_CLASSIC;
   }

   public static Identifier teethGlow(double phase) {
      boolean og = DabyWSClientConfig.stormSkin >= 0.5;
      return phase >= 5.0 && phase < 6.0
         ? Identifier.fromNamespaceAndPath("dabywitherstormmod", og ? "textures/entity/wither_storm_og_p5_e.png" : "textures/entity/wither_storm_p5_e.png")
         : Identifier.fromNamespaceAndPath("dabywitherstormmod", og ? "textures/entity/wither_storm_og_e.png" : "textures/entity/wither_storm_e.png");
   }
}
