extends GPUParticles3D
## Plume3D — a pooled bubble/blood/mist burst used for bites, deaths and ambient
## vents. Self-frees to the pool on finish so thousands of events never churn
## the allocator.
##
## ATTACH THIS SCRIPT TO: GPUParticles3D (a plume prefab). Released via
## PoolService.release(node) which calls _on_pool_released() to stop + reset.

func _ready() -> void:
	one_shot = true
	if not finished.is_connected(_on_finished):
		finished.connect(_on_finished)


func _on_finished() -> void:
	if get_parent() != null:
		PoolService.release(self)


func _on_pool_released() -> void:
	emitting = false


func _on_pool_acquired() -> void:
	restart(true)
