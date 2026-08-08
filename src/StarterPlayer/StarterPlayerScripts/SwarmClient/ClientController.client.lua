--!nonstrict
-- CLIENT // CLONE: Multi-Volume Agent Swarm — HUD, feed, stats, click-inspect.
local Shared = require(game:GetService("ReplicatedStorage").Swarm.Shared)

local Players = game:GetService("Players")
local ReplicatedStorage = game:GetService("ReplicatedStorage")
local RunService = game:GetService("RunService")
local TweenService = game:GetService("TweenService")
local UserInputService = game:GetService("UserInputService")

local player = Players.LocalPlayer
local playerGui = player:WaitForChild("PlayerGui")

local remoteFolder = ReplicatedStorage:WaitForChild("SwarmRemotes")
local remotes = {}
for _, name in ipairs(Shared.RemoteNames) do
	remotes[name] = remoteFolder:WaitForChild(name)
end

-- ------------------------------------------------------------------ UI core
local gui = Instance.new("ScreenGui")
gui.Name = "SwarmHUD"
gui.IgnoreGuiInset = true
gui.ResetOnSpawn = false
gui.Parent = playerGui

local function mkText(name, parent, pos, size, text, color, font, sizeScale)
	local lbl = Instance.new("TextLabel")
	lbl.Name = name
	lbl.Position = pos
	lbl.Size = size
	lbl.Text = text or ""
	lbl.TextColor3 = color or Color3.fromRGB(220, 220, 235)
	lbl.BackgroundTransparency = 1
	lbl.Font = font or Enum.Font.Gotham
	lbl.TextXAlignment = Enum.TextXAlignment.Left
	lbl.TextYAlignment = Enum.TextYAlignment.Top
	lbl.TextScaled = false
	lbl.TextSize = sizeScale or 16
	lbl.ZIndex = 5
	lbl.Parent = parent
	return lbl
end

local function mkPanel(name, parent, pos, size, bg, transp)
	local f = Instance.new("Frame")
	f.Name = name
	f.Position = pos
	f.Size = size
	f.BackgroundColor3 = bg or Color3.fromRGB(10, 12, 20)
	f.BackgroundTransparency = transp or 0.35
	f.BorderSizePixel = 0
	f.ZIndex = 4
	f.Parent = parent
	local stroke = Instance.new("UIStroke")
	stroke.Thickness = 1
	stroke.Color = Color3.fromRGB(120, 130, 180)
	stroke.Transparency = 0.5
	stroke.Parent = f
	return f
end

-- top-left: title
local titlePanel = mkPanel("TitlePanel", gui, UDim2.fromOffset(12, 10), UDim2.fromOffset(320, 78))
mkText("GameTitle", titlePanel, UDim2.fromOffset(14, 8), UDim2.fromOffset(292, 22),
	Shared.GameTitle, Color3.fromRGB(255, 255, 255), Enum.Font.GothamBlack, 18)
mkText("Subtitle", titlePanel, UDim2.fromOffset(14, 32), UDim2.fromOffset(292, 16),
	Shared.GameSubtitle .. " · v" .. Shared.Version, Color3.fromRGB(170, 180, 220), Enum.Font.Gotham, 13)
mkText("Hint", titlePanel, UDim2.fromOffset(14, 52), UDim2.fromOffset(292, 14),
	"Click any clone to inspect · chat /help", Color3.fromRGB(120, 130, 160), Enum.Font.Gotham, 12)

