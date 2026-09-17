#!/usr/bin/env python3
"""trace_sky_sheets.py -- BUILD #416: trace the three reference sky sheets EXACTLY.

The brief's rule: extract the colours per phase with a tool, never by eye. This
is that tool. It reads the three delivered sheets

    "phase 5 turquoise sky.png"      -> phase 5.0 - 5.1   (teal)
    "phase5sky0purple sky.png"       -> phase 5.5 - 5.9   (purple)
    "phase6sky 6 witherstorm.png"    -> phase 6.0 - 7.0   (rose)

samples each one down its centre band into the six-stop column the shaders use,
and REWRITES every consumer of that column in place:

    mcsm-core-shaders/core/sky.fsh                  PHASE5_TEAL / PHASE55_PUR / PHASE6_ROSE
    mcsm-core-shaders/core/position.fsh             SKY_REF_TEAL / SKY_REF_PURPLE / SKY_REF_ROSE
    mcsm-extras/.../client/McsmStormPhase.java      SKY_TEAL / SKY_PURPLE / SKY_ROSE
    mcsm-core-shaders/include/mcsm_visuals.glsl     the derived constants (P5_*, P55_*, P6_*,
                                                    MCSM_CLOUD_*) via ci/palette_tables.DERIVED

then re-runs the parity gate so the result is proven consistent before anything
is committed.

    python3 ci/trace_sky_sheets.py --check          # report what it would change
    python3 ci/trace_sky_sheets.py --apply          # write it
    python3 ci/trace_sky_sheets.py --dir PATH       # where the sheets live
    python3 ci/trace_sky_sheets.py --self-test      # round-trip test, no sheets needed

Where to put the sheets: any of
    ci/sky_sheets/<name>.png        (committed, if you want the trace reproducible)
    uploads/<name>.png              (repo root)
    /home/user/uploads/<name>.png   (the workspace upload directory)
or pass --dir explicitly. Spaces in the names are fine, and the file names are
matched loosely (case, underscores, "phase 5"/"phase5").
"""
import argparse
import glob
import os
import re
import shutil
import struct
import subprocess
import sys
import zlib

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
sys.path.insert(0, HERE)
import palette_tables as pal  # noqa: E402

# role -> (loose filename fragments, phase the sheet owns)
SHEETS = [
    ("teal", ["turquoise"], 5.05),
    ("purple", ["purple", "5sky0"], 5.65),
    ("rose", ["witherstorm", "phase6", "6wither"], 6.20),
]
SEARCH_DIRS = [
    os.path.join(ROOT, "ci", "sky_sheets"),
    os.path.join(ROOT, "uploads"),
    "/home/user/uploads",
    os.path.join(ROOT, "reference"),
]
# Every file that carries a copy of a column, as (path, regex, token per role).
GLSL_CONSUMERS = [
    (
        "mcsm-core-shaders/core/sky.fsh",
        r"(const\s+vec3\s+%s\s*\[\s*\d+\s*\]\s*=\s*vec3\[\]\s*\()(.*?)(\)\s*;)",
        {"teal": "PHASE5_TEAL", "purple": "PHASE55_PUR", "rose": "PHASE6_ROSE",
         "ember": "EMBER_END"},
    ),
    (
        "mcsm-core-shaders/core/position.fsh",
        r"(const\s+vec3\s+%s\s*\[\s*\d+\s*\]\s*=\s*vec3\[\]\s*\()(.*?)(\)\s*;)",
        {"teal": "SKY_REF_TEAL", "purple": "SKY_REF_PURPLE", "rose": "SKY_REF_ROSE"},
    ),
]
JAVA_CONSUMER = (
    "mcsm-extras/java/net/mcsm/extras/client/McsmStormPhase.java",
    r"(float\[\]\[\]\s+%s\s*=\s*\{)(.*?)(\}\s*;)",
    {"teal": "SKY_TEAL", "purple": "SKY_PURPLE", "rose": "SKY_ROSE",
     "ember": "SKY_EMBER"},
)
# BUILD #476 -- the column resolution IS the still: see palette_tables.STOPS. The
# trace samples one row per stop straight out of the sheet, so this is the number of
# rows of the sheet that reach the shader. 6 was an approximation; 32 is the image.
STOPS = pal.STOPS


