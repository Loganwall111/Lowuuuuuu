# README — How the mod gets compiled (the "GitHub compiler" and the local one)

Two ways to build the jar. Both run the identical recipe in `ci/build.sh` /
`ci/build.ps1`: compile the `mcsm-extras` Java sources with JDK 25, then lay
them + the core shaders + `jar-overrides/` on top of the newest delivery jar
and stamp the version.

## A) GitHub Actions — automatic build on every push ("the GitHub compiler")

The workflow is written and ready at **`ci/workflows/build-mcsm.yml`**. It
cannot be pushed into `.github/workflows/` by the build bot (the app token
lacks the `workflows` permission), so it needs one manual step from the repo
owner:

1. In the repo on GitHub, go to **Actions → New workflow → set up a workflow
   yourself**.
2. Paste the entire contents of `ci/workflows/build-mcsm.yml` from this branch
   (or copy the file into `.github/workflows/build-mcsm.yml` locally and push).
3. Commit — done. Every push to `arena/01a06df7-lowuuuuuu` then:
   - installs Temurin JDK 25 on a clean runner,
   - downloads the Minecraft 26.2 client jar + Mixin + deps (runners have
     full network),
   - runs the GLSL gate (`glslcheck/shimcheck.py`),
   - compiles `mcsm-extras` with `javac --release 25`,
   - assembles and zip-tests the new jar,
   - attaches it as a run **Artifact**, and
   - when `VERSION` is bumped to something with no release yet, creates a
     GitHub Release automatically.

Builds/artifacts appear at
<https://github.com/Loganwall111/Lowuuuuuu/actions>.

## B) Local on your PC — no GitHub needed

You already have JDK 25 (the Azul build you play Minecraft with) and internet.

```powershell
cd C:\path\to\Lowuuuuuu
powershell -ExecutionPolicy Bypass -File ci\build.ps1
```

Output: `out\dabywitherstormmod-<version>-26.2-beta-mcsm.jar` (+ `.sha256`).
Only that jar goes into `mods\` — delete older ones first.

(Linux/macOS equivalent: `bash ci/build.sh`.)

## Rules that still apply (from delivery/HANDOFF.md)

- Never reuse a retired version number — bump `VERSION` first.
- Never ship without the embedded textures/shaders.
- After ANY Java source edit, the jar must be recompiled (that's what these
  scripts are for — a shader-only edit can be overlaid without javac).
- After ANY shader edit, run `python3 glslcheck/shimcheck.py mcsm-core-shaders
  jar-overrides/assets/dabywitherstormmod/shaders/core/storm_glow.fsh
  jar-overrides/assets/dabywitherstormmod/shaders/post/storm_sun_glow.fsh`
  (42/42 expected — build.ps1 on GitHub runners does it for you; locally run
  it yourself if you edited shaders).
