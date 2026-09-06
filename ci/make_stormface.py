#!/usr/bin/env python3
"""Generate the phase-5.5+ purple face overlay texture (256x256 RGBA).

Two zones in one image: a bright violet FRINGE annulus hugging the storm's
silhouette (r 0.62-0.95) and a faint violet WASH across the whole face
(r < 0.62), so one quad can lay a second purple silhouette over the
creature exactly like the reference frames.
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
        # fringe: peak around 0.78, gone by 0.98 and below 0.60
        fin = max(0.0, min(1.0, (r - 0.58) / 0.14))
        fout = max(0.0, min(1.0, (0.98 - r) / 0.16))
        fringe = (fin * fin * (3 - 2 * fin)) * (fout * fout * (3 - 2 * fout))
        # face wash: soft, strongest mid-face, fading to the centre
        wash = max(0.0, min(1.0, (0.62 - r) / 0.62))
        wash = wash * 0.30 * (0.35 + 0.65 * min(1.0, r / 0.35))
        a = max(fringe * 0.95, wash)
        # violet fringe, slightly pinker wash
        t = fringe
        cr = int(138 + (176 - 138) * (1 - t))
        cg = int(58 + (106 - 58) * (1 - t))
        cb = int(232 + (232 - 232) * t)
        px.append((cr, cg, cb, int(a * 235)))

write_png(sys.argv[1], W, H, px)
print("storm face overlay written:", sys.argv[1])
