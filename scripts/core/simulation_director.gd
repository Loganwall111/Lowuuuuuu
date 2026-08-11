extends Node
## SimulationDirector — distance-banded, time-sliced agent simulation
## (AUTOLOAD: "SimulationDirector").
##
## THE PROBLEM IT SOLVES
## A living ecosystem wants hundreds of creatures and thousands of food motes.
## If every one of them implements _physics_process(), the engine pays for a
## script call, a virtual dispatch and a frame of physics per agent per tick.
## That is the single most common reason "ecosystem" prototypes die at ~200
## entities.
##
## THE SOLUTION
## Agents do NOT tick themselves. They register here and receive
##     simulation_tick(delta_since_last_tick: float, lod: int)
## at a cadence chosen from their distance to the focus (the player camera):
##
##   LOD 0  (near)   every physics tick        full steering + animation
##   LOD 1  (mid)    every 4th tick            steering, no fine animation
##   LOD 2  (far)    every 16th tick           drift / statistical behaviour
##   LOD 3  (dormant) never ticked, hidden     frozen until the player returns
##
## Because `delta_since_last_tick` is passed in, an agent's motion integrates
## correctly no matter which band it is in — a creature does not slow down when
## it drops to LOD 2, it simply updates in coarser steps.
##
## Band re-evaluation is itself amortised: only a slice of the population is
## re-classified each frame (BUDGET_PER_FRAME), so the classification pass is
## O(budget) rather than O(agents).
##
## DEPENDENCIES: none (leaf service). Agents must implement
## `simulation_tick(delta: float, lod: int) -> void`.

const LOD_NEAR := 0
const LOD_MID := 1
const LOD_FAR := 2
const LOD_DORMANT := 3

## Squared distances (metres²) — squared to avoid sqrt in the hot loop.
const BAND_NEAR_SQ := 40.0 * 40.0
const BAND_MID_SQ := 110.0 * 110.0
const BAND_FAR_SQ := 300.0 * 300.0

const TICK_INTERVAL := [1, 4, 16, 0] ## in physics ticks; 0 == never
## How many agents get re-classified per frame.
const BUDGET_PER_FRAME := 96

var focus: Node3D = null

var _agents: Array[Node] = []
var _index_of: Dictionary = {}         # agent -> index (O(1) unregister)
var _lod: PackedByteArray = PackedByteArray()
var _countdown: PackedInt32Array = PackedInt32Array()
var _accumulated: PackedFloat32Array = PackedFloat32Array()
var _hide_when_dormant: PackedByteArray = PackedByteArray()
var _cursor: int = 0
var _focus_position: Vector3 = Vector3.ZERO

## Live statistics for the debug overlay.
var stats: Dictionary = {"agents": 0, "near": 0, "mid": 0, "far": 0, "dormant": 0, "ticked": 0}


func _ready() -> void:
	process_priority = -100 # run before gameplay nodes that read stats


func set_focus(node: Node3D) -> void:
	focus = node


## Registers an agent. `hide_when_dormant` lets purely decorative entities
## disappear entirely at extreme range (huge saving on draw calls); creatures
## that must stay visible on the horizon pass false.
func register_agent(agent: Node, hide_when_dormant: bool = true) -> void:
	if _index_of.has(agent):
		return
	if not agent.has_method("simulation_tick"):
		push_error("SimulationDirector: %s lacks simulation_tick()." % agent)
		return
	_index_of[agent] = _agents.size()
	_agents.append(agent)
	_lod.append(LOD_NEAR)
	_countdown.append(0)
	_accumulated.append(0.0)
	_hide_when_dormant.append(1 if hide_when_dormant else 0)
	if not agent.tree_exiting.is_connected(_on_agent_exiting):
		agent.tree_exiting.connect(_on_agent_exiting.bind(agent), CONNECT_ONE_SHOT)


func unregister_agent(agent: Node) -> void:
	if not _index_of.has(agent):
		return
	var idx: int = _index_of[agent]
	var last: int = _agents.size() - 1
	# Swap-remove keeps registration O(1) and avoids reallocating the arrays.
	if idx != last:
		var moved: Node = _agents[last]
		_agents[idx] = moved
		_lod[idx] = _lod[last]
		_countdown[idx] = _countdown[last]
		_accumulated[idx] = _accumulated[last]
		_hide_when_dormant[idx] = _hide_when_dormant[last]
		_index_of[moved] = idx
	_agents.resize(last)
	_lod.resize(last)
	_countdown.resize(last)
	_accumulated.resize(last)
	_hide_when_dormant.resize(last)
	_index_of.erase(agent)
	if _cursor > last:
		_cursor = 0


func _on_agent_exiting(agent: Node) -> void:
	unregister_agent(agent)


func _physics_process(delta: float) -> void:
	var count := _agents.size()
	if count == 0:
		return
	if is_instance_valid(focus):
		_focus_position = focus.global_position

	_reclassify_slice(count)

	var near_count := 0
	var mid_count := 0
	var far_count := 0
	var dormant_count := 0
	var ticked := 0

	for i in count:
		var band := _lod[i]
		match band:
			LOD_NEAR: near_count += 1
			LOD_MID: mid_count += 1
			LOD_FAR: far_count += 1
			_: dormant_count += 1
		if band == LOD_DORMANT:
			continue
		_accumulated[i] += delta
		_countdown[i] -= 1
		if _countdown[i] > 0:
			continue
		_countdown[i] = TICK_INTERVAL[band]
		var agent: Node = _agents[i]
		if not is_instance_valid(agent):
			continue
		agent.simulation_tick(_accumulated[i], band)
		_accumulated[i] = 0.0
		ticked += 1

	stats["agents"] = count
	stats["near"] = near_count
	stats["mid"] = mid_count
	stats["far"] = far_count
	stats["dormant"] = dormant_count
	stats["ticked"] = ticked


## Re-classifies at most BUDGET_PER_FRAME agents, wrapping around the array.
func _reclassify_slice(count: int) -> void:
	var budget: int = mini(BUDGET_PER_FRAME, count)
	for _n in budget:
		if _cursor >= count:
			_cursor = 0
		var agent: Node = _agents[_cursor]
		if agent is Node3D and is_instance_valid(agent):
			var d_sq: float = (agent as Node3D).global_position.distance_squared_to(_focus_position)
			var band := LOD_DORMANT
			if d_sq < BAND_NEAR_SQ:
				band = LOD_NEAR
			elif d_sq < BAND_MID_SQ:
				band = LOD_MID
			elif d_sq < BAND_FAR_SQ:
				band = LOD_FAR
			if band != _lod[_cursor]:
				_lod[_cursor] = band
				_countdown[_cursor] = 0
				if _hide_when_dormant[_cursor] == 1:
					(agent as Node3D).visible = band != LOD_DORMANT
				if agent.has_method("on_simulation_lod_changed"):
					agent.on_simulation_lod_changed(band)
		_cursor += 1


func get_neighbours(query: Node3D, radius: float = 30.0, max_count: int = 24) -> Array:
	var out: Array = []
	if query == null:
		return out
	var qp := query.global_position
	for a in _agents:
		if a == query or not (a is Node3D) or not is_instance_valid(a):
			continue
		if (a as Node3D).global_position.distance_to(qp) <= radius:
			out.append(a)
		if out.size() >= max_count:
			break
	return out

func get_agent_count() -> int:
	return _agents.size()
