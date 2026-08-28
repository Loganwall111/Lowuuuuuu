# MCSM Visuals r1 — Instructions

Everything you need to install, verify, build, and publish the
**MCSM Visuals r1** release (`v1.9.61-26.2`).

---

## 1. Requirements

| Item | Version |
| :--- | :--- |
| Minecraft | **26.2** (Java Edition) |
| Mod loader | **Fabric Loader 0.19.3+** |
| Mods | **Fabric API** (any build for 26.2), **Mod Menu** (recommended, for config) |
| Shader loader | **Iris** or **OptiFine** (only needed for the shader pack) |

---

## 2. Installation

### 2.1 The three files

| File | Where it goes |
| :--- | :--- |
| `dabywitherstormmod-1.9.61-26.2-beta-r1.jar` | `.minecraft/mods/` |
| `MCSM_ResourcePack.zip` | `.minecraft/resourcepacks/` |
| `MCSM_ShaderPack.zip` | `.minecraft/shaderpacks/` |

`.minecraft` is `%APPDATA%/.minecraft` on Windows, `~/Library/Application Support/minecraft` on macOS, `~/.minecraft` on Linux.

**Do not** unzip any of them. Drop the files in as-is.

### 2.2 In-game

1. Launch Minecraft 26.2 with the Fabric loader.
2. **Options → Resource Packs** → enable **MCSM Resource Pack** (move it above any other packs).
3. **Options → Video Settings → Shader Packs** (Iris/OptiFine) → select **MCSM_ShaderPack**.
4. **Options → Mods → Wither Storm Mod → Config** (Mod Menu) → confirm:
   - `stormAtmosphere` = ON (full-screen storm post pass)
   - `stormBlobFX` = ON (phase-5 blob, halo, rear fog, flash, vortex)
   - `atmospherePulse` = ON (breathing glare)
   - preset **MCSM** (default) — carries the Story-Mode beam/eye colours.

### 2.3 Recommended settings

- Video Settings → Quality → **Custom Sky: ON**, **Sky / Sun & Moon: ON**
- Shader Pack → Shader Options → **Story Mode Clouds: ON** (procedural),
  **Dynamic Skybox: ON**, **Story Mode Lighting: ON**, **Wither Storm Teeth Glow: ON**

---

## 3. What you should see (phase-by-phase verification)

| Phase | Expected look |
| :--- | :--- |
| Any | Lavender zenith → orange horizon skybox; clock keeps running |
| 4.0+ | Light-blue halo at the storm centre (stays to the end) |
| 4.5 | Sky/fog turns **green** |
| 5.0 | Sky/fog turns **turquoise** |
| 5.1–5.9 | Giant colour-shifting centre blob (dark purple → magenta → pink/blue/black), heavy magenta/purple/black fog layer on the storm's **back**, moving with it |
| 6.0+ | Bright **flash directly above the storm every 2 minutes** |
| 7–8 | **Vortex model mesh** rotating/tumbling on top of the storm |
| Day/night | Cloud colour shifts (white/coral → periwinkle); **sun-cast shadows sweep the ground and water** |

The storm body is **100% shader/GLSL** — no 3D shells, no PNG cloud sheets.

---

## 4. Building from source

Requires **JDK 25** and **Gradle 9.5.1** (the wrapper is included; CI uses the same versions).

```bash
gradle build --no-daemon --stacktrace
```

Output: `build/libs/dabywitherstormmod-1.9.61-26.2-beta.jar`

Rebuild the two packs from the committed directories (flat zips, PNG-free check):

```bash
python3 tools/build_mcsm_packs.py
# writes MCSM_ResourcePack.zip and MCSM_ShaderPack.zip at the repo root
```

Validate everything:

```bash
python3 tools/validate_release_artifacts.py \
  --rp MCSM_ResourcePack.zip --sp MCSM_ShaderPack.zip \
  --jar build/libs/dabywitherstormmod-1.9.61-26.2-beta.jar \
  --expect-version 1.9.61-26.2-beta
```

> The repo-staged fallback jar in `docs/releases/r1/` is produced by
> `tools/merge_release_jar.py` (original classes + merged resources, no
> `geo/` Blockbench sources, no `ffmpeg`). The **CI-built jar always
> supersedes it** — that's the one to distribute.

---

## 5. Publishing a release

Release-asset uploads are blocked from the dev sandbox (`uploads.github.com`
unreachable), so publishing happens on GitHub's runner via the workflow.

### Steps

1. **Merge PR #18** (`arena/01a04a20-lowuuuuuu` → `main`).
2. GitHub **Actions → “MCSM Integrated Release Build” → Run workflow** with:
   - `release_tag` = `v1.9.61-26.2-mcsm-r1`
3. The workflow will:
   - compile the mod jar (renamed `-r<run-number>`),
   - rebuild both packs via `tools/build_mcsm_packs.py`,
   - validate all three artifacts,
   - force-upload them to the release `v1.9.61-26.2-mcsm-r1` (already created),
   - regenerate the release notes + SHA-256 digests.

Until then, the artifacts are downloadable from the repo at
`docs/releases/r1/` (open each file → **Download raw file**), with checksums
in `docs/releases/r1/SHA256SUMS.txt`.

---

## 6. Troubleshooting

| Symptom | Fix |
| :--- | :--- |
| Storm atmosphere post pass never appears | It self-disables when a shader pack is active (the pack provides its own sky/fog). Turn the shader pack off to see the mod's post pass, or keep it on and rely on the pack's look. Check the log for `[dabywitherstormmod] storm atmosphere: ...`. |
| Clouds look flat / no pattern | Shader pack not enabled, or **Story Mode Clouds** off in Shader Options. |
| Shadows missing on water | Re-select the shader pack (program list refresh), enable **Dynamic Skybox**. |
| Mod not loading | Confirm Fabric API is installed, loader ≥ 0.19.3, Minecraft 26.2. |
| Old-looking storm | A stale jar may be cached — rename the jar (CI builds are already renamed per run) or clear `.minecraft/mods` of older `dabywitherstormmod-1.9.60*` jars. |
| PNG cloud sheets reappear after a rebuild | `tools/build_mcsm_packs.py` hard-fails if any `cloudTex` binding or cloud PNG sneaks back — keep the cloud programs procedural. |
