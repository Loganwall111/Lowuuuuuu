class_name BiomeWetland
extends BaseBiome
## BiomeWetland — freshwater/marsh variant (mosquito path start). Shares all
## player/audio plumbing with BaseBiome; adds a shallow pond (WaterVolume),
## reeds (MultiMesh) and surface insects (fish scene reused as a generic
## ambient creature).
##
## ATTACH THIS SCRIPT TO: WetlandBiome (Node3D)  (WetlandBiome.tscn)

func _ready() -> void:
	biome_id = &"wetland"
	_build_world()


func _build_world() -> void:
	_build_terrain(180.0, 6.0, 13, Color(0.25, 0.3, 0.2), Color(0.4, 0.45, 0.3))
	_spawn_fish(fish_count)
