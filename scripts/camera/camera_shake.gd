class_name CameraShake
extends RefCounted
## CameraShake — trauma-based, deterministic shake (no random spikes).
## Trauma rises with events and decays continuously; the visible offset is
## trauma^2 so small bumps stay subtle and big hits feel punchy. Rotation and
## positional offsets are derived from continuous noise so they never pop.

var trauma: float = 0.0
var _time: float = 0.0
var _max_rotation: float = 0.06   ## radians
var _max_offset: float = 0.25     ## metres

var _rot: Vector3 = Vector3.ZERO
var _pos: Vector3 = Vector3.ZERO


func add_trauma(amount: float, _duration: float) -> void:
	trauma = clampf(trauma + amount, 0.0, 1.0)


func update(delta: float) -> void:
	_time += delta
	trauma = maxf(0.0, trauma - delta * 1.2)
	var shake := trauma * trauma
	_rot.x = _noise(_time * 37.0) * _max_rotation * shake
	_rot.y = _noise(_time * 29.0 + 11.0) * _max_rotation * shake
	_pos.x = _noise(_time * 41.0 + 5.0) * _max_offset * shake
	_pos.y = _noise(_time * 23.0 + 9.0) * _max_offset * shake


func get_offset() -> Vector3:
	return _pos


func get_rotation() -> Vector3:
	return _rot


static func _noise(t: float) -> float:
	# Cheap deterministic pseudo-noise: layered sines.
	return sin(t * 1.3) * 0.6 + sin(t * 2.7 + 1.1) * 0.4
