# SIFT Bridge — Minecraft Dungeons II × Minecraft passthrough

The "game inside a game" technique from those videos is a **passthrough mod**: two unmodified
engines run side by side; a mod on each side links them over a local socket and shared memory;
the host game composites the guest's frame (colour + depth) into its own picture, and feeds its
camera / ground back into the guest. We reverse-engineered the two reference implementations
(`rehan-remade/universal-modder` — Minecraft×GTA V, MIT; `chasmlol/SkyCraft` — Minecraft×Skyrim)
and rebuilt the pattern for **your stack: Fabric Minecraft 26.3 guest ↔ Unreal (Dungeons II) host**,
aimed at your Sift / rift obsession: the bridge pipes Wither-Storm presence bursts from your pack
into the host as Sift rift pulses.

> **Honest status** — this sandbox is headless Linux with no JDK, no Windows, no UE: the real
> games can only run on your machine. What lives here is (a) the full protocol, proven live by the
> Python harness below, (b) build-ready Fabric-mod source for Windows, and (c) the UE4SS + ReShade
> host modules. Nothing here touches any game install, and nothing in the pack is modified.

## Architecture

```
 Minecraft 26.3 + SIFT Bridge (Fabric, our mc/)          Dungeons II / any UE host
   CameraMixin ── crosshair vectors ──┐                        ue/dungeons2 (UE4SS)
   60 Hz cam / 20 Hz pos / explosion   │  ws://127.0.0.1:8080   WS server on 8080 ← listens
   + rift pulses ("sift")        ┌─────┴──────────────────────→ mirror camera (smoothed)
                               │                                     │
   FrameExporter ── world RGBA + depth + HUD overlay ──┐              │
   named shared memory "Local\SiftBridgeFrame" (ring,  │── read-only ─┘
   3 slots, seqlock)                                   └→ ReShade add-on composites it
                                                        depth-tested vs the host's reversed-Z buffer
```

Two transports, exactly like upstream: **WebSocket JSON** for vectors/control (small, chatty),
**shared memory** for pixels (huge, zero-copy). See `PROTOCOL.md` for the byte-level layout.

## Reverse-engineered protocol summary (what you asked for)

* `universal-modder` (GTA×MC): host ↔ guest control is JSON over `127.0.0.1` WebSocket; frames go
  through a named Win32 file mapping (`CreateFileMappingW`, 4 KiB header + 3-slot ring of
  RGBA8 colour / float32 depth / RGBA8 overlay, seqlock `seq` odd-while-writing). Depth is
  compositing-critical: the host re-projects the guest colour against its own reversed-Z depth.
* `SkyCraft`: one big single mapping with header/state regions plus rings for input, collision,
  events, actors (struct offsets in `Proto.java`), so both SKSE and Fabric read/write the same
  schema. Minecraft runs hidden; the host draws everything.
* **Our SIFT variant:** we invert the socket topology the user specified — the **host listens on
  8080**, Minecraft connects and broadcasts its crosshair vectors (`cam`, `pos`) — same JSON
  shapes as upstream so tools stay compatible; frame ring re-magicked to `SBRF`.

## Layout

| Path | What |
| --- | --- |
| `PROTOCOL.md` | wire + shm schema (the contract) |
| `mc/` | Fabric guest mod source (JDK 25 / Loom; `./gradlew build` on Windows) |
| `ue/dungeons2/` | UE4SS host module: `lua/` quick path (no build) + `src/` C++ full path |
| `ue/reshade/` | engine-agnostic ReShade compositor adapted from upstream (MIT, see NOTICE.md) |
| `harness/` | Python stand-ins for both games — the **live proof** in this sandbox |
| `scripts/run_demo.sh` | one-command demo |
| `assets/` | your incoming payload lands here — excluded from git |

## Run it here (already running in this session)

```
scripts/run_demo.sh        # fake host + fake guest, real protocol
# preview UI:  http://0.0.0.0:8081     (composited scene + guest frame + live stats)
python3 harness/tests/test_bridge.py   # deterministic end-to-end checks
```

The test proves: hello → ≥30 cam packets @60 Hz → pos @20 Hz → rift pulses → shm frame
round-trip through the seqlock reader → ESC kill switch (`key escape` → `bye` → ring unlinked).

## Run it with the real games (your Windows machine)

1. **Guest:** JDK 25 + `cd passthrough/mc && ./gradlew build`; drop
   `build/libs/siftbridge-0.1.0.jar` into a **26.3 Fabric** instance's `mods/` (separate instance
   from the Forge pack — see §pack).
2. **Host, quick path:** install UE4SS for Dungeons II, copy `ue/dungeons2/lua/SiftBridge` to
   `ue4ss/Mods/`, enable in `mods.txt`. Camera rotation mirroring works immediately.
3. **Host, full path:** build `ue/dungeons2/src` against the UE4SS C++ template, or skip UE
   internals entirely and use `ue/reshade/` (ReShade add-on) which composites the frame ring
   against any reversed-Z game.
4. Launch either game first; the guest keeps reconnecting. **ESC / F8** detaches everything.

## §pack — coexistence with the Wither Storm pack

This checkout is a **Forge** pack (Cracker's-style Wither Storm mod, Forge + CEM/OGS assets);
SIFT Bridge targets **Fabric** 26.3, so they cannot share one instance. That is fine and safe:
the bridge instance carries the *same storm content* (or you wait for a Forge port — the bridge
core is ~600 lines and porting is mechanical). The pack's directories (`overrides/`, `mcsm-*`)
are left untouched; the bridge writes nothing outside its own socket, ring and daemon threads.

## Phase-3 safety envelope (binding)

* `127.0.0.1`-only sockets; no auth token because nothing off-machine can connect.
* No game/save/registry file is written by any component (read-only frame mapping on the host).
* Kill switch is distributed: local **F8**, host **ESC** → `key escape` → guest sends `bye`,
  unlinks `/dev/shm`/closes `Local\SiftBridgeFrame`, closes the socket; processes die with the game.
* Any external process is stopped by PID only, never by name pattern.

## Known limitations

* Java `FrameExporter` readback is the synchronous prototype path at 1080p (upstream's async
  GpuBuffer ring is the production upgrade); host-side frame-level chase cam is a later revision.
* UE class paths in the C++ module must be confirmed against a Live View dump of your build.
* Dungeons II online/co-op with anti-cheat: don't. Bridge is for your own sessions.

## Credits

Architecture, mixin targets and shm design after `rehan-remade/universal-modder`
(examples/minecraft-gta5-passthrough) and `chasmlol/SkyCraft` — both MIT; see `NOTICE`.
