--!nonstrict
-- SERVER // CLONE: Multi-Volume Agent Swarm
-- The hive-mind brain: spawns 35 clones, runs their roles, tasks,
-- combat, economy, structures, respawns and the wave director.

local SwarmController = {}
local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Shared = require(ReplicatedStorage.Swarm.Shared)
local CloneModel = require(ReplicatedStorage.Swarm.CloneModel)
local SwarmLib = require(script.Parent.SwarmLib)
local CrystalModel = require(ReplicatedStorage.Swarm.CrystalModel)

local Workspace = game:GetService("Workspace")
local RunService = game:GetService("RunService")
local Players = game:GetService("Players")
local PhysicsService = game:GetService("PhysicsService")
local Debris = game:GetService("Debris")
local CollectionService = game:GetService("CollectionService")
local TweenService = game:GetService("TweenService")

local SPAWN = Vector3.new(0, 1, 12)
local HOME = Vector3.new(0, 0, 0)

local AGENT_ROLES = {}
local agentCounter = 0
for _, role in ipairs(Shared.RoleOrder) do
	local r = Shared.Roles[role]
	for i = 1, r.count do
		agentCounter = agentCounter + 1
		table.insert(AGENT_ROLES, { role = role, index = agentCounter })
	end
end

local function newSector(def)
	return {
		def = def,
		region = Region3.new(
			def.center - def.size / 2,
			def.center + def.size / 2
		),
		agents = {},
		explored = def.isHome or false,
		secured = false,
		taskWeight = math.random(8, 16),
	}
end

function SwarmController.new()
	local self = setmetatable({}, { __index = SwarmController })
	self.Agents = {}          -- array of agent records
	self.ById = {}            -- index -> agent record
	self.Sectors = {}
	for _, def in ipairs(Shared.Volumes) do
		table.insert(self.Sectors, newSector(def))
	end
	self.Structures = {}      -- parts with Structure attribute
	self.Crystals = {}        -- active node models
	self.Turrets = {}         -- built turret records {model, pad}
	self.NullTicks = {}       -- hostile records
	self.Tasks = {}           -- task queue
	self.TaskSeq = 0
	self.Stock = { crystals = 60 }
	self.Level = 1
	self.Wave = 0
	self.WaveActive = false
	self.SwarmSize = #AGENT_ROLES
	self.DeadQueue = {}
	self.Feed = {}
	self.BroadcastAccum = 0
	self.MiningAccum = 0
	self.RepairAccum = 0
	self.ThinkAccum = 0
	self.Running = false
	self.WallSlots = {}
	self.PadSlots = {}
	self.GodMode = false
	return self
end

-- build wall build slots in a ring around home + turret pads
function SwarmController:buildSlots()
	for i = 1, 12 do
		local ang = (i - 1) / 12 * math.pi * 2
		local r = 32
		local pos = HOME + Vector3.new(math.cos(ang) * r, 0, math.sin(ang) * r)
		table.insert(self.WallSlots, { pos = pos, index = i, built = false, progress = 0, builder = nil })
	end
	for i = 1, 4 do
		local pos = nil
		for _, s in ipairs(self.Structures) do
			if s:GetAttribute("Structure") == "TurretPad" and s:GetAttribute("PadIndex") == i then
				pos = s.Position
			end
		end
		table.insert(self.PadSlots, { pos = pos or (HOME + Vector3.new((-1) ^ i * 30, 0, i <= 2 and -30 or 30)), padIndex = i, built = false, progress = 0, builder = nil })
	end
end

