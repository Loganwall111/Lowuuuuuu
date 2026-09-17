#!/usr/bin/env python3
"""Generate the phase-6 storm halo ring texture (256x256 RGBA).

A soft annulus: empty centre (the blob and the storm own that), a bright
violet-pink ring that lands on the storm's SIDES, fading out before the
quad edge so the halo never extends past the main storm's silhouette.
Written with the repo's own make_branding.write_png (flat pixel list).
"""
import math
import sys

sys.path.insert(0, "ci")
from make_branding import write_png  # noqa: E402

W = H = 256
CX = CY = 127.5

px = []
for y in range(H):
    for x in range(W):
        dx = (x - CX) / 127.5
        dy = (y - CY) / 127.5
        r = math.sqrt(dx * dx + dy * dy)
        # ring: 0 at centre, peak around 0.62, gone by 0.98
        inner = max(0.0, min(1.0, (r - 0.30) / 0.22))
        outer = max(0.0, min(1.0, (0.98 - r) / 0.30))
        a = inner * inner * (3 - 2 * inner) if inner > 0 else 0.0
        a *= outer * outer * (3 - 2 * outer) if outer > 0 else 0.0
        # violet inside the ring, pink toward its outer edge
        t = max(0.0, min(1.0, (r - 0.35) / 0.55))
        cr = int(176 + (232 - 176) * t)
        cg = int(106 + (160 - 106) * t)
        cb = int(232 + (200 - 232) * t)
        px.append((cr, cg, cb, int(a * 210)))

write_png(sys.argv[1], W, H, px)
print("halo ring written:", sys.argv[1])
