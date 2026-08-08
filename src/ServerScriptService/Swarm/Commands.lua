--!nonstrict
-- SERVER // Chat command parsing.
local Commands = {}

local HELP = {
	"/help        — this list",
	"/wave        — trigger the next wave immediately",
	"/reform      — recall the swarm to VAULT-1",
	"/report      — print sector + swarm status to the feed",
	"/heal        — restore all clones",
	"/god         — toggle god mode",
	"/swarm <n>   — announce swarm size n (1-35)",
}

function Commands.handle(player, msg, controller)
	if not msg or msg:sub(1, 1) ~= "/" then return end
	local parts = {}
	for w in msg:gmatch("%S+") do
		table.insert(parts, w)
	end
	local cmd = (parts[1] or ""):lower():gsub("^/", "")
	if cmd == "help" then
		for _, line in ipairs(HELP) do
			controller:feed("HELP", line)
		end
	elseif cmd == "wave" then
		controller:startWave(true)
	elseif cmd == "reform" then
		controller:reform()
	elseif cmd == "report" then
		controller:report()
	elseif cmd == "heal" then
		controller:healAll()
	elseif cmd == "god" then
		controller:toggleGod()
	elseif cmd == "swarm" then
		controller:dispatch("swarm", { parts[2] })
	else
		controller:feed("SYS", "Unknown command. Type /help")
	end
end

return Commands
