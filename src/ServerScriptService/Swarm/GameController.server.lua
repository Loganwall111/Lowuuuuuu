--!nonstrict
-- SERVER // Bootstraps the whole game: remotes, swarm brain, chat commands.
local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Shared = require(ReplicatedStorage.Swarm.Shared)
local SwarmController = require(script.Parent.SwarmController)
local Commands = require(script.Parent.Commands)

local Players = game:GetService("Players")

-- create remotes
local remoteFolder = Instance.new("Folder")
remoteFolder.Name = "SwarmRemotes"
remoteFolder.Parent = ReplicatedStorage

local remotes = {}
for _, name in ipairs(Shared.RemoteNames) do
	local r = Instance.new("RemoteEvent")
	r.Name = name
	r.Parent = remoteFolder
	remotes[name] = r
end

-- start the swarm brain
local controller = SwarmController.new()
controller.Remotes = remotes
controller:start()

-- client requests
remotes.RequestAgentInfo.OnServerEvent:Connect(function(player)
	local stats = {
		crystals = math.floor(controller.Stock.crystals),
		level = controller.Level,
		wave = controller.Wave,
		waveActive = controller.WaveActive,
		swarmAlive = 0,
		swarmTotal = #controller.Agents,
	}
	for _, agent in ipairs(controller.Agents) do
		if agent.Alive then stats.swarmAlive = stats.swarmAlive + 1 end
	end
	remotes.StatsBroadcast:FireClient(player, stats)
end)

-- chat commands
Players.PlayerAdded:Connect(function(player)
	player:WaitForChild("PlayerGui")
	player.Chatted:Connect(function(msg)
		Commands.handle(player, msg, controller)
	end)
end)

print("[CLONE] Multi-Volume Agent Swarm online. 35 clones running. Use /help in chat.")
