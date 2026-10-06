#!/usr/bin/env python3
"""Fake host — a Minecraft Dungeons II stand-in for the sandbox.

Implements the UE4SS module's job in miniature (PROTOCOL.md):
  * WebSocket **server** on 127.0.0.1:8080 — the guest (real or fake) connects to us.
  * Mirrors the incoming crosshair/camera vectors with smoothing (what D2's camera rig does).
  * Reads the shared-memory frame ring exactly like the production seqlock reader.
  * Software-composites a 'Sift' scene with the guest frame embedded, and serves it as a
    live preview over HTTP on 0.0.0.0:8081 (that's the stand-in for the ReShade add-on).
"""
from __future__ import annotations

import argparse
import asyncio
import json
import math
import os
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import pngutil
import shmframes
import wsproto

W, H = 480, 270
GUEST_W, GUEST_H = 192, 108
GUEST_X, GUEST_Y = 12, 12


def lerp_angle(a: float, b: float, t: float) -> float:
    d = ((b - a + 180.0) % 360.0) - 180.0
    return a + d * t


class HostState:
    def __init__(self):
        self.connected = False
        self.guest_info = None
        self.latest_cam = None
        self.cams = self.pos = self.rifts = self.explosions = 0
        self.smooth_yaw = None
        self.smooth_pitch = None
        self.rift_events: list[tuple[float, list]] = []   # (time, pos)
        self.bye = asyncio.Event()
        self.guest_conn = None                            # writer, for control messages
        self.ring_frame = None
        self.frames_read = 0

    def mirror(self, cam: dict) -> None:
        yaw, pitch = cam["r"][0], cam["r"][1]
        if self.smooth_yaw is None:
            self.smooth_yaw, self.smooth_pitch = yaw, pitch
        else:
            self.smooth_yaw = lerp_angle(self.smooth_yaw, yaw, 0.25)
            self.smooth_pitch += (pitch - self.smooth_pitch) * 0.25


