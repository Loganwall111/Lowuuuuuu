# SESSION HANDOFF — Devouring Storms (2026-08-25, pass 6)

Paste/read this at the start of a new session. Workspace: /home/user/Lowuuuuuu,
branch arena/01a03982-lowuuuuuu (session-fixed; never switch/push elsewhere).
This pass CONTINUES arena/01a0354e-lowuuuuuu (merged in full — pass 5 +
cloud-box fix — plus this session's work on top).

## Project
Fabric mod for Minecraft 26.2, package net.dabicco.devouringstorms, repo
Loganwall111/Lowuuuuuu. Full rebrand done: no original-creator refs, credit
Logan Wall, commands -> devouringstorms, Ctrl+O config
(DevouringStormsClientConfig). PRs #10/#12 stay DRAFT, never merge; this
session's PR follows the same pattern.
Builds ONLY via GitHub Actions (no local JDK): jar is
"1.9.61-26.2-beta+build.N.<sha>"; artifact now ALSO uniquely named
"dabywitherstormmod-b<run>-<sha>" (build.yml) so every download is distinct.
Gate CI only via: gh run list --branch arena/01a03982-lowuuuuuu
and gh run watch <id> --exit-status (all log routes are dead/unreadable).

## What pass 6 shipped (this session)
User asks: remove other clouds + sky dust; regular halos; purple flashbang
phase 6+ that does NOT light the whole screen (separate from the skybox);
match newest screenshots to the older good ones (attachments didn't survive
the turn — worked from the standing specs + last bug report instead).

- NEW client/StormMutationFlash.java: localized purple mutation flash-bang.
  World-anchored at the storm core (x, y + 0.45*bodyR, z), additive glow
  pipelines (depth-tested, NO depth write) so terrain occludes it — it can
  never wash the whole screen and never touches lightmap/fog/sky. Triggered:
  phase crossings 6.0 (strength 1.0) / 7.0 (0.85) / 8.0 (0.9) tracked per
  entityId in LAST_PHASE, plus a slow crackle every ~16-22s/storm while
  phase >= 5.9. Fast attack (2t) + quad decay (26t life), dist fade to
  1600 blocks. Config key mutationFlashBang (default ON) + GUI row.
  Registered: COLLECT_SUBMITS submit, START_CLIENT_TICK tick, clear() in all
  three lifecycle spots.
- StormCloudDeck: per-storm cloud prisms REMOVED from submit() (they were the
  "glitchy boxes over top"). Only the optional global deck + no-storm ambient
  ceiling still render. replacesVanillaClouds() now also hides the vanilla
  cloud layer (incl. weather cells) whenever ANY tracked storm is phase >=
  4.25 — clean "sky only, no clouds" under the official plates.
- StormPresenceFX: ejecta/spark dust shuts off once StormSkyDome.domeVeil
  >= 0.5 (live sparks fast-decay). Sky is clean under the anomaly plate.
- WitherStormRenderer.submitStormAura: REWRITTEN as ONE regular halo — a
  flat camera-facing ring (yaw+pitch billboard of halo_ring.png) centred on
  the core, phase-tinted (blue-white 4 -> dark teal wash ~5 -> purple
  5.15-5.45 -> blue 5.5 -> pink 5.7 -> mutation red/orange/magenta 6+), soft
  gradient light core inside, gentle breathe. Old layered gradient cylinders
  removed from the aura (submitGradientCylinder/submitBackPlate still used by
  night light + collapse glow). 7.5+ vortex rings kept. User quote: "they
  work just regular Halos after all".
- world_glow_combine.fsh: glow saturation 1.75 -> 2.2 + chroma reinforcement
  (glow += max(glow - luma, 0) * 0.35) so world lighting reads COLOURED.
- gradle.properties mod_version -> 1.9.61-26.2-beta; build.yml artifact name
  dabywitherstormmod-b<run_number>-<sha> (+ workflow_dispatch trigger).

## Verified intact from earlier passes
StormSkyDome (official phase4_energy/phase59_anomaly plates, additive, no
depth write, RADIUS 520); official rendertype_clouds.vsh override in
assets/minecraft/shaders/core (alpha math fixed in 8f616dc); OG traced stage
shells render in EVERY skin (stormStageShells default ON); WorldShadows
selfShadow; lightmap darken; impact light; stormShadow defaults ON.

## Commits <-> green runs
(previous session: be6018a=32794211093, f1bc3e7=32794678617, 4c949b0=32796206417,
8563f89=32835552913, 9fde820=32861093160, 8f616dc=32864907130)
This session: fill in after CI goes green.

## Open items (next steps)
1. User retests THIS jar: clean storm sky (no cloud boxes, no dust), regular
   halo, purple mutation bang at the split (localized!), colored lighting,
   OG models, shadows. Screenshots didn't reach the sandbox this turn — if
   the look still doesn't match their reference shots, ask them to re-attach.
2. If sky still "messed up" during weather: next suspect is vanilla's rain
   CLOUD cells via the overridden rendertype_clouds.vsh (CloudColorMixin now
   cancels CloudRenderer.render entirely under a phase>=4.25 storm, so it
   should be gone; if not, check that mixin's method signature still matches).
3. Stage D DAE direct wiring optional (shells carry equivalent geometry).
4. Keep PR draft, stay on branch, keep green checkpoints.

## Gotchas / dead ends (DO NOT REPEAT)
- User web-UI upload commits (like 217682f) REPLACE THE WHOLE TREE with a
  stale snapshot (298 deletions). NEVER reset --hard onto them; diff vs last
  good commit and extract only genuinely-new files via git show <sha>:<file>.
- Remote tip may be such an upload: push with git push origin
  +<branch> (plain --force-with-lease failed once: stale lease).
- CI logs unreadable (annotations generic, --log-failed noise, zip EOF).
- User attachments may not survive turn boundaries; extract immediately.
- No local JDK/toolchain; python patch anchors need exact-text grep first.
- API notes if CI goes red: ClientDistantStormManager.all() returns Collection
  (not List); StormData{x,y,z,phase,entityId,expansionPhase,dispX/Y,Z};
  Identifier.fromNamespaceAndPath; GlowRenderTypes.glow/translucent(Identifier);
  SubmitNodeCollector.submitCustomGeometry; LevelRenderContext.levelState()
  .cameraRenderState.pos; FULL_BRIGHT=15728880; CameraRenderState.pos public
  Vec3; Mth.lerp(amount, min, max).

## Standing user specs
- Sky timeline: 4 regular; 4.5 green; 5 turquoise; 5.15-5.7 purple->pink
  horizon; 5.5-6.0 crossfade to anomaly; 6 black briefly then pinkish purple;
  6/7 pinkish purple / vibrant red-orange-magenta; 8 purple + dark pink rings.
  Turquoise fog only ~4.5-5.
- OG dark black/purple MCSM look default; teeth mint-cyan #7FFFD4->#00FFFF
  FULL_BRIGHT; additive + depthMask(false) glows; phase 6 big swarming cubes
  on the sides; lighting must read visibly COLORED; shadows default-on
  everywhere; everything seamless and automatic. HALOS ARE REGULAR FLAT
  RINGS NOW (user confirmed 2026-08-25) — camera-facing billboard is fine.
- Phase 6+ mutation flash-bang: LOCALIZED purple burst at the storm, never a
  full-screen flash, separate effect from the purple anomaly skybox.
- Reference videos: youtu.be/E-NYcNk4h6, iBLYyNS4f3U, 8VlaLp2G1Aw.
