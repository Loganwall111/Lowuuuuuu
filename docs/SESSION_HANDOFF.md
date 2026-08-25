# SESSION HANDOFF — Devouring Storms (2026-08-25)

Paste/read this at the start of a new session. Workspace: /home/user/Lowuuuuuu,
branch arena/01a0354e-lowuuuuuu (session-fixed; never switch/push elsewhere).

## Project
Fabric mod for Minecraft 26.2, package net.dabicco.devouringstorms, repo
Loganwall111/Lowuuuuuu. Full rebrand done: no original-creator refs, credit
Logan Wall, commands -> devouringstorms, Ctrl+O config
(DevouringStormsClientConfig). PRs #10/#12 stay DRAFT, never merge.
Builds ONLY via GitHub Actions (no local JDK): artifact jar "+build.N.<sha>".
Gate CI only via: gh run list --branch arena/01a0354e-lowuuuuuu
and gh run watch <id> --exit-status (all log routes are dead/unreadable).

## Commits <-> green runs
be6018a=32794211093, f1bc3e7=32794678617, 4c949b0=32796206417,
8563f89=32835552913, 9fde820=32861093160 (pass 5),
8f616dc=32864907130 (cloud-boxes fix, LATEST GREEN).

## What pass 5 shipped (9fde820)
- User uploaded official assets via commit 217682f (web upload = DANGER, see
  Gotchas). Filed: textures/sky/phase4_energy.png + phase59_anomaly.png,
  geo/witherstormStageD_Center_Massive.dae, tools/official/rendertype_clouds_OFFICIAL.vsh.
- NEW client/StormSkyDome.java: entity-tethered additive dome on storm core;
  vertical elevation sampling (ELEVATIONS {0,10,22,38,58} deg, RING_WEIGHTS
  {1,.82,.6,.36,.18}, 24 segments); energy->anomaly crossfade smooth(5.5,6.0);
  mutation tint 6->8 orange->red->magenta; proximity clamp(1.25-dist/1200,.28,1);
  static domeVeil(phase)=smooth(5.4,5.7); radius now 520; registered after
  canopy in DevouringStormsModClient.
- StormCloudDeck: per-storm presence *= (1 - domeVeil) -> prisms fade under
  the anomaly sky (renderField param is named presence).
- Config defaults: ambientMcsmClouds=false, glareEjecta=false (sky dust gone),
  worldShadowStrength=0.70.
- WorldShadows: drawShadowPass selfShadow=true so tall terrain (cliffs/towers)
  gets shaded instead of skipped.
- WitherStormRenderer: stage shells render in EVERY skin now (OG traced models
  default ON; stageShellName/stageShellAlpha no longer gate on StormSkins.shaded()).
- world_glow_combine.fsh: glow saturated toward dominant hue
  (glow = mix(vec3(luma), glow, 1.75)) so light reads COLORED not white.
- StormPalettes: SKY_CATACLYSM {0.36,.10,.17}, CLOUD_CATACLYSM {0.34,.11,.18}.
- StormSkyCanopy glow-arc removed (brace-verified).

## Latest bug report + fix (8f616dc, NEEDS USER RETEST)
User: "sky is so messed up... weather storm literally glitchy with a bunch of
boxes over top" (3 screenshots never reached the sandbox - no pixel evidence).
Root cause found in the ported rendertype_clouds.vsh: fade normalization
divided by wrong scale -> underside alpha could go NEGATIVE (-0.2) = inverted
blend = glitchy boxes during weather; CloudColor.a applied TWICE; all faces
flat brightness 1.0 = white slabs; CloudHeight 2.5x stretch on 26.2's already
extruded rain cells = towering boxes. Fix: CloudHeight=1.0, per-face brightness
top 1.0/bottom 0.62/N-S 0.78/E-W 0.88, alpha=mix(0.72,1.0,clamp(pos.y/slabHeight,
0,1)), CloudColor applied once. FSH contract unchanged (multiplies
1.0-linear_fog_value(vertexDistance,0,FogCloudsEnd)).

## Open items (next steps)
1. User retests 8f616dc jar: weather clouds + sky + shadows + colored light
   + OG models. If boxes persist: next suspects = StormCloudDeck prisms
   (boxes by design; may need removal/quieting below phase 5.4 too),
   StormSkyDome 24-seg seams, shells on OG skin.
2. Halos: user said "just regular Halos after all" - not yet acted on; ask
   what they want and simplify.
3. Stage D DAE direct wiring optional (shells carry equivalent geometry).
4. Phase color-grading post pass verification (palettes cover most of it).
5. Keep PRs draft, stay on branch, keep green checkpoints.

## Gotchas / dead ends (DO NOT REPEAT)
- User web-UI upload commits (like 217682f) REPLACE THE WHOLE TREE with a
  stale snapshot (298 deletions). NEVER reset --hard onto them; diff vs last
  good commit and extract only genuinely-new files via git show <sha>:<file>.
- Remote tip may be such an upload: push with git push origin
  +arena/01a0354e-lowuuuuuu (plain --force-with-lease failed once: stale lease).
- CI logs unreadable (annotations generic, --log-failed noise, zip EOF).
- User attachments may not survive turn boundaries; extract immediately.
- No local JDK/toolchain; python patch anchors need exact-text grep first.
- API notes if CI goes red: ClientDistantStormManager.all() returns Collection
  (not List); StormData{x,y,z,phase,entityId,expansionPhase,dispX/Y,Z};
  Identifier.fromNamespaceAndPath; GlowRenderTypes.glow/translucent(Identifier);
  SubmitNodeCollector.submitCustomGeometry; LevelRenderContext.levelState()
  .cameraRenderState.pos; FULL_BRIGHT=15728880.

## Standing user specs
- Sky timeline: 4 regular; 4.5 green; 5 turquoise; 5.15-5.7 purple->pink
  horizon; 5.5-6.0 crossfade to anomaly; 6 black briefly then pinkish purple;
  6/7 pinkish purple / vibrant red-orange-magenta; 8 purple + dark pink rings.
  Turquoise fog only ~4.5-5.
- OG dark black/purple MCSM look default; teeth mint-cyan #7FFFD4->#00FFFF
  FULL_BRIGHT; additive + depthMask(false) glows; NO camera-billboarded halos;
  black halo only >=5.5; phase 6 big swarming cubes on the sides; clouds
  3D blocky voxel translucent bottoms 0.4-0.6 alpha indigo/storm-blue/dark
  purple; lighting must read visibly COLORED; shadows default-on everywhere;
  everything seamless and automatic.
- Reference videos: youtu.be/E-NYcNk4h6, iBLYyNS4f3U, 8VlaLp2G1Aw.
