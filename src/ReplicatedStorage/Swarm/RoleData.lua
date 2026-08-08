--!nonstrict
-- Shared material/color tables for clone cosmetics.
local RoleData = {}

RoleData.Materials = {
	SCOUT = Enum.Material.Neon,
	GATHERER = Enum.Material.Fabric,
	BUILDER = Enum.Material.Concrete,
	MEDIC = Enum.Material.SmoothPlastic,
	DEFENDER = Enum.Material.Metal,
	ENGINEER = Enum.Material.Slate,
	COORDINATOR = Enum.Material.DiamondPlate,
}

-- per-role chest trim color (slightly different from team color)
RoleData.Trim = {
	SCOUT = Color3.fromRGB(0, 170, 220),
	GATHERER = Color3.fromRGB(60, 180, 80),
	BUILDER = Color3.fromRGB(220, 130, 30),
	MEDIC = Color3.fromRGB(230, 60, 90),
	DEFENDER = Color3.fromRGB(220, 50, 50),
	ENGINEER = Color3.fromRGB(220, 180, 50),
	COORDINATOR = Color3.fromRGB(150, 80, 230),
}

return RoleData
