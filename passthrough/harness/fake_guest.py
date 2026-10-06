#!/usr/bin/env python3
"""Fake guest — a Minecraft stand-in for the sandbox.

Speaks exactly what the Fabric mod speaks (PROTOCOL.md): connects to the host's
ws://127.0.0.1:8080, sends hello, streams the crosshair/camera vector at 60 Hz,
poses at 20 Hz, publishes a synthetic frame ring to /dev/shm/SiftBridgeFrame,
fires explosion/rift events, and honours the ESC kill switch ({"t":"key","k":"escape"}
→ bye + unlink + exit). Run with --once for the test suite, or just leave it running.
"""
from __future__ import annotations

import argparse
import asyncio
import json
import math
import os
import signal
import struct
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import shmframes
import wsproto

HOST, PORT = "127.0.0.1", 8080
RING_W = RING_H = 64


def synth_world(t: float, yaw: float) -> tuple[bytes, bytes, bytes]:
    """A tiny blocky 'Minecraft' view: banded sky, dark ground, storm silhouette + crosshair overlay."""
    world = bytearray(RING_W * RING_H * 4)
    depth = bytearray(RING_W * RING_H * 4)
    overlay = bytearray(RING_W * RING_H * 4)
    horizon = RING_H // 2 + int(6 * math.sin(t * 0.7))
    for y in range(RING_H):
        for x in range(RING_W):
            i = (y * RING_W + x) * 4
            if y < horizon:
                b = 0.35 + 0.3 * (y / max(horizon, 1))
                world[i:i + 4] = bytes((int(70 * b), int(120 * b), int(190 * b), 255))
                depth[i:i + 4] = struct.pack("<f", 0.0)              # reversed Z: 0 = far
            else:
                band = ((x + int(yaw)) // 8 + y // 8) & 1
                g = 90 if band else 70
                world[i:i + 4] = bytes((g - 20, g + 25, g - 30, 255))
                d = 1.0 - min((y - horizon) / RING_H, 1.0) * 0.9    # near-ish ground
                depth[i:i + 4] = struct.pack("<f", d)
            # storm silhouette drifting with yaw
            sx = (x - int(yaw * 0.5)) % RING_W
            if y < horizon and 24 <= sx <= 40 and y >= horizon - 26 and (sx + y) % 3:
                world[i:i + 4] = bytes((18, 12, 24, 255))
    # overlay: crosshair + hotbar strip (premultiplied, so alpha-carrying pixels only)
    cx, cy = RING_W // 2, RING_H // 2
    for (dx, dy) in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1), (2, 0), (-2, 0), (0, 2), (0, -2)):
        i = ((cy + dy) * RING_W + (cx + dx)) * 4
        overlay[i:i + 4] = bytes((255, 255, 255, 255))
    for x in range(RING_W // 2 - 16, RING_W // 2 + 16):
        i = ((RING_H - 5) * RING_W + x) * 4
        overlay[i:i + 4] = bytes((40, 40, 40, 255))
    return bytes(world), bytes(depth), bytes(overlay)


class Guest:
    def __init__(self):
        self.ring = shmframes.RingWriter(max_w=RING_W, max_h=RING_H)
        self.t0 = time.monotonic()
        self.stop = asyncio.Event()
        self.stats = {"cam_sent": 0, "pos_sent": 0, "rifts": 0, "frames": 0, "host_msgs": 0}

    def pose(self, t: float):
        r = 24.0 + 4.0 * math.sin(t * 0.23)
        a = t * 0.31
        x, z = r * math.cos(a), r * math.sin(a)
        y = 68.0 + 3.0 * math.sin(t * 0.6)
        yaw = math.degrees(-a) + 90.0                    # facing along the orbit
        pitch = 8.0 * math.sin(t * 0.4)
        return (x, y, z), (yaw, pitch, 0.0)

    async def run(self, once: bool):
        while not self.stop.is_set():
            try:
                await self.session(once)
            except (ConnectionError, OSError) as e:
                print(f"[guest] link dropped ({e}); reconnecting in 2 s", flush=True)
            if once:
                break
            await asyncio.sleep(2)

    async def session(self, once: bool):
        reader, writer = await asyncio.wait_for(asyncio.open_connection(HOST, PORT), 4)
        await wsproto.client_handshake(reader, writer, HOST, PORT)
        await wsproto.send_text(writer, json.dumps({
            "t": "hello", "v": 1, "shm": "Local\\SiftBridgeFrame",
            "pid": os.getpid(), "mc": "26.3 (fake)"}), mask=True)
        print(f"[guest] connected to ws://{HOST}:{PORT}", flush=True)

        frame = 0
        last_pos = last_rift = last_shm = 0.0
        while not self.stop.is_set():
            now = time.monotonic()
            t = now - self.t0
            (x, y, z), (yaw, pitch, roll) = self.pose(t)
            frame += 1
            await wsproto.send_text(writer, json.dumps({
                "t": "cam", "f": frame, "p": [round(x, 4), round(y, 4), round(z, 4)],
                "r": [round(yaw, 2), round(pitch, 2), round(roll, 2)],
                "fov": 70.0, "fp": True, "ts": time.time_ns()}), mask=True)
            self.stats["cam_sent"] += 1

            if now - last_pos >= 0.05:                   # 20 Hz pos
                last_pos = now
                await wsproto.send_text(writer, json.dumps({
                    "t": "pos", "p": [x, y, z], "v": [0, 0, 0],
                    "yaw": yaw, "bodyYaw": yaw, "onGround": True, "ts": time.time_ns()}), mask=True)
                self.stats["pos_sent"] += 1

            if now - last_rift >= 3.0:                   # storm presence → rift pulse
                last_rift = now
                await wsproto.send_text(writer, json.dumps({
                    "t": "explosion", "pos": [x + 6, y - 2, z + 3], "r": 4.0}), mask=True)
                await wsproto.send_text(writer, json.dumps({
                    "t": "rift", "pos": [x + 6, y - 2, z + 3], "r": 1.0, "kind": "sift"}), mask=True)
                self.stats["rifts"] += 1

            if now - last_shm >= 0.1:                    # 10 Hz frame ring
                last_shm = now
                w, d, o = synth_world(t, yaw)
                self.ring.publish(RING_W, RING_H, w, d, o, (x, y, z), (yaw, pitch, roll),
                                  70.0, 0.05, 1024.0)
                self.stats["frames"] += 1

            # drain host messages without blocking the 60 Hz cadence
            try:
                msg = await asyncio.wait_for(wsproto.read_message(reader), 0.016)
            except asyncio.TimeoutError:
                msg = "__none__"
            if msg is None:
                raise ConnectionError("host closed")
            if msg != "__none__":
                self.stats["host_msgs"] += 1
                m = json.loads(msg)
                if m.get("t") == "key" and m.get("k") == "escape" and m.get("down"):
                    print("[guest] ESC kill switch from host — clean shutdown", flush=True)
                    await self.shutdown(writer)
                    return
        await self.shutdown(writer)

    async def shutdown(self, writer):
        try:
            await wsproto.send_text(writer, json.dumps({"t": "bye"}), mask=True)
            await wsproto.send_close(writer, mask=True)
        except (ConnectionError, RuntimeError):
            pass
        try:
            writer.close()
        except RuntimeError:
            pass
        self.ring.release()
        print("[guest] bye sent, ring unlinked", flush=True)


async def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--once", action="store_true", help="single session, no reconnect loop")
    args = ap.parse_args()
    guest = Guest()
    loop = asyncio.get_running_loop()
    for sig in (signal.SIGINT, signal.SIGTERM):
        loop.add_signal_handler(sig, guest.stop.set)
    await guest.run(args.once)
    print(f"[guest] final stats: {guest.stats}", flush=True)


if __name__ == "__main__":
    asyncio.run(main())
