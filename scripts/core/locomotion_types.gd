class_name LocomotionTypes
extends RefCounted
## LocomotionTypes — shared integer identifiers for movement states.
## Stored as an int (not an Object) so a pawn's current_state can be written to
## save files, compared in O(1), and broadcast on the EventBus cheaply.
## The LocomotionController maps these IDs to concrete state objects at runtime
## via a registry, so adding a state (e.g. BURROWING) is purely additive.

enum State {
	SWIMMING,
	FLOATING,
	CRAWLING,
	WALKING,
	RUNNING,
	FLYING,
	CLIMBING,
	SPACE_FLIGHT,
}

enum Capability {
	GROUND,
	WATER,
	AIR,
	ZERO_G,
}

static func name_of(state: int) -> StringName:
	match state:
		State.SWIMMING: return &"Swimming"
		State.FLOATING: return &"Floating"
		State.CRAWLING: return &"Crawling"
		State.WALKING: return &"Walking"
		State.RUNNING: return &"Running"
		State.FLYING: return &"Flying"
		State.CLIMBING: return &"Climbing"
		State.SPACE_FLIGHT: return &"Space Flight"
		_: return &"Unknown"

static func has_capability(state: int) -> int:
	match state:
		State.CRAWLING, State.WALKING, State.RUNNING: return Capability.GROUND
		State.SWIMMING, State.FLOATING: return Capability.WATER
		State.FLYING: return Capability.AIR
		State.CLIMBING: return Capability.GROUND
		State.SPACE_FLIGHT: return Capability.ZERO_G
		_: return Capability.GROUND
