#!/usr/bin/env python3
"""make_mcsm_sounds.py -- BUILD #425: the mod's OWN sounds, generated from scratch.

THE REPORT. "The radio, like, it wasn't using custom sounds -- it was using
sounds that were already in the game" and, of the MASSG's distortion, "currently
only using regular [sounds], I would like custom scary sounds".

Both were true: every audio cue in this overlay was a vanilla SoundEvent
(AMBIENT_CAVE, END_PORTAL_SPAWN, ELDER_GUARDIAN_CURSE, ...), so nothing the mod
does sounds like itself.

WHAT THIS WRITES. Real Ogg Vorbis files, synthesised here and shipped under the
mod's own namespace (`assets/mcsm/sounds/...`), plus the `assets/mcsm/sounds.json`
that binds them to the SoundEvents the Java registers (McsmSounds). No sample is
copied from anywhere: every file is a sum of oscillators, noise and envelopes
computed in this script from a fixed seed, so a rerun is byte-identical and a
diff is reviewable.

  radio/static          filtered noise bed            the carrier between stations
  radio/carrier         slow morse-ish tone pulses    a signal being received
  radio/voice           formant blips                 a person, too far away
  radio/distress        rising two-tone alarm         a station in trouble
  radio/morse           keyed dashes                  an automated beacon
  massg/breath          low inhale/exhale             the creature, close
  massg/giggle          pitch-bent laugh fragments    it is amused by you
  massg/whisper         breathy near-words            it imitates you
  massg/roar            detuned low growl             the summon
  massg/heart           slow double thump            something enormous, alive
  oblivion/drone        vast minor drone              the ruined dimension
  oblivion/glitch       quantised data corruption     the reality tears
  oblivion/warp         shepard-tone sweep            the world folding
  ui/terminal_open      holographic power-up          the console waking
  ui/terminal_key       soft glass tick               typing into it
  ui/terminal_deny      descending refusal            ACCESS DENIED

Usage:
    python3 ci/make_mcsm_sounds.py            # write the sounds + sounds.json
    python3 ci/make_mcsm_sounds.py --check    # verify them (build gate)
"""
import argparse
import json
import math
import os
import random
import struct
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "jar-overrides/assets/mcsm/sounds")
MANIFEST = os.path.join(ROOT, "jar-overrides/assets/mcsm/sounds.json")
RATE = 22050
SEED = 4250425

try:
    import numpy as np
    import soundfile as sf
except Exception:                                    # pragma: no cover
    np = None
    sf = None


# ---------------------------------------------------------------------------
# the building blocks -- plain numpy, no samples
# ---------------------------------------------------------------------------

def t(seconds):
    return np.linspace(0.0, seconds, int(RATE * seconds), endpoint=False, dtype=np.float64)


def env(x, attack=0.01, release=0.2):
    """A simple attack/release envelope over a buffer.

    The ramps are clipped to the buffer: some of the sounds below are only a few
    hundred samples long, and a ramp longer than the buffer is the difference
    between a tick and a crash.
    """
    n = len(x)
    if n == 0:
        return x
    a = min(n, max(1, int(RATE * attack)))
    r = min(n - a if n > a else 1, max(1, int(RATE * release)))
    e = np.ones(n)
    e[:a] = np.linspace(0.0, 1.0, a)
    if r > 0:
        e[n - r:] = np.linspace(1.0, 0.0, r)
    return x * e


def noise(seconds, rng, lp=0.15, hp=0.0):
    """Noise, coarsely low-passed by a moving average and optionally high-passed."""
    n = int(RATE * seconds)
    raw = np.array([rng.uniform(-1.0, 1.0) for _ in range(n)], dtype=np.float64)
    if lp > 0.0:
        k = max(1, int(1.0 / max(lp, 1e-4)))
        kernel = np.ones(k) / k
        raw = np.convolve(raw, kernel, mode="same")
    if hp > 0.0:
        k = max(1, int(1.0 / max(hp, 1e-4)))
        kernel = np.ones(k) / k
        raw = raw - np.convolve(raw, kernel, mode="same")
    return raw


def tone(freq, seconds, shape="sine", amp=1.0):
    x = t(seconds)
    if shape == "square":
        v = np.sign(np.sin(2 * math.pi * freq * x))
    elif shape == "tri":
        v = 2.0 * np.abs(2.0 * (x * freq - np.floor(x * freq + 0.5))) - 1.0
    elif shape == "saw":
        v = 2.0 * (x * freq - np.floor(x * freq + 0.5))
    else:
        v = np.sin(2 * math.pi * freq * x)
    return amp * v


