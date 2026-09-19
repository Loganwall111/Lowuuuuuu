package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dabicco.witherstormmod.client.StormBackdrop;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.mcsm.extras.client.McsmCreatorArms;
import net.mcsm.extras.client.McsmEarlyStormBackdrop;
import net.mcsm.extras.client.McsmExperimentalStoryStage;
import net.mcsm.extras.client.McsmSkyFloorBand;
import net.mcsm.extras.client.McsmStormBlob;

/**
 * Devouring Storms: mega-phase 2 - hands the storm blob over to the
 * corrected re-submission (pinkish-violet 5.5-5.9, centred dark core,
 * smoothed skybox glide, weaker red ember). Client-only mixin on the base
 * mod's own StormBackdrop; the replacement draws nothing unless the game
 * client is up, and honours every base config toggle.
 */
@Mixin(StormBackdrop.class)
public abstract class McsmStormBlobMixin {

    @Inject(method = "submit", at = @At("HEAD"), remap = false, require = 0)
    private static void dabyws$correctedBlob(LevelRenderContext ctx, CallbackInfo ci) {
        if (Minecraft.getInstance() != null) {
            // Keep the optional stage hook and compatibility callback, but do
            // not cancel the base method. Restoring StormBackdrop.submit brings
            // back the smooth original phase backdrop instead of the retired
            // generated black rings/oval cards.
            // The native backdrop owns phases 3.9 through 9; this restrained
            // companion only fills the earlier phase-1 to phase-3 buildup.
            McsmEarlyStormBackdrop.submit(ctx);
            McsmExperimentalStoryStage.submit(ctx);
            McsmStormBlob.submit(ctx);
            // BUILD #457 -- the painted sky: a camera-anchored cube, one painting
            // per dimension, drawn before the floor band so the band owns the
            // under-world and the cube owns everything above it.
            net.mcsm.extras.client.McsmPaintedSky.submit(ctx);
            // BUILD #470 -- and the weather in that air: three drifting cloud decks,
            // each painted with its dimension's own cloud sheet, drawn after the cube
            // (so the deck reads over the walls) and before the floor band, which owns
            // everything below the world. A sky with no clouds in it was the standing
            // "the sky is not fully the sky yet".
            net.mcsm.extras.client.McsmCloudDeck.submit(ctx);
            // V2 EPIC - Volumetric 3D cloud meshes / fog volumes - real 3D, not 2D bands
            net.mcsm.extras.client.McsmVolumetricCloudMesh.submit(ctx);
            // V2 - Wither storm particles emitting off the ground
            net.mcsm.extras.client.McsmGroundParticles.submit(ctx);
            // V2 - Black hole 3D backdrop interactive for End and other dims
            net.mcsm.extras.client.McsmBlackHoleBackdrop.submitWorld(ctx);
            // V2 NEXT-GEN Phase 1 - Amazing sun visual: 3D spinning sun that moves across world
            net.mcsm.extras.client.McsmSpinningSun.submit(ctx);
            // V2 NEXT-GEN - Earth-like planet with animated clouds spinning across world
            net.mcsm.extras.client.McsmEarthPlanet.submit(ctx);
            // V2 NEXT-GEN - Aurora borealis
            net.mcsm.extras.client.McsmAuroraBorealis.submit(ctx);
            // V2 NEXT-GEN - Orbital planets: Saturn, Mars, Jupiter, Venus, Moon + asteroid belt
            net.mcsm.extras.client.McsmOrbitalPlanets.submit(ctx);
            // V2 NEXT-GEN - Shooting stars, twinkling stars, full bright distant stars
            net.mcsm.extras.client.McsmShootingStars.submit(ctx);
            // V2 NEXT-GEN Phase 2 - Wavy water with physics, push waves interaction
            net.mcsm.extras.client.McsmWavyWater.submit(ctx);
            // V2 NEXT-GEN Phase 2 - VFX Light Rays (god rays) to main overworld
            net.mcsm.extras.client.McsmLightRays.submit(ctx);
            // V2 NEXT-GEN Phase 2 - Ambient World: fireflies, bioluminescent water, reflections, wolf howling, crickets
            net.mcsm.extras.client.McsmAmbientWorld.submit(ctx);
            // V2 NEXT-GEN Phase 4 - Time Warp Effect: realistic warp, chromatic blur, universes passing by, time router
            net.mcsm.extras.client.McsmTimeWarpEffect.submit(ctx);
            // V2 NEXT-GEN Phase 5 - Laserbeam shooting stars: blue/green/black shifting laser
            net.mcsm.extras.client.McsmLaserBeamRenderer.submit(ctx);
            // BUILD #474 -- and the ceiling those clouds sit under: the STORM'S own sky,
            // all 360 degrees of it, from the horizon to the zenith. The cube and the deck
            // are the dimensions' skies; the Overworld -- where the story, the cities and
            // the storm are -- had the shader's flat gradient and one lobe of storm in one
            // direction, which is the standing "the sky is not fully the sky yet". Its
            // colours come from McsmStormPhase.columnFor, i.e. the same reference tables
            // sky.fsh samples, so it deepens the phase's own palette and never fights it.
            net.mcsm.extras.client.McsmStormCanopy.submit(ctx);
            // BUILD #434 -- the sky's bottom layer. Drawn in the world, not in
            // the shader, so it exists for players who never install the pack.
            McsmSkyFloorBand.submit(ctx);
            // Build #416 (D.8, phase 3) -- the Creator's arms, through the rips.
            // Drawn last so the limbs read over the backdrop they hang in front of.
            McsmCreatorArms.submit(ctx);
            // BUILD #485 -- the ninth layer: reality-glitch nightmare / uninpossible layer
            // Photorealistic 3D geometry spire & castle mesh, gothic hanging fortresses,
            // jagged mountain ridges, colossal Creator mesh - true 3D scene-graph VertexBuffer
            net.mcsm.extras.client.McsmNinthLayerGeometry.submit(ctx);
            // BUILD #487 -- extreme Creator skybox – Creator moves around skybox in ALL dimensions,
            // built-in garden visible as part of skybox, entire Minecraft world on its body,
            // world pushed up, you see you're on its body when flying extremely high, watching
            net.mcsm.extras.client.McsmCreatorSkybox.submit(ctx);
            // BUILD #493 / 7000.0.21-M -- Procedural Void-Glitch Generator & Reality Mutation Engine
            // Gigantic vortex void skybox with spinning square rings, 3D particles, reality rip VFX,
            // movies zoom by as jump further, procedural layers never end, new reality underneath
            net.mcsm.extras.client.McsmVoidVortexSkybox.submit(ctx);
        }
        // Intentionally no ci.cancel(): the original smooth backdrop owns this
        // pass. The custom ring/vortex generators are not submitted here.
    }
}