class Compositor:
    """The stand-in for MCPassthrough.fx / SiftBridge.fx: host scene + guest frame in one image."""

    def __init__(self, state: HostState):
        self.s = state

    def render(self) -> bytes:
        s = self.s
        rgb = bytearray(W * H * 3)
        yaw = s.smooth_yaw or 0.0
        pitch = s.smooth_pitch or 0.0

        # ---- the Sift: storm-lit sky over a dark plain
        horizon = int(H * 0.62 - pitch * 1.2)
        for y in range(H):
            row = y * W * 3
            if y < horizon:
                k = y / max(horizon, 1)
                r = int(28 + 54 * k + 20 * math.sin(time.monotonic() * 1.3 + k * 6))
                g = int(8 + 14 * k)
                b = int(46 + 72 * k)
                for x in range(W):
                    i = row + x * 3
                    rgb[i:i + 3] = bytes((max(0, min(r, 255)), g, b))
            else:
                for x in range(W):
                    grid = ((x + int(yaw)) // 24 + (y - horizon) // 14) & 1
                    v = 30 if grid else 22
                    i = row + x * 3
                    rgb[i:i + 3] = bytes((v, v + 6, v + 2))

        # ---- rift pulses from guest explosion events
        now = time.monotonic()
        s.rift_events = [e for e in s.rift_events if now - e[0] < 4.0]
        for t0, pos in s.rift_events:
            age = now - t0
            cx = int((pos[0] * 3.0 - yaw * 2.0) % W)
            cy = max(8, horizon - 24)
            rad = int(10 + age * 26)
            strength = max(0.0, 1.0 - age / 4.0)
            for y in range(max(0, cy - rad), min(H, cy + rad)):
                for x in range(max(0, cx - rad), min(W, cx + rad)):
                    d = math.hypot(x - cx, y - cy)
                    if d < rad:
                        f = (1.0 - d / rad) * strength
                        i = (y * W + x) * 3
                        rgb[i] = min(255, rgb[i] + int(150 * f))
                        rgb[i + 1] = min(255, rgb[i + 1] + int(40 * f))
                        rgb[i + 2] = min(255, rgb[i + 2] + int(190 * f))

        # ---- the guest's Minecraft frame, depth-composited 'in front' (here: inset window)
        f = s.ring_frame
        gx, gy = GUEST_X, GUEST_Y
        if f is not None and f.width and f.height:
            for y in range(GUEST_H):
                sy = min(f.height - 1, y * f.height // GUEST_H)
                for x in range(GUEST_W):
                    sx = min(f.width - 1, x * f.width // GUEST_W)
                    si = (sy * f.width + sx) * 4
                    di = ((gy + y) * W + (gx + x)) * 3
                    rgb[di:di + 3] = f.world_rgba[si:si + 3]
        else:
            for y in range(GUEST_H):
                for x in range(GUEST_W):
                    v = 40 + 30 * (((x + int(yaw)) // 16 + y // 16) & 1)
                    di = ((gy + y) * W + (gx + x)) * 3
                    rgb[di:di + 3] = bytes((v // 2, v, v // 2 + 10))

        # ---- guest-window chrome
        for x in range(gx - 2, gx + GUEST_W + 2):
            for (yy, c) in ((gy - 2, (200, 120, 255)), (gy + GUEST_H + 1, (200, 120, 255))):
                if 0 <= yy < H and 0 <= x < W:
                    i = (yy * W + x) * 3
                    rgb[i:i + 3] = bytes(c)

        # ---- HUD bars (health / presence) bottom-left
        for k, c in enumerate(((180, 30, 40), (120, 40, 190))):
            bw = 90 - k * 24
            yy = H - 16 + k * 7
            for x in range(12, 12 + bw):
                i = (yy * W + x) * 3
                rgb[i:i + 3] = bytes(c)
        return pngutil.encode_rgb(W, H, rgb)


class FakeHost:
    def __init__(self, port: int = 8080, preview_port: int = 8081):
        self.port, self.preview_port = port, preview_port
        self.state = HostState()
        self.comp = Compositor(self.state)
        self.png = self.comp.render()
        self.frame_tick = 0

    # ------------------------------------------------------------------ WS link

    async def handle_guest(self, reader, writer):
        s = self.state
        if s.connected:
            writer.close()
            return
        await wsproto.server_handshake(reader, writer)
        s.connected = True
        s.guest_conn = writer
        print("[host] guest connected", flush=True)
        try:
            while True:
                msg = await wsproto.read_message(reader)
                if msg is None:
                    break
                try:
                    m = json.loads(msg)
                except json.JSONDecodeError:
                    continue
                t = m.get("t")
                if t == "hello":
                    s.guest_info = m
                    print(f"[host] hello from pid {m.get('pid')} (shm {m.get('shm')})", flush=True)
                elif t == "cam":
                    s.latest_cam = m
                    s.cams += 1
                    s.mirror(m)
                elif t == "pos":
                    s.pos += 1
                elif t == "explosion":
                    s.explosions += 1
                elif t == "rift":
                    s.rifts += 1
                    s.rift_events.append((time.monotonic(), m.get("pos", [0, 0, 0])))
                elif t == "bye":
                    print("[host] guest said bye — clean shutdown", flush=True)
                    s.bye.set()
                    break
        except (ConnectionError, asyncio.IncompleteReadError):
            pass
        s.connected = False
        s.guest_conn = None
        s.latest_cam = None
        print("[host] guest disconnected", flush=True)

    async def send_control(self, obj: dict) -> None:
        w = self.state.guest_conn
        if w is not None:
            await wsproto.send_text(w, json.dumps(obj), mask=False)

    # ------------------------------------------------------------------ frame ring + preview

    async def frame_loop(self):
        reader = None
        while True:
            try:
                if reader is None and os.path.exists(shmframes.NAME):
                    try:
                        reader = shmframes.RingReader()
                        print("[host] frame ring attached", flush=True)
                    except RuntimeError:
                        reader = None
                if reader is not None:
                    f = reader.latest()
                    if f is not None:
                        self.state.ring_frame = f
                        self.state.frames_read += 1
                    if not os.path.exists(shmframes.NAME):
                        reader.close()
                        reader = None
            except (ValueError, BufferError, OSError):
                reader = None
            await asyncio.sleep(0.05)

    async def render_loop(self):
        while True:
            self.png = self.comp.render()
            self.frame_tick += 1
            await asyncio.sleep(0.1)

    # ------------------------------------------------------------------ preview HTTP

    async def handle_http(self, reader, writer):
        try:
            req = (await asyncio.wait_for(reader.readuntil(b"\r\n\r\n"), 5)).decode("latin-1", "replace")
        except (asyncio.IncompleteReadError, asyncio.TimeoutError, ConnectionError):
            writer.close()
            return
        path = req.split(" ", 2)[1] if " " in req else "/"
        if path.startswith("/frame.png"):
            body = self.png
            head = (f"HTTP/1.1 200 OK\r\nContent-Type: image/png\r\nContent-Length: {len(body)}\r\n"
                    "Cache-Control: no-store\r\nAccess-Control-Allow-Origin: *\r\nConnection: close\r\n\r\n")
        elif path.startswith("/stats"):
            s = self.state
            body = json.dumps({
                "connected": s.connected, "guest": s.guest_info,
                "cam_messages": s.cams, "pos_messages": s.pos,
                "rift_events": s.rifts, "explosions": s.explosions,
                "shm_frames_read": s.frames_read,
                "smoothed_camera": {"yaw": s.smooth_yaw, "pitch": s.smooth_pitch},
                "latest_cam": s.latest_cam,
            }).encode()
            head = (f"HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: {len(body)}\r\n"
                    "Access-Control-Allow-Origin: *\r\nConnection: close\r\n\r\n")
        else:
            body = PAGE.encode()
            head = (f"HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length: {len(body)}\r\n"
                    "Connection: close\r\n\r\n")
        writer.write(head.encode() + body)
        await writer.drain()
        writer.close()

    # ------------------------------------------------------------------ lifecycle

    async def serve(self):
        ws_server = await asyncio.start_server(self.handle_guest, "127.0.0.1", self.port)
        http_server = await asyncio.start_server(self.handle_http, "0.0.0.0", self.preview_port)
        print(f"[host] link on ws://127.0.0.1:{self.port} — preview on http://0.0.0.0:{self.preview_port}", flush=True)
        await asyncio.gather(
            ws_server.serve_forever(),
            http_server.serve_forever(),
            self.frame_loop(),
            self.render_loop(),
        )


PAGE = """<!doctype html><html><head><meta charset="utf-8">
<title>SIFT Bridge — passthrough preview</title>
<style>
 body{background:#0b0714;color:#d8c8f8;font-family:ui-monospace,monospace;margin:24px}
 h1{color:#c084fc;font-size:20px} img{border:2px solid #7c3aed;border-radius:6px;image-rendering:pixelated;width:960px;max-width:90vw}
 pre{background:#171027;padding:12px;border-radius:6px;font-size:12px;overflow:auto}
 .ok{color:#4ade80}.bad{color:#f87171}
</style></head><body>
<h1>SIFT Bridge &mdash; Minecraft Dungeons II &times; Minecraft passthrough (sandbox preview)</h1>
<p>Left window = the guest's frame ring (what D2's compositor would depth-test into the scene).
Background = the mirrored Sift scene driven by the guest's camera vector.</p>
<img id="f" src="/frame.png"><pre id="s">connecting…</pre>
<script>
async function tick(){
  document.getElementById('f').src='/frame.png?'+Date.now();
  try{
    const r=await fetch('/stats');const j=await r.json();
    const c=j.connected?'<span class=ok>guest connected</span>':'<span class=bad>guest offline</span>';
    document.getElementById('s').innerHTML=c+
      '\\ncam packets: '+j.cam_messages+'   pos packets: '+j.pos_messages+
      '\\nrift pulses: '+j.rift_events+'   explosions: '+j.explosions+
      '\\nshm frames read: '+j.shm_frames_read+
      '\\nsmoothed camera: '+JSON.stringify(j.smoothed_camera)+
      '\\nlatest cam: '+JSON.stringify(j.latest_cam);
  }catch(e){document.getElementById('s').textContent='stats: '+e}
}
setInterval(tick,500);tick();
</script></body></html>
"""


async def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--port", type=int, default=8080)
    ap.add_argument("--preview-port", type=int, default=8081)
    args = ap.parse_args()
    await FakeHost(args.port, args.preview_port).serve()


if __name__ == "__main__":
    asyncio.run(main())