# ---------------------------------------------------------------------------
# PNG decoding (no Pillow in CI, and the sheets can use any filter type)
# ---------------------------------------------------------------------------
def _paeth(a, b, c):
    p = a + b - c
    pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
    if pa <= pb and pa <= pc:
        return a
    return b if pb <= pc else c


def read_png(path):
    """-> (width, height, [(r,g,b,a), ...]) with every filter type applied."""
    with open(path, "rb") as f:
        data = f.read()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("%s: not a PNG" % path)
    i = 8
    idat = b""
    w = h = depth = color = None
    while i < len(data):
        (ln,) = struct.unpack(">I", data[i:i + 4])
        typ = data[i + 4:i + 8]
        chunk = data[i + 8:i + 8 + ln]
        i += 12 + ln
        if typ == b"IHDR":
            w, h, depth, color = struct.unpack(">IIBB", chunk[:10])
        elif typ == b"IDAT":
            idat += chunk
        elif typ == b"IEND":
            break
    if depth != 8:
        raise ValueError("%s: only 8-bit PNGs are supported (got depth %d)" % (path, depth))
    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}.get(color)
    if channels is None:
        raise ValueError("%s: unsupported PNG colour type %d" % (path, color))
    raw = zlib.decompress(idat)
    stride = w * channels
    out = []
    prev = bytearray(stride)
    pos = 0
    for _y in range(h):
        ftype = raw[pos]
        pos += 1
        line = bytearray(raw[pos:pos + stride])
        pos += stride
        if ftype == 1:
            for x in range(channels, stride):
                line[x] = (line[x] + line[x - channels]) & 0xFF
        elif ftype == 2:
            for x in range(stride):
                line[x] = (line[x] + prev[x]) & 0xFF
        elif ftype == 3:
            for x in range(stride):
                a = line[x - channels] if x >= channels else 0
                line[x] = (line[x] + ((a + prev[x]) >> 1)) & 0xFF
        elif ftype == 4:
            for x in range(stride):
                a = line[x - channels] if x >= channels else 0
                c = prev[x - channels] if x >= channels else 0
                line[x] = (line[x] + _paeth(a, prev[x], c)) & 0xFF
        elif ftype != 0:
            raise ValueError("%s: bad PNG filter %d" % (path, ftype))
        out.append(line)
        prev = line
    px = []
    for line in out:
        for x in range(w):
            v = line[x * channels:(x + 1) * channels]
            if channels == 4:
                px.append((v[0], v[1], v[2], v[3]))
            elif channels == 3:
                px.append((v[0], v[1], v[2], 255))
            elif channels == 2:
                px.append((v[0], v[0], v[0], v[1]))
            else:
                px.append((v[0], v[0], v[0], 255))
    return w, h, px


def trace_column(path):
    """Six stops (0 = top of the sheet, 1 = bottom) from the sheet's centre band.

    Averaging a band rather than a single pixel column is what makes the trace
    robust against PNG noise and any thin highlight line in the source art; the
    answer is still exactly what the sheet shows, to 1/255.
    """
    w, h, px = read_png(path)
    x0 = int(w * 0.40)
    x1 = max(x0 + 1, int(w * 0.60))
    stops = []
    for i in range(STOPS):
        t = i / float(STOPS - 1)
        y = min(h - 1, int(round(t * (h - 1))))
        acc = [0.0, 0.0, 0.0]
        n = 0
        for x in range(x0, x1):
            r, g, b, _a = px[y * w + x]
            acc[0] += r
            acc[1] += g
            acc[2] += b
            n += 1
        stops.append([acc[k] / (255.0 * n) for k in range(3)])
    return stops


