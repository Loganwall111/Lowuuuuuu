package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dabicco.witherstormmod.DabyWitherStormMod;
import net.mcsm.extras.McsmBuiltinPack;
import net.mcsm.extras.McsmAdams;
import net.mcsm.extras.McsmCreatorRealm;
import net.mcsm.extras.McsmVoidAging;
import net.mcsm.extras.McsmBlackHole;
import net.mcsm.extras.McsmCities;
import net.mcsm.extras.McsmMassg;
import net.mcsm.extras.McsmMazes;
import net.mcsm.extras.McsmPortals;
import net.mcsm.extras.McsmVoidDescent;
import net.mcsm.extras.McsmSkyVortexes;
import net.mcsm.extras.McsmRituals;
import net.mcsm.extras.McsmServerRooms;
import net.mcsm.extras.McsmVoid;
import net.mcsm.extras.McsmSounds;
import net.mcsm.extras.McsmTentacles;
import net.mcsm.extras.McsmTerminal;
import net.mcsm.extras.McsmCreatures;
import net.mcsm.extras.McsmTornadoes;
import net.mcsm.extras.McsmContent;
import net.mcsm.extras.McsmUiSounds;
import net.mcsm.extras.entity.McsmEntities;

/**
 * Registers the built-in Story Look resource pack during the mod's own
 * initialization (target and method name verified against the compiled
 * base mod), so the pack is known to the pack repository before its first
 * reload and comes up enabled by default.
 */
@Mixin(DabyWitherStormMod.class)
public abstract class McsmBuiltinPackMixin {

    @Inject(method = "onInitialize", at = @At("HEAD"), remap = false)
    private void dabyws$builtinPack(CallbackInfo ci) {
        McsmBuiltinPack.register();
        // 1.9.205 -- Story Mode character entity type + attributes.
        McsmEntities.register();
        // Build #416 -- UI sound events, registered here while the built-in
        // registries are still open (their lazy registration used to throw and
        // leave every menu silent).
        McsmUiSounds.initialize();
        // Build #416 (D.8) -- the Decayed Reality content pack: 35 blocks, 54
        // items, doors, trap doors, the new weapons and our own creative tab,
        // registered here because this is the only window in which the
        // built-in registries are still open (the same reason the base mod's
        // own ModBlocks/ModItems pass runs from onInitialize).
        McsmContent.register();
        McsmContent.registerTab();
        // Build #416 (D.8, phase 2) -- the abandoned cities of the decayed
        // reality. ServerTickEvents.END_LEVEL_TICK is the base mod's own hook
        // (DabyWitherStormMod registers McsmWorldgen.tick on it), so this needs
        // no mixin and no new API surface at all.
        McsmCities.register();
        // Build #416 (D.8, phase 3) -- the bestiary, the boss ladder and the
        // Creator's manifestation. Same hook as the cities: every creature on it
        // is a vanilla type re-kitted at spawn time, so this needs no registry
        // entry and works on worlds saved before it existed.
        McsmCreatures.register();
        // Build #416 (D.8, phase 4) -- the world itself starts breaking: the
        // black hole event and the mega-tornadoes. Both are level-tick systems
        // that decide when the sky is allowed to open and when the weather turns,
        // and both own their own cleanup, so neither can leave anything behind.
        McsmBlackHole.register();
        McsmTornadoes.register();
        // BUILD #433 -- "sky vortexes spawning monsters": the picture that
        // finally puts something on the ground. Same level-tick hook, same
        // registry-by-id creature resolution as the rest of the extras.
        McsmSkyVortexes.register();
        // BUILD #443 -- the rituals, and the endless dimension they open.
        McsmRituals.register();
        McsmAdams.register();
        // BUILD #462 -- and the Creator's own dimension: the fourth world, and
        // the only one that was built rather than left.
        McsmCreatorRealm.register();
        // BUILD #463 -- and what the void does to whoever keeps going back.
        McsmVoidAging.register();
        // BUILD #444 -- the two underground structures.
        McsmMazes.register();
        McsmServerRooms.register();
        // BUILD #451 -- the void, and the doorways between the dimensions.
        McsmVoid.register();
        McsmPortals.register();
        // BUILD #479 -- and the way in that needs no doorway at all: the world's own
        // floor. Dive into the regular Minecraft void and the fall keeps going, down
        // through the four stops and into the gel. No screen, no damage, no command.
        McsmVoidDescent.register();
        // BUILD #458 -- say out loud which world is made of what, so a build's
        // per-dimension identity is checkable from the game log alone.
        System.out.println(net.mcsm.extras.McsmIdentity.summary());
        System.out.println(net.mcsm.extras.McsmLocks.summary());
        // Build #416 (D.8, phase 5) -- the story terminal: the antenna's
        // restricted console, the radio signals it picks up, the password the
        // world keeps for itself. It owns no packet channel any more (the
        // client screen and the world action are wired locally / by name), so
        // this is a plain server tick registration, not a payload registry.
        McsmTerminal.register();
        // Build #416 (D.8, phase 6) -- THE MASSG. The creature that cannot be
        // killed and cannot be deleted, its hallucinations, and the countdown it
        // runs in the sky. Registered last on purpose: it is the end of the list.
        McsmMassg.register();
        // BUILD #425 -- the mod's own sounds. Touching the class registers all
        // sixteen SoundEvents; the Ogg files themselves ship in the overlay at
        // assets/mcsm/sounds/ (see ci/make_mcsm_sounds.py).
        McsmSounds.initialize();
        // BUILD #426 -- the tentacles take the player. The config switch for it
        // has existed since the port and nothing read it; this is the half that
        // turns a decoration into a grab.
        McsmTentacles.register();
    }
}
