--!nonstrict
-- Shared factory: a full R6 clone body with role cosmetics, name tag, hp gui.
local CloneModel = {}
local Shared = require(script.Parent.Shared)
local RoleData = require(script.Parent.RoleData)

local HEAD_COLORS = {
	Color3.fromRGB(245, 205, 175), Color3.fromRGB(230, 185, 155), Color3.fromRGB(215, 170, 140),
	Color3.fromRGB(255, 225, 195), Color3.fromRGB(240, 196, 165),
}

local function part(name, size, color, material, parent)
	local p = Instance.new("Part")
	p.Name = name
	p.Size = size
	p.Material = material
	p.Color = color
	p.Parent = parent
	return p
end

local function weld(p, to, c0)
	local w = Instance.new("Weld")
	w.Name = p.Name .. "Weld"
	w.Part0 = to
	w.Part1 = p
	w.C0 = c0 or CFrame.new()
	w.Parent = p
end

function CloneModel.new(parent, roleId, nameText, color, index, spawnPos)
	local role = Shared.Roles[roleId]
	local skin = HEAD_COLORS[index % #HEAD_COLORS + 1]
	local mat = RoleData.Materials[roleId]
	local trim = RoleData.Trim[roleId]

	local model = Instance.new("Model")
	model.Name = string.format("%s-%02d", role.prefix, index)
	model:SetAttribute("CloneId", index)
	model:SetAttribute("Role", roleId)
	model:SetAttribute("DisplayName", nameText)
	model:SetAttribute("Hp", role.hp)
	model:SetAttribute("MaxHp", role.hp)

	local humanoid = Instance.new("Humanoid")
	humanoid.MaxHealth = role.hp
	humanoid.Health = role.hp
	humanoid.WalkSpeed = role.speed
	humanoid.JumpPower = 38
	humanoid.AutoRotate = true
	humanoid.DisplayDistanceType = Enum.HumanoidDisplayDistanceType.None
	humanoid.HealthDisplayType = Enum.HumanoidHealthDisplayType.AlwaysOff
	humanoid.Parent = model

	local hrp = Instance.new("Part")
	hrp.Name = "HumanoidRootPart"
	hrp.Size = Vector3.new(2, 2, 1)
	hrp.Transparency = 1
	hrp.CanCollide = false
	hrp.Parent = model

	local hips = Instance.new("Part")
	hips.Name = "HumanoidHip"
	hips.Size = Vector3.new(2, 2, 1)
	hips.Transparency = 1
	hips.CanCollide = false
	hips.Parent = model

	local rootJoint = Instance.new("RootJoint")
	rootJoint.Part0 = hips
	rootJoint.Part1 = hrp
	rootJoint.C0 = CFrame.new(0, 0, 0) * CFrame.Angles(0, 0, 0)
	rootJoint.Parent = model

	humanoid:SetStateEnabled(Enum.HumanoidStateType.FallingDown, false)
	humanoid:SetStateEnabled(Enum.HumanoidStateType.Ragdoll, false)
	humanoid:SetStateEnabled(Enum.HumanoidStateType.Physics, false)
	humanoid:SetStateEnabled(Enum.HumanoidStateType.GettingUp, false)
	humanoid:SetStateEnabled(Enum.HumanoidStateType.Climbing, false)
	humanoid:SetStateEnabled(Enum.HumanoidStateType.Swimming, false)
	humanoid:SetStateEnabled(Enum.HumanoidStateType.Jumping, false)
	humanoid:SetStateEnabled(Enum.HumanoidStateType.Freefall, false)
	humanoid:SetStateEnabled(Enum.HumanoidStateType.Seated, false)
	humanoid:SetStateEnabled(Enum.HumanoidStateType.PlatformStanding, false)
	humanoid:SetStateEnabled(Enum.HumanoidStateType.FallingDown, false)

	local torso = part("Torso", Vector3.new(2, 2, 1), color, mat, model)
	local head = part("Head", Vector3.new(1.2, 1.2, 1.2), skin, Enum.Material.SmoothPlastic, model)
	local leftArm = part("Left Arm", Vector3.new(1, 2, 1), color, mat, model)
	local rightArm = part("Right Arm", Vector3.new(1, 2, 1), color, mat, model)
	local leftLeg = part("Left Leg", Vector3.new(1, 2, 1), Color3.fromRGB(70, 70, 80), Enum.Material.Fabric, model)
	local rightLeg = part("Right Leg", Vector3.new(1, 2, 1), Color3.fromRGB(70, 70, 80), Enum.Material.Fabric, model)

	torso.Material = mat
	torso.Color = color

	-- visor (role-colored)
	local visor = part("Visor", Vector3.new(1.05, 0.3, 0.15), role.color, Enum.Material.Neon, head)
	weld(visor, head, CFrame.new(0, 0.1, -0.62))

	-- antenna
	local antenna = part("Antenna", Vector3.new(0.1, 0.7, 0.1), Color3.fromRGB(200, 200, 210), Enum.Material.Metal, head)
	weld(antenna, head, CFrame.new(0, 0.95, 0))
	local tip = part("Tip", Vector3.new(0.22, 0.22, 0.22), role.color, Enum.Material.Neon, head)
	weld(tip, head, CFrame.new(0, 1.35, 0))

	-- chest trim
	local trimPart = part("Trim", Vector3.new(1.9, 0.35, 1.05), trim, Enum.Material.Neon, torso)
	weld(trimPart, torso, CFrame.new(0, 0.6, 0))

	-- shoulder pads
	local padL = part("PadL", Vector3.new(1.15, 0.6, 1.15), trim, mat, leftArm)
	weld(padL, leftArm, CFrame.new(0, 0.5, 0))
	local padR = part("PadR", Vector3.new(1.15, 0.6, 1.15), trim, mat, rightArm)
	weld(padR, rightArm, CFrame.new(0, 0.5, 0))

	-- backpack
	local pack = part("Pack", Vector3.new(1.6, 1.5, 0.5), Color3.fromRGB(45, 45, 55), Enum.Material.SmoothPlastic, torso)
	weld(pack, torso, CFrame.new(0, 0, 0.75))
	local packLight = part("PackLight", Vector3.new(0.4, 0.4, 0.1), role.color, Enum.Material.Neon, torso)
	weld(packLight, torso, CFrame.new(0, 0.35, 1.05))

	-- faceplate
	local plate = part("Faceplate", Vector3.new(0.9, 0.9, 0.1), Color3.fromRGB(20, 20, 28), Enum.Material.SmoothPlastic, head)
	weld(plate, head, CFrame.new(0, 0, -0.62))

	-- hat? no. name tag
	local tag = Instance.new("BillboardGui")
	tag.Name = "TagGui"
	tag.Size = UDim2.fromOffset(170, 60)
	tag.StudsOffset = Vector3.new(0, 3.1, 0)
	tag.Adornee = head
	tag.AlwaysOnTop = false

	local lbl = Instance.new("TextLabel")
	lbl.Name = "TagLabel"
	lbl.Size = UDim2.fromScale(1, 0.55)
	lbl.BackgroundTransparency = 1
	lbl.Font = Enum.Font.GothamBold
	lbl.TextScaled = true
	lbl.Text = string.format("%s  %s", model.Name, nameText)
	lbl.TextColor3 = role.color
	lbl.TextStrokeTransparency = 0.15
	lbl.TextStrokeColor3 = Color3.fromRGB(0, 0, 0)
	lbl.Parent = tag

	local sub = Instance.new("TextLabel")
	sub.Name = "RoleLabel"
	sub.Size = UDim2.fromScale(1, 0.45)
	sub.Position = UDim2.fromScale(0, 0.55)
	sub.BackgroundTransparency = 1
	sub.Font = Enum.Font.Gotham
	sub.TextScaled = true
	sub.Text = role.label .. " · " .. (Shared.sectorIdAt and "ROAM" or "ROAM")
	sub.TextColor3 = Color3.fromRGB(210, 210, 220)
	sub.TextStrokeTransparency = 0.3
	sub.Parent = tag
	tag.Parent = model

	local hpGui = Instance.new("BillboardGui")
	hpGui.Name = "HpGui"
	hpGui.Size = UDim2.fromOffset(90, 10)
	hpGui.StudsOffset = Vector3.new(0, 2.65, 0)
	hpGui.Adornee = head
	hpGui.AlwaysOnTop = false

	local hbg = Instance.new("Frame")
	hbg.Size = UDim2.fromScale(1, 1)
	hbg.BackgroundColor3 = Color3.fromRGB(10, 10, 15)
	hbg.BackgroundTransparency = 0.4
	hbg.BorderSizePixel = 0
	hbg.Parent = hpGui

	local hfill = Instance.new("Frame")
	hfill.Name = "Fill"
	hfill.Size = UDim2.fromScale(1, 1)
	hfill.BackgroundColor3 = Color3.fromRGB(90, 230, 110)
	hfill.BorderSizePixel = 0
	hfill.Parent = hbg
	hpGui.Parent = model

	local chestLight = Instance.new("PointLight")
	chestLight.Color = role.color
	chestLight.Range = 9
	chestLight.Brightness = 1
	chestLight.Parent = torso

	-- full body brighten: tints all parts slightly
	model:SetAttribute("SkinColor", tostring(skin))

	model.Parent = parent
	model:PivotTo(CFrame.new(spawnPos) * CFrame.Angles(0, math.rad(math.random(-45, 45)), 0))

	-- wait for the humanoid to bind
	humanoid:SetStateEnabled(Enum.HumanoidStateType.FallingDown, false)
	return model
end

function CloneModel.applyTool(model, toolType, parent)
	local roleId = model:GetAttribute("Role")
	local role = Shared.Roles[roleId]
	local sizes = {
		pickaxe = Vector3.new(0.25, 0.25, 2.2),
		rifle = Vector3.new(0.35, 0.35, 2.6),
		medkit = Vector3.new(1.1, 0.7, 0.5),
		wrench = Vector3.new(0.25, 0.25, 1.6),
		hammer = Vector3.new(0.25, 0.25, 1.6),
		scanner = Vector3.new(0.8, 0.8, 0.4),
		beacon = Vector3.new(0.6, 1.1, 0.6),
	}
	local tool = Instance.new("Tool")
	tool.Name = toolType and string.upper(toolType) or "TOOL"
	tool.CanBeDropped = false
	tool.RequiresHandle = false
	tool.Parent = model
	local handle = part("Handle", sizes[toolType] or Vector3.new(0.3, 0.3, 1.5), role.color, Enum.Material.Metal, tool)
	handle.CanCollide = false
	handle.Massless = true
	handle.CFrame = CFrame.new(0, 0, 0)
	local glow = Instance.new("PointLight")
	glow.Color = role.color
	glow.Range = 6
	glow.Brightness = 1
	glow.Parent = handle

	local motor = Instance.new("Motor6D")
	motor.Name = "ToolWeld"
	motor.Part0 = model:FindFirstChild("Right Arm")
	motor.Part1 = handle
	motor.C0 = CFrame.new(0, -1.1, 0)
	motor.C1 = CFrame.new(0, 0, 0.7)
	motor.Parent = handle
	return tool
end

function CloneModel.updateHpBar(model)
	local hpGui = model:FindFirstChild("HpGui")
	if not hpGui then return end
	local fill = hpGui:FindFirstChild("Fill")
	if not fill then return end
	local hp = model:GetAttribute("Hp") or 0
	local max = model:GetAttribute("MaxHp") or 1
	local frac = math.clamp(hp / max, 0, 1)
	fill.Size = UDim2.fromScale(frac, 1)
	fill.BackgroundColor3 = frac > 0.5 and Color3.fromRGB(90, 230, 110) or (frac > 0.25 and Color3.fromRGB(255, 190, 70) or Color3.fromRGB(255, 70, 70))
end

function CloneModel.setSectorLabel(model, sectorName)
	local tag = model:FindFirstChild("TagGui")
	if not tag then return end
	local lbl = tag:FindFirstChild("RoleLabel")
	if not lbl then return end
	lbl.Text = sectorName
end

return CloneModel
