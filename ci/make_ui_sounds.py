#!/usr/bin/env python3
"""Synthesize the Devouring Storms UI sound set.

Writes the three Story Mode UI one-shots straight into
``jar-overrides/assets/mcsm/sounds/`` as **Ogg Vorbis**, which is the only
container Minecraft's sound engine can decode (JOrbis / VorbisAudioStream).
The previous pass shipped these as ``.wav``; Minecraft cannot decode RIFF
WAVE, so the events existed but were always silent -- that is why the menus
had no button noise at all.

Design (all mono, 44.1 kHz, peak-normalized, click-free fades):
  ds_btn_hover  ~0.10 s  soft crystal tink, quiet (0.42 peak)
  ds_btn_click  ~0.20 s  warm two-partial chime + microscopic noise tick
  ds_menu_open  ~0.55 s  rising filtered-noise whoosh blooming into a chord

Usage:  python3 ci/make_ui_sounds.py
Needs numpy + soundfile (``pip install numpy soundfile``); nothing else in
the build depends on this script, it only regenerates the committed assets.
"""
from __future__ import annotations

import os
import struct
import sys

import numpy as np

try:
    import soundfile as sf
except ImportError:  # pragma: no cover - tooling only
    sys.exit("make_ui_sounds: needs numpy + soundfile (pip install numpy soundfile)")

SR = 44100
OUT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..",
                       "jar-overrides", "assets", "mcsm", "sounds")
OUT_DIR = os.path.normpath(OUT_DIR)


def n_samples(seconds: float) -> int:
    return int(SR * seconds)


def exp_env(n: int, tau: float, attack: float) -> np.ndarray:
    """Percussive envelope: linear attack then exponential decay."""
    t = np.arange(n) / SR
    rise = np.clip(t / max(attack, 1e-6), 0.0, 1.0)
    return rise * np.exp(-t / max(tau, 1e-6))


def partials(n: int, layers: list[tuple[float, float, float]],
             detune: float = 0.0) -> np.ndarray:
    """layers = [(freq, amp, tau), ...]; slight detune spreads even partials."""
    t = np.arange(n) / SR
    out = np.zeros(n)
    for i, (freq, amp, tau) in enumerate(layers):
        f = freq * (1.0 + detune * (1 if i % 2 else -1))
        out += amp * np.sin(2.0 * np.pi * f * t) * exp_env(n, tau, 0.0015)
    return out


def noise(n: int, seed: int = 7) -> np.ndarray:
    return np.random.default_rng(seed).normal(0.0, 1.0, n)


def svf_bandpass(x: np.ndarray, cutoff: np.ndarray, q: float = 0.85) -> np.ndarray:
    """Per-sample state-variable band pass (lets the cutoff sweep)."""
    low = 0.0
    band = 0.0
    out = np.zeros(len(x))
    for i in range(len(x)):
        f = 2.0 * np.sin(np.pi * min(float(cutoff[i]), SR * 0.45) / SR)
        high = x[i] - low - q * band
        band += f * high
        low += f * band
        out[i] = band
    return out


def de_click(x: np.ndarray, ms: float = 1.5) -> np.ndarray:
    n = max(2, int(SR * ms / 1000.0))
    x = x.copy()
    x[:n] *= np.linspace(0.0, 1.0, n)
    x[-n:] *= np.linspace(1.0, 0.0, n)
    return x


def normalize(x: np.ndarray, peak: float) -> np.ndarray:
    m = float(np.max(np.abs(x))) if len(x) else 0.0
    if m <= 1e-9:
        return x
    return x / m * peak


def build_hover() -> np.ndarray:
    n = n_samples(0.105)
    body = partials(n, [(1568.0, 0.16, 0.030),
                        (3136.0, 0.055, 0.018),
                        (4699.0, 0.022, 0.010)])
    tick = noise(n) * exp_env(n, 0.0022, 0.0002) * 0.25
    tick = np.diff(tick, prepend=0.0)  # crude high pass -> soft "tick"
    return normalize(de_click(body + tick), 0.42)


def build_click() -> np.ndarray:
    n = n_samples(0.205)
    body = partials(n, [(523.25, 0.09, 0.075),
                        (1046.50, 0.22, 0.058),
                        (1568.00, 0.13, 0.046),
                        (2093.00, 0.070, 0.034),
                        (3136.00, 0.035, 0.020)],
                    detune=0.0016)
    tick = np.diff(noise(n) * exp_env(n, 0.0016, 0.0002), prepend=0.0) * 0.34
    x = np.tanh(1.25 * (body + tick)) / np.tanh(1.25)
    return normalize(de_click(x), 0.62)


def build_menu_open() -> np.ndarray:
    n = n_samples(0.55)
    # rising air whoosh
    t = np.arange(n) / SR
    cutoff = 320.0 * (3000.0 / 320.0) ** np.clip(t / 0.30, 0.0, 1.0)
    sweep = svf_bandpass(noise(n, seed=31), cutoff)
    rise = np.clip(t / 0.17, 0.0, 1.0) ** 1.4
    sweep *= rise * np.exp(-t / 0.13) * 0.5
    # chord bloom landing at 0.10 s
    delay = n_samples(0.10)
    chord_n = n - delay
    chord = partials(chord_n, [(440.00, 0.075, 0.190),
                               (554.37, 0.070, 0.185),
                               (659.25, 0.070, 0.180),
                               (880.00, 0.055, 0.160),
                               (1760.00, 0.020, 0.105)])
    bloom = np.zeros(n)
    bloom[delay:] = chord
    return normalize(de_click(sweep + bloom), 0.70)


def write(name: str, data: np.ndarray) -> None:
    os.makedirs(OUT_DIR, exist_ok=True)
    path = os.path.join(OUT_DIR, name + ".ogg")
    sf.write(path, data.astype(np.float32), SR, format="OGG", subtype="VORBIS")
    info = sf.info(path)
    raw = open(path, "rb").read()
    # sanity: real Ogg Vorbis pages, not a renamed wav
    ok = raw[:4] == b"OggS" and b"vorbis" in raw[:256]
    print("wrote %-28s %5.2f s  %6d B  ogg-vorbis=%s"
          % (name + ".ogg", info.duration, len(raw), ok))
    if not ok:
        sys.exit("make_ui_sounds: encoder did not produce Ogg Vorbis")
    if struct.unpack("<H", raw[28:30])[0] != 1:  # header type: first page
        pass


def main() -> None:
    write("ds_btn_hover", build_hover())
    write("ds_btn_click", build_click())
    write("ds_menu_open", build_menu_open())
    for dead in ("ds_btn_hover.wav", "ds_btn_click.wav", "ds_menu_open.wav"):
        p = os.path.join(OUT_DIR, dead)
        if os.path.exists(p):
            os.remove(p)
            print("removed undecodable", dead)


if __name__ == "__main__":
    main()
