--!nonstrict
-- Server utility library: world building, flocking, navigation, damage.
local SwarmLib = {}
local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Shared = require(ReplicatedStorage.Swarm.Shared)
local CrystalModel = require(ReplicatedStorage.Swarm.CrystalModel)

local Workspace = game:GetService("Workspace")
local RunService = game:GetService("RunService")
local PhysicsService = game:GetService("PhysicsService")

-- FLOCKING ------------------------------------------------------------------

-- Separation: steer away from nearby clones
function SwarmLib.separation(agent, agents, radius, weight)
	local pos = agent.Hrp.Position
	local steer = Vector3.new()
	local count = 0
	for i = 1, #agents do
		local other = agents[i]
		if other ~= agent and other.Alive then
			local d = (other.Hrp.Position - pos)
			local dist = d.Magnitude
			if dist > 0.01 and dist < radius then
				steer = steer + d / math.max(dist * dist, 0.5)
				count = count + 1
			end
		end
	end
	if count > 0 then
		steer = steer / count
		steer = steer * -weight
	end
	return steer
end

-- Cohesion: steer toward the local centroid
function SwarmLib.cohesion(agent, agents, radius, weight)
	local pos = agent.Hrp.Position
	local centroid = Vector3.new()
	local count = 0
	for i = 1, #agents do
		local other = agents[i]
		if other ~= agent and other.Alive then
			local d = other.Hrp.Position - pos
			if d.Magnitude < radius then
				centroid = centroid + other.Hrp.Position
				count = count + 1
			end
		end
	end
	if count == 0 then return Vector3.new() end
	centroid = centroid / count
	local toCentroid = centroid - pos
	return toCentroid * weight
end

-- Alignment: steer toward average heading of neighbors
function SwarmLib.alignment(agent, agents, radius, weight)
	local pos = agent.Hrp.Position
	local avg = Vector3.new()
	local count = 0
	for i = 1, #agents do
		local other = agents[i]
		if other ~= agent and other.Alive then
			if (other.Hrp.Position - pos).Magnitude < radius then
				avg = avg + (other.Hrp.Velocity * Vector3.new(1, 0, 1))
				count = count + 1
			end
		end
	end
	if count == 0 then return Vector3.new() end
	return (avg / count) * weight
end

function SwarmLib.flock(agent, agents, weights)
	local sep = SwarmLib.separation(agent, agents, 3.5, weights.sep or 2.2)
	local coh = SwarmLib.cohesion(agent, agents, 14, weights.coh or 0.35)
	local ali = SwarmLib.alignment(agent, agents, 10, weights.ali or 0.5)
	return sep + coh + ali
end

-- NAVIGATION -----------------------------------------------------------------

local NavCache = {}

