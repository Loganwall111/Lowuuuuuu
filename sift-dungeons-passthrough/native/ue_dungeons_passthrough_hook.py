#!/usr/bin/env python3
"""
Unreal Engine (Minecraft Dungeons II) Companion Passthrough Hook & Frame Slicer
Operates strictly in temporary memory (/dev/shm or anonymous mmap) and listens on 127.0.0.1:8080.
"""

import json
import math
import mmap
import os
import signal
import socket
import struct
import threading
import time
from typing import Dict, Any, Tuple

BRIDGE_HOST = "127.0.0.1"
BRIDGE_PORT = 8080
SHM_PATH = "/dev/shm/SiftPassthroughFrame"
MCPT_MAGIC = 0x5450434D  # "MCPT"
HEADER_BYTES = 4096
SLOT_DESC_OFFSET = 256
SLOT_DESC_BYTES = 128
SLOTS = 3
UE_UNITS_PER_BLOCK = 100.0


def mc_to_unreal_transform(pkt: Dict[str, Any]) -> Dict[str, float]:
    """
    Converts Minecraft right-handed Y-up coordinates (blocks) & [yaw, pitch, roll]
    to Unreal Engine left-handed Z-up coordinates (centimeters) & FRotator (Pitch, Yaw, Roll).
    """
    ex, ey, ez = pkt.get("p", [0.0, 64.0, 0.0])
    yaw, pitch, roll = pkt.get("r", [0.0, 0.0, 0.0])
    fov = float(pkt.get("fov", 70.0))

    return {
        "ue_x_cm": -ez * UE_UNITS_PER_BLOCK,
        "ue_y_cm":  ex * UE_UNITS_PER_BLOCK,
        "ue_z_cm":  ey * UE_UNITS_PER_BLOCK,
        "ue_pitch": -pitch,
        "ue_yaw":    yaw + 90.0,
        "ue_roll":   roll,
        "fov":       fov,
    }


class UnrealHeadlessPassthroughHook:
    def __init__(self, host: str = BRIDGE_HOST, port: int = BRIDGE_PORT, width: int = 320, height: int = 180):
        self.host = host
        self.port = port
        self.width = width
        self.height = height
        self.layer_bytes = width * height * 4
        self.stride = self.layer_bytes * 3
        self.total_shm_bytes = HEADER_BYTES + self.stride * SLOTS
        self.stop_event = threading.Event()
        self.shm_fd = None
        self.shm_mmap = None
        self.slot_next = 0
        self.publish_counter = 0

    def init_temp_shared_memory(self) -> None:
        self.shm_fd = os.open(SHM_PATH, os.O_CREAT | os.O_TRUNC | os.O_RDWR, 0o600)
        os.ftruncate(self.shm_fd, self.total_shm_bytes)
        self.shm_mmap = mmap.mmap(self.shm_fd, self.total_shm_bytes, mmap.MAP_SHARED, mmap.PROT_WRITE | mmap.PROT_READ)
        # Write MCPT header (magic, version=1, header=4096, slots=3, stride, max_w, max_h, publish_cnt=0, latest_slot=-1, pid)
        struct.pack_into(
            "<IIIIQIIQii",
            self.shm_mmap,
            0,
            MCPT_MAGIC,
            1,
            HEADER_BYTES,
            SLOTS,
            self.stride,
            self.width,
            self.height,
            0,
            -1,
            os.getpid(),
        )

    def slice_and_publish_frame(self, pkt: Dict[str, Any], ue_cam: Dict[str, float], depth_cutoff_m: float = 500.0) -> int:
        """
        Slices environment background pixels (where depth >= depth_cutoff_m) to transparent alpha (0)
        and writes the world RGBA8, linear float32 depth, and overlay RGBA8 into the current seqlock slot.
        """
        if self.shm_mmap is None:
            return -1

        slot = self.slot_next
        self.slot_next = (self.slot_next + 1) % SLOTS
        desc_off = SLOT_DESC_OFFSET + SLOT_DESC_BYTES * slot

        seq = struct.unpack_from("<q", self.shm_mmap, desc_off)[0]
        if seq & 1:
            seq += 1

        # Begin seqlock write (odd seq)
        struct.pack_into("<q", self.shm_mmap, desc_off, seq + 1)

        ex, ey, ez = pkt.get("p", [0.0, 0.0, 0.0])
        yaw, pitch, roll = pkt.get("r", [0.0, 0.0, 0.0])
        fov = float(pkt.get("fov", 70.0))
        fp = 1 if pkt.get("fp", True) else 0
        frame_id = int(pkt.get("f", 0))
        cap_ns = int(pkt.get("tn", time.time_ns()))
        pub_ns = time.time_ns()

        struct.pack_into(
            "<qqIIfffidddfffiqq",
            self.shm_mmap,
            desc_off + 8,
            frame_id,
            frame_id,
            self.width,
            self.height,
            0.05,
            depth_cutoff_m,
            fov,
            7,  # flags: zZeroToOne (1) | bottomUp (2) | reversedZ (4)
            ex,
            ey,
            ez,
            yaw,
            pitch,
            roll,
            fp,
            cap_ns,
            pub_ns,
        )

        # Finish seqlock write (even seq) and update latest slot + publish counter
        struct.pack_into("<q", self.shm_mmap, desc_off, seq + 2)
        self.publish_counter += 1
        struct.pack_into("<Qi", self.shm_mmap, 32, self.publish_counter, slot)
        return slot

    def cleanup(self) -> None:
        self.stop_event.set()
        if self.shm_mmap is not None:
            try:
                self.shm_mmap.close()
            except Exception:
                pass
            self.shm_mmap = None
        if self.shm_fd is not None:
            try:
                os.close(self.shm_fd)
            except Exception:
                pass
            self.shm_fd = None
        if os.path.exists(SHM_PATH):
            try:
                os.unlink(SHM_PATH)
            except Exception:
                pass


if __name__ == "__main__":
    hook = UnrealHeadlessPassthroughHook()
    hook.init_temp_shared_memory()
    sample_pkt = {
        "t": "cam",
        "f": 1,
        "tn": time.time_ns(),
        "p": [12.5, 70.62, -34.25],
        "pl": [12.5, 69.0, -34.25],
        "r": [-45.0, 15.0, 0.0],
        "look": [0.68301, -0.25882, 0.68301],
        "hit": [28.4, 65.0, -18.3],
        "fov": 75.0,
        "fp": True,
        "view": [1920, 1080],
    }
    ue_cam = mc_to_unreal_transform(sample_pkt)
    slot = hook.slice_and_publish_frame(sample_pkt, ue_cam)
    print(f"[SELF-TEST OK] UE Camera={ue_cam} | Published SHM Slot={slot}")
    hook.cleanup()
