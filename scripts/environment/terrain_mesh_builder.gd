class_name TerrainMeshBuilder
extends RefCounted
## TerrainMeshBuilder — builds a heightmap ground mesh at runtime (no imported
## terrain asset required). Uses FastNoiseLite for organic, seedable shapes and
## vertex colours for depth-based tinting so the seabed/forest floor reads well
## under volumetric fog.
##
## Called by biome scenes (BiomeReef/BiomeForest/...) in _ready. Pure helper.

const SUBDIV := 64


static func build(width: float, depth: float, height: float, seed: int, freq: float,
		low_color: Color, high_color: Color) -> Mesh:
	var noise := FastNoiseLite.new()
	noise.noise_type = FastNoiseLite.TYPE_SIMPLEX
	noise.seed = seed
	noise.frequency = freq
	noise.fractal_octaves = 4

	var st := SurfaceTool.new()
	st.begin(Mesh.PRIMITIVE_TRIANGLES)
	var half_w := width * 0.5
	var half_d := depth * 0.5
	var step_w := width / SUBDIV
	var step_d := depth / SUBDIV

	for z in (SUBDIV + 1):
		for x in (SUBDIV + 1):
			var wx := -half_w + x * step_w
			var wz := -half_d + z * step_d
			var h := noise.get_noise_2d(wx, wz) * height
			var t := clampf((h / height) * 0.5 + 0.5, 0.0, 1.0)
			var col := low_color.lerp(high_color, t)
			st.set_color(col)
			st.add_vertex(Vector3(wx, h, wz))
	for z in SUBDIV:
		for x in SUBDIV:
			var i0 := z * (SUBDIV + 1) + x
			var i1 := i0 + 1
			var i2 := i0 + (SUBDIV + 1)
			var i3 := i2 + 1
			st.add_index(i0); st.add_index(i2); st.add_index(i1)
			st.add_index(i1); st.add_index(i2); st.add_index(i3)
	st.generate_normals()
	return st.commit()
