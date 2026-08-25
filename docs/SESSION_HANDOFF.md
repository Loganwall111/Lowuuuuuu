# SESSION HANDOFF — Devouring Storms (2026-08-25, pass 8 = blue halo fix)

Pass 8 (same day): user clarified the ONE billboard is the BLUE HALO of
phase 4 - light blue, dead-centre of the storm, the mass lit from its middle
with blue looming off the sides. submitCoreGlow retuned: phase 4 = light
blue edge (0.42,0.72,1.0) + hot white-blue centre, disc scaled up 1.22x in
the blue window (4.0-4.9), then the SAME single glow follows the palette
(teal 5.0 -> purple 5.15-5.45 -> pink 5.7 -> mutated magenta-red 6+).
Confirmed already-covered asks: clouds wired into skybox (pass 7), coloured
lighting for glowing blocks (world_glow_combine chroma boost), twinkling
stars (StormStarfield + vanilla), coloured shadows incl. water
(StormShadow.shadowColor already blends the phase palette into the tint).

# PREVIOUS HANDOFF (pass 7 = TELLTALE SKYBOX)

Paste/read this at the start of a new session. Workspace: /home/user/Lowuuuuuu,
branch arena/01a03982-lowuuuuuu (session-fixed; never switch/push elsewhere).
Pass 6 merged arena/01a0354e (pass 5). Pass 7 = the Telltale skybox refactor
the user specified after realizing the "globe" approach was wrong: it was
always a DYNAMIC SKYBOX attached to the back, native sky-pass architecture.

## Project
Fabric mod for Minecraft 26.2, package net.dabicco.devouringstorms, repo
Loganwall111/Lowuuuuuu. Builds ONLY via GitHub Actions (no local JDK; CDN
hosts blocked from sandbox: gradle/maven/adoptium/GH release-assets ALL EOF -
only api.github.com + github.com git work). Jar stamped
"<mod_version>+build.<run>.<sha>" by build.gradle; PR #13 (draft) is the
build gate (on: pull_request). gh token FLAPS (401 Bad credentials) - retry
later if it dies mid-session; it came back on its own twice.

## 26.2 SKY SOURCE OF TRUTH (verified via Renekovski/26.2-mcp mirror)
- SkyRenderer public methods: renderSkyDisc(int), renderDarkDisc(),
  renderSunMoonAndStars(PoseStack,float,float,float,MoonPhase,float,float),
  renderSunriseAndSunset(...), renderEndSky(), renderEndFlash(PoseStack,
  float,float,float), extractRenderState(...). Called from
  LevelRenderer.addSkyPass inside a frame-graph "sky" pass lambda; the
  poseStack is camera-rotation-only => camera-locked geometry = infinite
  depth. Each render* makes its OWN RenderPass on the main target.
- RenderPipelines.END_SKY: MATRICES_PROJECTION_SNIPPET + "core/position_tex_color"
  shaders + Sampler0 + BlendFunction.TRANSLUCENT + POSITION_TEX_COLOR QUADS.
  Our StormSkyBox pipeline = same but BlendFunction.ADDITIVE, no depth state.
- CONFIRMED APIs: Minecraft.getTextureManager().getTexture(id) auto-loads
  (SimpleTexture) and returns AbstractTexture; .getTextureView()/.getSampler();
  mc.getMainRenderTarget().getColorTextureView()/getDepthTextureView();
  mc.gameRenderer.mainCamera().position() (record-style accessor - NOT
  getMainCamera); Camera.position(); RenderSystem.getModelViewMatrixCopy();
  RenderSystem.getDynamicUniforms().writeTransform(Matrix4f, Vector4f);
  device.createBuffer(Supplier,32,ByteBuffer); sequential QUADS indices via
  RenderSystem.getSequentialBuffer(QUADS).getBuffer(quads*6) + .type().

## What pass 7 shipped (this session, Telltale architecture)
- NEW client/SkyAtmosphereController.java: central phase-linked controller.
  Nearest storm -> intensity (proximity clamp(1.25-dist/2400,.16,1)),
  energyWeight/anomalyWeight (crossfade smooth(5.5,6.0)), cloudWeight,
  coneRadians (lerp smooth(4,6.5): 0.66rad -> 2.1rad full takeover),
  fogScale (1.0 -> 0.6), churn, mutationTint (orange->red->magenta 6-8),
  stormDir (horizon-biased camera->storm unit vector).