# ---------------------------------------------------------------------------
# writing the trace back into the sources
# ---------------------------------------------------------------------------
def glsl_rows(stops):
    """Rows for a `const vec3 NAME[6] = vec3[](...);` initialiser.

    The closing `);` is supplied by the caller's regex replacement, so the last
    row must NOT carry it -- emitting it here is what produced a doubled
    terminator the first time this ran.
    """
    rows = []
    for i in range(0, len(stops), 3):
        line = "    " + ", ".join("vec3(%.3f, %.3f, %.3f)" % tuple(s) for s in stops[i:i + 3])
        if i + 3 < len(stops):
            line += ","
        rows.append(line)
    return "\n".join(rows)


def java_rows(stops):
    """Rows for a `float[][] NAME = { ... };` initialiser, one row per source
    line group. A trailing comma is legal in Java and keeps every row uniform."""
    rows = []
    for i in range(0, len(stops), 3):
        rows.append("        " + ", ".join("{%.3fF, %.3fF, %.3fF}" % tuple(s)
                                           for s in stops[i:i + 3]) + ",")
    return "\n".join(rows)


def patched_text(path, regex_tpl, token, body):
    """Return (new_text, changed) for one array declaration. Pure: never writes."""
    """Rewrite one array declaration in place.

    Deliberately structural rather than clever. Two earlier versions of this
    function corrupted the file:
      * a non-greedy regex group left a doubled `);` when re-run over its own
        output (a GLSL syntax error), and
      * searching for the terminator with `\n\s*\)\s*;` assumed the closing
        bracket sits on its own line, but the pristine files close the array on
        the last row's line -- so the search ran past the declaration and ate
        the NEXT one.
    A row never contains `);` (GLSL) or `};` (Java), so the first literal
    occurrence after the opener is always the terminator. Any duplicated
    terminators from an older mangled write are swallowed, and the result is
    asserted to contain exactly one declaration of the token.
    """
    full = os.path.join(ROOT, path)
    with open(full, encoding="utf-8") as f:
        text = f.read()

    opener = re.compile(regex_tpl % re.escape(token), re.S)
    m = opener.search(text)
    if not m:
        raise SystemExit("[trace] %s: could not find %s to rewrite" % (path, token))

    terminator = "};" if path.endswith(".java") else ");"
    # m.end(1) is the end of the OPENING group -- m.end() is the end of the
    # whole match, which already includes the array body and its terminator.
    # Using m.end() here made the scan start inside the NEXT declaration.
    close_at = text.find(terminator, m.end(1))
    if close_at < 0:
        raise SystemExit("[trace] %s: no terminator for %s" % (path, token))

    start = m.start()
    end = close_at + len(terminator)
    # swallow duplicated terminators left behind by an earlier bad write
    probe = re.compile(r"\s*\n\s*" + re.escape(terminator))
    while True:
        dm = probe.match(text, end)
        if not dm:
            break
        end = dm.end()

    new_text = text[:start] + m.group(1) + "\n" + body + "\n" + terminator + text[end:]

    # structural assertions: the declaration survived exactly once
    if new_text.count("const vec3 %s[" % token) > 1 or new_text.count(token) != text.count(token):
        raise SystemExit("[trace] %s: refusing to write -- %s would be duplicated or lost"
                         % (path, token))
    return new_text, new_text != text


def patch(path, regex_tpl, token, body):
    """Write patched_text() through to disk. Returns True when the file changed."""
    new_text, changed = patched_text(path, regex_tpl, token, body)
    if changed:
        with open(os.path.join(ROOT, path), "w", encoding="utf-8") as f:
            f.write(new_text)
    return changed


