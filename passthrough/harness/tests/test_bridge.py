#!/usr/bin/env python3
"""End-to-end proof of the SIFT Bridge link, fully inside the sandbox:

  fake guest (Minecraft stand-in)  →  ws://127.0.0.1:8080  →  fake host (D2 stand-in)
       └─ frame ring /dev/shm/SiftBridgeFrame ──────────────→  seqlock reader → compositor

Verifies: hello, 60 Hz cam stream, 20 Hz pos, rift/explosion events, shm frame round-trip
(magic/layout/seqlock), and the ESC kill switch (bye + unlink). Stdlib only.
"""
from __future__ import annotations

import asyncio
import os
import sys
import time

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
import shmframes
from fake_guest import Guest
from fake_host import FakeHost

PORT, PREVIEW = 8080, 8091


async def wait_for(cond, timeout=12.0, what=""):
    t0 = time.monotonic()
    while time.monotonic() - t0 < timeout:
        if cond():
            return
        await asyncio.sleep(0.05)
    raise AssertionError(f"timed out waiting for {what}")


async def main():
    # clean slate
    if os.path.exists(shmframes.NAME):
        os.unlink(shmframes.NAME)

    host = FakeHost(PORT, PREVIEW)
    ws_server = await asyncio.start_server(host.handle_guest, "127.0.0.1", PORT)
    loops = [asyncio.create_task(host.frame_loop()), asyncio.create_task(host.render_loop())]

    guest_task = asyncio.create_task(Guest().run(once=True))

    s = host.state
    await wait_for(lambda: s.guest_info is not None, what="hello")
    assert s.guest_info["shm"] == "Local\\SiftBridgeFrame", "hello must carry the ring name"
    print("ok  hello:", s.guest_info)

    await wait_for(lambda: s.cams >= 30, what="30+ cam packets")
    await wait_for(lambda: s.pos >= 5, what="5+ pos packets")
    await wait_for(lambda: s.rifts >= 1 and s.explosions >= 1, what="rift pulse")
    print(f"ok  telemetry: {s.cams} cam, {s.pos} pos, {s.rifts} rifts, {s.explosions} explosions")

    # cam schema: frame counter, position triple, rotation triple, fov, fp, ts
    cam = s.latest_cam
    assert all(k in cam for k in ("f", "p", "r", "fov", "fp", "ts")), "cam packet incomplete"
    assert len(cam["p"]) == 3 and len(cam["r"]) == 3
    print("ok  cam schema:", {k: cam[k] for k in ("f", "fov", "fp")}, "p=", [round(v, 1) for v in cam["p"]])

    # camera mirroring tracked the stream
    assert s.smooth_yaw is not None and abs(((s.smooth_yaw - cam["r"][0] + 180) % 360) - 180) < 25
    print(f"ok  camera mirrored: smoothed yaw {s.smooth_yaw:.1f} vs latest {cam['r'][0]:.1f}")

    await wait_for(lambda: s.frames_read >= 3, what="3+ shm frames")
    f = s.ring_frame
    assert f.width == 64 and f.height == 64 and f.flags & 5 == 5, "ring frame layout mismatch"
    assert len(f.world_rgba) == 64 * 64 * 4 and len(f.world_depth) == 64 * 64 * 4
    zs = shmframes.depth_to_metres(f.world_depth[:8], 2, 1, f.near, f.far, f.flags)
    print(f"ok  shm round-trip: {f.width}x{f.height}, flags={f.flags}, depth→m sample {[round(z,2) for z in zs]}")

    # ESC kill switch: host sends key/escape → guest replies bye, unlinks the ring, exits
    await host.send_control({"t": "key", "k": "escape", "down": True})
    await wait_for(lambda: s.bye.is_set(), what="bye")
    await asyncio.wait_for(guest_task, timeout=5)
    await wait_for(lambda: not os.path.exists(shmframes.NAME), what="ring unlinked")
    print("ok  kill switch: bye received, /dev/shm/SiftBridgeFrame unlinked, guest exited")

    for t in loops:
        t.cancel()
    ws_server.close()
    await ws_server.wait_closed()
    print("\nALL BRIDGE CHECKS PASSED")


if __name__ == "__main__":
    asyncio.run(main())
