class_name ProceduralAudioKit
extends RefCounted
## ProceduralAudioKit — synthesises short PCM tones into AudioStreamWAV at
## runtime so the game has real audio WITHOUT any imported sound files. Used by
## AudioDirector for eat/evolve/hurt/UI blips. Asset-free by design.
##
## PCM is 16-bit little-endian mono. Guarded so a future engine change can't
## crash the game — callers treat the result as optional.

const MIX_RATE := 44100

enum ToneKind { EAT, EVOLVE, HURT, CLICK, PING, WHOOSH }

static func make_tone(kind: int) -> AudioStreamWAV:
	match kind:
		ToneKind.EAT: return _synth(520.0, 0.12, 0.5, 0.01, 0.08, 1.0)
		ToneKind.EVOLVE: return _arp([330.0, 440.0, 660.0], 0.5, 0.5)
		ToneKind.HURT: return _synth(140.0, 0.22, 0.7, 0.005, 0.18, 1.0)
		ToneKind.CLICK: return _synth(880.0, 0.05, 0.3, 0.002, 0.03, 1.0)
		ToneKind.PING: return _synth(1200.0, 0.18, 0.35, 0.005, 0.15, 1.0)
		ToneKind.WHOOSH: return _noise(0.2, 0.4)
		_: return _synth(440.0, 0.1, 0.4, 0.01, 0.08, 1.0)


static func _synth(freq: float, dur: float, vol: float, atk: float, rel: float, decay_exp: float) -> AudioStreamWAV:
	var n := int(dur * MIX_RATE)
	var data := PackedByteArray()
	data.resize(n * 2)
	for i in n:
		var t := float(i) / MIX_RATE
		var env := 1.0
		if t < atk:
			env = t / atk
		elif t > dur - rel:
			env = maxf(0.0, (dur - t) / rel)
		env *= exp(-decay_exp * t)
		var s := sin(TAU * freq * t) * env * vol
		var v := int(clampf(s, -1.0, 1.0) * 32767.0)
		var o := i * 2
		data[o] = v & 0xFF
		data[o + 1] = (v >> 8) & 0xFF
	return _wrap(data)


static func _arp(freqs: PackedFloat32Array, step: float, vol: float) -> AudioStreamWAV:
	var dur := step * freqs.size()
	var n := int(dur * MIX_RATE)
	var data := PackedByteArray()
	data.resize(n * 2)
	for i in n:
		var t := float(i) / MIX_RATE
		var idx := mini(int(t / step), freqs.size() - 1)
		var env := exp(-2.0 * (t - idx * step))
		var s := sin(TAU * freqs[idx] * t) * env * vol
		var v := int(clampf(s, -1.0, 1.0) * 32767.0)
		var o := i * 2
		data[o] = v & 0xFF
		data[o + 1] = (v >> 8) & 0xFF
	return _wrap(data)


static func _noise(dur: float, vol: float) -> AudioStreamWAV:
	var n := int(dur * MIX_RATE)
	var data := PackedByteArray()
	data.resize(n * 2)
	for i in n:
		var t := float(i) / MIX_RATE
		var env := exp(-4.0 * t)
		var s := (randf() * 2.0 - 1.0) * env * vol
		var v := int(clampf(s, -1.0, 1.0) * 32767.0)
		var o := i * 2
		data[o] = v & 0xFF
		data[o + 1] = (v >> 8) & 0xFF
	return _wrap(data)


static func _wrap(data: PackedByteArray) -> AudioStreamWAV:
	var wav := AudioStreamWAV.new()
	if "data" in wav:
		wav.data = data
	wav.mix_rate = MIX_RATE
	if "format" in wav:
		wav.format = AudioStreamWAV.FORMAT_16_BITS
	wav.stereo = false
	return wav