def rewrite_derived(apply):
    """Regenerate the derived constants in mcsm_visuals.glsl from the tables."""
    path = os.path.join(ROOT, pal.VISUALS)
    with open(path, encoding="utf-8") as f:
        text = f.read()
    changed = 0
    for spec in pal.DERIVED:
        name, role, mode, value, scale = spec
        if mode == "row":
            base = list(pal.rows(role)[int(value)])
        elif mode == "mixw":
            t, w = value
            base = [v * (1.0 - w) + w for v in pal.sample_column(pal.rows(role), t)]
        else:
            base = list(pal.sample_column(pal.rows(role), value))
        c = [v * scale for v in base]
        body = "const vec3 %s = vec3(%.1f, %.1f, %.1f) / 255.0;   // %s" % (
            name, c[0] * 255.0, c[1] * 255.0, c[2] * 255.0, pal._hex(c))
        # replace the whole line, INCLUDING any stale `// #RRGGBB` comment
        rx = re.compile(r"const\s+vec3\s+%s\s*=\s*vec3\([^;]*?\)\s*/\s*255\.0;[^\n]*" % re.escape(name))
        if not rx.search(text):
            print("  ! %s not found in %s" % (name, pal.VISUALS))
            continue
        newtext = rx.sub(body.replace("\\", "\\\\"), text, count=1)
        if newtext != text:
            changed += 1
            if apply:
                text = newtext
    if apply and changed:
        with open(path, "w", encoding="utf-8") as f:
            f.write(text)
    return changed


def rewrite_from_hex(apply):
    """BUILD #416 -- write the SUPPLIED hex anchors into every consumer.

    The artist's three hexes per sheet (ceiling / middle / horizon) are the
    ground truth. They live in ci/palette_tables.SHEET_HEX; this expands them to
    the six-stop column the shaders use and rewrites sky.fsh, position.fsh,
    McsmStormPhase.java and the derived constants in mcsm_visuals.glsl. No image
    decoding is involved, which is the point: the numbers are injected, not read.
    """
    # BUILD #476 -- every column the sky interpolates, at pal.STOPS rows: the three
    # supplied sheets AND the authored ember fall (a six-row column in a 32-row sky
    # is the one place the shader would have had to stretch its source).
    roles = list(pal.SHEET_HEX.keys()) + list(pal.AUTHORED.keys())
    traced = {role: pal.hex_column(role) for role in roles}
    print("[hex] the sky's source tables (ceiling / middle / horizon, or authored):")
    for role in roles:
        anchors = pal.SHEET_HEX.get(role, ["authored"])
        print("  %-7s %-24s -> %s ... %s" % (role, "  ".join(anchors),
                                            pal._hex(traced[role][0]),
                                            pal._hex(traced[role][-1])))
    writes = []
    for path, tpl, tokens in GLSL_CONSUMERS:
        for role, token in tokens.items():
            if role in traced:
                writes.append((path, tpl, token, glsl_rows(traced[role])))
    path, tpl, tokens = JAVA_CONSUMER
    for role, token in tokens.items():
        if role in traced:
            writes.append((path, tpl, token, java_rows(traced[role])))

    if not apply:
        # BUILD #416 -- this used to print the number of SITES it would write,
        # which is not a drift measurement: it read "9 table sites ... would be
        # rewritten" even on a tree that was byte-for-byte correct, so it could
        # never fail. It now dry-runs every rewrite and counts the ones whose
        # output actually differs, and exits non-zero when any drifted.
        drift = []
        for path, tpl, token, body in writes:
            _new, changed = patched_text(path, tpl, token, body)
            if changed:
                drift.append("%s :: %s" % (path, token))
        derived = rewrite_derived(False)
        if drift or derived:
            for site in drift:
                print("[hex] DRIFT %s" % site)
            if derived:
                print("[hex] DRIFT %s (%d derived constants)" % (pal.VISUALS, derived))
            print("[hex] --check: FAILED -- %d table sites + %d derived constants out of date; "
                  "run --from-hex --apply" % (len(drift), derived))
            return 1
        print("[hex] --check: OK -- %d table sites + the derived constants already match "
              "the supplied anchors" % len(writes))
        return 0

    changed = 0
    for path, tpl, token, body in writes:
        if patch(path, tpl, token, body):
            changed += 1
            print("  wrote %s :: %s" % (path, token))
    changed += rewrite_derived(True)
    rc = subprocess.call([sys.executable, os.path.join(HERE, "palette_tables.py"), "--quiet"])
    print("[hex] %d sites rewritten; parity gate %s" % (changed, "OK" if rc == 0 else "FAILED"))
    if rc != 0:
        print("[hex] rerun 'python3 ci/make_backdrop_sheets.py' then shimcheck")
    return rc


