--!nonstrict
-- SHARED // CLONE: Multi-Volume Agent Swarm
-- Constants + helpers used by BOTH the server and the client.

local Shared = {}

Shared.GameTitle = "CLONE // MULTI-VOLUME AGENT SWARM"
Shared.GameSubtitle = "35 clones · 7 volumes · one hive mind"
Shared.Version = "1.0.0"

Shared.CLONE_TAG = "SwarmClone"
Shared.HOSTILE_TAG = "NullTick"
Shared.NODE_TAG = "CrystalNode"

-- Ordered so agent numbering is deterministic (SCO-01.., GAT-01.., ...)
Shared.RoleOrder = {
	"SCOUT", "GATHERER", "BUILDER", "MEDIC", "DEFENDER", "ENGINEER", "COORDINATOR",
}

Shared.Roles = {
	SCOUT = {
		label = "Scout", color = Color3.fromRGB(0, 214, 255), letter = "S", prefix = "SCO", count = 5,
		hp = 70, speed = 22, tool = "scanner",
		desc = "Recon unit. Reveals sectors and marks crystal nodes.",
	},
	GATHERER = {
		label = "Gatherer", color = Color3.fromRGB(96, 220, 110), letter = "G", prefix = "GAT", count = 8,
		hp = 90, speed = 16, tool = "pickaxe",
		desc = "Mines crystal nodes and hauls ore to the depot.",
	},
	BUILDER = {
		label = "Builder", color = Color3.fromRGB(255, 168, 60), letter = "B", prefix = "BLD", count = 5,
		hp = 100, speed = 14, tool = "wrench",
		desc = "Raises walls, turrets and the antenna from depot stock.",
	},
	MEDIC = {
		label = "Medic", color = Color3.fromRGB(255, 96, 120), letter = "M", prefix = "MED", count = 4,
		hp = 80, speed = 18, tool = "medkit",
		desc = "Repairs clones and structures in the field.",
	},
	DEFENDER = {
		label = "Defender", color = Color3.fromRGB(255, 80, 80), letter = "D", prefix = "DEF", count = 5,
		hp = 130, speed = 19, tool = "rifle",
		desc = "Engages Null Ticks before they reach the swarm.",
	},
	ENGINEER = {
		label = "Engineer", color = Color3.fromRGB(255, 220, 90), letter = "E", prefix = "ENG", count = 4,
		hp = 95, speed = 15, tool = "hammer",
		desc = "Upgrades depot, antenna and turret systems.",
	},
	COORDINATOR = {
		label = "Coordinator", color = Color3.fromRGB(190, 120, 255), letter = "C", prefix = "CRD", count = 4,
		hp = 85, speed = 20, tool = "beacon",
		desc = "Hive-mind units. Rebalance sectors and boost nearby workers.",
	},
}

-- 35 unique unit names (one per clone)
Shared.UnitNames = {
	SCOUT = { "Echo", "Vega", "Nimbus", "Pixel", "Sable" },
	GATHERER = { "Rust", "Moss", "Ember", "Fern", "Tide", "Slate", "Wisp", "Cinder" },
	BUILDER = { "Faber", "Mason", "Onyx", "Brick", "Quill" },
	MEDIC = { "Salve", "Tend", "Cure", "Patch" },
	DEFENDER = { "Blitz", "Valk", "Bash", "Rex", "Shard" },
	ENGINEER = { "Cog", "Bolt", "Spanner", "Volt" },
	COORDINATOR = { "Oracle", "Hive", "Matrix", "Node" },
}

Shared.Bios = {
	"Prefers the left flank of every formation.",
	"Counts every step between sectors.",
	"Humming a tune only clones can hear.",
	"Refuses to be called a copy.",
	"Hoarder of shiny rocks. Very shiny.",
	"Always double-checks the hive's math.",
	"Once got lost in VAULT-1 for an hour.",
	"Bets on wave spawn timings. Usually wins.",
	"Pings the hive network two hundred times a minute.",
	"Patiently waits for the swarm to catch up.",
	"Optimizes patrol routes nobody asked for.",
	"Secretly names every Null Tick.",
	"Knows the depot's exact crystal count. Always.",
	"Takes 'work together' a little too literally.",
	"Trained on 40,000 hours of swarm footage.",
	"Writes poetry about the hive mind.",
}

-- The seven "volumes" the swarm operates in.
Shared.Volumes = {
	{ id = "VAULT-1", name = "VAULT-1", sub = "Command Core", center = Vector3.new(0, 0, 0), size = Vector3.new(76, 40, 76),
		tint = Color3.fromRGB(150, 160, 255), isHome = true },
	{ id = "ARC-2", name = "ARC-2", sub = "Crystal Ridge", center = Vector3.new(96, 0, 0), size = Vector3.new(84, 40, 84),
		tint = Color3.fromRGB(0, 220, 200) },
	{ id = "ARC-3", name = "ARC-3", sub = "North Reaches", center = Vector3.new(48, 0, 84), size = Vector3.new(84, 40, 84),
		tint = Color3.fromRGB(0, 200, 255) },
	{ id = "ARC-4", name = "ARC-4", sub = "Ashen Field", center = Vector3.new(-48, 0, 84), size = Vector3.new(84, 40, 84),
		tint = Color3.fromRGB(255, 170, 60) },
	{ id = "ARC-5", name = "ARC-5", sub = "Static Drift", center = Vector3.new(-96, 0, 0), size = Vector3.new(84, 40, 84),
		tint = Color3.fromRGB(170, 120, 255) },
	{ id = "ARC-6", name = "ARC-6", sub = "Ember Hollow", center = Vector3.new(-48, 0, -84), size = Vector3.new(84, 40, 84),
		tint = Color3.fromRGB(255, 90, 120) },
	{ id = "ARC-7", name = "ARC-7", sub = "Mist Basin", center = Vector3.new(48, 0, -84), size = Vector3.new(84, 40, 84),
		tint = Color3.fromRGB(120, 220, 120) },
}

Shared.RemoteNames = { "RequestAgentInfo", "StatsBroadcast", "TaskBroadcast", "FeedBroadcast", "Command" }

-- helpers ------------------------------------------------------------

function Shared.clamp(v, a, b)
	return math.max(a, math.min(b, v))
end

function Shared.lerp(a, b, t)
	return a + (b - a) * t
end

function Shared.format(n)
	if n >= 1000000 then
		return string.format("%.1fM", n / 1000000)
	elseif n >= 10000 then
		return string.format("%.1fk", n / 1000)
	end
	return string.format("%d", math.floor(n + 0.5))
end

function Shared.randomInRect(center, size)
	return center + Vector3.new((math.random() - 0.5) * size.X, 0, (math.random() - 0.5) * size.Z)
end

function Shared.randomInSector(sectorDef, inset)
	local s = sectorDef.size
	return sectorDef.center + Vector3.new(
		(math.random() - 0.5) * (s.X - inset * 2),
		0,
		(math.random() - 0.5) * (s.Z - inset * 2)
	)
end

function Shared.sectorIdAt(sectors, pos)
	for _, sec in ipairs(sectors) do
		if sec.region:ContainsPoint(pos) then
			return sec.def.id
		end
	end
	return nil
end

return Shared
