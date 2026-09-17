#!/usr/bin/env python3
"""make_glare.py — the Telltale-exposed glare textures (mega-phase 5c).

The reference frames exposed how the original game builds the storm glare:
  * a plain SOFT GRADIENT quad hung BEHIND the silhouette (the wide purple /
    blue aura) - not a bloom pass, not a hard ring;
  * flat EMISSIVE squares for the mouth details (white dashed teeth, magenta
    emitter cube, cyan-white inner mouth), whose softness comes only from
    distance.

This generator writes both ingredients:
    storm_glare.png - 256x256 white radial gradient, alpha falling off
                      smoothly to zero (tinted per phase at draw time);
    storm_white.png - 8x8 flat white, the emissive square primitive.

Usage: python3 ci/make_glare.py <glare_out.png> <white_out.png>
"""
import math
import sys

sys.path.insert(0, 'ci')
from make_branding import write_png  # noqa: E402  (same helper as make_halo)


def glare():
    n = 256
    px = []
    for y in range(n):
        for x in range(n):
            dx = (x + 0.5) / n * 2.0 - 1.0
            dy = (y + 0.5) / n * 2.0 - 1.0
            r = math.sqrt(dx * dx + dy * dy)
            t = min(r, 1.0)
            # smooth filled falloff: bright core, long soft skirt, zero edge
            a = (1.0 - t) ** 2.1
            a = a * a * (3.0 - 2.0 * a)
            v = int(round(255.0 * a))
            px.append((255, 255, 255, v))
    return n, px


def white():
    n = 8
    px = [(255, 255, 255, 255)] * (n * n)
    return n, px


def main():
    if len(sys.argv) != 3:
        print(__doc__)
        return 2
    n, px = glare()
    write_png(sys.argv[1], n, n, px)
    n, px = white()
    write_png(sys.argv[2], n, n, px)
    print('[glare] wrote %s and %s' % (sys.argv[1], sys.argv[2]))
    return 0


if __name__ == '__main__':
    sys.exit(main())