def sweep(f0, f1, seconds, amp=1.0):
    x = t(seconds)
    f = f0 + (f1 - f0) * (x / max(seconds, 1e-6))
    phase = 2 * math.pi * np.cumsum(f) / RATE
    return amp * np.sin(phase)


def tremolo(x, hz, depth=0.5):
    return x * (1.0 - depth + depth * (0.5 + 0.5 * np.sin(2 * math.pi * hz * t(len(x) / RATE))))


def bits(x, n=6):
    """Quantise the waveform -- the 'made of shaders' digital edge."""
    peak = np.max(np.abs(x)) or 1.0
    y = x / peak
    y = np.round(y * (2 ** (n - 1))) / (2 ** (n - 1))
    return y * peak


def norm(x, peak=0.85):
    m = np.max(np.abs(x))
    return x if m < 1e-9 else x * (peak / m)


# ---------------------------------------------------------------------------
# the sounds
# ---------------------------------------------------------------------------

def radio_static():
    rng = random.Random(SEED + 1)
    n = noise(8.0, rng, lp=0.02, hp=0.35)
    crackle = noise(8.0, rng, lp=0.6, hp=0.05)
    hits = np.zeros_like(n)
    for _ in range(14):
        at = rng.randrange(0, len(n) - 2000)
        hits[at:at + rng.randrange(80, 900)] += rng.uniform(-0.5, 0.5)
    bed = tremolo(n * 0.5 + crackle * 0.12, 0.13, 0.25)
    return norm(env(bed + hits * 0.4, 0.2, 0.6), 0.7)


def radio_carrier():
    rng = random.Random(SEED + 2)
    sig = np.zeros(int(RATE * 6.0))
    at = 0
    for _ in range(70):
        length = rng.choice([40, 60, 90, 160, 260])
        if at + length >= len(sig):
            break
        f = rng.choice([520.0, 660.0, 780.0, 940.0])
        seg = tone(f, length / RATE, "sine", 0.5) + tone(f * 2.01, length / RATE, "sine", 0.16)
        seg = env(seg, 0.004, 0.01)
        room = min(len(seg), len(sig) - at)
        if room > 0:
            sig[at:at + room] += seg[:room]
        at += length + rng.randrange(20, 120)
    hiss = noise(6.0, rng, lp=0.05, hp=0.4) * 0.10
    return norm(env(sig + hiss, 0.05, 0.4), 0.6)


def radio_voice():
    """Formant blips: a human cadence with no words in it."""
    rng = random.Random(SEED + 3)
    out = np.zeros(int(RATE * 6.0))
    at = int(RATE * 0.4)
    for _ in range(26):
        length = rng.uniform(0.09, 0.22)
        f0 = rng.uniform(95.0, 150.0)
        seg = np.zeros(int(RATE * length))
        for h, amp in ((1, 1.0), (2, 0.5), (3, 0.32), (4, 0.2), (5, 0.12)):
            seg += amp * tone(f0 * h, length, "sine", 1.0)
        # two formants over the top: it reads as a voice without being one
        seg += 0.35 * tone(rng.uniform(500, 900), length, "sine")
        seg += 0.22 * tone(rng.uniform(1100, 1800), length, "sine")
        seg = env(seg, 0.01, 0.03)
        room = min(len(seg), max(0, len(out) - at))
        if room > 0:
            out[at:at + room] += seg[:room]
        at += len(seg) + rng.randrange(300, 1400)
        if at >= len(out) - RATE:
            break
    return norm(env(out, 0.05, 0.5), 0.55)


def radio_distress():
    parts = []
    for i in range(4):
        parts.append(env(tone(880.0 if i % 2 == 0 else 622.0, 0.34, "square", 0.45), 0.01, 0.05))
        parts.append(np.zeros(int(RATE * 0.14)))
    return norm(np.concatenate(parts), 0.7)


def radio_morse():
    rng = random.Random(SEED + 5)
    out = np.zeros(int(RATE * 7.0))
    at = int(RATE * 0.3)
    for _ in range(46):
        long = rng.random() < 0.4
        length = 0.24 if long else 0.08
        seg = env(tone(700.0, length, "sine", 0.6), 0.005, 0.01)
        if at + len(seg) >= len(out):
            break
        out[at:at + len(seg)] += seg
        at += len(seg) + int(RATE * (0.10 if long else 0.06))
    out += noise(7.0, rng, lp=0.03, hp=0.45) * 0.07
    return norm(out, 0.6)