# ---------------------------------------------------------------------------
# The superseded set
# ---------------------------------------------------------------------------
# BUILD #416 -- ci/sky_sheets/superseded/ holds the three sheets the supplied hex
# anchors REPLACE. They are the washed-out trace older builds shipped (teal
# climbing to #9EAC9F at the horizon, purple to #A85CB4, rose to #C89CA6), which
# is exactly what the brief rejected. They are kept because they are the evidence
# for what changed, not because they are authoritative -- the anchors are.
#
# Their six-stop trace is recorded here so the folder cannot quietly turn into a
# different set of images: `--verify` re-traces them and fails if they no longer
# match what they claim to be. Dropping the CURRENT sheets into ci/sky_sheets/
# (top level) instead makes `--verify` compare the shipped tables against them
# directly, which is the stronger check and the one that should run once those
# images exist in the repository.
SUPERSEDED_DIR = os.path.join(ROOT, "ci", "sky_sheets", "superseded")
SUPERSEDED_TRACE = {
    "teal": [(0.1176, 0.1725, 0.1765), (0.2196, 0.2863, 0.2941), (0.3333, 0.4000, 0.3961),
             (0.4235, 0.4863, 0.4667), (0.4902, 0.5412, 0.5059), (0.5529, 0.5882, 0.5294)],
    "purple": [(0.2392, 0.0863, 0.2588), (0.3529, 0.1451, 0.3843), (0.4627, 0.2039, 0.5059),
               (0.5490, 0.2471, 0.6000), (0.6039, 0.2745, 0.6588), (0.6510, 0.2980, 0.7098)],
    "rose": [(0.3373, 0.2549, 0.3255), (0.4157, 0.3098, 0.3804), (0.4667, 0.3529, 0.4196),
             (0.5412, 0.4039, 0.4745), (0.6549, 0.4863, 0.5451), (0.7451, 0.5725, 0.6196)],
}


def check_superseded():
    """Confirm ci/sky_sheets/superseded/ still holds the sheets it claims to."""
    if not os.path.isdir(SUPERSEDED_DIR):
        return 0
    found = {}
    for role, fragments, _phase in SHEETS:
        for path in sorted(glob.glob(os.path.join(SUPERSEDED_DIR, "*.png"))):
            low = os.path.basename(path).lower().replace("_", " ").replace("-", " ")
            if any(f.lower() in low for f in fragments):
                found.setdefault(role, path)
                break
    if not found:
        return 0
    ok = True
    for role, path in sorted(found.items()):
        want = SUPERSEDED_TRACE.get(role)
        got = trace_column(path)
        worst = max(abs(got[i][k] - want[i][k])
                    for i in range(min(len(want), len(got))) for k in range(3))
        if want is None or worst > 0.02:
            ok = False
            print("  FAIL superseded %-7s -- %s is not the recorded superseded sheet (%.4f)"
                  % (role, os.path.basename(path), worst if want else -1.0))
        else:
            print("  ok   superseded %-7s %s -> %s (replaced by the supplied anchors, %.4f)"
                  % (role, pal._hex(got[0]), pal._hex(got[-1]), worst))
    print("[trace] superseded set intact: %d sheets -- evidence of what the anchors replace, "
          "NOT the authority" % len(found))
    return 0 if ok else 1


