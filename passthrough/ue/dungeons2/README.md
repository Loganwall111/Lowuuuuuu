# SIFT Bridge — Dungeons II side (host)

Two routes, same protocol (`../../PROTOCOL.md`):

| Route | What it is | When |
| --- | --- | --- |
| `lua/SiftBridge/main.lua` | UE4SS **Lua** mod: WS server on `127.0.0.1:8080`, mirrors control rotation | Fastest to try; proves the vector path |
| `src/SiftBridgeModule.{h,cpp}` | UE4SS **C++** mod: same link + read-only mapping of `Local\SiftBridgeFrame` for depth compositing | Full passthrough |
| `../reshade/` | ReShade add-on (engine-agnostic compositor) | Compositing without touching UE internals |

## Install (Windows, your own copy of Minecraft Dungeons II)

1. Install [RE-UE4SS](https://github.com/UE4SS-RE/RE-UE4SS) next to the shipping exe
   (`dwmapi.dll` + `ue4ss/` folder).
2. **Lua route:** copy `lua/SiftBridge/` to `ue4ss/Mods/`, add the line from
   `config/mods.txt` to `ue4ss/Mods/mods.txt`.
3. **C++ route:** build against the UE4SS C++ template, drop the DLL in
   `ue4ss/Mods/SiftBridge/dlls/main.dll`.
4. Launch Dungeons II, then launch Minecraft with the SIFT Bridge Fabric mod
   (`..\..\mc`). Either game can start first — Minecraft keeps reconnecting.
5. **ESC** (or F8 in Minecraft) detaches the bridge: the listener stops, the mapping is
   unmapped, the socket closes. No files are left behind.

## Verify before trusting the camera code

The engine calls (`SetControlRotation`, player-controller lookup) are stock UE, but confirm the
exact class paths against a **UE4SS Live View** dump of your Dungeons II build — internal build
`Spicewood` may rename gameplay classes. Nothing here writes game files; the worst case of a
wrong path is a no-op camera.

## Coordinate mapping

```
UE.x = MC.x        UE.y = MC.z        UE.z = MC.y        (1 block = 1 metre = 100 UE units in shipping scale)
UE yaw = 180 − MC yaw
```

## Safety

* Loopback-only socket, read-only file mapping — the bridge can't corrupt a save even in theory.
* Never run this alongside EAC-enabled modes; Dungeons II co-op sessions are fine offline/lan,
  but keep the bridge to your own sessions.