def massg_breath():
    rng = random.Random(SEED + 10)
    inhale = noise(1.5, rng, lp=0.03, hp=0.3)
    exhale = noise(2.0, rng, lp=0.02, hp=0.2)
    lift = np.linspace(0.3, 1.0, len(inhale))
    drop = np.linspace(1.0, 0.2, len(exhale))
    breath = np.concatenate([inhale * lift, np.zeros(int(RATE * 0.25)), exhale * drop])
    low = tone(38.0, len(breath) / RATE, "sine", 0.5)
    low = env(low, 0.3, 0.6)
    return norm(env(breath * 0.9, 0.2, 0.7) + low, 0.8)


def massg_giggle():
    rng = random.Random(SEED + 11)
    out = np.zeros(int(RATE * 2.6))
    at = int(RATE * 0.1)
    for i in range(9):
        length = rng.uniform(0.055, 0.11)
        base = rng.uniform(180.0, 300.0) * (1.0 - i * 0.03)
        seg = 0.0
        for h in (1, 2, 3, 5):
            seg = seg + (1.0 / h) * tone(base * h, length, "saw" if h == 1 else "sine", 1.0)
        seg = env(seg, 0.004, 0.02) * rng.uniform(0.6, 1.0)
        if at + len(seg) < len(out):
            out[at:at + len(seg)] += seg
        at += len(seg) + int(RATE * rng.uniform(0.05, 0.16))
    return norm(bits(out, 7), 0.55)


def massg_whisper():
    rng = random.Random(SEED + 12)
    out = noise(3.4, rng, lp=0.01, hp=0.12)
    # a slow formant crawl gives the noise the shape of near-words
    crawl = 1.0 + 0.5 * np.sin(2 * math.pi * 0.7 * t(len(out) / RATE))
    out = out * crawl
    out += 0.18 * tone(70.0, 3.4, "sine") * np.sin(2 * math.pi * 0.25 * t(3.4))
    return norm(env(out, 0.3, 0.9), 0.45)


def massg_roar():
    rng = random.Random(SEED + 13)
    body = (tone(54.0, 3.2, "saw", 0.8) + tone(54.7, 3.2, "saw", 0.6)
            + tone(108.0, 3.2, "sine", 0.35) + tone(27.0, 3.2, "sine", 0.5))
    body = body * (1.0 + 0.4 * np.sin(2 * math.pi * 5.5 * t(3.2)))
    growl = noise(3.2, rng, lp=0.004, hp=0.05) * 0.5
    return norm(env(body + growl, 0.08, 0.5), 0.95)


def massg_heart():
    def thump(at, amp):
        seg = env(tone(46.0, 0.30, "sine", amp), 0.004, 0.16)
        low = 0.4 * env(tone(31.0, 0.30, "sine", amp), 0.006, 0.2)
        seg = seg + low[:len(seg)]
        return seg, at

    out = np.zeros(int(RATE * 1.9))
    for at, amp in ((0.02, 1.0), (0.34, 0.72), (1.02, 1.0), (1.34, 0.72)):
        seg, _ = thump(at, amp)
        i = int(RATE * at)
        out[i:i + len(seg)] += seg[:max(0, len(out) - i)]
    return norm(out, 0.9)


def oblivion_drone():
    rng = random.Random(SEED + 20)
    x = t(9.0)
    drone = np.zeros_like(x)
    for f, a in ((41.0, 0.7), (61.5, 0.5), (82.0, 0.34), (123.0, 0.2), (164.5, 0.12)):
        drone += a * np.sin(2 * math.pi * f * x + 0.4 * np.sin(2 * math.pi * 0.05 * x))
    shimmer = tone(1640.0, 9.0, "sine", 0.05) * (0.5 + 0.5 * np.sin(2 * math.pi * 0.11 * x))
    wind = noise(9.0, rng, lp=0.006, hp=0.02) * 0.25
    return norm(env(drone + shimmer + wind, 1.2, 2.0), 0.6)


def oblivion_glitch():
    rng = random.Random(SEED + 21)
    out = np.zeros(int(RATE * 3.0))
    at = 0
    while at < len(out) - 4000:
        n = rng.randrange(600, 4200)
        f = rng.choice([220.0, 340.0, 420.0, 660.0, 880.0])
        seg = bits(tone(f, n / RATE, rng.choice(["square", "saw"]), 0.5), 4)
        seg = env(seg, 0.001, 0.002)
        room = min(len(seg), len(out) - at)
        if room > 0:
            out[at:at + room] += seg[:room]
        at += n + rng.randrange(0, 1800)
    out += noise(3.0, rng, lp=0.5, hp=0.02) * 0.12
    return norm(out, 0.6)