def find_sheets(extra_dir):
    dirs = ([extra_dir] if extra_dir else []) + SEARCH_DIRS
    found = {}
    for role, fragments, _phase in SHEETS:
        for d in dirs:
            if not d or not os.path.isdir(d):
                continue
            for path in sorted(glob.glob(os.path.join(d, "*.png"))):
                low = os.path.basename(path).lower().replace("_", " ").replace("-", " ")
                if any(f.lower() in low for f in fragments):
                    found.setdefault(role, path)
                    break
            if role in found:
                break
    return found


def self_test():
    """Round-trip: bake a sheet from the shipped tables, trace it back, compare."""
    import tempfile
    ok = True
    for role, phase in (("teal", 5.05), ("purple", 5.65), ("rose", 6.20)):
        col = pal.rows(role)
        w, h = 64, 256
        rows = []
        for y in range(h):
            t = y / (h - 1)
            c = pal.sample_column(col, t)
            px = bytes(max(0, min(255, int(round(v * 255)))) for v in c)
            rows.append(b"\x00" + px * w)

        def chunk(typ, d):
            return (struct.pack(">I", len(d)) + typ + d
                    + struct.pack(">I", zlib.crc32(typ + d) & 0xFFFFFFFF))

        blob = (b"\x89PNG\r\n\x1a\n"
                + chunk(b"IHDR", struct.pack(">IIBB", w, h, 8, 2) + b"\x00\x00\x00")
                + chunk(b"IDAT", zlib.compress(b"".join(rows), 6))
                + chunk(b"IEND", b""))
        with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as tmp:
            tmp.write(blob)
            tmp_path = tmp.name
        got = trace_column(tmp_path)
        os.unlink(tmp_path)
        worst = 0.0
        for i, s in enumerate(got):
            want = pal.sample_column(col, i / (STOPS - 1.0))
            worst = max(worst, max(abs(s[k] - want[k]) for k in range(3)))
        status = "ok " if worst <= 1.5 / 255.0 else "FAIL"
        if worst > 1.5 / 255.0:
            ok = False
        print("  %s round-trip %-7s max channel error %.5f" % (status, role, worst))
    print("[trace] self-test %s" % ("PASSED" if ok else "FAILED"))
    return 0 if ok else 1


def verify_hex():
    """The shipped tables must match the SUPPLIED hex anchors exactly."""
    ok = True
    for role in pal.SHEET_HEX:
        want = pal.hex_column(role)
        got = pal.rows(role)
        worst = 0.0
        where = ""
        for i in range(STOPS):
            g = pal.sample_column(got, i / (STOPS - 1.0))
            d = max(abs(want[i][k] - g[k]) for k in range(3))
            if d > worst:
                worst = d
                where = "stop %d: shipped %s vs hex %s" % (i, pal._hex(g), pal._hex(want[i]))
        if worst > 2.0 / 255.0:
            ok = False
            print("  FAIL hex %-7s drift %.4f -- %s" % (role, worst, where))
        else:
            print("  ok   hex %-7s matches the supplied anchors (%.4f)" % (role, worst))
    print("[trace] %s" % ("the shipped tables ARE the supplied hexes" if ok
                          else "HEX DRIFT: run 'trace_sky_sheets.py --from-hex --apply'"))
    return 0 if ok else 1


