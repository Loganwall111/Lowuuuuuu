"""Reader + writer for the SIFT Bridge frame ring (PROTOCOL.md §2), pure stdlib.

This is the Linux/tmpfs twin of the production ring:
  * Java guest   → SharedMemory.createPosix()   → /dev/shm/SiftBridgeFrame
  * UE4SS module → OpenFileMappingA("Local\\SiftBridgeFrame") on Windows
The binary layout is identical; only the OS object differs.

    ring = RingWriter(max_w=256, max_h=256)      # fake guest side
    ring.publish(width, height, world, depth, overlay, pose)

    rd = RingReader()                            # fake host side
    f = rd.latest()   # → Frame or None, seqlock-safe
"""
from __future__ import annotations

import mmap
import os
import struct
import time
from dataclasses import dataclass

NAME = "/dev/shm/SiftBridgeFrame"
MAGIC = 0x46524253  # "SBRF"
VERSION = 1
HEADER = 4096
SLOTS = 3
SLOT_DESC = 256
SLOT_DESC_BYTES = 128


class RingWriter:
    def __init__(self, path: str = NAME, max_w: int = 256, max_h: int = 256):
        self.max_w, self.max_h = max_w, max_h
        self.layer_max = max_w * max_h * 4
        self.stride = self.layer_max * 3
        self.size = HEADER + self.stride * SLOTS
        self.path = path
        self.f = open(path, "wb+")
        self.f.truncate(self.size)
        self.mm = mmap.mmap(self.f.fileno(), self.size)
        struct.pack_into("<iiiiqii", self.mm, 0,
                         MAGIC, VERSION, HEADER, SLOTS, self.stride, max_w, max_h)
        struct.pack_into("<i", self.mm, 40, -1)               # latest slot: none yet
        struct.pack_into("<i", self.mm, 44, os.getpid())
        self.publish_counter = 0
        self.frame = 0

    def publish(self, width: int, height: int, world: bytes, depth: bytes, overlay: bytes,
                cam: tuple[float, float, float], rot: tuple[float, float, float],
                fov: float, near: float, far: float, first_person: bool = True) -> None:
        assert len(world) == len(overlay) == width * height * 4 and len(depth) == width * height * 4
        self.frame += 1
        slot = self.frame % SLOTS
        d = SLOT_DESC + SLOT_DESC_BYTES * slot
        base = HEADER + self.stride * slot
        n = width * height * 4

        struct.pack_into("<q", self.mm, d, (self.publish_counter << 1) | 1)      # busy (odd)
        struct.pack_into("<q", self.mm, d + 8, self.frame)
        struct.pack_into("<ii", self.mm, d + 16, width, height)
        struct.pack_into("<fff", self.mm, d + 24, near, far, fov)
        struct.pack_into("<i", self.mm, d + 36, 1 | 4)                            # [0,1] + reversed Z
        struct.pack_into("<ddd", self.mm, d + 40, *cam)
        struct.pack_into("<fffi", self.mm, d + 64, *rot, 1 if first_person else 0)
        struct.pack_into("<q", self.mm, d + 80, time.time_ns())

        self.mm[base:base + n] = world
        self.mm[base + n:base + 2 * n] = depth
        self.mm[base + 2 * n:base + 3 * n] = overlay

        self.publish_counter += 1
        struct.pack_into("<q", self.mm, d + 96, time.time_ns())
        struct.pack_into("<q", self.mm, 32, self.publish_counter)                 # header counter
        struct.pack_into("<i", self.mm, 40, slot)                                 # latest slot
        struct.pack_into("<q", self.mm, d, self.publish_counter << 1)             # done (even)

    def release(self) -> None:
        try:
            self.mm[:] = b"\0" * self.size
        except (ValueError, BufferError):
            pass
        self.mm.close()
        self.f.close()
        try:
            os.unlink(self.path)
        except FileNotFoundError:
            pass


@dataclass
class Frame:
    slot: int
    mc_frame: int
    width: int
    height: int
    near: float
    far: float
    fov: float
    flags: int
    cam: tuple[float, float, float]
    rot: tuple[float, float, float]
    first_person: bool
    world_rgba: bytes      # top-down rows, premultiplied RGBA
    world_depth: bytes     # float32 raw depth
    overlay_rgba: bytes


class RingReader:
    def __init__(self, path: str = NAME):
        self.path = path
        probe = os.open(path, os.O_RDONLY)
        try:
            header_probe = os.pread(probe, HEADER, 0)
        finally:
            os.close(probe)
        magic, version, header, slots, stride, maxw, maxh = struct.unpack_from("<iiiiqii", header_probe, 0)
        if magic != MAGIC:
            raise RuntimeError("no SIFT frame ring (is the guest running with a host attached?)")
        self.slots, self.stride, self.header = slots, stride, header
        self.f = open(path, "rb")
        self.mm = mmap.mmap(self.f.fileno(), header + stride * slots, prot=mmap.PROT_READ)

    def published(self) -> int:
        return struct.unpack_from("<q", self.mm, 32)[0]

    def latest(self) -> Frame | None:
        for _ in range(4):                                   # seqlock retry
            slot = struct.unpack_from("<i", self.mm, 40)[0]
            if slot < 0:
                return None
            d = SLOT_DESC + SLOT_DESC_BYTES * slot
            seq = struct.unpack_from("<q", self.mm, d)[0]
            if seq & 1:
                continue
            (mc_frame, w, h, near, far, fov, flags, cx, cy, cz,
             yaw, pitch, roll, fp, cap, pub) = struct.unpack_from("<qiifffidddfffiqq", self.mm, d + 8)
            n = w * h * 4
            base = self.header + self.stride * slot
            world = self.mm[base:base + n]
            depth = self.mm[base + n:base + 2 * n]
            overlay = self.mm[base + 2 * n:base + 3 * n]
            if struct.unpack_from("<q", self.mm, d)[0] != seq:
                continue                                     # torn: try again
            return Frame(slot, mc_frame, w, h, near, far, fov, flags, (cx, cy, cz),
                         (yaw, pitch, roll), bool(fp), bytes(world), bytes(depth), bytes(overlay))
        return None

    def close(self) -> None:
        self.mm.close()
        self.f.close()


def depth_to_metres(raw: bytes, width: int, height: int, near: float, far: float, flags: int) -> list[float]:
    """Reverse the reversed-Z [0,1] depth to view-space metres (PROTOCOL.md §2)."""
    vals = struct.unpack(f"<{width * height}f", raw)
    out = []
    for d in vals:
        if flags & 4:                                        # reversed Z
            z = near * far / (near + d * (far - near)) if d > 0 else float("inf")
        else:
            z = near * far / (far - d * (far - near)) if d < 1 else float("inf")
        out.append(z)
    return out
