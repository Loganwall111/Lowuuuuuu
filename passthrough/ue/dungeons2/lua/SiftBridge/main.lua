-- SIFT Bridge — Lua quick path (UE4SS). No C++ build required.
--
-- UE4SS bundles luasocket and a json lib, so the same link works from Lua: we listen on
-- 127.0.0.1:8080, decode Minecraft's {"t":"cam",...} stream, and mirror the control rotation.
-- The C++ module (../src) adds the shared-memory frame ring for compositing; this script is the
-- fastest way to prove the vector path on a fresh Dungeons II install.

local socket = require("socket")
local json = require("json")

local PORT = 8080
local server = nil
local client = nil
local latest = nil
local running = true

local function ws_accept(c)
    local req = ""
    while not req:find("\r\n\r\n") do
        local chunk = c:receive(2048)
        if not chunk then return false end
        req = req .. chunk
    end
    local key = req:match("Sec%-WebSocket%-Key: ([^\r\n]+)")
    local sha1 = require("sha1") -- UE4SS ships a pure-lua sha1 in most builds; else use C++ module
    local GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
    local b64 = require("base64").encode(sha1.sha1_bin(key .. GUID))
    c:send("HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n" ..
           "Sec-WebSocket-Accept: " .. b64 .. "\r\n\r\n")
    return true
end

local function ws_read(c) -- one masked text frame
    local h = c:receive(2)
    if not h then return nil end
    local op, len = h:byte(1) % 16, h:byte(2) % 128
    if len == 126 then
        local e = c:receive(2); len = e:byte(1) * 256 + e:byte(2)
    elseif len == 127 then
        local e = c:receive(8); len = 0
        for i = 1, 8 do len = len * 256 + e:byte(i) end
    end
    local mask = c:receive(4)
    local data = len > 0 and c:receive(len) or ""
    if mask then
        local out = {}
        for i = 1, #data do
            out[i] = string.char(data:byte(i) ~ mask:byte(((i - 1) % 4) + 1))
        end
        data = table.concat(out)
    end
    if op == 8 then return nil end
    if op ~= 1 then return "" end
    return data
end

ExecuteInGameThread(function()
    server = assert(socket.bind("127.0.0.1", PORT))
    server:settimeout(0)
    print("[SiftBridge] listening on ws://127.0.0.1:" .. PORT)
end)

RegisterHook("/Script/Engine.GameViewportClient:Tick", function()
    -- accept exactly one guest; Minecraft keeps reconnecting so order doesn't matter
    if running and not client and server then
        local c = server:accept()
        if c then
            c:settimeout(2)
            if ws_accept(c) then client = c else c:close() end
        end
    end
    if client then
        c_ok, msg = pcall(ws_read, client)
        if not c_ok or msg == nil then
            if client then client:close() end
            client = nil
            latest = nil
        elseif msg ~= "" then
            local ok, m = pcall(json.decode, msg)
            if ok and m and m.t == "cam" and m.p and m.r then
                latest = m
            elseif ok and m and (m.t == "bye" or m.t == "key" and m.k == "escape") then
                running = false
                if client then client:close() end
                client = nil
            end
        end
    end
    if latest then
        -- MC (x, y, z) → UE (x, z, y); UE yaw = 180 − MC yaw. Verify against Live View.
        local pc = UGameplayStatics.GetPlayerController(0)
        if pc then
            pc:SetControlRotation({ Pitch = latest.r[2], Yaw = 180.0 - latest.r[1], Roll = latest.r[3] or 0 })
        end
    end
end)

NotifyOnNewObject("/Script/Engine.PlayerController", function() end)