def verify(traced):
    """Compare a fresh trace against the tables the shaders ship right now."""
    ok = True
    for role, _frag, phase in SHEETS:
        shipped = pal.rows(role)
        worst = 0.0
        where = ""
        for i in range(STOPS):
            want = pal.sample_column(shipped, i / (STOPS - 1.0))
            got = traced[role][i]
            d = max(abs(want[k] - got[k]) for k in range(3))
            if d > worst:
                worst = d
                where = "stop %d: shipped %s vs sheet %s" % (i, pal._hex(want), pal._hex(got))
        if worst > 2.0 / 255.0:
            ok = False
            print("  FAIL %-7s drift %.4f -- %s" % (role, worst, where))
        else:
            print("  ok   %-7s matches the sheet (max channel error %.4f)" % (role, worst))
    print("[trace] %s" % ("the shipped tables ARE the sheets" if ok
                          else "DRIFT: shipped tables are not what the sheets say -- run --apply"))
    return 0 if ok else 1


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--apply", action="store_true", help="write the traced values")
    ap.add_argument("--check", action="store_true", help="report only (default)")
    ap.add_argument("--verify", action="store_true",
                    help="trace and FAIL if the shipped tables disagree with the sheets")
    ap.add_argument("--dir", help="directory holding the three sheets")
    ap.add_argument("--self-test", action="store_true")
    ap.add_argument("--from-hex", action="store_true", dest="from_hex",
                    help="inject the supplied ci/palette_tables.SHEET_HEX anchors "
                         "(no image decoding); combine with --apply to write")
    args = ap.parse_args()

    if args.self_test:
        return self_test()

    if args.from_hex:
        return rewrite_from_hex(args.apply)

    found = find_sheets(args.dir)
    missing = [role for role, _f, _p in SHEETS if role not in found]
    if missing:
        if args.verify:
            # No sheets checked in: nothing to verify against. Say so loudly
            # rather than passing silently.
            print("::notice title=sky-sheets::the CURRENT sky sheets are not in the repo "
                  "(looked in %s) -- the SUPPLIED HEX ANCHORS are the authority, and the "
                  "superseded set is verified instead" % ", ".join(SEARCH_DIRS))
        print("[trace] reference sheets NOT found: %s" % ", ".join(missing))
        print("[trace] looked in: %s" % ", ".join(d for d in SEARCH_DIRS))
        print("[trace] the CURRENT sheets are not in ci/sky_sheets/ yet; the supplied hex "
              "anchors are the authority until they are")
        for role, path in found.items():
            print("  found %-7s %s" % (role, path))
        if args.verify:
            rc_sup = check_superseded()
            rc_hex = verify_hex()
            return rc_sup if rc_hex == 0 else rc_hex
        return 2

    traced = {}
    for role, _frag, phase in SHEETS:
        traced[role] = trace_column(found[role])
        lo = pal._hex(traced[role][0])
        hi = pal._hex(traced[role][-1])
        print("[trace] %-7s phase %.2f  %s -> %s  (zenith -> horizon)  %s"
              % (role, phase, lo, hi, os.path.basename(found[role])))

    if args.verify:
        rc_hex = verify_hex()
        rc_png = verify(traced) if not missing else 0
        return rc_png if rc_hex == 0 else rc_hex

    writes = []
    for path, tpl, tokens in GLSL_CONSUMERS:
        for role, token in tokens.items():
            writes.append((path, tpl, token, glsl_rows(traced[role])))
    path, tpl, tokens = JAVA_CONSUMER
    for role, token in tokens.items():
        writes.append((path, tpl, token, java_rows(traced[role])))

    if not args.apply:
        print("[trace] --check: %d table sites would be rewritten (%d files) + derived constants"
              % (len(writes), len({w[0] for w in writes}) + 1))
        print("[trace] rerun with --apply to write them, then 'python3 ci/palette_tables.py'")
        return 0

    changed = 0
    for path, tpl, token, body in writes:
        if patch(path, tpl, token, body):
            changed += 1
            print("  wrote %s :: %s" % (path, token))
    changed += rewrite_derived(True)

    rc = subprocess.call([sys.executable, os.path.join(HERE, "palette_tables.py"), "--quiet"])
    print("[trace] %d sites rewritten; parity gate %s" % (changed, "OK" if rc == 0 else "FAILED"))
    if rc != 0:
        print("[trace] NOTE: rerun 'python3 ci/make_backdrop_sheets.py' to re-bake the sheets,")
        print("[trace]       then 'python3 glslcheck/shimcheck.py mcsm-core-shaders'.")
    return rc


if __name__ == "__main__":
    sys.exit(main())
