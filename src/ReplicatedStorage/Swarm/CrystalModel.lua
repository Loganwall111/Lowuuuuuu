--!nonstrict
-- Shared factory: the crystal node (resource the swarm mines).
local Crystal = {}
local Shared = require(script.Parent.Shared)

Crystal.NODE_SIZE = Vector3.new(3, 5.2, 3)
Crystal.MAX_HP = 260

function Crystal.new(parent, cf, scale, seed)
	local model = Instance.new("Model")
	model.Name = "CrystalNode"
	model.PrimaryPart = nil

	local crystal = Instance.new("Part")
	crystal.Name = "Crystal"
	crystal.Size = Vector3.new(3.4, 5.4, 3.4) * (scale or 1)
	crystal.CFrame = cf
	crystal.Anchored = true
	crystal.CanCollide = true
	crystal.Material = Enum.Material.Crystal
	crystal.Color = Color3.fromRGB(140, 120, 255)
	if crystal.Material == Enum.Material.Crystal then
		crystal.Color = Color3.fromRGB(170, 160, 255)
	end
	crystal:SetAttribute("CrystalSeed", seed or math.random(1, 10000))
	crystal.Touched:Connect(function(other)
		local parent = other and other.Parent
		if parent and parent:FindFirstChild("Humanoid") then
			local hrp = parent:FindFirstChild("HumanoidRootPart")
			if hrp then
				parent:SetAttribute("CrystalBoost", tick() + 6)
			end
		end
	end)
	crystal.Parent = model

	local core = Instance.new("Part")
	core.Name = "Core"
	core.Size = Vector3.new(1.4, 1.6, 1.4)
	core.CFrame = cf * CFrame.new(0, 1.2, 0)
	core.Anchored = true
	core.CanCollide = false
	core.Material = Enum.Material.Neon
	core.Color = Color3.fromRGB(255, 255, 255)
	core.Parent = model

	local beacon = Instance.new("Part")
	beacon.Name = "Beacon"
	beacon.Shape = Enum.PartType.Cylinder
	beacon.Size = Vector3.new(0.3, 0.3, 0.3)
	beacon.CFrame = cf * CFrame.new(0, 2.8, 0)
	beacon.Anchored = true
	beacon.CanCollide = false
	beacon.Material = Enum.Material.Neon
	beacon.Color = Color3.fromRGB(200, 180, 255)
	beacon.Parent = model

	local hpBar = Instance.new("BillboardGui")
	hpBar.Name = "HpGui"
	hpBar.Size = UDim2.fromOffset(120, 12)
	hpBar.StudsOffset = Vector3.new(0, 3.6, 0)
	hpBar.Adornee = crystal
	hpBar.AlwaysOnTop = false

	local bg = Instance.new("Frame")
	bg.Name = "Bg"
	bg.Size = UDim2.fromScale(1, 1)
	bg.BackgroundColor3 = Color3.fromRGB(15, 15, 20)
	bg.BackgroundTransparency = 0.35
	bg.BorderSizePixel = 0
	bg.Parent = hpBar

	local fill = Instance.new("Frame")
	fill.Name = "Fill"
	fill.Size = UDim2.fromScale(1, 1)
	fill.BackgroundColor3 = Color3.fromRGB(190, 160, 255)
	fill.BorderSizePixel = 0
	fill.Parent = bg

	fill.Parent = bg
	hpBar.Parent = model

	model:SetAttribute("Hp", Crystal.MAX_HP)
	model:SetAttribute("MaxHp", Crystal.MAX_HP)
	model:SetAttribute("Volume", Shared.Volumes[math.random(2, #Shared.Volumes)].id)
	model.Parent = parent

	local emissive = Instance.new("PointLight")
	emissive.Color = Color3.fromRGB(190, 160, 255)
	emissive.Range = 16
	emissive.Brightness = 1.5
	emissive.Parent = core

	return model
end

function Crystal.updateHpBar(model)
	local fill = model:FindFirstChild("HpGui") and model.HpGui:FindFirstChild("Fill")
	if not fill then return end
	local hp = model:GetAttribute("Hp") or 0
	local max = model:GetAttribute("MaxHp") or 1
	local frac = math.clamp(hp / max, 0, 1)
	fill.Size = UDim2.fromScale(frac, 1)
	fill.BackgroundColor3 = frac > 0.5 and Color3.fromRGB(190, 160, 255) or (frac > 0.25 and Color3.fromRGB(255, 170, 80) or Color3.fromRGB(255, 70, 70))
end

return Crystal
