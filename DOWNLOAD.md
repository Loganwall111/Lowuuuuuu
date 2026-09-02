# ⬇️ Downloads

| File | Size | Where it goes |
|---|---|---|
| `download/dabywitherstormmod-1.9.60-26.2-beta.jar` | 77 MB | `.minecraft/mods/` |
| `download/WitherStormSkies-v5.zip` | 11 KB | `.minecraft/shaderpacks/` |

`BUILD SUCCESSFUL in 1m 24s` · all 14 shaders compile · zip-verified

---

# ⚠️ The cloud shader spec you pasted targets an older Minecraft

I have to flag this before anything else, because writing it as specified would have produced two files that compile perfectly and then **draw nothing**.

I pulled the real `rendertype_clouds` out of the 26.2 client jar. The modern cloud renderer:

| The spec says | 26.2 actually has |
|---|---|
| `in vec3 Position`, `in vec4 Color`, `in vec2 UV0` | **no vertex attributes at all** |
| geometry from vertex data | `isamplerBuffer CloudFaces` + `gl_VertexID` |
| `uniform mat4 ModelViewMat` | `layout(std140) uniform DynamicTransforms` |
| `Sampler0` cloud texture | no texture — colour comes from `CloudInfo` |
| `FogStart` / `FogEnd` | `FogCloudsEnd` in a `Fog` block |

Declaring `in vec3 Position` in 26.2 binds nothing, so every vertex collapses to the origin and the cloud layer disappears.

**So I implemented your design against the real pipeline** — every feature you asked for, on the API that exists:

- **2.5× extrusion** — scaled about the slab midpoint, so the deck thickens around its own centre instead of drifting upward
- **Real world normals** — per-face, and *flipped for inside faces*, or the interior of a cloud lights inside out
- **`Light0_Direction` moving shadows** — imported from `light.glsl`, the same light vanilla uses on entities
- **Dark undersides (0.46) and trailing edges (0.20)**
- **Time-of-day tint** — lavender dawn, warm pink dusk, storm blue-grey night, driven off the light's height
- **`discard` under alpha 0.1** — crisp blocky edges

Both validated with `glslangValidator` against the **real vanilla includes**, and I ran the stock vanilla shader through the same harness as a control to prove the test itself was meaningful.

They ship inside the jar at `assets/minecraft/shaders/core/`, not as a resource pack.

---

# 🌫️ The fuzzy gradient, motion blur and mob artefacts

All three were the shaderpack fighting your mod. Fixed at the source:

| Symptom | Cause | Fix |
|---|---|---|
| fuzzy gradient sky | shader drew **its own dome**, over your skyboxes | removed — vanilla sky passes straight through |
| "motion blur" | 13-tap cross blur in `bloomPass` | **BLOOM off by default** |
| cubes/fuzz on mobs | bloom bleeding off bright entity pixels | same fix |
| washed-out look | tonemap + contrast 1.10 + vibrance 1.20 | **all neutral (1.00), tonemap off** |
| dark screen corners | vignette | **off** |

The shader no longer touches the sky at all. What you see is the skyboxes built into the mod — which is what you asked for. Every effect is still a toggle if you want it back.

---

# 🌑 Halo restored, layered, and shrunk

Both layers now run together, which is what you described:

1. **`StormBackdrop`** — the halo attached behind the storm, following it. **Shrunk 15%** (`stormBackdropSize` 6.0 → 5.1), "just a tiny bit".
2. **`StormSkyGradient`** — the wide purple sky wedge, *underneath* it.

Phase colours per your table: 4.5 green · 5.0 turquoise · 5.1 purple · 5.25–5.5 pink-purple · 6.0 pink · 7.0 dark red purple. Turquoise teeth and OG textures are unchanged.

---

## Note on the sandbox

Six Java files were lost to a workspace reset mid-session (they'd only ever existed in the scratch build tree). All are now written to `dabby-patch/overlay/` first, which persists — that failure mode is closed.

## Still open

NPCs with dialogue · Nether pink wiring · config toward 600–800

**I can't run Minecraft here.** The cloud shaders are validated against real vanilla includes with a passing control test, but validation isn't the same as seeing them render — if the clouds look wrong, tell me how and I'll correct it.