function SwarmController:start()
	if self.Running then return end
	self.Running = true

	-- physics groups
	PhysicsService:RegisterCollisionGroup("Clones")
	PhysicsService:RegisterCollisionGroup("Hostiles")
	PhysicsService:RegisterCollisionGroup("Structures")
	PhysicsService:RegisterCollisionGroup("SectorFloor")
	PhysicsService:CollisionGroupSetCollidable("Clones", "Clones", false)
	PhysicsService:CollisionGroupSetCollidable("Hostiles", "Hostiles", false)
	PhysicsService:CollisionGroupSetCollidable("Clones", "Structures", true)
	PhysicsService:CollisionGroupSetCollidable("Hostiles", "Structures", true)
	PhysicsService:CollisionGroupSetCollidable("Clones", "SectorFloor", true)
	PhysicsService:CollisionGroupSetCollidable("Hostiles", "SectorFloor", true)
	PhysicsService:CollisionGroupSetCollidable("Clones", "Default", false)
	PhysicsService:CollisionGroupSetCollidable("Hostiles", "Default", false)

	SwarmLib.buildWorld(self.Sectors)

	local world = Workspace:FindFirstChild("SwarmWorld")
	local structuresFolder = world:FindFirstChild("Structures") or Instance.new("Folder")
	structuresFolder.Name = "Structures"
	structuresFolder.Parent = world
	SwarmLib.buildBaseStructures(structuresFolder)

	-- collect structure parts (models and loose parts)
	local function collectStructure(part)
		if part:IsA("BasePart") and part:GetAttribute("Structure") then
			PhysicsService:SetPartCollisionGroup(part, "Structures")
			table.insert(self.Structures, part)
		end
	end
	for _, child in ipairs(structuresFolder:GetChildren()) do
		if child:IsA("BasePart") then
			collectStructure(child)
		else
			for _, p in ipairs(child:GetDescendants()) do
				collectStructure(p)
			end
		end
	end

	self:buildSlots()

	-- crystal nodes
	self.Crystals = SwarmLib.spawnCrystals(world, self.Sectors, 8)

	-- spawn the 35 clones
	for _, def in ipairs(AGENT_ROLES) do
		self:spawnAgent(def.role, def.index)
	end

	self:feed("HIVE", "Swarm link established. 35 clones online.")
	self:feed("SYS", "Sector volumes scanned: " .. tostring(#self.Sectors))

	RunService.Heartbeat:Connect(function(dt)
		self:onHeartbeat(dt)
	end)
end

-- agent factory -------------------------------------------------------------

function SwarmController:spawnAgent(roleId, index)
	local role = Shared.Roles[roleId]
	local names = Shared.UnitNames[roleId]
	local nameText = names[(index - 1) % #names + 1]
	local color = role.color

	-- spreads spawns around home so they don't clip
	local ang = math.random() * math.pi * 2
	local pos = SPAWN + Vector3.new(math.cos(ang) * (2 + math.random() * 3), 1, math.sin(ang) * (2 + math.random() * 3))

	local model = CloneModel.new(Workspace, roleId, nameText, color, index, pos)
	CloneModel.applyTool(model, role.tool, Workspace)

	local hrp = model:FindFirstChild("HumanoidRootPart")
	local humanoid = model:FindFirstChildOfClass("Humanoid")
	PhysicsService:SetPartCollisionGroup(hrp, "Clones")
	for _, part in ipairs(model:GetDescendants()) do
		if part:IsA("BasePart") then
			PhysicsService:SetPartCollisionGroup(part, "Clones")
		end
	end
	CollectionService:AddTag(model, Shared.CLONE_TAG)

	local agent = {
		Model = model,
		Index = index,
		RoleId = roleId,
		Name = nameText,
		Controller = self,
		Hrp = hrp,
		Humanoid = humanoid,
		Alive = true,
		Task = nil,          -- current behavior key
		TaskTarget = nil,    -- part/model reference
		TaskPos = nil,       -- vector3 target
		MoveTarget = nil,
		HomeSector = self.Sectors[1],
		Sector = self.Sectors[1],
		FlockWeight = { sep = 2.2, coh = 0.35, ali = 0.5 },
		Cooldowns = {},
		MiningTarget = nil,
		MineProgress = 0,
		RespawnAt = nil,
		PatrolPos = nil,
		Status = "ONLINE",
		RepairTarget = nil,
		BuildSlot = nil,
		SpeedBoostUntil = 0,
		HostileTarget = nil,
	}

	table.insert(self.Agents, agent)
	self.ById[index] = agent
	self.Sectors[1].agents[#self.Sectors[1].agents + 1] = agent

	humanoid.Died:Connect(function()
		self:onAgentDied(agent)
	end)

	return agent
end

function SwarmController:releaseBuildSlot(agent)
	if agent.BuildSlot then
		agent.BuildSlot.builder = nil
		agent.BuildSlot.progress = 0
		if agent.BuildSlot.scaffold then
			agent.BuildSlot.scaffold:Destroy()
			agent.BuildSlot.scaffold = nil
		end
		agent.BuildSlot = nil
	end
end

function SwarmController:onAgentDied(agent)
	if not agent.Alive then return end
	agent.Alive = false
	agent.Status = "OFFLINE"
	agent.RespawnAt = os.clock() + 7
	self:releaseBuildSlot(agent)
	-- release any claimed tasks
	for _, t in ipairs(self.Tasks) do
		if t.claimedBy == agent then
			t.claimedBy = nil
		end
	end
	table.insert(self.DeadQueue, agent)
	self:feed(agent.RoleId, agent.Model.Name .. " " .. agent.Name .. " went offline. Respawn scheduled.")

	local pos = agent.Hrp.Position
	local burst = Instance.new("Part")
	burst.Name = "DownBurst"
	burst.Shape = Enum.PartType.Ball
	burst.Size = Vector3.new(1, 1, 1)
	burst.CFrame = CFrame.new(pos)
	burst.Anchored = true
	burst.CanCollide = false
	burst.Material = Enum.Material.Neon
	burst.Color = Shared.Roles[agent.RoleId].color
	burst.Transparency = 0.6
	burst.Parent = Workspace
	TweenService:Create(burst, TweenInfo.new(0.6), { Size = Vector3.new(6, 6, 6), Transparency = 1 }):Play()
	Debris:AddItem(burst, 1)
end

function SwarmController:respawnDue(agent)
	local model = agent.Model
	local role = Shared.Roles[agent.RoleId]
	model:SetAttribute("Hp", role.hp)
	model:SetAttribute("MaxHp", role.hp)
	agent.Humanoid.Health = role.hp
	agent.Alive = true
	agent.Status = "BOOT"
	agent.RespawnAt = nil
	agent.Task = nil
	agent.TaskTarget = nil
	agent.MoveTarget = nil
	agent.HostileTarget = nil
	agent.MiningTarget = nil
	agent.RepairTarget = nil
	local ang = math.random() * math.pi * 2
	local pos = SPAWN + Vector3.new(math.cos(ang) * 3, 1, math.sin(ang) * 3)
	agent.Model:PivotTo(CFrame.new(pos))
	agent.Hrp.Velocity = Vector3.new()
	agent.Humanoid.WalkSpeed = role.speed
	agent.Humanoid:ChangeState(Enum.HumanoidStateType.Running)
	self:feed("RESPAWN", agent.Model.Name .. " " .. agent.Name .. " back online at VAULT-1.")
end

-- task queue ----------------------------------------------------------------

function SwarmController:enqueueTask(kind, volumeId, pos, data)
	self.TaskSeq = self.TaskSeq + 1
	table.insert(self.Tasks, {
		id = self.TaskSeq,
		kind = kind,
		volume = volumeId,
		pos = pos,
		data = data or {},
		claimedBy = nil,
		createdAt = os.clock(),
	})
	return self.TaskSeq
end

function SwarmController:refreshTasks()
	-- prune stale tasks
	for i = #self.Tasks, 1, -1 do
		local t = self.Tasks[i]
		local stale = false
		if t.kind == "MINE" then
			local node = t.data.node
			stale = (not node) or (not node.Parent) or ((node:GetAttribute("Hp") or 0) <= 0)
		elseif t.kind == "BUILD" then
			local slot = t.data.slot or t.data.pad
			stale = (not slot) or slot.built
		elseif t.kind == "HAUL" then
			stale = (os.clock() - t.createdAt) > 90
		end
		if stale then
			if t.claimedBy and t.claimedBy.Task == t.kind then
				t.claimedBy.Task = nil
			end
			table.remove(self.Tasks, i)
		end
	end
	-- mine tasks for unclaimed nodes
	for _, node in ipairs(self.Crystals) do
		if not node:GetAttribute("Claimed") and node:GetAttribute("Hp") and node:GetAttribute("Hp") > 0 then
			local vol = node:GetAttribute("Sector") or "ARC-2"
			local exists = false
			for _, t in ipairs(self.Tasks) do
				if t.kind == "MINE" and t.data.node == node then exists = true break end
			end
			if not exists then
				self:enqueueTask("MINE", vol, node:GetPivot().Position, { node = node })
			end
		end
	end
	-- build tasks for unbuilt wall slots
	for _, slot in ipairs(self.WallSlots) do
		if not slot.built and not slot.builder then
			local exists = false
			for _, t in ipairs(self.Tasks) do
				if t.kind == "BUILD" and t.data.slot == slot then exists = true break end
			end
			if not exists then
				self:enqueueTask("BUILD", "VAULT-1", slot.pos, { slot = slot })
			end
		end
	end
	for _, pad in ipairs(self.PadSlots) do
		if not pad.built and not pad.builder then
			local exists = false
			for _, t in ipairs(self.Tasks) do
				if t.kind == "BUILD" and t.data.pad == pad then exists = true break end
			end
			if not exists then
				self:enqueueTask("BUILD", "VAULT-1", pad.pos, { pad = pad })
			end
		end
	end
end

function SwarmController:claimTask(agent, kinds)
	local best = nil
	local bestScore = 0
	for _, t in ipairs(self.Tasks) do
		local nodeOk = true
		if t.data.node then
			nodeOk = (t.data.node:GetAttribute("Hp") or 0) > 0
		end
		if t.claimedBy == nil and nodeOk then
			if kinds[t.kind] then
				local dist = (t.pos - agent.Hrp.Position).Magnitude
				local volumeScore = (t.volume == agent.Sector.def.id) and 4 or 1
				local score = volumeScore * 3 + 60 / (1 + dist / 30)
				if score > bestScore then
					bestScore = score
					best = t
				end
			end
		end
	end
	if best then
		best.claimedBy = agent
		return best
	end
	return nil
end

function SwarmController:releaseTask(task, agent)
	if task and task.claimedBy == agent then
		task.claimedBy = nil
	end
end

-- heartbeat ----------------------------------------------------------------

function SwarmController:onHeartbeat(dt)
	local now = os.clock()

	-- respawns
	if #self.DeadQueue > 0 then
		for i = #self.DeadQueue, 1, -1 do
			local agent = self.DeadQueue[i]
			if agent.RespawnAt and now >= agent.RespawnAt then
				self:respawnDue(agent)
				table.remove(self.DeadQueue, i)
			end
		end
	end

	-- tick timers
	self.MiningAccum = self.MiningAccum + dt
	self.RepairAccum = self.RepairAccum + dt
	self.ThinkAccum = self.ThinkAccum + dt

	-- sector membership update (every ~0.5s)
	if self.ThinkAccum >= 0.5 then
		self.ThinkAccum = 0
		for _, sec in ipairs(self.Sectors) do
			sec.agents = {}
		end
		for _, agent in ipairs(self.Agents) do
			if agent.Alive then
				local sid = Shared.sectorIdAt(self.Sectors, agent.Hrp.Position)
				local sec = sid and self:findSector(sid) or self.Sectors[1]
				agent.Sector = sec
				table.insert(sec.agents, agent)
				CloneModel.setSectorLabel(agent.Model, sec.def.id .. " · " .. sec.def.sub)
			end
		end
		self:refreshTasks()
	end

	-- role behaviors for every agent
	for _, agent in ipairs(self.Agents) do
		if agent.Alive then
			self:think(agent, dt, now)
		end
	end

	-- hostiles
	if self.WaveActive then
		self:thinkHostiles(dt, now)
		self:updateTurrets(dt)
	elseif #self.NullTicks > 0 then
		self:thinkHostiles(dt, now)
		self:updateTurrets(dt)
	end

	-- crystal regen / mining payout & repair ticks
	if self.MiningAccum >= 0.35 then
		self.MiningAccum = 0
		for _, agent in ipairs(self.Agents) do
			if agent.Alive and agent.Task == "MINE" and agent.MiningTarget then
				local node = agent.MiningTarget
				if node and node:IsA("Model") and node:GetAttribute("Hp") and node:GetAttribute("Hp") > 0 then
					local hp = node:GetAttribute("Hp") - 22
					node:SetAttribute("Hp", math.max(0, hp))
					CrystalModel.updateHpBar(node)
					local spark = Instance.new("Part")
					spark.Shape = Enum.PartType.Ball
					spark.Size = Vector3.new(0.4, 0.4, 0.4)
					spark.CFrame = node.Crystal.CFrame * CFrame.new((math.random() - 0.5) * 1.6, (math.random() - 0.5) * 1.6, 0.6)
					spark.Anchored = true
					spark.CanCollide = false
					spark.Material = Enum.Material.Neon
					spark.Color = Color3.fromRGB(200, 180, 255)
					spark.Transparency = 0.2
					spark.Parent = Workspace
					TweenService:Create(spark, TweenInfo.new(0.5), { Transparency = 1, Size = Vector3.new(0.1, 0.1, 0.1) }):Play()
					Debris:AddItem(spark, 0.6)
					if hp <= 0 then
						self:depleteNode(node, agent)
					end
				end
			end
		end
	end

	if self.RepairAccum >= 0.5 then
		self.RepairAccum = 0
		for _, agent in ipairs(self.Agents) do
			if agent.Alive and agent.Task == "REPAIR" and agent.RepairTarget then
				local t = agent.RepairTarget
				local hp = t:GetAttribute("Hp")
				local max = t:GetAttribute("MaxHp")
				if hp and max and hp < max then
					t:SetAttribute("Hp", math.min(max, hp + 14))
					self:spawnHealSpark(t, Shared.Roles.MEDIC.color)
				end
			end
		end
	end

	-- wave director
	self:updateWave(now)

	-- broadcast stats to clients (~2.5/s)
	self.BroadcastAccum = self.BroadcastAccum + dt
	if self.BroadcastAccum >= 0.4 then
		self.BroadcastAccum = 0
		self:broadcastStats()
	end
end

function SwarmController:findSector(id)
	for _, sec in ipairs(self.Sectors) do
		if sec.def.id == id then return sec end
	end
	return self.Sectors[1]
end

-- per-agent behavior --------------------------------------------------------

function SwarmController:think(agent, dt, now)
	local role = Shared.Roles[agent.RoleId]
	local hp = agent.Model:GetAttribute("Hp") or role.hp

	-- coordinators boost nearby clones
	if agent.RoleId == "COORDINATOR" then
		for _, other in ipairs(self.Agents) do
			if other ~= agent and other.Alive and (other.Hrp.Position - agent.Hrp.Position).Magnitude < 14 then
				other.SpeedBoostUntil = now + 0.6
			end
		end
		-- coordinators also repaint sector assignment: rebalance volumes
		self:rebalanceSectors(agent)
	end

	-- natural hp regen when out of combat
	if hp < role.hp then
		agent.Model:SetAttribute("Hp", math.min(role.hp, hp + dt * 1.2))
	end

	-- check for a hostile within threat range
	local threat = self:findThreat(agent, role)
	if threat then
		self:combat(agent, threat, dt, now)
		return
	end

	-- clear task if target no longer valid
	self:validateTask(agent)

	local task = agent.Task
	if task == "MINE" and agent.MiningTarget then
		self:doMine(agent, dt, now)
	elseif task == "BUILD" then
		self:doBuild(agent, dt, now)
	elseif task == "REPAIR" and agent.RepairTarget then
		self:doRepair(agent, dt, now)
	elseif task == "HAUL" then
		self:doHaul(agent, dt, now)
	elseif task == "FIGHT" then
		-- handled above
	else
		-- autonomous role behavior
		self:autonomous(agent, dt, now)
	end
end

function SwarmController:findThreat(agent, role)
	local range = role.label == "Defender" and 65 or 30
	local best = nil
	local bestD = range
	for _, nt in ipairs(self.NullTicks) do
		if nt.Alive then
			local d = (nt.Hrp.Position - agent.Hrp.Position).Magnitude
			if d < bestD then
				bestD = d
				best = nt
			end
		end
	end
	if best then
		-- release any build slot before switching to combat
		if agent.Task == "BUILD" and agent.BuildSlot then
			self:releaseBuildSlot(agent)
		end
		agent.Task = "FIGHT"
		agent.HostileTarget = best
		return best
	end
	agent.HostileTarget = nil
	return nil
end

function SwarmController:combat(agent, nt, dt, now)
	local role = Shared.Roles[agent.RoleId]
	local d = (nt.Hrp.Position - agent.Hrp.Position).Magnitude
	local range = role.label == "Defender" and 26 or (role.label == "Scout" and 18 or 4)
	if d > range then
		SwarmLib.moveToward(agent, nt.Hrp.Position, dt)
		agent.Status = "ENGAGE"
	else
		agent.Hrp.Velocity = Vector3.new(0, agent.Hrp.Velocity.Y, 0)
		local cdKey = "atk"
		local last = agent.Cooldowns[cdKey] or 0
		local interval = role.label == "Defender" and 0.55 or 0.9
		if now - last >= interval then
			agent.Cooldowns[cdKey] = now
			local dmg = role.label == "Defender" and 16 or (role.label == "Scout" and 5 or 8)
			local dead = SwarmLib.hurtModel(nt.Model, dmg, agent)
			self:spawnMuzzle(agent, nt)
			if dead then
				self:onNullTickDown(nt, agent)
			end
		end
		agent.Status = "FIGHT"
	end
end

function SwarmController:validateTask(agent)
	if agent.Task == "MINE" then
		local node = agent.MiningTarget
		if not node or not node.Parent or not node:GetAttribute("Hp") or node:GetAttribute("Hp") <= 0 then
			agent.Task = nil
			agent.MiningTarget = nil
		end
	elseif agent.Task == "REPAIR" then
		local t = agent.RepairTarget
		if not t or not t.Parent or not t:GetAttribute("Hp") or not t:GetAttribute("MaxHp") or t:GetAttribute("Hp") >= t:GetAttribute("MaxHp") then
			agent.Task = nil
			agent.RepairTarget = nil
		end
	elseif agent.Task == "BUILD" then
		local slot = agent.BuildSlot
		if not slot then
			agent.Task = nil
		elseif slot.built then
			self:releaseBuildSlot(agent)
			agent.Task = nil
		end
	elseif agent.Task == "FIGHT" then
		local nt = agent.HostileTarget
		if not nt or not nt.Alive then
			agent.Task = nil
			agent.HostileTarget = nil
		end
	elseif agent.Task == "HAUL" then
		if not agent.TaskPos then agent.Task = nil end
	end
end

-- role behaviors ------------------------------------------------------------

function SwarmController:autonomous(agent, dt, now)
	local role = Shared.Roles[agent.RoleId]
	agent.Status = "AUTO"

	if role.label == "Scout" then
		self:scoutBehavior(agent, dt)
	elseif role.label == "Gatherer" then
		self:gathererBehavior(agent, dt)
	elseif role.label == "Builder" then
		self:builderBehavior(agent, dt)
	elseif role.label == "Medic" then
		self:medicBehavior(agent, dt, now)
	elseif role.label == "Defender" then
		self:defenderBehavior(agent, dt)
	elseif role.label == "Engineer" then
		self:engineerBehavior(agent, dt)
	elseif role.label == "Coordinator" then
		self:coordinatorBehavior(agent, dt)
	end
end

local PATROL_POINTS = {}
for i = 1, 8 do
	local ang = (i - 1) / 8 * math.pi * 2
	PATROL_POINTS[i] = HOME + Vector3.new(math.cos(ang) * 26, 0, math.sin(ang) * 26)
end

function SwarmController:pickPatrol(agent)
	local best = nil
	local bestD = 0
	for _, p in ipairs(PATROL_POINTS) do
		local d = (p - agent.Hrp.Position).Magnitude
		if d > 18 and d > bestD then
			bestD = d
			best = p
		end
	end
	return best or PATROL_POINTS[math.random(#PATROL_POINTS)]
end

function SwarmController:scoutBehavior(agent, dt)
	-- explore: pick a random unexplored sector, wander to its far corner
	local unexp = {}
	for _, sec in ipairs(self.Sectors) do
		if not sec.explored then
			unexp[#unexp + 1] = sec
		end
	end
	if #unexp > 0 then
		local sec = unexp[math.random(#unexp)]
		local target = Shared.randomInSector(sec.def, 12)
		agent.MoveTarget = target
		local d = SwarmLib.moveToward(agent, target, dt)
		agent.Status = "SECTOR " .. sec.def.id
		if d < 6 then
			sec.explored = true
			self:feed("SCOUT", agent.Model.Name .. " scanned " .. sec.def.id .. " — volume secured.")
		end
	else
		-- roam between volumes
		local sec = self.Sectors[math.random(2, #self.Sectors)]
		local target = Shared.randomInSector(sec.def, 14)
		local d = SwarmLib.moveToward(agent, target, dt)
		agent.Status = "SCAN " .. sec.def.id
		if d < 4 then
			agent.PatrolPos = nil
		end
	end
end

function SwarmController:gathererBehavior(agent, dt)
	-- try to grab a MINE task, else haul leftovers, else idle at depot
	if agent.Task == nil then
		local t = self:claimTask(agent, { MINE = true })
		if t then
			agent.Task = "MINE"
			agent.TaskPos = t.pos
			agent.MiningTarget = t.data.node
			agent.Status = "TASK MINE"
		else
			-- idle: stand near depot
			if not agent.MoveTarget or (agent.Hrp.Position - agent.MoveTarget).Magnitude < 2 then
				agent.MoveTarget = HOME + Vector3.new((math.random() - 0.5) * 10, 0, (math.random() - 0.5) * 10)
			end
			SwarmLib.moveToward(agent, agent.MoveTarget, dt)
			agent.Status = "STANDBY"
		end
	end
end

function SwarmController:doMine(agent, dt, now)
	local node = agent.MiningTarget
	if not node or not node.Parent or (node:GetAttribute("Hp") or 0) <= 0 then
		agent.Task = nil
		agent.MiningTarget = nil
		return
	end
	local crystalPart = node:FindFirstChild("Crystal") or node.PrimaryPart
	if not crystalPart then
		agent.Task = nil
		return
	end
	local d = (crystalPart.Position - agent.Hrp.Position).Magnitude
	if d > 3.6 then
		SwarmLib.moveToward(agent, crystalPart.Position, dt)
		agent.Status = "TO NODE"
	else
		agent.Hrp.Velocity = Vector3.new(0, agent.Hrp.Velocity.Y, 0)
		-- face the node
		local look = (crystalPart.Position - agent.Hrp.Position) * Vector3.new(1, 0, 1)
		if look.Magnitude > 0.1 then
			agent.Hrp.CFrame = CFrame.new(agent.Hrp.Position, agent.Hrp.Position + look.Unit)
		end
		agent.Status = "MINING"
		-- actual damage handled in heartbeat tick (MiningAccum)
	end
end

function SwarmController:depleteNode(node, agent)
	local vol = node:GetAttribute("Sector") or "ARC-2"
	local gain = 12 + self.Level * 3
	local nodePos = node:GetPivot().Position
	self.Stock.crystals = self.Stock.crystals + gain
	node:Destroy()
	-- remove from list
	for i = #self.Crystals, 1, -1 do
		if self.Crystals[i] == node then
			table.remove(self.Crystals, i)
		end
	end
	-- drop deposit beam
	local beam = Instance.new("Part")
	beam.Name = "DepositBeam"
	beam.Size = Vector3.new(1, 14, 1)
	beam.CFrame = CFrame.new(nodePos + Vector3.new(0, 7, 0))
	beam.Anchored = true
	beam.CanCollide = false
	beam.Material = Enum.Material.Neon
	beam.Color = Color3.fromRGB(120, 255, 160)
	beam.Transparency = 0.3
	beam.Parent = Workspace
	TweenService:Create(beam, TweenInfo.new(0.8), { Transparency = 1 }):Play()
	Debris:AddItem(beam, 1)

	self:feed("GATHERER", agent.Model.Name .. " mined a node in " .. vol .. "  +" .. tostring(gain) .. " ore")
	self:enqueueTask("HAUL", vol, HOME, { agent = agent })
	agent.Task = "HAUL"
	agent.TaskPos = HOME
	-- respawn node later elsewhere
	delay(18 + math.random() * 10, function()
		if self.Running then
			local sec = self.Sectors[math.random(2, #self.Sectors)]
			local pos = Shared.randomInSector(sec.def, 10)
			pos = Vector3.new(pos.X, 0, pos.Z)
			local new = CrystalModel.new(Workspace:FindFirstChild("SwarmWorld"), CFrame.new(pos), 0.9 + math.random() * 0.5, math.random(1, 9999))
			new:SetAttribute("Sector", sec.def.id)
			table.insert(self.Crystals, new)
		end
	end)
end

function SwarmController:doHaul(agent, dt, now)
	local target = agent.TaskPos or HOME
	local d = SwarmLib.moveToward(agent, target, dt)
	agent.Status = "HAUL"
	if d < 5 then
		-- deposited
		agent.Task = nil
		agent.TaskPos = nil
		agent.MoveTarget = nil
		local beam = Instance.new("Part")
		beam.Size = Vector3.new(0.6, 10, 0.6)
		beam.CFrame = CFrame.new(target + Vector3.new(0, 5, 0))
		beam.Anchored = true
		beam.CanCollide = false
		beam.Material = Enum.Material.Neon
		beam.Color = Color3.fromRGB(0, 240, 200)
		beam.Transparency = 0.4
		beam.Parent = Workspace
		TweenService:Create(beam, TweenInfo.new(0.6), { Transparency = 1 }):Play()
		Debris:AddItem(beam, 0.7)
	end
end

function SwarmController:builderBehavior(agent, dt)
	-- claim BUILD task
	if agent.Task == nil then
		local t = self:claimTask(agent, { BUILD = true })
		if t then
			agent.Task = "BUILD"
			agent.TaskPos = t.pos
			agent.BuildSlot = t.data.slot or t.data.pad
			agent.BuildSlot.builder = agent
			agent.Status = "TASK BUILD"
		else
			-- patrol near base edge
			if not agent.MoveTarget or (agent.Hrp.Position - agent.MoveTarget).Magnitude < 3 then
				agent.MoveTarget = self:pickPatrol(agent)
			end
			SwarmLib.moveToward(agent, agent.MoveTarget, dt)
			agent.Status = "PATROL"
		end
	end
end

function SwarmController:doBuild(agent, dt, now)
	local slot = agent.BuildSlot
	if not slot then
		agent.Task = nil
		return
	end
	local pos = slot.pos
	local d = (pos - agent.Hrp.Position).Magnitude
	if d > 4 then
		SwarmLib.moveToward(agent, pos, dt)
		agent.Status = "TO BUILD SITE"
		return
	end
	agent.Hrp.Velocity = Vector3.new(0, agent.Hrp.Velocity.Y, 0)
	agent.Status = "BUILDING"
	slot.progress = slot.progress + dt
	-- hologram scaffold while building
	if not slot.scaffold then
		local sc = Instance.new("Part")
		sc.Name = "Scaffold"
		sc.Size = slot.pad and Vector3.new(5, 5, 5) or Vector3.new(2.5, 6, 24)
		sc.CFrame = CFrame.new(pos)
		sc.Anchored = true
		sc.CanCollide = false
		sc.Material = Enum.Material.Neon
		sc.Color = Shared.Roles.BUILDER.color
		sc.Transparency = 0.55
		sc.Parent = Workspace
		slot.scaffold = sc
	end
	if slot.progress >= 4 then
		slot.progress = 0
		slot.built = true
		slot.builder = nil
		if slot.scaffold then
			slot.scaffold:Destroy()
			slot.scaffold = nil
		end
		self:completeBuild(agent, slot)
	end
end

function SwarmController:completeBuild(agent, slot)
	local cost = slot.pad and 60 or 30
	if self.Stock.crystals >= cost then
		self.Stock.crystals = self.Stock.crystals - cost
	end
	local world = Workspace:FindFirstChild("SwarmWorld")
	local structures = world and world:FindFirstChild("Structures")
	if slot.pad then
		-- build turret
		local turret = Instance.new("Model")
		turret.Name = "Turret" .. tostring(slot.pad.padIndex or "")
		local base = Instance.new("Part")
		base.Name = "Base"
		base.Size = Vector3.new(4, 1.4, 4)
		base.CFrame = CFrame.new(slot.pos) + Vector3.new(0, 0.7, 0)
		base.Anchored = true
		base.Material = Enum.Material.Metal
		base.Color = Color3.fromRGB(120, 126, 150)
		base:SetAttribute("Structure", "Turret")
		base:SetAttribute("MaxHp", 260)
		base:SetAttribute("Hp", 260)
		base.Parent = turret
		local barrel = Instance.new("Part")
		barrel.Name = "Barrel"
		barrel.Size = Vector3.new(0.6, 0.6, 3)
		barrel.CFrame = CFrame.new(slot.pos) + Vector3.new(0, 1.8, 1)
		barrel.Anchored = true
		barrel.Material = Enum.Material.Neon
		barrel.Color = Shared.Roles.ENGINEER.color
		barrel.Parent = turret
		local orb = Instance.new("Part")
		orb.Name = "Orb"
		orb.Size = Vector3.new(1.4, 1.4, 1.4)
		orb.Shape = Enum.PartType.Ball
		orb.CFrame = CFrame.new(slot.pos) + Vector3.new(0, 2.6, 0)
		orb.Anchored = true
		orb.CanCollide = false
		orb.Material = Enum.Material.Neon
		orb.Color = Color3.fromRGB(255, 90, 90)
		orb.Parent = turret
		local pt = Instance.new("PointLight")
		pt.Color = Color3.fromRGB(255, 90, 90)
		pt.Range = 14
		pt.Brightness = 1.5
		pt.Parent = orb
		turret.Parent = structures
		for _, p in ipairs(turret:GetDescendants()) do
			if p:IsA("BasePart") then
				PhysicsService:SetPartCollisionGroup(p, "Structures")
			end
		end
		table.insert(self.Structures, base)
		table.insert(self.Turrets, { model = turret, orb = orb, barrel = barrel, cooldown = 0, level = 1 })
		self:feed("ENGINEER", agent.Model.Name .. " raised a TURRET on pad " .. tostring(slot.pad.padIndex or "?") .. ".")
	else
		-- build wall
		local wall = Instance.new("Part")
		wall.Name = "Wall" .. tostring(slot.index or math.random(100)) .. "_B"
		wall.Size = Vector3.new(2.5, 6, 24)
		wall.CFrame = CFrame.new(slot.pos)
		wall.Anchored = true
		wall.Material = Enum.Material.Concrete
		wall.Color = Color3.fromRGB(135, 132, 160)
		wall:SetAttribute("Structure", "Wall")
		wall:SetAttribute("MaxHp", 420)
		wall:SetAttribute("Hp", 420)
		wall.Parent = structures
		PhysicsService:SetPartCollisionGroup(wall, "Structures")
		table.insert(self.Structures, wall)
		self:feed("BUILDER", agent.Model.Name .. " raised a wall at VAULT-1 perimeter.")
	end
	-- release agent
	agent.Task = nil
	agent.BuildSlot = nil
	agent.MoveTarget = nil
end

function SwarmController:medicBehavior(agent, dt, now)
	if agent.Task == nil then
		-- find injured clone or structure
		local best = nil
		local bestFrac = 1
		for _, other in ipairs(self.Agents) do
			if other.Alive and other ~= agent then
				local hp = other.Model:GetAttribute("Hp") or 0
				local max = other.Model:GetAttribute("MaxHp") or 1
				local frac = hp / max
				if frac < 0.85 and frac < bestFrac then
					bestFrac = frac
					best = other.Model
				end
			end
		end
		if not best then
			for _, st in ipairs(self.Structures) do
				local hp = st:GetAttribute("Hp")
				local max = st:GetAttribute("MaxHp")
				if hp and max then
					local frac = hp / max
					if frac < 0.9 and frac < bestFrac then
						bestFrac = frac
						best = st
					end
				end
			end
		end
		if best then
			agent.Task = "REPAIR"
			agent.RepairTarget = best
			agent.Status = "TASK REPAIR"
		else
			if not agent.MoveTarget or (agent.Hrp.Position - agent.MoveTarget).Magnitude < 3 then
				agent.MoveTarget = HOME + Vector3.new((math.random() - 0.5) * 16, 0, (math.random() - 0.5) * 16)
			end
			SwarmLib.moveToward(agent, agent.MoveTarget, dt)
			agent.Status = "ON CALL"
		end
	end
end

function SwarmController:doRepair(agent, dt, now)
	local t = agent.RepairTarget
	if not t or not t.Parent then
		agent.Task = nil
		agent.RepairTarget = nil
		return
	end
	local tPos = self:getPos(t)
	local d = (tPos - agent.Hrp.Position).Magnitude
	if d > 4 then
		SwarmLib.moveToward(agent, tPos, dt)
		agent.Status = "TO PATIENT"
	else
		agent.Hrp.Velocity = Vector3.new(0, agent.Hrp.Velocity.Y, 0)
		agent.Status = "REPAIRING"
		-- healing applied in RepairAccum tick
	end
end

function SwarmController:defenderBehavior(agent, dt)
	if agent.Task == nil then
		-- guard the nearest turret pad / home, patrol ring
		if not agent.MoveTarget or (agent.Hrp.Position - agent.MoveTarget).Magnitude < 4 then
			agent.MoveTarget = self:pickPatrol(agent)
		end
		local spd = agent.Hrp.Position - agent.MoveTarget
		SwarmLib.moveToward(agent, agent.MoveTarget, dt)
		agent.Status = "GUARD"
	end
end

function SwarmController:engineerBehavior(agent, dt)
	-- upgrade antenna or depot if stock high enough; else assist build
	local depot = self:findStructure("Depot")
	local antenna = self:findStructure("Antenna")
	local depo = depot and depot:GetAttribute("Level") or 1
	if self.Stock.crystals >= 100 + depo * 50 and depo < 4 then
		if agent.Task == nil then
			local target = (depot or antenna)
			if target then
				agent.Task = "UPGRADE"
				agent.TaskPos = target.Position
				agent.UpgradeTarget = target
				agent.Status = "TASK UPGRADE"
			end
		end
	end
	if agent.Task == "UPGRADE" then
		local target = agent.UpgradeTarget
		if not target or not target.Parent then
			agent.Task = nil
			return
		end
		local d = (target.Position - agent.Hrp.Position).Magnitude
		if d > 5 then
			SwarmLib.moveToward(agent, target.Position, dt)
			agent.Status = "TO UPGRADE"
		else
			agent.Hrp.Velocity = Vector3.new(0, agent.Hrp.Velocity.Y, 0)
			agent.Status = "UPGRADING"
			agent.UpgradeProgress = (agent.UpgradeProgress or 0) + dt
			if not agent.UpgradeFx then
				local fx = Instance.new("Part")
				fx.Size = Vector3.new(8, 8, 8)
				fx.Shape = Enum.PartType.Cylinder
				fx.CFrame = CFrame.new(target.Position) + Vector3.new(0, 4, 0)
				fx.Anchored = true
				fx.CanCollide = false
				fx.Material = Enum.Material.Neon
				fx.Color = Shared.Roles.ENGINEER.color
				fx.Transparency = 0.6
				fx.Parent = Workspace
				agent.UpgradeFx = fx
			end
			if agent.UpgradeProgress >= 5 then
				agent.UpgradeProgress = 0
				local cost = 100 + depo * 50
				self.Stock.crystals = math.max(0, self.Stock.crystals - cost)
				local lvl = (target:GetAttribute("Level") or 1) + 1
				target:SetAttribute("Level", lvl)
				target:SetAttribute("MaxHp", (target:GetAttribute("MaxHp") or 700) + 250)
				target:SetAttribute("Hp", target:GetAttribute("MaxHp"))
				if agent.UpgradeFx then agent.UpgradeFx:Destroy() agent.UpgradeFx = nil end
				if target:GetAttribute("Structure") == "Depot" then
					self.Level = lvl
					self:feed("ENGINEER", agent.Model.Name .. " upgraded DEPOT to level " .. tostring(lvl) .. ".")
				else
					self:feed("ENGINEER", agent.Model.Name .. " upgraded ANTENNA to level " .. tostring(lvl) .. ".")
				end
				agent.Task = nil
				agent.UpgradeTarget = nil
			end
		end
		return
	end
	-- default: help build turrets
	self:builderBehavior(agent, dt)
end

function SwarmController:coordinatorBehavior(agent, dt)
	-- travel between sector cores broadcasting
	if not agent.MoveTarget or (agent.Hrp.Position - agent.MoveTarget).Magnitude < 4 then
		local sec = self.Sectors[math.random(2, #self.Sectors)]
		agent.MoveTarget = sec.def.center
	end
	SwarmLib.moveToward(agent, agent.MoveTarget, dt)
	agent.Status = "SYNC " .. (agent.Sector and agent.Sector.def.id or "?")
	-- occasional ping pulse
	if math.random() < dt * 0.8 then
		self:pingPulse(agent.Hrp.Position)
	end
end

function SwarmController:rebalanceSectors(agent)
	-- every few seconds, if a sector is empty & unexplored, coordinator heads there
	for _, sec in ipairs(self.Sectors) do
		if not sec.explored and #sec.agents == 0 then
			if not agent.MoveTarget then
				agent.MoveTarget = Shared.randomInSector(sec.def, 12)
				self:feed("COORDINATOR", agent.Model.Name .. " redirecting swarm to " .. sec.def.id .. ".")
			end
			break
		end
	end
end

function SwarmController:pingPulse(pos)
	local ring = Instance.new("Part")
	ring.Name = "Ping"
	ring.Shape = Enum.PartType.Cylinder
	ring.Size = Vector3.new(2, 0.3, 2)
	ring.CFrame = CFrame.new(pos + Vector3.new(0, 0.6, 0))
	ring.Anchored = true
	ring.CanCollide = false
	ring.Material = Enum.Material.Neon
	ring.Color = Shared.Roles.COORDINATOR.color
	ring.Transparency = 0.25
	ring.Parent = Workspace
	TweenService:Create(ring, TweenInfo.new(0.8, Enum.EasingStyle.Quad, Enum.EasingDirection.Out), { Size = Vector3.new(12, 0.3, 12), Transparency = 1 }):Play()
	Debris:AddItem(ring, 1)
end

-- hostiles ------------------------------------------------------------------

function SwarmController:startWave(forceWave)
	if self.WaveActive and not forceWave then
		self:feed("SYS", "Wave already in progress.")
		return
	end
	if forceWave and self.WaveActive then
		self:feed("SYS", "Wave already in progress — clearing remaining hostiles.")
		for i = #self.NullTicks, 1, -1 do
			local nt = self.NullTicks[i]
			nt.Model:Destroy()
		end
		self.NullTicks = {}
	end
	self.Wave = self.Wave + 1
	self.WaveActive = true
	local count = 4 + self.Wave * 3
	local hpMult = 1 + (self.Wave - 1) * 0.55
	self:feed("WAVE", "WAVE " .. tostring(self.Wave) .. " — " .. tostring(count) .. " Null Ticks detected.")
	for i = 1, count do
		self:spawnNullTick(hpMult, i, count)
	end
end

function SwarmController:spawnNullTick(hpMult, i, count)
	local model = Instance.new("Model")
	model.Name = "NullTick"
	local role = Shared.Roles.DEFENDER
	local hp = math.floor(55 * hpMult + 10)

	local hrp = Instance.new("Part")
	hrp.Name = "HumanoidRootPart"
	hrp.Size = Vector3.new(2, 2, 1)
	hrp.Transparency = 1
	hrp.CanCollide = false
	hrp.Parent = model

	local humanoid = Instance.new("Humanoid")
	humanoid.MaxHealth = hp
	humanoid.Health = hp
	humanoid.WalkSpeed = 14
	humanoid.DisplayDistanceType = Enum.HumanoidDisplayDistanceType.None
	humanoid.HealthDisplayType = Enum.HumanoidHealthDisplayType.AlwaysOff
	humanoid.Parent = model

	local torso = Instance.new("Part")
	torso.Name = "Torso"
	torso.Size = Vector3.new(2, 2, 1)
	torso.Material = Enum.Material.Slate
	torso.Color = Color3.fromRGB(40, 40, 46)
	torso.Parent = model

	local head = Instance.new("Part")
	head.Name = "Head"
	head.Size = Vector3.new(1.2, 1.2, 1.2)
	head.Material = Enum.Material.Metal
	head.Color = Color3.fromRGB(28, 28, 34)
	head.Parent = model

	local eye = Instance.new("Part")
	eye.Name = "Eye"
	eye.Size = Vector3.new(0.9, 0.25, 0.15)
	eye.Material = Enum.Material.Neon
	eye.Color = Color3.fromRGB(255, 60, 60)
	eye.Parent = head

	local armL = Instance.new("Part")
	armL.Name = "Left Arm"
	armL.Size = Vector3.new(1, 2, 1)
	armL.Material = Enum.Material.Metal
	armL.Color = Color3.fromRGB(40, 40, 46)
	armL.Parent = model

	local armR = Instance.new("Part")
	armR.Name = "Right Arm"
	armR.Size = Vector3.new(1, 2, 1)
	armR.Material = Enum.Material.Metal
	armR.Color = Color3.fromRGB(40, 40, 46)
	armR.Parent = model

	local legL = Instance.new("Part")
	legL.Name = "Left Leg"
	legL.Size = Vector3.new(1, 2, 1)
	legL.Material = Enum.Material.Slate
	legL.Color = Color3.fromRGB(30, 30, 36)
	legL.Parent = model

	local legR = Instance.new("Part")
	legR.Name = "Right Leg"
	legR.Size = Vector3.new(1, 2, 1)
	legR.Material = Enum.Material.Slate
	legR.Color = Color3.fromRGB(30, 30, 36)
	legR.Parent = model

	-- welds
	local function weld(p, to, c0)
		local w = Instance.new("Weld")
		w.Part0 = to
		w.Part1 = p
		w.C0 = c0 or CFrame.new()
		w.Parent = p
	end
	weld(torso, hrp, CFrame.new(0, -0.8, 0))
	weld(head, torso, CFrame.new(0, 1.7, 0))
	weld(eye, head, CFrame.new(0, 0.05, -0.62))
	weld(armL, torso, CFrame.new(-1.2, 0.6, 0))
	weld(armR, torso, CFrame.new(1.2, 0.6, 0))
	weld(legL, torso, CFrame.new(-0.5, -1.8, 0))
	weld(legR, torso, CFrame.new(0.5, -1.8, 0))

	local ang = math.random() * math.pi * 2
	local radius = 130 + math.random() * 50
	local pos = HOME + Vector3.new(math.cos(ang) * radius, 0, math.sin(ang) * radius)

	model.Parent = Workspace
	model:PivotTo(CFrame.new(pos))
	for _, p in ipairs(model:GetDescendants()) do
		if p:IsA("BasePart") then
			PhysicsService:SetPartCollisionGroup(p, "Hostiles")
		end
	end
	CollectionService:AddTag(model, Shared.HOSTILE_TAG)
	model:SetAttribute("Hp", hp)
	model:SetAttribute("MaxHp", hp)

	local rec = {
		Model = model,
		Hrp = hrp,
		Humanoid = humanoid,
		Alive = true,
		Target = nil,
		Cooldown = 0,
		Speed = 13 + math.random() * 3,
		Wave = self.Wave,
	}
	table.insert(self.NullTicks, rec)
	return rec
end

function SwarmController:thinkHostiles(dt, now)
	for _, nt in ipairs(self.NullTicks) do
		if nt.Alive then
			-- pick target: nearest clone, else structure
			local target = nt.Target
			if not target or not target.Parent then
				target = self:pickHostileTarget(nt)
				nt.Target = target
			end
			if target then
				local targetPos = self:getPos(target)
				local d = (targetPos - nt.Hrp.Position).Magnitude
				local delta = (targetPos - nt.Hrp.Position) * Vector3.new(1, 0, 1)
				if d > 3 and delta.Magnitude > 0.5 then
					nt.Hrp.Velocity = delta.Unit * nt.Speed
					local look = delta.Unit
					if look.Magnitude > 0.1 then
						nt.Hrp.CFrame = CFrame.new(nt.Hrp.Position, nt.Hrp.Position + look)
					end
				else
					nt.Hrp.Velocity = Vector3.new(0, nt.Hrp.Velocity.Y, 0)
					if now - nt.Cooldown >= 1 then
						nt.Cooldown = now
						local isClone = target:IsA("Model") and target:FindFirstChildOfClass("Humanoid")
						if isClone then
							local dead = SwarmLib.hurtModel(target, 12, nt)
							if dead then
								nt.Target = nil
							end
						else
							local hp = target:GetAttribute("Hp")
							if hp then
								target:SetAttribute("Hp", math.max(0, hp - 15))
								self:spawnHealSpark(target, Color3.fromRGB(255, 70, 70))
							end
						end
					end
				end
			else
				nt.Hrp.Velocity = Vector3.new(0, nt.Hrp.Velocity.Y, 0)
			end
		end
	end
	-- cleanup dead
	for i = #self.NullTicks, 1, -1 do
		local nt = self.NullTicks[i]
		if not nt.Alive then
			table.remove(self.NullTicks, i)
		end
	end
end

function SwarmController:getPos(obj)
	if obj:IsA("BasePart") then
		return obj.Position
	end
	return obj:GetPivot().Position
end

function SwarmController:pickHostileTarget(nt)
	local best = nil
	local bestD = math.huge
	for _, agent in ipairs(self.Agents) do
		if agent.Alive then
			local d = (agent.Hrp.Position - nt.Hrp.Position).Magnitude
			if d < bestD then
				bestD = d
				best = agent.Model
			end
		end
	end
	-- if none nearby clones, go for structures
	if not best or bestD > 90 then
		for _, st in ipairs(self.Structures) do
			local hp = st:GetAttribute("Hp")
			if hp and hp > 0 then
				local d = (st.Position - nt.Hrp.Position).Magnitude
				if d < bestD then
					bestD = d
					best = st
				end
			end
		end
	end
	return best
end

function SwarmController:onNullTickDown(nt, killer)
	if not nt.Alive then return end
	nt.Alive = false
	nt.Model:Destroy()
	self:feed("DEFENDER", killer.Model.Name .. " nullified a Null Tick. (" .. tostring(#self.NullTicks - 1) .. " remain)")
	-- drop crystal scrap
	self.Stock.crystals = self.Stock.crystals + 4
	local orb = Instance.new("Part")
	orb.Shape = Enum.PartType.Ball
	orb.Size = Vector3.new(0.8, 0.8, 0.8)
	orb.CFrame = CFrame.new(nt.Hrp.Position)
	orb.Anchored = true
	orb.CanCollide = false
	orb.Material = Enum.Material.Neon
	orb.Color = Color3.fromRGB(255, 80, 80)
	orb.Parent = Workspace
	TweenService:Create(orb, TweenInfo.new(0.7), { Transparency = 1, Size = Vector3.new(0.2, 0.2, 0.2) }):Play()
	Debris:AddItem(orb, 0.8)
end

function SwarmController:updateWave(now)
	if not self.WaveActive then
		-- auto-start next wave after delay
		if self.Wave > 0 then
			if not self.NextWaveAt then
				self.NextWaveAt = now + 45
				self:feed("SYS", "Next wave in 45s. Swarm is recovering.")
			elseif now >= self.NextWaveAt then
				self.NextWaveAt = nil
				self:startWave()
			end
		end
		return
	end
	local alive = 0
	for _, nt in ipairs(self.NullTicks) do
		if nt.Alive then alive = alive + 1 end
	end
	if alive == 0 then
		self.WaveActive = false
		self.NextWaveAt = now + 45
		self:feed("SYS", "Wave " .. tostring(self.Wave) .. " repelled. +" .. tostring(10 + self.Wave * 5) .. " crystals.")
		self.Stock.crystals = self.Stock.crystals + 10 + self.Wave * 5
	end
end

function SwarmController:updateTurrets(dt)
	for _, turret in ipairs(self.Turrets) do
		if turret.model and turret.model.Parent then
			local best = nil
			local bestD = 50
			for _, nt in ipairs(self.NullTicks) do
				if nt.Alive then
					local d = (nt.Hrp.Position - turret.orb.Position).Magnitude
					if d < bestD then
						bestD = d
						best = nt
					end
				end
			end
			turret.cooldown = turret.cooldown - dt
			if best and turret.cooldown <= 0 then
				turret.cooldown = 0.6
				-- aim barrel
				local look = (best.Hrp.Position - turret.orb.Position) * Vector3.new(1, 0, 1)
				if look.Magnitude > 0.1 then
					turret.orb.CFrame = CFrame.new(turret.orb.Position, turret.orb.Position + look.Unit)
				end
				local dead = SwarmLib.hurtModel(best.Model, 12, turret.model)
				-- laser
				local laser = Instance.new("Part")
				laser.Name = "Laser"
				laser.Shape = Enum.PartType.Cylinder
				local mid = (turret.orb.Position + best.Hrp.Position) / 2
				local len = (best.Hrp.Position - turret.orb.Position).Magnitude
				laser.Size = Vector3.new(0.3, 0.3, len)
				laser.CFrame = CFrame.new(mid, best.Hrp.Position)
				laser.Anchored = true
				laser.CanCollide = false
				laser.Material = Enum.Material.Neon
				laser.Color = Color3.fromRGB(255, 90, 90)
				laser.Transparency = 0.15
				laser.Parent = Workspace
				TweenService:Create(laser, TweenInfo.new(0.2), { Transparency = 1 }):Play()
				Debris:AddItem(laser, 0.25)
				if dead then
					self:onNullTickDown(best, { Model = turret.model })
				end
			end
		end
	end
end

-- fx + feed -----------------------------------------------------------------

function SwarmController:spawnMuzzle(agent, nt)
	local tip = agent.Hrp.Position + agent.Hrp.CFrame.LookVector * 2.2 + Vector3.new(0, 0.5, 0)
	local tracer = Instance.new("Part")
	tracer.Name = "Tracer"
	tracer.Shape = Enum.PartType.Cylinder
	local mid = (tip + nt.Hrp.Position) / 2
	local len = (nt.Hrp.Position - tip).Magnitude
	tracer.Size = Vector3.new(0.18, 0.18, len)
	tracer.CFrame = CFrame.new(mid, nt.Hrp.Position)
	tracer.Anchored = true
	tracer.CanCollide = false
	tracer.Material = Enum.Material.Neon
	tracer.Color = Shared.Roles[agent.RoleId].color
	tracer.Transparency = 0.1
	tracer.Parent = Workspace
	TweenService:Create(tracer, TweenInfo.new(0.15), { Transparency = 1 }):Play()
	Debris:AddItem(tracer, 0.2)
end

function SwarmController:spawnHealSpark(target, color)
	local tPos = self:getPos(target)
	local spark = Instance.new("Part")
	spark.Shape = Enum.PartType.Ball
	spark.Size = Vector3.new(0.6, 0.6, 0.6)
	spark.CFrame = CFrame.new(tPos + Vector3.new((math.random() - 0.5) * 1.5, math.random() * 2, (math.random() - 0.5) * 1.5))
	spark.Anchored = true
	spark.CanCollide = false
	spark.Material = Enum.Material.Neon
	spark.Color = color
	spark.Parent = Workspace
	TweenService:Create(spark, TweenInfo.new(0.5), { Transparency = 1, Size = Vector3.new(0.15, 0.15, 0.15) }):Play()
	Debris:AddItem(spark, 0.6)
end

function SwarmController:findStructure(kind)
	for _, st in ipairs(self.Structures) do
		if st:GetAttribute("Structure") == kind then
			return st
		end
	end
	return nil
end

function SwarmController:feed(system, msg)
	local entry = { t = os.clock(), sys = system, msg = msg }
	table.insert(self.Feed, entry)
	if #self.Feed > 40 then
		table.remove(self.Feed, 1)
	end
end

function SwarmController:broadcastStats()
	local remotes = self.Remotes
	if not remotes then return end
	local counts = {}
	for _, role in ipairs(Shared.RoleOrder) do
		counts[role] = { total = Shared.Roles[role].count, alive = 0, hp = 0 }
	end
	local aliveAll = 0
	for _, agent in ipairs(self.Agents) do
		if agent.Alive then
			aliveAll = aliveAll + 1
			counts[agent.RoleId].alive = counts[agent.RoleId].alive + 1
		end
	end
	local volumeCounts = {}
	for _, sec in ipairs(self.Sectors) do
		volumeCounts[sec.def.id] = #sec.agents
	end
	local stats = {
		crystals = math.floor(self.Stock.crystals),
		level = self.Level,
		wave = self.Wave,
		waveActive = self.WaveActive,
		swarmAlive = aliveAll,
		swarmTotal = #self.Agents,
		hostiles = #self.NullTicks,
		hostilesAlive = aliveAll and 0 or 0,
		counts = counts,
		volumes = volumeCounts,
		turrets = #self.Turrets,
	}
	-- hostile count alive
	local h = 0
	for _, nt in ipairs(self.NullTicks) do if nt.Alive then h = h + 1 end end
	stats.hostilesAlive = h

	remotes.StatsBroadcast:FireAllClients(stats)

	-- task broadcast (sparse)
	self.TaskAccum = (self.TaskAccum or 0) + 1
	if self.TaskAccum >= 3 then
		self.TaskAccum = 0
		local tasks = {}
		for _, t in ipairs(self.Tasks) do
			if not t.claimedBy then
				table.insert(tasks, { kind = t.kind, volume = t.volume, pos = t.pos })
			end
		end
		local feed = {}
		for i = math.max(1, #self.Feed - 7), #self.Feed do
			table.insert(feed, self.Feed[i])
		end
		remotes.TaskBroadcast:FireAllClients(tasks)
		remotes.FeedBroadcast:FireAllClients(feed)
	end
end

-- commands ------------------------------------------------------------------

function SwarmController:reform()
	self:feed("SYS", "Swarm re-forming at VAULT-1...")
	local ang = 0
	for i, agent in ipairs(self.Agents) do
		if agent.Alive then
			ang = ang + (math.pi * 2 / self.SwarmSize) * 1
			local pos = SPAWN + Vector3.new(math.cos(ang * 3) * 8, 0, math.sin(ang * 3) * 8)
			agent.MoveTarget = pos
			agent.Task = nil
			agent.TaskTarget = nil
			agent.MiningTarget = nil
			agent.RepairTarget = nil
			agent.Status = "REFORM"
		end
	end
end

function SwarmController:report()
	local lines = { "== SWARM STATUS ==" }
	for _, sec in ipairs(self.Sectors) do
		local status = sec.explored and (sec.secured and "SECURED" or "ACTIVE") or "UNKNOWN"
		table.insert(lines, string.format("%s  %-8s  agents:%d", sec.def.id, status, #sec.agents))
	end
	table.insert(lines, string.format("Crystals: %d   Level: %d   Wave: %d", math.floor(self.Stock.crystals), self.Level, self.Wave))
	for _, line in ipairs(lines) do
		self:feed("REPORT", line)
	end
end

function SwarmController:healAll()
	for _, agent in ipairs(self.Agents) do
		if agent.Alive then
			local max = agent.Model:GetAttribute("MaxHp") or 100
			agent.Model:SetAttribute("Hp", max)
			agent.Humanoid.Health = max
		end
	end
	self:feed("SYS", "All clones fully healed.")
end

function SwarmController:toggleGod()
	self.GodMode = not self.GodMode
	self:feed("SYS", self.GodMode and "GOD MODE enabled — clones cannot fall." or "GOD MODE disabled.")
end

-- dispatch parsed command
function SwarmController:dispatch(cmd, args)
	if cmd == "reform" then
		self:reform()
	elseif cmd == "report" then
		self:report()
	elseif cmd == "wave" then
		self:startWave(true)
	elseif cmd == "heal" then
		self:healAll()
	elseif cmd == "god" then
		self:toggleGod()
	elseif cmd == "swarm" then
		local n = tonumber(args[1])
		if n and n >= 1 and n <= 35 then
			self.SwarmSize = n
			self:feed("SYS", "Swarm size set to " .. tostring(n) .. " (visual badge only — all 35 remain online).")
		end
	end
end

return SwarmController
