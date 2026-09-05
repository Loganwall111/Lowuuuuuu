# Compile the MCSM JAR

This branch builds the sources/assets from the requested revision
[`2a9f780`](https://github.com/Loganwall111/Lowuuuuuu/commit/2a9f7806883438467300d92ed5b854fb154ae950),
with Minecraft 26.2 GUI API fixes. `VERSION` is **1.9.101** to distinguish the
result from the previously published, shaders-only 1.9.100 artifact.

This is an **overlay build**, not a rebuild of the entire upstream mod: the
repository contains the MCSM Java sources and assets, plus a prebuilt base
mod. The base's other classes, resources and mixin registrations are retained.

## GitHub Actions

The ready-to-install workflow is [`workflows/build-mcsm.yml`](workflows/build-mcsm.yml).
It compiles with Temurin JDK 25, checks the shaders and assembled classes, and
uploads a JAR, SHA-256 checksum and build report. It does **not** publish a
release or commit anything back to the repository.

The current Arena GitHub connection rejected workflow-file pushes because it
lacks **Workflows** permission. Reconnect GitHub in Arena with that permission,
or install the workflow using GitHub's editor:

1. Copy the contents of `ci/workflows/build-mcsm.yml` on this branch.
2. Open the [new workflow editor](https://github.com/Loganwall111/Lowuuuuuu/new/arena/01a06f0e-lowuuuuuu?filename=.github/workflows/build-mcsm.yml).
3. Paste and commit on **arena/01a06f0e-lowuuuuuu**. That push starts the build.

After it is installed, rebuild without editing files:

```bash
gh workflow run build-mcsm.yml --repo Loganwall111/Lowuuuuuu --ref arena/01a06f0e-lowuuuuuu
```

Download the **mcsm-jar-…** artifact from the successful run. The original
[linked run](https://github.com/Loganwall111/Lowuuuuuu/actions/runs/33930633043)
failed. A subsequent [green run](https://github.com/Loganwall111/Lowuuuuuu/actions/runs/33933807480)
explicitly reported a Java compilation failure and a shaders-only fallback;
its green status is **not proof of a successful Java compilation**.

## Local Linux / WSL build

Install JDK **25 or newer**, Python **3.9 or newer**, `curl`, `unzip` and `zip`.
The checked-in shader validator is a Linux x86-64 executable. Then:

```bash
bash ci/build.sh            # VERSION, currently 1.9.101
bash ci/build.sh 1.9.102     # optional explicit version
```

The script works from any working directory. It prefers `$JAVA_HOME/bin`,
otherwise `javac` on `PATH`. Set `MCSM_BASE_JAR` to select a particular base
JAR; otherwise it uses the numerically newest JAR in `delivery/`.

On Windows, `ci/build.ps1` runs this same build inside **WSL**, rather than
maintaining a second, divergent compilation recipe. Install the listed tools
in WSL (a Windows-only JDK does not satisfy that requirement).

Output:

- `out/dabywitherstormmod-1.9.101-26.2-beta-mcsm.jar`
- The adjacent `.jar.sha256` (verify from `out/` with `sha256sum -c *.sha256`)
- `out/BUILD_INFO.txt`, `out/javac.log` and `out/glsl.log`

Dependencies and temporary compilation files are cached under `.cache/mcsm/`
and are not committed. Minecraft's client and Java library versions come
from its official **26.2** manifest, with SHA-1 verification. Mixin comes from
Fabric's Maven repository. No stale hardcoded client URL is used as a fallback.

## Success criteria

A build is successful only when all of these pass:

1. The shader syntax gate (42 translation units).
2. `javac --release 25` for **every** `mcsm-extras/java` source.
3. Every source has a fresh Java 25 class in the assembled JAR, byte-for-byte.
4. All registered mixins exist and all shader/texture overrides match source.
5. The assembled ZIP passes its integrity check.

A compiler failure exits nonzero and **never** assembles a shaders-only JAR.
A repeat failure removes any stale output with the same version. Compiler
errors are also emitted as a GitHub Checks annotation, so they remain readable
when an environment cannot download Actions logs.

Each valid JAR includes `META-INF/mcsm-build.json` recording the source
commit, source/base/class hashes and the compiler version. This is build
verification, **not** an in-game rendering or runtime Mixin test.

Run the offline build-safety tests with:

```bash
python3 -m unittest discover -s ci/tests -v
```