- NEW client/StormSkyBox.java: LAYER 1 native sky pass. renderSkyLayers()
  called from SkyRendererMixin HEAD-inject on renderSunMoonAndStars; when
  active it draws AND cancels (sun/moon/stars suppressed during storm).
  Camera-locked dome rings (RADIUS 320, elevations {0,7,15,26,40,58,78,90}deg,
  ring weights), ADDITIVE pipeline (no depth test/write = glDepthMask(false)+
  no depth test, no mountain clipping), per-vertex cone weighting via
  dot(dir, stormBearingLocal()) (bearing transformed into view space with
  modelview.transformDirection), UV vertical by elevation + horizontal
  azimuth+churn (u=0.5+0.35*sin(az+churn), v=0.55-elev*0.0058 clamp .06-.55).
  Layers: energy plate (cyan tint + yellow horizon accent ring 0), anomaly
  plate (mutation tint), 2 cloud bands from textures/misc/mcsm_cloud.png
  (12-36deg + 6-26deg, chunky segments 18/14, counter-churn). Per-frame
  ByteBufferBuilder -> MeshData -> GpuBuffer (closed), drawLayer(texture,
  emitter) public helper reused by the flash.
- StormMutationFlash: rewritten as SKY-LAYER bloom (renderSkyBloom(target)):
  additive disc + racing ring quads on the tangent plane of the storm
  bearing at SKY_R=240 sky depth. Triggers unchanged (phase crossings 6/7/8
  + ~16-22s crackle). NOT full-screen, no lightmap/fog interaction.
- SkyRendererMixin: + dabyws$stormSkyBackdrop HEAD cancellable inject.
- RenderPipelinesAccessor: + MATRICES_PROJECTION_SNIPPET accessor.
- WitherStormRenderer LAYER 3: submitSkyBackdrop/submitNightLight calls
  REMOVED (methods remain, dead); submitStormAura replaced by submitCoreGlow
  = THE one 2D billboard (user: white glow at storm centre, only 2D element)
  - camera-facing soft white glow + hot centre at radius*0.3 up the core,
  warm white -> magenta heat 5.95-6.4; 7.5+ vortex rings kept; collapse
  glow kept. GUI rows renamed to "Storm Core Glow".
- DevouringStormsModClient: StormSkyDome::submit + StormMutationFlash::submit
  registrations REMOVED (world dome retired; flash is sky-layer now).
  StormSkyDome class kept only for domeVeil() (StormPresenceFX uses it).
- StormSkyCanopy + StormCataclysmFX: early-return when
  SkyAtmosphereController.active() (stand down under the skybox).
- FogRendererMixin (LAYER 2): controller updated at fog time; horizon fog
  compressed by fogScale (environmental/renderDistance start+end *=).
- Colored lighting: world_glow_combine.fsh chroma boost (pass 6) + phase
  palettes still drive fog/sky/lightmap-darken sync.
- mod_version -> 1.9.62-26.2-beta.

## Commits <-> green runs
(previous: 8f616dc=32864907130, e7d8f02=32877016373, ecf96cd=32877962720)
Pass 7: fill in after CI green.

## Open items (next steps)
1. User retests pass-7 jar: storm sky should now sit at true infinite depth
   (no mountain clipping, no boxes), churn around the storm, widen 6+, sun/
   moon/stars gone during storm, fog heavier at horizon, white core glow
   billboard at the storm's centre, purple mutation bangs in the sky layer.
2. If skybox too bright/washed: lower alpha ceilings (235 in StormSkyBox.alpha)
   or intensity clamp; if too faint: raise RADIUS or cone floor.
3. If renderSunMoonAndStars inject ever fails to apply, check the 26.2 mirror
   (Renekovski/26.2-mcp) for signature drift FIRST.
4. Keep PR draft, stay on branch, keep green checkpoints.