def oblivion_warp():
    """A shepard tone: it falls forever and never arrives."""
    x = t(6.0)
    out = np.zeros_like(x)
    for k in range(6):
        f0 = 55.0 * (2 ** k)
        for i in range(3):
            f = f0 * (2 ** i) * (2 ** (x / 6.0))
            a = np.sin(2 * math.pi * f * x)
            out += a * (0.25 / (1 + i))
    return norm(env(out, 0.4, 1.2), 0.55)


def ui_terminal_open():
    up = sweep(160.0, 1400.0, 0.75, 0.5)
    up += 0.35 * sweep(320.0, 2800.0, 0.75, 0.4)
    up += 0.2 * tone(1200.0, 0.75, "sine")
    return norm(env(up, 0.01, 0.25), 0.7)


def mix2(a, b):
    """Two buffers of different lengths, summed over the shorter one."""
    n = min(len(a), len(b))
    return a[:n] + b[:n]


def ui_terminal_key():
    seg = env(tone(1980.0, 0.05, "sine", 0.6), 0.001, 0.02)
    seg = mix2(seg, env(tone(2960.0, 0.05, "sine", 0.3), 0.001, 0.01))
    return norm(seg, 0.5)


def ui_terminal_deny():
    parts = [env(tone(320.0, 0.16, "square", 0.45), 0.004, 0.04),
             env(tone(214.0, 0.30, "square", 0.45), 0.004, 0.12)]
    return norm(np.concatenate(parts), 0.6)


SOUNDS = {
    "radio/static": radio_static,
    "radio/carrier": radio_carrier,
    "radio/voice": radio_voice,
    "radio/distress": radio_distress,
    "radio/morse": radio_morse,
    "massg/breath": massg_breath,
    "massg/giggle": massg_giggle,
    "massg/whisper": massg_whisper,
    "massg/roar": massg_roar,
    "massg/heart": massg_heart,
    "oblivion/drone": oblivion_drone,
    "oblivion/glitch": oblivion_glitch,
    "oblivion/warp": oblivion_warp,
    "ui/terminal_open": ui_terminal_open,
    "ui/terminal_key": ui_terminal_key,
    "ui/terminal_deny": ui_terminal_deny,
}

# The SoundEvents the Java registers (McsmSounds), each bound to one file.
EVENTS = {
    "radio_static": "radio/static",
    "radio_carrier": "radio/carrier",
    "radio_voice": "radio/voice",
    "radio_distress": "radio/distress",
    "radio_morse": "radio/morse",
    "massg_breath": "massg/breath",
    "massg_giggle": "massg/giggle",
    "massg_whisper": "massg/whisper",
    "massg_roar": "massg/roar",
    "massg_heart": "massg/heart",
    "oblivion_drone": "oblivion/drone",
    "oblivion_glitch": "oblivion/glitch",
    "oblivion_warp": "oblivion/warp",
    "terminal_open": "ui/terminal_open",
    "terminal_key": "ui/terminal_key",
    "terminal_deny": "ui/terminal_deny",
}


# Every event gets a subtitle, so a player can see what they just heard (and so
# the file is legible in a bug report).
SUBTITLES = {
    "radio_static": "subtitles.mcsm.radio_static",
    "radio_carrier": "subtitles.mcsm.radio_carrier",
    "radio_voice": "subtitles.mcsm.radio_voice",
    "radio_distress": "subtitles.mcsm.radio_distress",
    "radio_morse": "subtitles.mcsm.radio_morse",
    "massg_breath": "subtitles.mcsm.massg_breath",
    "massg_giggle": "subtitles.mcsm.massg_giggle",
    "massg_whisper": "subtitles.mcsm.massg_whisper",
    "massg_roar": "subtitles.mcsm.massg_roar",
    "massg_heart": "subtitles.mcsm.massg_heart",
    "oblivion_drone": "subtitles.mcsm.oblivion_drone",
    "oblivion_glitch": "subtitles.mcsm.oblivion_glitch",
    "oblivion_warp": "subtitles.mcsm.oblivion_warp",
    "terminal_open": "subtitles.mcsm.terminal_open",
    "terminal_key": "subtitles.mcsm.terminal_key",
    "terminal_deny": "subtitles.mcsm.terminal_deny",
}

