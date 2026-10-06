compositor.cpp, compositor.h and SiftBridge.fx are adapted from
universal-modder/examples/minecraft-gta5-passthrough (gta/src, gta/shaders),
MIT — Copyright Rehan and universal-modder contributors.
Changes: shared-memory name → Local\SiftBridgeFrame, magic → 0x46524253 ("SBRF"), effect name.
The ReShade add-on route is engine-agnostic: it composites the guest frame against the host's
reversed-Z depth buffer, so it works for Dungeons II exactly as it worked for GTA V.