## Gotchas / dead ends (DO NOT REPEAT)
- Arena app token CANNOT push .github/workflows/* changes (rejected; patch
  saved at docs/ci-artifact-name.patch for web-UI application).
- User web-UI upload commits (like 217682f) REPLACE THE WHOLE TREE with a
  stale snapshot; extract only genuinely-new files via git show <sha>:<file>.
- CI logs unreadable from sandbox (results-receiver EOF); find compile errors
  by DIFF AUDIT vs last green commit (pass 6: lossy double->float narrowing).
- User attachments usually don't reach the sandbox; work from specs + palettes.
- No local JDK/toolchain; gradle/maven/adoptium hosts blocked.
- API notes if CI goes red: mappings are Mojang-record-style in places:
  gameRenderer.mainCamera() (not getMainCamera), Camera.position(),
  ClientDistantStormManager.all() Collection, StormData{x,y,z,phase,entityId,
  expansionPhase,dispX/Y,Z}, FULL_BRIGHT=15728880.

## Standing user specs
- TELLTALE LAYERED SKY (pass 7 spec): L1 native dynamic skybox pass at
  infinite depth, camera-locked, additive, no depth test/write; L2 fog/
  ambient/world-tint sync + denser horizon fog + suppress vanilla weather
  clouds/prisms/sun/moon; L3 entity renderer = foreground only (core shell,
  heads, tentacles, tractor beams, particles) - NO sky domes/aura meshes/
  big billboards on the entity; phase 4 black/purple void + cyan energy +
  yellow horizon; 5.9 purple/void/orange (sky_only_no_clouds.png); 6-8 cone
  widens + red/magenta/orange; mutation flashbangs = additive radial bloom
  IN THE SKY LAYER, never full-screen whiteout.
- The white glow at the storm's centre is the ONLY 2D billboard ("the light
  coming off the storm"); everything else is 3D or sky-layer.
- Sky timeline: 4 regular; 4.5 green; 5 turquoise; 5.15-5.7 purple->pink;
  5.5-6.0 crossfade to anomaly; 6 black then pinkish purple; 6/7 pinkish
  purple / vibrant red-orange-magenta; 8 purple + dark pink rings.
- OG dark black/purple MCSM look default; teeth mint-cyan FULL_BRIGHT;
  lighting must read visibly COLORED; shadows default-on; seamless+automatic.
- Reference videos: youtu.be/E-NYcNk4h6, iBLYyNS4f3U, 8VlaLp2G1Aw.

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
This session: 583fa5b=32873887081 RED (lossy double->float narrowing in
StormMutationFlash.Flash ctor — javac rejects implicit narrowing),
e7d8f02=32877016373 GREEN (2m40s, run #112). Jar inside the artifact:
devouringstorms-1.9.61-26.2-beta+build.112.e7d8f02.jar (unique name per build).
PR #13 (draft) is this session's build gate.

IMPORTANT CI lesson learned this session: the Arena app token CANNOT push
commits touching .github/workflows/* (push rejected: "without workflows
permission"). The unique-artifact-name workflow tweak is saved as
docs/ci-artifact-name.patch — apply it via the GitHub web UI if wanted.
The jar filename stamping (+build.N.sha via build.gradle) needs no workflow
change and already makes every jar uniquely named for Minecraft updates.

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

## PASS 7 CONT — THE 26.2 RENDER API GROUND TRUTH (learned via 16 CI bisect probes)

The Renekovski/26.2-mcp mirror is an OLDER API era than the artifact we compile
against. It lied about: VertexFormat.Mode (real = com.mojang.blaze3d.PrimitiveTopology),
drawIndexed arity (real = 5 args: (indexCount, 1, 0, 0, 0)), withVertexFormat (real =
withVertexBinding(0, fmt) + withPrimitiveTopology), Minecraft.getMainRenderTarget (real =
mc.gameRenderer.mainRenderTarget()), and possibly new BufferBuilder(...) (never proven).

### VERIFIED-REAL 26.x API (every line green in CI run 32905697166 / commit 6fef80e)
- Custom vertex format: VertexFormat.builder(0).addAttribute("InPosition", GpuFormat.RGB32_FLOAT)
  .addAttribute("InTexCoords", GpuFormat.RG32_FLOAT)...build() — attribute names are YOURS,
  matched only by your own shaders. RGBA32_FLOAT unverified; RGB32/RG32/R32_FLOAT verified.
- Pipeline: RenderPipeline.builder().withLocation(id).withVertexShader(id).withFragmentShader(id)
  .withVertexBinding(0, SKY_FORMAT).withPrimitiveTopology(PrimitiveTopology.QUADS)
  .withBindGroupLayout(BindGroupLayout.builder().withSampler("Sampler0").build())
  .withBindGroupLayout(BindGroupLayout.builder().withUniform("SkyConfig", UniformType.UNIFORM_BUFFER).build())
  .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE)).withCull(false).build()
- Indices: RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS) -> AutoStorageIndexBuffer;
  .getBuffer(quads*6) -> GpuBuffer; pass.setIndexBuffer(indices, indexer.type());
  pass.drawIndexed(indexCount, 1, 0, 0, 0)   // FIVE args
- Vertices: stage floats -> ByteBuffer -> GpuBufferPool.write(name, 40, data);
  pass.setVertexBuffer(0, vbo.slice(0L, bytes))
- UBO: (new Std140SizeCalculator()).putMat4f().get() + Std140Builder.intoBuffer(data).putMat4f(m);
  pass.setUniform("SkyConfig", ubo)
- Pass: RenderSystem.getDevice().createCommandEncoder().createRenderPass(supplier,
  mainTarget.getColorTextureView(), Optional.empty(), mainTarget.getDepthTextureView(),
  OptionalDouble.empty()); mainTarget = mc.gameRenderer.mainRenderTarget()
- View matrix: new Matrix4f(RenderSystem.getModelViewStack()) (cheatutils-verified)
- Texture: mc.getTextureManager().getTexture(id) -> AbstractTexture (probe-verified);
  tex.getTextureView(); sampler via RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)
- Projection capture via @ModifyArg(renderLevel/ProjectionMatrixBuffer.getBuffer) FAILS THE
  BUILD in our artifact (mixin AP cannot resolve) — do not retry that hook. Current sky pass
  uses SkyMatrices.projection(): live-aspect 70-degree perspective fallback (fov effects and
  non-70 fov cause a mild sky-scale mismatch; capture hook needs a different injection point).
- Bisect method when CI is the only oracle: stub whole subsystem green, restore halves, then
  statements, then sub-expressions (greens are reliable; a red can be an infra flake — retest
  any 'impossible' red before believing it, e.g. int*int*int arg 'failing').