-- top-right: live stats
local statsPanel = mkPanel("StatsPanel", gui, UDim2.new(1, -332, 0, 10), UDim2.fromOffset(320, 150))
local statsLabels = {}
local statNames = {
	{ key = "wave", label = "WAVE", color = Color3.fromRGB(255, 140, 140) },
	{ key = "swarm", label = "SWARM ONLINE", color = Color3.fromRGB(120, 230, 160) },
	{ key = "crystals", label = "CRYSTALS", color = Color3.fromRGB(190, 160, 255) },
	{ key = "turrets", label = "TURRETS", color = Color3.fromRGB(255, 200, 120) },
	{ key = "hostiles", label = "HOSTILES", color = Color3.fromRGB(255, 90, 90) },
	{ key = "level", label = "DEPOT LEVEL", color = Color3.fromRGB(160, 200, 255) },
}
for i, def in ipairs(statNames) do
	local y = 8 + (i - 1) * 22
	mkText(def.key .. "_L", statsPanel, UDim2.fromOffset(14, y), UDim2.fromOffset(130, 18), def.label, Color3.fromRGB(130, 140, 175), Enum.Font.Gotham, 12)
	statsLabels[def.key] = mkText(def.key .. "_V", statsPanel, UDim2.fromOffset(150, y), UDim2.fromOffset(156, 18), "—", def.color, Enum.Font.GothamBold, 14)
end

-- left: event feed
local feedPanel = mkPanel("FeedPanel", gui, UDim2.fromOffset(12, 98), UDim2.fromOffset(320, 330))
mkText("FeedTitle", feedPanel, UDim2.fromOffset(12, 8), UDim2.fromOffset(120, 16), "HIVE FEED", Color3.fromRGB(170, 180, 220), Enum.Font.GothamBold, 13)
local feedList = Instance.new("UIListLayout")
feedList.SortOrder = Enum.SortOrder.LayoutOrder
feedList.Padding = UDim.new(0, 3)
feedList.Parent = feedPanel
local feedScroller = Instance.new("ScrollingFrame")
feedScroller.Name = "FeedScroller"
feedScroller.Position = UDim2.fromOffset(12, 28)
feedScroller.Size = UDim2.fromOffset(296, 292)
feedScroller.BackgroundTransparency = 1
feedScroller.BorderSizePixel = 0
feedScroller.ScrollBarThickness = 3
feedScroller.AutomaticCanvasSize = Enum.AutomaticSize.Y
feedScroller.CanvasSize = UDim2.fromScale(0, 1)
feedScroller.Parent = feedPanel
local feedLayout = Instance.new("UIListLayout")
feedLayout.SortOrder = Enum.SortOrder.LayoutOrder
feedLayout.Padding = UDim.new(0, 4)
feedLayout.Parent = feedScroller

local feedColors = {
	SYS = Color3.fromRGB(160, 170, 200),
	HIVE = Color3.fromRGB(190, 160, 255),
	SCOUT = Shared.Roles.SCOUT.color,
	GATHERER = Shared.Roles.GATHERER.color,
	BUILDER = Shared.Roles.BUILDER.color,
	MEDIC = Shared.Roles.MEDIC.color,
	DEFENDER = Shared.Roles.DEFENDER.color,
	ENGINEER = Shared.Roles.ENGINEER.color,
	COORDINATOR = Shared.Roles.COORDINATOR.color,
	WAVE = Color3.fromRGB(255, 120, 120),
	REPORT = Color3.fromRGB(255, 220, 140),
	HELP = Color3.fromRGB(160, 220, 160),
	RESPAWN = Color3.fromRGB(150, 220, 255),
	RES = Color3.fromRGB(150, 220, 255),
}

-- right: sector volumes
local volPanel = mkPanel("VolPanel", gui, UDim2.new(1, -232, 0, 170), UDim2.fromOffset(220, 320))
mkText("VolTitle", volPanel, UDim2.fromOffset(12, 8), UDim2.fromOffset(180, 16), "VOLUMES / SECTORS", Color3.fromRGB(170, 180, 220), Enum.Font.GothamBold, 13)
local volLabels = {}
local volListLayout = Instance.new("UIListLayout")
volListLayout.Padding = UDim.new(0, 4)
volListLayout.Parent = volPanel

-- bottom-left: command bar
local cmdPanel = mkPanel("CmdPanel", gui, UDim2.new(0, 12, 1, -34), UDim2.fromOffset(520, 24))
mkText("CmdText", cmdPanel, UDim2.fromOffset(10, 4), UDim2.fromOffset(500, 16),
	"/wave  ·  /reform  ·  /report  ·  /heal  ·  /god  ·  /help", Color3.fromRGB(150, 160, 190), Enum.Font.Gotham, 13)

