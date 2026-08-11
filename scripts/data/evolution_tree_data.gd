class_name EvolutionTreeData
extends Resource
## EvolutionTreeData — an ordered, branching list of stages for one lineage.
## Implemented as a flat array + an id index so (a) designers edit it in the
## Inspector like a list, and (b) the runtime gets O(1) lookups by stage_id.

@export var tree_name: String
@export var root_stage_id: StringName
@export var stages: Array[EvolutionStageData] = []

var _index: Dictionary = {}

func _build_index() -> void:
	_index.clear()
	for stage in stages:
		if stage != null and stage.stage_id != &"":
			_index[stage.stage_id] = stage

func get_stage(stage_id: StringName) -> EvolutionStageData:
	if _index.is_empty():
		_build_index()
	return _index.get(stage_id, null) as EvolutionStageData

func get_root() -> EvolutionStageData:
	return get_stage(root_stage_id)

func get_descendants(stage_id: StringName) -> Array[EvolutionStageData]:
	var stage := get_stage(stage_id)
	if stage == null:
		return []
	var out: Array[EvolutionStageData] = []
	for next_id in stage.next_stage_ids:
		var child := get_stage(next_id)
		if child != null:
			out.append(child)
	return out

## Returns a human-readable list of problems so the design team can catch a
## dangling branch reference before it ever reaches play.
func validate() -> Array[String]:
	var problems: Array[String] = []
	if stages.is_empty():
		problems.append("Tree '%s' has no stages." % tree_name)
		return problems
	if get_root() == null:
		problems.append("Tree '%s' root '%s' not found." % [tree_name, root_stage_id])
	var ids: Dictionary = {}
	for stage in stages:
		if stage == null:
			problems.append("Null stage in tree '%s'." % tree_name)
			continue
		if stage.stage_id == &"":
			problems.append("A stage in '%s' has an empty id." % tree_name)
			continue
		if ids.has(stage.stage_id):
			problems.append("Duplicate stage id '%s'." % stage.stage_id)
		ids[stage.stage_id] = true
		for nxt in stage.next_stage_ids:
			if not ids.has(nxt) and get_stage(nxt) == null:
				problems.append("Stage '%s' points to missing '%s'." % [stage.stage_id, nxt])
	return problems