SUBTITLE_TEXT = {
    "subtitles.mcsm.radio_static": "Radio hiss",
    "subtitles.mcsm.radio_carrier": "A signal resolves",
    "subtitles.mcsm.radio_voice": "Someone speaks",
    "subtitles.mcsm.radio_distress": "A distress call",
    "subtitles.mcsm.radio_morse": "An automated beacon",
    "subtitles.mcsm.massg_breath": "It breathes",
    "subtitles.mcsm.massg_giggle": "It giggles",
    "subtitles.mcsm.massg_whisper": "It whispers your name",
    "subtitles.mcsm.massg_roar": "The MASSG arrives",
    "subtitles.mcsm.massg_heart": "Something enormous beats",
    "subtitles.mcsm.oblivion_drone": "The ruined dimension hums",
    "subtitles.mcsm.oblivion_glitch": "Reality tears",
    "subtitles.mcsm.oblivion_warp": "The world folds",
    "subtitles.mcsm.terminal_open": "The terminal wakes",
    "subtitles.mcsm.terminal_key": "Terminal key",
    "subtitles.mcsm.terminal_deny": "Access denied",
}

LANG = os.path.join(ROOT, "jar-overrides/assets/mcsm/lang/en_us.json")


def merge_lang():
    """Add the subtitle strings to the mod's own lang file, keeping every key."""
    doc = {}
    if os.path.isfile(LANG):
        try:
            with open(LANG) as f:
                doc = json.load(f)
        except Exception:
            doc = {}
    before = len(doc)
    doc.update(SUBTITLE_TEXT)
    with open(LANG, "w") as f:
        json.dump(doc, f, indent=2, sort_keys=True)
        f.write("\n")
    return len(doc) - before


def write_all():
    if sf is None:
        print("[sounds] soundfile/numpy are not available -- cannot write the Ogg files")
        return 1
    os.makedirs(OUT, exist_ok=True)
    made = []
    for name, fn in SOUNDS.items():
        buf = fn()
        path = os.path.join(OUT, name + ".ogg")
        os.makedirs(os.path.dirname(path), exist_ok=True)
        sf.write(path, np.asarray(buf, dtype=np.float64), RATE, format="OGG", subtype="VORBIS")
        made.append((name, os.path.getsize(path)))
    # MERGE, never clobber. assets/mcsm/sounds.json already carries the menu's
    # three UI one-shots (ds_btn_hover / ds_btn_click / ds_menu_open) and their
    # subtitles; writing our map over it would silently mute the menus.
    doc = {}
    if os.path.isfile(MANIFEST):
        try:
            with open(MANIFEST) as f:
                doc = json.load(f)
        except Exception:
            doc = {}
    for event, file in EVENTS.items():
        entry = {"sounds": [{"name": "mcsm:" + file, "stream": False}]}
        if event in SUBTITLES:
            entry["subtitle"] = SUBTITLES[event]
        doc[event] = entry
    with open(MANIFEST, "w") as f:
        json.dump(doc, f, indent=2, sort_keys=True)
        f.write("\n")
    total = sum(s for _n, s in made)
    for name, size in made:
        print("[sounds] %-22s %6d B" % (name, size))
    added = merge_lang()
    print("[sounds] %d files, %d bytes, sounds.json with %d events, %d new subtitles"
          % (len(made), total, len(doc), added))
    return 0


def check():
    problems = []
    MANIFEST_TEXT = open(MANIFEST).read() if os.path.isfile(MANIFEST) else ""
    if not os.path.isfile(MANIFEST):
        problems.append("assets/mcsm/sounds.json is missing -- run "
                        "python3 ci/make_mcsm_sounds.py")
    for name in SOUNDS:
        path = os.path.join(OUT, name + ".ogg")
        if not os.path.isfile(path):
            problems.append("%s.ogg is missing -- run python3 ci/make_mcsm_sounds.py" % name)
            continue
        if os.path.getsize(path) < 512:
            problems.append("%s.ogg is %d bytes -- too small to be real audio"
                            % (name, os.path.getsize(path)))
    if not problems:
        total = sum(os.path.getsize(os.path.join(OUT, n + ".ogg")) for n in SOUNDS)
        print("[sounds] OK -- %d custom Ogg files (%d bytes) bound by %d SoundEvents"
              % (len(SOUNDS), total, len(EVENTS)))
        return 0
    for p in problems:
        print("[sounds] FAIL %s" % p)
    return 1


def main(argv):
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true")
    args = ap.parse_args(argv)
    if args.check:
        return check()
    return write_all()


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