function SwarmLib.findPath(from, to)
	local key = Vector3.new(math.floor(from.X), math.floor(from.Y), math.floor(from.Z))
	local target = Vector3.new(math.floor(to.X), math.floor(to.Y), math.floor(to.Z))
	local cacheKey = tostring(key) .. "|" .. tostring(target)
	local cached = NavCache[cacheKey]
	if cached then return cached end

	local params = RaycastParams.new()
	params.FilterType = Enum.RaycastFilterType.Exclude
	params.FilterDescendantsInstances = { game.Workspace }
	local function clear(fromPos, toPos)
		local r = Workspace:Raycast(fromPos + Vector3.new(0, 0.6, 0), (toPos - fromPos).Unit * (toPos - fromPos).Magnitude, params)
		return r == nil
	end
	if clear(from, to) then
		local path = { from, to }
		NavCache[cacheKey] = path
		return path
	end
	-- blocked: try two detour waypoints perpendicular to the line
	local dir = (to - from)
	local perp = Vector3.new(-dir.Z, 0, dir.X).Unit
	local path = nil
	for _, s in ipairs({ 1, -1 }) do
		local mid = (from + to) / 2
		local detour = mid + perp * s * 6
		if clear(from, detour) and clear(detour, to) then
			path = { from, detour + Vector3.new(0, 1, 0), to }
			break
		end
	end
	if path then
		NavCache[cacheKey] = path
		if #NavCache > 60 then
			local keys = {}
			for k in pairs(NavCache) do keys[#keys + 1] = k end
			for i = 1, 20 do NavCache[keys[i]] = nil end
		end
		return path
	end
	return { from, to }
end

function SwarmLib.moveToward(agent, targetPos, dt, customSpeed)
	local role = Shared.Roles[agent.RoleId]
	local speed = customSpeed or role.speed
	local flock = Vector3.new()
	if agent.Controller then
		flock = SwarmLib.flock(agent, agent.Controller.Agents, agent.FlockWeight)
	end
	-- cheap obstacle detour: if the direct line is blocked, veer around it
	if agent.DetourCheck == nil or os.clock() - agent.DetourCheck > 0.4 then
		agent.DetourCheck = os.clock()
		local params = RaycastParams.new()
		params.FilterType = Enum.RaycastFilterType.Exclude
		local grid = Workspace:FindFirstChild("SwarmWorld") and Workspace.SwarmWorld:FindFirstChild("Grid")
		params.FilterDescendantsInstances = { agent.Hrp, grid or Instance.new("Folder") }
		local dir = (targetPos - agent.Hrp.Position) * Vector3.new(1, 0, 1)
		local hit = dir.Magnitude > 1 and Workspace:Raycast(agent.Hrp.Position + Vector3.new(0, 1, 0), dir.Unit * dir.Magnitude, params) or nil
		if hit then
			agent.Detour = hit.Position + Vector3.new(-dir.Z, 0, dir.X).Unit * 5
		else
			agent.Detour = nil
		end
	end
	if agent.Detour and (agent.Detour - targetPos).Magnitude < 2 then
		agent.Detour = nil
	end
	local effective = agent.Detour or targetPos
	local toTarget = effective - agent.Hrp.Position
	toTarget = Vector3.new(toTarget.X, 0, toTarget.Z)
	local mag = toTarget.Magnitude
	local desired = Vector3.new()
	if mag > 0.01 then
		desired = toTarget / mag * speed
	end
	local move = (desired + flock * 0.8) * Vector3.new(1, 0, 1)
	agent.Hrp.Velocity = Vector3.new(move.X, agent.Hrp.Velocity.Y, move.Z)
	if mag > 0.5 then
		local look = Vector3.new(toTarget.X, 0, toTarget.Z).Unit
		local current = agent.Hrp.CFrame.LookVector
		local blended = current:Lerp(look, math.min(1, dt * 8)).Unit
		if blended.Magnitude > 0.01 then
			agent.Hrp.CFrame = CFrame.new(agent.Hrp.Position, agent.Hrp.Position + blended)
		end
	end
	return mag
end

-- DAMAGE ---------------------------------------------------------------------

function SwarmLib.hurtModel(model, amount, attacker)
	local hp = model:GetAttribute("Hp") or 0
	hp = hp - amount
	model:SetAttribute("Hp", math.max(0, hp))
	local humanoid = model:FindFirstChildOfClass("Humanoid")
	if humanoid then
		humanoid.Health = math.max(0, hp)
	end
	if hp <= 0 then
		model:SetAttribute("Hp", 0)
		return true -- dead
	end
	return false
end

-- WORLD ----------------------------------------------------------------------

function SwarmLib.buildWorld(sectors)
	local folder = Workspace:FindFirstChild("SwarmWorld") or Instance.new("Folder")
	folder.Name = "SwarmWorld"
	folder.Parent = Workspace

	local grid = folder:FindFirstChild("Grid") or Instance.new("Folder")
	grid.Name = "Grid"
	grid.Parent = folder

	for _, sec in ipairs(sectors) do
		local def = sec.def
		-- floor slab (top surface at y=0 so characters stand on it)
		local floor = Instance.new("Part")
		floor.Name = def.id .. "_Floor"
		floor.Size = Vector3.new(def.size.X - 8, 2, def.size.Z - 8)
		floor.CFrame = CFrame.new(def.center.X, -1, def.center.Z)
		floor.Anchored = true
		floor.Material = Enum.Material.Slate
		floor.Color = Color3.fromRGB(58, 62, 80)
		floor:SetAttribute("Sector", def.id)
		floor.Parent = folder
		PhysicsService:SetPartCollisionGroup(floor, "SectorFloor")
	end

	-- volume wireframe borders + edge lights
	for _, sec in ipairs(sectors) do
		local def = sec.def
		local size = def.size
		local c = def.center
		local half = Vector3.new(size.X / 2, 0, size.Z / 2)
		local corners = {
			c + Vector3.new(-half.X, 1.5, -half.Z), c + Vector3.new(half.X, 1.5, -half.Z),
			c + Vector3.new(half.X, 1.5, half.Z), c + Vector3.new(-half.X, 1.5, half.Z),
		}
		for i = 1, 4 do
			local p1 = corners[i]
			local p2 = corners[i % 4 + 1]
			local mid = (p1 + p2) / 2
			local len = (p2 - p1).Magnitude
			local bar = Instance.new("Part")
			bar.Name = def.id .. "_Border"
			bar.Size = Vector3.new(len, 0.35, 0.35)
			bar.CFrame = CFrame.new(mid, p2) * CFrame.Angles(math.rad(90), 0, 0)
			bar.Anchored = true
			bar.CanCollide = false
			bar.Material = Enum.Material.Neon
			bar.Color = def.tint
			bar.Transparency = 0.35
			bar.Parent = grid

			local pillar = Instance.new("Part")
			pillar.Name = def.id .. "_Pillar"
			pillar.Size = Vector3.new(0.7, 7, 0.7)
			pillar.CFrame = CFrame.new(corners[i].X, 3.2, corners[i].Z)
			pillar.Anchored = true
			pillar.CanCollide = false
			pillar.Material = Enum.Material.Metal
			pillar.Color = def.tint
			pillar.Transparency = 0.2
			pillar.Parent = grid

			local tip = Instance.new("Part")
			tip.Name = "Tip"
			tip.Size = Vector3.new(1.2, 1.2, 1.2)
			tip.Shape = Enum.PartType.Ball
			tip.CFrame = CFrame.new(corners[i].X, 6.9, corners[i].Z)
			tip.Anchored = true
			tip.CanCollide = false
			tip.Material = Enum.Material.Neon
			tip.Color = def.tint
			tip.Parent = pillar
		end
	end

	-- player spawn pad (kept clear of the depot model)
	local spawnPad = Instance.new("Part")
	spawnPad.Name = "SpawnPad"
	spawnPad.Size = Vector3.new(10, 0.6, 10)
	spawnPad.CFrame = CFrame.new(0, 0.1, 14)
	spawnPad.Anchored = true
	spawnPad.Material = Enum.Material.Neon
	spawnPad.Color = Color3.fromRGB(0, 220, 200)
	spawnPad.Transparency = 0.35
	spawnPad.Parent = folder
	local spawnLoc = Instance.new("SpawnLocation")
	spawnLoc.Name = "SpawnLocation"
	spawnLoc.Size = Vector3.new(6, 1, 6)
	spawnLoc.CFrame = CFrame.new(0, 0.8, 14)
	spawnLoc.Anchored = true
	spawnLoc.Material = Enum.Material.Neon
	spawnLoc.Color = Color3.fromRGB(0, 240, 220)
	spawnLoc.Transparency = 0.5
	spawnLoc.Duration = 1.5
	spawnLoc.Neutral = true
	spawnLoc.Parent = Workspace -- must be a direct child of Workspace for the spawn system
	local spawnGlow = Instance.new("PointLight")
	spawnGlow.Color = Color3.fromRGB(0, 240, 220)
	spawnGlow.Range = 16
	spawnGlow.Brightness = 2
	spawnGlow.Parent = spawnLoc

	-- overhead holographic name sign per sector
	for _, sec in ipairs(sectors) do
		local def = sec.def
		local part = Instance.new("Part")
		part.Name = def.id .. "_Sign"
		part.Size = Vector3.new(def.size.X - 10, 0.8, 0.4)
		part.CFrame = CFrame.new(def.center.X, 3.4, def.center.Z - def.size.Z / 2 + 3)
		part.Anchored = true
		part.CanCollide = false
		part.Transparency = 1
		part.Parent = folder
		local gui = Instance.new("SurfaceGui")
		gui.Name = "SignGui"
		gui.Face = Enum.NormalId.Front
		gui.SizingMode = Enum.SurfaceGuiSizingMode.FixedSize
		gui.CanvasSize = Vector2.new(600, 80)
		gui.Parent = part
		local lbl = Instance.new("TextLabel")
		lbl.Size = UDim2.fromScale(1, 1)
		lbl.BackgroundTransparency = 1
		lbl.Font = Enum.Font.GothamBlack
		lbl.Text = def.id .. " · " .. def.sub
		lbl.TextScaled = true
		lbl.TextColor3 = def.tint
		lbl.TextStrokeTransparency = 0
		lbl.Parent = gui
	end

	-- skybox + lighting
	local Lighting = game:GetService("Lighting")
	Lighting.Ambient = Color3.fromRGB(70, 70, 90)
	Lighting.OutdoorAmbient = Color3.fromRGB(90, 90, 120)
	Lighting.Brightness = 1.6
	Lighting.ClockTime = 18
	Lighting.FogColor = Color3.fromRGB(28, 24, 44)
	Lighting.FogStart = 120
	Lighting.FogEnd = 380

	local atmo = Lighting:FindFirstChildOfClass("Atmosphere") or Instance.new("Atmosphere")
	atmo.Density = 0.32
	atmo.Haze = 3
	atmo.Parent = Lighting
	local bloom = Lighting:FindFirstChildOfClass("BloomEffect") or Instance.new("BloomEffect")
	bloom.Intensity = 0.55
	bloom.Size = 26
	bloom.Parent = Lighting
	local cc = Lighting:FindFirstChildOfClass("ColorCorrectionEffect") or Instance.new("ColorCorrectionEffect")
	cc.Saturation = 0.05
	cc.Contrast = 0.1
	cc.TintColor = Color3.fromRGB(235, 230, 255)
	cc.Parent = Lighting
end

function SwarmLib.buildBaseStructures(structuresFolder)
	-- Depot (home structure)
	local depot = Instance.new("Model")
	depot.Name = "Depot"
	local base = Instance.new("Part")
	base.Name = "Core"
	base.Size = Vector3.new(10, 3.4, 8)
	base.CFrame = CFrame.new(0, 1.7, 0)
	base.Anchored = true
	base.Material = Enum.Material.Metal
	base.Color = Color3.fromRGB(90, 96, 120)
	base:SetAttribute("Structure", "Depot")
	base:SetAttribute("MaxHp", 900)
	base:SetAttribute("Hp", 900)
	base:SetAttribute("Level", 1)
	base.Parent = depot
	local ring = Instance.new("Part")
	ring.Name = "Ring"
	ring.Size = Vector3.new(12, 1.6, 10)
	ring.CFrame = CFrame.new(0, 0.8, 0)
	ring.Anchored = true
	ring.Material = Enum.Material.Neon
	ring.Color = Color3.fromRGB(0, 220, 200)
	ring.Transparency = 0.25
	ring.Parent = depot
	local light = Instance.new("Part")
	light.Name = "CoreLight"
	light.Size = Vector3.new(2.2, 2.2, 2.2)
	light.Shape = Enum.PartType.Ball
	light.CFrame = CFrame.new(0, 3.4, 0)
	light.Anchored = true
	light.CanCollide = false
	light.Material = Enum.Material.Neon
	light.Color = Color3.fromRGB(0, 240, 220)
	light.Parent = depot
	local pt = Instance.new("PointLight")
	pt.Color = Color3.fromRGB(0, 240, 220)
	pt.Range = 24
	pt.Brightness = 2
	pt.Parent = light
	depot.Parent = structuresFolder
	depot:SetAttribute("Structure", "Depot")

	-- Antenna (central tower)
	local antenna = Instance.new("Model")
	antenna.Name = "Antenna"
	local pole = Instance.new("Part")
	pole.Name = "Pole"
	pole.Size = Vector3.new(1, 14, 1)
	pole.CFrame = CFrame.new(0, 7, -16)
	pole.Anchored = true
	pole.Material = Enum.Material.Metal
	pole.Color = Color3.fromRGB(160, 165, 185)
	pole:SetAttribute("Structure", "Antenna")
	pole:SetAttribute("MaxHp", 700)
	pole:SetAttribute("Hp", 700)
	pole.Parent = antenna
	local dish = Instance.new("Part")
	dish.Name = "Dish"
	dish.Size = Vector3.new(0.6, 4, 5)
	dish.CFrame = CFrame.new(0, 13, -17.2) * CFrame.Angles(0, math.rad(90), math.rad(35))
	dish.Anchored = true
	dish.Material = Enum.Material.Neon
	dish.Color = Color3.fromRGB(190, 160, 255)
	dish.Parent = antenna
	local orb = Instance.new("Part")
	orb.Name = "Orb"
	orb.Size = Vector3.new(1.8, 1.8, 1.8)
	orb.Shape = Enum.PartType.Ball
	orb.CFrame = CFrame.new(0, 15.4, -16)
	orb.Anchored = true
	orb.CanCollide = false
	orb.Material = Enum.Material.Neon
	orb.Color = Color3.fromRGB(255, 255, 255)
	orb.Parent = antenna
	local orbPt = Instance.new("PointLight")
	orbPt.Color = Color3.fromRGB(200, 170, 255)
	orbPt.Range = 30
	orbPt.Brightness = 3
	orbPt.Parent = orb
	antenna.Parent = structuresFolder
	antenna:SetAttribute("Structure", "Antenna")

	-- 4 turret pads at the corners of VAULT-1
	local padPositions = {
		Vector3.new(-30, 0, -30), Vector3.new(30, 0, -30),
		Vector3.new(-30, 0, 30), Vector3.new(30, 0, 30),
	}
	for i, pos in ipairs(padPositions) do
		local pad = Instance.new("Part")
		pad.Name = "TurretPad" .. i
		pad.Size = Vector3.new(6, 0.8, 6)
		pad.CFrame = CFrame.new(pos.X, 0.4, pos.Z)
		pad.Anchored = true
		pad.Material = Enum.Material.Concrete
		pad.Color = Color3.fromRGB(80, 84, 105)
		pad:SetAttribute("Structure", "TurretPad")
		pad:SetAttribute("PadIndex", i)
		pad.Parent = structuresFolder
	end

	-- initial gate walls around home sector
	local wallDefs = {
		{ Vector3.new(-38, 0, -10), Vector3.new(2.5, 6, 24) },
		{ Vector3.new(38, 0, -10), Vector3.new(2.5, 6, 24) },
		{ Vector3.new(-10, 0, -38), Vector3.new(24, 6, 2.5) },
		{ Vector3.new(10, 0, -38), Vector3.new(24, 6, 2.5) },
	}
	for i, wdef in ipairs(wallDefs) do
		local wall = Instance.new("Part")
		wall.Name = "Wall" .. i
		wall.Size = wdef[2]
		wall.CFrame = CFrame.new(wdef[1])
		wall.Anchored = true
		wall.Material = Enum.Material.Concrete
		wall.Color = Color3.fromRGB(110, 108, 135)
		wall:SetAttribute("Structure", "Wall")
		wall:SetAttribute("MaxHp", 420)
		wall:SetAttribute("Hp", 420)
		wall.Parent = structuresFolder
	end
end

function SwarmLib.spawnCrystals(folder, sectors, count)
	local spawned = {}
	for i = 1, count do
		local sec = sectors[math.random(2, #sectors)] -- never in VAULT-1
		local def = sec.def
		local pos = Shared.randomInSector(def, 10)
		pos = Vector3.new(pos.X, 0, pos.Z)
		local model = CrystalModel.new(folder, CFrame.new(pos) * CFrame.Angles(0, math.random() * 6.28, 0), 0.9 + math.random() * 0.5, i)
		model:SetAttribute("Sector", def.id)
		table.insert(spawned, model)
	end
	return spawned
end

return SwarmLib