-- ------------------------------------------------------------------ state
local state = {
	stats = nil,
	feed = {},
	volumes = {},
}

local function setText(lbl, text)
	if lbl and lbl.Text ~= text then
		lbl.Text = text
	end
end

remotes.StatsBroadcast.OnClientEvent:Connect(function(stats)
	state.stats = stats
	setText(statsLabels.wave, string.format("WAVE %d%s", stats.wave, stats.waveActive and " ⚠" or ""))
	setText(statsLabels.swarm, string.format("%d / %d", stats.swarmAlive, stats.swarmTotal))
	setText(statsLabels.crystals, tostring(math.floor(stats.crystals)))
	setText(statsLabels.turrets, tostring(stats.turrets))
	setText(statsLabels.hostiles, tostring(stats.hostilesAlive))
	setText(statsLabels.level, "Lv " .. tostring(stats.level))
	if stats.volumes then
		state.volumes = stats.volumes
	end
end)

remotes.FeedBroadcast.OnClientEvent:Connect(function(feed)
	if type(feed) == "table" then
		state.feed = feed
		for i, child in ipairs(feedScroller:GetChildren()) do
			if child:IsA("TextLabel") then child:Destroy() end
		end
		local order = 0
		for _, entry in ipairs(feed) do
			order = order + 1
			local color = feedColors[entry.sys] or Color3.fromRGB(200, 200, 210)
			local sysShort = (entry.sys or "SYS"):sub(1, 4)
			local line = mkText("FeedLine", feedScroller, UDim2.fromOffset(0, 0), UDim2.new(1, -8, 0, 26),
				string.format("[%s]  %s", sysShort, entry.msg), color, Enum.Font.Gotham, 12)
			line.TextWrapped = true
			line.TextYAlignment = Enum.TextYAlignment.Center
			line.LayoutOrder = order
			line.AutomaticSize = Enum.AutomaticSize.Y
			line.TextSize = 12
			line.Size = UDim2.new(1, -8, 0, 26)
		end
		feedScroller.CanvasPosition = Vector2.new(0, feedScroller.AbsoluteCanvasSize.Y)
	end
end)

remotes.TaskBroadcast.OnClientEvent:Connect(function(tasks)
	if type(tasks) ~= "table" then return end
	for _, child in ipairs(volPanel:GetChildren()) do
		if child.Name == "TaskLine" then child:Destroy() end
	end
	local order = 1000
	for _, t in ipairs(tasks) do
		order = order + 1
		local line = mkText("TaskLine", volPanel, UDim2.fromOffset(12, 0), UDim2.fromOffset(196, 16),
			string.format("◈ %s @ %s", t.kind, t.volume), Color3.fromRGB(140, 150, 185), Enum.Font.Gotham, 11)
		line.LayoutOrder = order
	end
end)

-- click-to-inspect -----------------------------------------------------------
local selectionPanel = mkPanel("SelectionPanel", gui, UDim2.new(1, -332, 1, -260), UDim2.fromOffset(320, 220))
selectionPanel.Visible = false
local selName = mkText("SelName", selectionPanel, UDim2.fromOffset(14, 10), UDim2.fromOffset(290, 22), "", Color3.fromRGB(255, 255, 255), Enum.Font.GothamBlack, 18)
local selRole = mkText("SelRole", selectionPanel, UDim2.fromOffset(14, 34), UDim2.fromOffset(290, 16), "", Color3.fromRGB(200, 200, 220), Enum.Font.Gotham, 13)
local selSector = mkText("SelSector", selectionPanel, UDim2.fromOffset(14, 54), UDim2.fromOffset(290, 16), "", Color3.fromRGB(150, 160, 190), Enum.Font.Gotham, 12)
local selHp = mkText("SelHp", selectionPanel, UDim2.fromOffset(14, 78), UDim2.fromOffset(290, 16), "", Color3.fromRGB(150, 160, 190), Enum.Font.Gotham, 12)
local hpFillBg = mkPanel("HpBg", selectionPanel, UDim2.fromOffset(14, 100), UDim2.fromOffset(290, 12), Color3.fromRGB(25, 25, 35), 0)
local hpFill = Instance.new("Frame")
hpFill.Name = "HpFill"
hpFill.Size = UDim2.fromScale(1, 1)
hpFill.BackgroundColor3 = Color3.fromRGB(90, 230, 110)
hpFill.BorderSizePixel = 0
hpFill.Parent = hpFillBg
local selBio = mkText("SelBio", selectionPanel, UDim2.fromOffset(14, 122), UDim2.fromOffset(290, 60), "", Color3.fromRGB(140, 150, 180), Enum.Font.Gotham, 12)
selBio.TextWrapped = true
selBio.AutomaticSize = Enum.AutomaticSize.Y
local selPos = mkText("SelPos", selectionPanel, UDim2.fromOffset(14, 188), UDim2.fromOffset(290, 16), "", Color3.fromRGB(110, 120, 150), Enum.Font.Gotham, 11)

