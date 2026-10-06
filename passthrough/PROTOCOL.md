# SIFT Bridge — wire protocol v1

The passthrough link between **Minecraft Java** (guest: renders the world, exports frames)
and the **host game** (Minecraft Dungeons II / any UE title: owns the camera, composites).

Everything below is the schema reverse-engineered from
`universal-modder/examples/minecraft-gta5-passthrough` (MIT), adapted:

* transport is **WebSocket on `127.0.0.1:8080`** (configurable, never binds public interfaces),
* **the host game is the WebSocket server** — it listens on 8080 (this inverts upstream,
  where Minecraft served; here it matches the SIFT-Bridge requirement that the UE side listens),
* Minecraft is a **WebSocket client**: it connects on launch, keeps reconnecting, and streams
  crosshair/camera vectors out the moment a host is present,
* the host mirrors its camera from the stream and may send control messages back either way.

---

## 1. Control channel — WebSocket JSON, one object per frame

All messages are single-line JSON objects with a `"t"` discriminator.

### Host → Minecraft

| `t` | Fields | Meaning |
| --- | --- | --- |
| `cam` | `f`:u64 host frame, `p`:[x,y,z], `r`:[yaw,pitch,roll]°, `fov`: vertical degrees, `fp`: bool first-person, `ts`: monotonic ns | Drive MC's camera. `p`/`r` are Minecraft coordinates. |
| `ground` | `c`:[x,z,yBottom,yTop, …] | Solid columns of the host world → MC barrier blocks (collision floor). |
| `clear` | — | Remove all host barriers. |
| `key` | `k`: `use\|attack\|pick\|inventory\|drop\|swap\|escape`, `down`:bool | Inject a key press. `escape` is reserved for the kill-switch path. |
| `slot` | `n`:0–8 | Select hotbar slot. |
| `scroll` | `d`:±1 | Hotbar scroll. |
| `hud` | `hidden`:bool | Hide/show MC HUD layer. |
| `view` | `w`,`h`:px | Resize MC render target to the host's picture. |
| `mode` | `m`: `hosted\|free` | `hosted`: MC mirrors host camera. `free`: MC plays normally, only telemetry flows out. |

### Minecraft → Host

| `t` | Fields | Meaning |
| --- | --- | --- |
| `hello` | `v`:1, `shm`: mapping name, `pid`: int, `mc`: version string | Sent on connect. `shm` names the frame ring (`Local\SiftBridgeFrame`). |
| `cam` | `f`:u64 mc frame, `p`:[x,y,z], `r`:[yaw,pitch,roll]°, `fov`, `fp`, `ts` | **The telemetry stream** — MC's own camera, 60 Hz (every render frame), even in `free` mode. This is what Phase-2 asked for: crosshair vectors out without touching the render tick. |
| `pos` | `p`:[x,y,z], `v`:[vx,vy,vz] blocks/s, `yaw`, `bodyYaw`, `onGround`:bool, `ts` | Player pose each client tick (20 Hz). |
| `explosion` | `pos`:[x,y,z], `r`: radius | A TNT/creeper/storm event happened in MC. |
| `rift` | `pos`:[x,y,z], `r`:strength, `kind`:`sift` | Presence spike (Wither-Storm-style presence → Sift rift pulse). |
| `bye` | — | Clean shutdown (ESC path). Sent before the socket closes. |

Coordinates: **1 block = 1 metre**. Host→MC mapping (UE is Z-up):
`MC.x = UE.x`, `MC.z = UE.y`, `MC.y = UE.z` (with a per-session origin offset), `yaw = 180 − heading`.
The origin offset is carried in the first `cam` message (`"origin":[x,y,z]`, optional).

## 2. Frame channel — shared memory ring, zero-copy

High-bandwidth pixels never go through the socket. They go through a **named shared-memory
mapping**, exactly like upstream's `Local\MCPassthroughFrame`:

* **Windows (real game path):** pagefile-backed `CreateFileMappingW` named `Local\SiftBridgeFrame`.
  Nothing touches disk. Destroyed automatically when Minecraft exits; we also unlink on ESC.
* **Linux (sandbox/harness path):** a `memfd`/tmpfs file at `/dev/shm/SiftBridgeFrame` with the
  identical binary layout, so the same reader code works on both.

### Layout (little-endian)

```
header (4096 B)
   0  int  magic   "SBRF" (0x46524253)
   4  int  version (1)
   8  int  header bytes (4096)
  12  int  slot count (3)
  16  long slot stride (3 * MAX_W * MAX_H * 4)
  24  int  max width  (3840)
  28  int  max height (2160)
  32  long publish counter          (bumped after each completed slot)
  40  int  latest slot              (-1 = none yet)
  44  int  publisher pid

slot descriptors at 256 + 128*i, each 128 B:
   0  long seq (odd while the slot is being written — readers skip odd)
   8  long mc frame counter
  16  int  width      20 int height
  24  float near (0.05)   28 float far   32 float vertical fov (deg)
  36  int  flags: 1 = depth in [0,1] (zZeroToOne), 2 = rows bottom-up, 4 = reversed Z (1 = near)
  40  double cam x   48 y   56 z
  64  float yaw  68 pitch  72 roll   76 int firstPerson
  80  long capture ns   88 long publish ns

slot data at 4096 + stride*i, three contiguous layers of width*height*4 bytes:
  world colour  RGBA8 (premultiplied alpha)   ← copied just before MC draws the hand
  world depth   float32 raw depth-buffer      ← lets the host depth-test MC into its scene
  overlay       RGBA8 (hand + HUD)            ← composited on top of everything
```

The seqlock (`seq` odd during write, re-checked after copy) means readers never need a lock and
never see a torn frame. Three slots = publisher never blocks on a slow reader.

### Depth decode (for the host compositor)

With reversed Z (`flags & 4`) and `[0,1]` range (`flags & 1`) — the UE5/GTA convention:

```
view_z = near * far / (near + d * (far - near))     (d <= 0  ⇒  empty sky)
```

## 3. Safety envelope (binding for all components)

* Sockets bind **`127.0.0.1` only**, no auth token needed because nothing off-machine can connect.
* No game file, save, or registry entry is ever written. The bridge is sidecar code + RAM only.
* Kill switch: **ESC** (or `bye`) → guest sends `bye`, closes the socket, unlinks the mapping,
  daemon threads die with the process. Host does the same on its ESC hook.
* Any process is stopped **by PID only**, never by name pattern.
