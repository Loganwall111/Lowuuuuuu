package net.dabicco.devouringstorms.client;

import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.minecraft.resources.Identifier;

/**
 * StormSkins — swaps the storm's body textures between the plain look and the
 * OG MCSM skins (glossy near-black flesh with purple sheen; the phase 0-3 body
 * uses the obsidian-purple command block tiles instead of vanilla's orange).
 *
 * The original textures are kept untouched; the OG variants are separate
 * *_og.png files and the active set is a client config choice. Batch 17 now
 * defaults new installs to the OG set so the mod boots into the more MCSM-like
 * presentation, while the classic look stays one click away.
 */
public final class StormSkins {
   private static final Identifier LEGACY_CLASSIC = Identifier.fromNamespaceAndPath("devouringstorms", "textures/entity/wither_storm.png");
   private static final Identifier LEGACY_OG = Identifier.fromNamespaceAndPath("devouringstorms", "textures/entity/wither_storm_og.png");
   private static final Identifier PHASE4_CLASSIC = Identifier.fromNamespaceAndPath("devouringstorms", "textures/entity/phase_4_assets.png");
   private static final Identifier PHASE4_OG = Identifier.fromNamespaceAndPath("devouringstorms", "textures/entity/phase_4_assets_og.png");
   private static final Identifier DEVOURER_CLASSIC = Identifier.fromNamespaceAndPath("devouringstorms", "textures/entity/devourer_assets.png");
   private static final Identifier DEVOURER_OG = Identifier.fromNamespaceAndPath("devouringstorms", "textures/entity/devourer_assets_og.png");

   private StormSkins() {
   }

   /** true when the player picked the OG obsidian-gloss skins. */
   public static boolean og() {
      return Math.round(DevouringStormsClientConfig.stormSkin) >= 1;
   }

   /** phase 0-3 body skin (the one carrying the command block belly). */
   public static Identifier legacy() {
      return og() ? LEGACY_OG : LEGACY_CLASSIC;
   }

   /** phase 4+ body / head / debris skin. */
   public static Identifier phase4() {
      return og() ? PHASE4_OG : PHASE4_CLASSIC;
   }

   /** devourer & severed storm skin. */
   public static Identifier devourer() {
      return og() ? DEVOURER_OG : DEVOURER_CLASSIC;
   }
}