local mouse = player:GetMouse()
local selected = nil

mouse.Button1Down:Connect(function()
	local hit = mouse.Target
	local model = hit and hit.Parent
	if model and model:GetAttribute("Role") then
		selected = model
		selectionPanel.Visible = true
	else
		selected = nil
		selectionPanel.Visible = false
	end
end)

-- pre-create volume lines once, update text only on change
local volLines = {}
for i, sec in ipairs(Shared.Volumes) do
	local line = mkText("VolLine", volPanel, UDim2.fromOffset(12, 32 + (i - 1) * 18), UDim2.fromOffset(196, 16),
		"", sec.tint, Enum.Font.Gotham, 12)
	line.LayoutOrder = 100 + i
	volLines[i] = line
end

RunService.RenderStepped:Connect(function()
	-- volume labels
	local stats = state.stats
	if stats and stats.volumes then
		for i, sec in ipairs(Shared.Volumes) do
			local n = stats.volumes[sec.id] or 0
			local line = volLines[i]
			local text = string.format("▮ %s   %s  %d", sec.id, n > 0 and "●" or "○", n)
			if line.Text ~= text then
				line.Text = text
			end
		end
	end
	-- selection tracking
	if selected and selected.Parent then
		local roleId = selected:GetAttribute("Role")
		local role = roleId and Shared.Roles[roleId]
		if role then
			setText(selName, string.format("%s  %s", selected.Name, selected:GetAttribute("DisplayName") or ""))
			setText(selRole, role.label .. " · " .. (role.desc or ""))
			local hp = selected:GetAttribute("Hp") or 0
			local max = selected:GetAttribute("MaxHp") or 1
			local frac = math.clamp(hp / max, 0, 1)
			hpFill.Size = UDim2.fromScale(frac, 1)
			hpFill.BackgroundColor3 = frac > 0.5 and Color3.fromRGB(90, 230, 110) or (frac > 0.25 and Color3.fromRGB(255, 190, 70) or Color3.fromRGB(255, 70, 70))
			setText(selHp, string.format("HP %d / %d", hp, max))
			local hrp = selected:FindFirstChild("HumanoidRootPart")
			if hrp then
				setText(selPos, string.format("POS %.0f, %.0f, %.0f", hrp.Position.X, hrp.Position.Y, hrp.Position.Z))
				-- nearest volume by distance
				local bestV = "ROAM"
				local bestD = 200
				for _, sec in ipairs(Shared.Volumes) do
					local d = (hrp.Position - sec.center).Magnitude
					if d < bestD then bestD = d bestV = sec.id end
				end
				setText(selSector, "SECTOR " .. bestV)
			end
			setText(selBio, (Shared.Bios[selected:GetAttribute("CloneId") or 1] or "") .. "  ·  " .. (roleId and Shared.Roles[roleId].desc or ""))
		end
	elseif selected then
		selected = nil
		selectionPanel.Visible = false
	end
end)

-- initial stats request
remotes.RequestAgentInfo:FireServer()

print("[CLONE] Client HUD online.")
