class_name MovementProfile
extends Resource
## MovementProfile — physics tuning for ONE locomotion state.
## A pawn combines a base profile (from its evolution stage) with modifiers
## from unlocked abilities and from its current body scale. This separation
## lets designers tune "how swimming feels" once, not per-species.

@export_group("Speed")
@export var max_speed: float = 8.0
@export var sprint_multiplier: float = 1.8
@export var vertical_speed_scale: float = 1.0

@export_group("Response")
@export var acceleration: float = 30.0
@export var deceleration: float = 24.0
@export var drag: float = 1.2
@export var turn_speed: float = 3.0
@export var pitch_speed: float = 2.2
@export var align_speed: float = 6.0

@export_group("Medium / Physics")
@export var medium_density: float = 1.0       ## resistance of the surrounding medium (water > air)
@export var medium_resistance_exponent: float = 1.0
@export var buoyancy: float = 0.0
@export var gravity_scale: float = 1.0

@export_group("Feel")
@export var bank_angle: float = 0.4            ## visual roll while turning
@export var undulation: float = 0.0            ## body-wave amplitude (eel/fish)
@export var mass_speed_exponent: float = 0.18 ## bigger body -> slightly slower top speed
@export var fov_offset: float = 0.0
@export var bob_amplitude: float = 0.06
@export var bob_frequency: float = 2.0

@export_group("Ground Only")
@export var step_height: float = 0.35
@export var max_slope: float = 0.78539818      ## ~45 degrees

## Top speed after accounting for body scale (growth shrinks it gently).
func effective_max_speed(body_scale: float) -> float:
	return max_speed * pow(maxf(body_scale, 0.05), -mass_speed_exponent)
