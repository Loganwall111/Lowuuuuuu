# MCSM 1.9.101 compilation build

This build targets **Minecraft 26.2, Fabric Loader 0.19.3+, and Java 25+**.
Install Fabric API and the other dependencies required by your mod setup.
Remove older `dabywitherstormmod` JARs before placing the compiled JAR in `mods/`.

The build combines the Java sources and shader/texture changes from revision
`2a9f780` with Minecraft 26.2 GUI API compatibility fixes. It does not add the
unrelated, later feature work from other branches.

Unlike the earlier shaders-only 1.9.100 release, this build must successfully
compile all supplied Java sources before it can produce a JAR. Its embedded
`META-INF/mcsm-build.json` records the exact compiler, source and class hashes.
See the build's `BUILD_INFO.txt` and checksum for verification.

Compilation, class/resource integrity and shader syntax checks do not prove
in-game behavior. Runtime Mixin application and the visual effects still need
testing in Minecraft. Only install an artifact from a successful **1.9.101**
compilation run; this document is not itself evidence that a build has run.
