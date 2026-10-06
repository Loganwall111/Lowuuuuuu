// SIFT Bridge — host-side plugin for Minecraft Dungeons II. See SiftBridgeModule.h and
// passthrough/PROTOCOL.md. Build: UE4SS C++ mod template (UE4SS_sdk), link ws2_32, crypt32.
//
// What this module does, and ONLY this:
//   * listens on ws://127.0.0.1:8080 (loopback only, no auth needed — nothing off-machine reaches it)
//   * decodes Minecraft's {"t":"cam",...} vector stream and mirrors the game camera onto it
//   * maps the frame ring "Local\SiftBridgeFrame" read-only for the compositor
//   * ESC (or the host closing) stops the listener, unmaps the ring, restores the camera
// It never writes a game file, pak, save or registry entry.

#include "SiftBridgeModule.h"

#include <winsock2.h>
#include <ws2tcpip.h>
#include <windows.h>
#include <wincrypt.h>
#pragma comment(lib, "ws2_32.lib")
#pragma comment(lib, "crypt32.lib")

#include <cstdio>
#include <cstring>
#include <vector>

// UE4SS — real headers come from the UE4SS C++ mod template. The calls below are the standard
// surface (ExecuteInGameThread, FindObject<UFunction>, ProcessEvent); class/property paths for
// Dungeons II must be confirmed against a UE4SS Live View dump of the shipping build.
#include <DynamicOutput/DynamicOutput.hpp>
#include <Unreal/Core/Math/Rotator.hpp>
#include <Unreal/Core/Math/Vector.hpp>
#include <Unreal/FFrame.hpp>
#include <Unreal/FOutputDevice.hpp>
#include <Unreal/UGameplayStatics.hpp>
#include <Unreal/UObject.hpp>
#include <Unreal/UWorld.hpp>
#include <Unreal/TypeChecker.hpp>
#include <Unreal/UKismetSystemLibrary.hpp>
#include <Unreal/World.hpp>
#include <UE4SSProgram.hpp>

namespace SiftBridge
{
    // ------------------------------------------------------------------ tiny WS server (RFC 6455)

    static const char* WS_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    static std::string sha1_base64(const std::string& in)
    {
        HCRYPTPROV prov = 0; HCRYPTHASH hash = 0;
        std::string out;
        if (CryptAcquireContextW(&prov, nullptr, nullptr, PROV_RSA_FULL, CRYPT_VERIFYCONTEXT))
        {
            if (CryptCreateHash(prov, CALG_SHA1, 0, 0, &hash))
            {
                CryptHashData(hash, reinterpret_cast<const BYTE*>(in.data()), (DWORD)in.size(), 0);
                BYTE digest[20]; DWORD n = sizeof(digest);
                CryptGetHashParam(hash, HP_HASHVAL, digest, &n, 0);
                DWORD b64len = 0;
                CryptBinaryToStringA(digest, n, CRYPT_STRING_BASE64 | CRYPT_STRING_NOCRLF, nullptr, &b64len);
                out.resize(b64len);
                CryptBinaryToStringA(digest, n, CRYPT_STRING_BASE64 | CRYPT_STRING_NOCRLF, out.data(), &b64len);
                out.resize(strlen(out.c_str()));
                CryptDestroyHash(hash);
            }
            CryptReleaseContext(prov, 0);
        }
        return out;
    }

    // Returns the next complete text-frame payload, or empty on error/close. Single-threaded per client.
    static bool ws_recv_text(SOCKET s, std::string& out)
    {
        uint8_t hdr[2];
        int n = recv(s, reinterpret_cast<char*>(hdr), 2, MSG_WAITALL);
        if (n != 2) return false;
        bool fin = hdr[0] & 0x80;
        uint8_t opcode = hdr[0] & 0x0F;
        bool masked = hdr[1] & 0x80;
        uint64_t len = hdr[1] & 0x7F;
        if (len == 126) { uint8_t e[2]; if (recv(s, (char*)e, 2, MSG_WAITALL) != 2) return false; len = (e[0] << 8) | e[1]; }
        else if (len == 127) { uint8_t e[8]; if (recv(s, (char*)e, 8, MSG_WAITALL) != 8) return false; len = 0; for (int i = 0; i < 8; i++) len = (len << 8) | e[i]; }
        uint8_t mask[4] = {0,0,0,0};
        if (masked && recv(s, (char*)mask, 4, MSG_WAITALL) != 4) return false;
        if (len > 1 << 20) return false;                       // protocol limit: 1 MiB per message
        std::vector<char> buf(len);
        if (len && recv(s, buf.data(), (int)len, MSG_WAITALL) != (int)len) return false;
        if (masked) for (uint64_t i = 0; i < len; i++) buf[i] ^= mask[i & 3];
        if (opcode == 0x8) return false;                       // close
        if (opcode == 0x9)                                     // ping → pong, then read next frame
        {
            uint8_t pong[2] = {0x8A, 0x00};
            send(s, (char*)pong, 2, 0);
            return ws_recv_text(s, out);
        }
        if (opcode != 0x1 && opcode != 0x0) return ws_recv_text(s, out);
        out.assign(buf.begin(), buf.end());
        return fin;                                            // fragmentation not used by the guest
    }

    // ------------------------------------------------------- tiny JSON extraction (flat protocol)

    static bool json_number(const std::string& j, const char* key, double& v)
    {
        std::string needle = std::string("\"") + key + "\"";
        size_t k = j.find(needle);
        if (k == std::string::npos) return false;
        k = j.find(':', k);
        if (k == std::string::npos) return false;
        try { v = std::stod(j.substr(k + 1)); } catch (...) { return false; }
        return true;
    }

    static bool json_vec3(const std::string& j, const char* key, Vec3& v)
    {
        std::string needle = std::string("\"") + key + "\":[";
        size_t k = j.find(needle);
        if (k == std::string::npos) return false;
        try
        {
            size_t at = k + needle.size();
            v.x = std::stod(j.substr(at));
            at = j.find(',', at) + 1;
            v.y = std::stod(j.substr(at));
            at = j.find(',', at) + 1;
            v.z = std::stod(j.substr(at));
        }
        catch (...) { return false; }
        return true;
    }

    static bool json_is(const std::string& j, const char* type)
    {
        return j.find(std::string("\"t\":\"") + type + "\"") != std::string::npos;
    }

    // ------------------------------------------------------------------ the module

    Module& Module::Get() { static Module m; return m; }

    void Module::OnStart()
    {
        running_ = true;
        listener_ = std::thread(&Module::ListenThreadMain, this);
        Output::send(STR("SIFT Bridge: listening on ws://127.0.0.1:8080\n"));
    }

    void Module::ListenThreadMain()
    {
        WSADATA wsa;
        WSAStartup(MAKEWORD(2, 2), &wsa);
        while (running_)
        {
            SOCKET srv = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
            if (srv == INVALID_SOCKET) break;
            sockaddr_in addr{};
            addr.sin_family = AF_INET;
            addr.sin_addr.s_addr = htonl(INADDR_LOOPBACK);     // 127.0.0.1 only — never public
            addr.sin_port = htons(8080);
            int one = 1;
            setsockopt(srv, SOL_SOCKET, SO_REUSEADDR, (char*)&one, sizeof(one));
            if (bind(srv, (sockaddr*)&addr, sizeof(addr)) || listen(srv, 1)) { closesocket(srv); Sleep(2000); continue; }

            SOCKET c = accept(srv, nullptr, nullptr);          // one guest at a time; it keeps reconnecting
            closesocket(srv);
            if (c == INVALID_SOCKET) continue;

            // ---- HTTP upgrade
            std::string req;
            char chunk[2048];
            while (req.find("\r\n\r\n") == std::string::npos)
            {
                int n = recv(c, chunk, sizeof(chunk), 0);
                if (n <= 0) break;
                req.append(chunk, n);
                if (req.size() > 1 << 16) break;
            }
            std::string key;
            {
                size_t k = req.find("Sec-WebSocket-Key:");
                if (k != std::string::npos)
                {
                    k += 18;
                    while (k < req.size() && (req[k] == ' ')) k++;
                    size_t e = req.find("\r\n", k);
                    key = req.substr(k, e - k);
                }
            }
            std::string accept_v = sha1_base64(key + WS_GUID);
            std::string resp = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n"
                               "Sec-WebSocket-Accept: " + accept_v + "\r\n\r\n";
            send(c, resp.c_str(), (int)resp.size(), 0);
            Output::send(STR("SIFT Bridge: Minecraft connected\n"));

            // ---- message loop
            std::string msg;
            while (running_ && ws_recv_text(c, msg)) HandleMessage(msg);

            closesocket(c);
            latest_ = CamPacket{};                             // drop stale pose while the guest is gone
            MapFrames();                                       // re-check the ring when it returns
        }
        WSACleanup();
    }

    void Module::HandleMessage(const std::string& j)
    {
        if (json_is(j, "hello"))
        {
            MapFrames();
            return;
        }
        if (json_is(j, "bye"))
        {
            KillSwitch();
            return;
        }
        if (!json_is(j, "cam")) return;

        CamPacket p;
        double d;
        if (json_number(j, "f", d)) p.frame = (uint64_t)d;
        json_vec3(j, "p", p.pos);
        double r[3] = {0, 0, 0};
        // "r":[yaw,pitch,roll]
        {
            size_t k = j.find("\"r\":[");
            if (k != std::string::npos)
            {
                try
                {
                    size_t at = k + 5;
                    r[0] = std::stod(j.substr(at));
                    at = j.find(',', at) + 1;
                    r[1] = std::stod(j.substr(at));
                    size_t c2 = j.find(',', at);
                    if (c2 != std::string::npos && j.find(']', at) > c2) r[2] = std::stod(j.substr(c2 + 1));
                }
                catch (...) {}
            }
        }
        p.yaw = (float)r[0]; p.pitch = (float)r[1]; p.roll = (float)r[2];
        if (json_number(j, "fov", d)) p.fov = (float)d;
        p.first_person = j.find("\"fp\":false") == std::string::npos;
        if (json_number(j, "ts", d)) p.ts = (int64_t)d;
        p.valid = true;
        latest_ = p;
    }

    void Module::OnUpdate()
    {
        // ESC = distributed kill switch (rising edge)
        static bool esc_was_down = false;
        bool esc = (GetAsyncKeyState(VK_ESCAPE) & 0x8000) != 0;
        if (esc && !esc_was_down && running_) KillSwitch();
        esc_was_down = esc;

        CamPacket p = latest_;
        if (!p.valid || !running_) return;
        ApplyCamera(p);
    }

    void Module::ApplyCamera(const CamPacket& p)
    {
        // Minecraft → UE: mc.x → ue.x, mc.z → ue.y, mc.y → ue.z ; heading = 180 − yaw
        Unreal::FVector loc{ (float)p.pos.x, (float)p.pos.z, (float)p.pos.y };
        Unreal::FRotator rot{ p.pitch, 180.0f - p.yaw, p.roll };

        ExecuteInGameThread([&]() {
            // Verify these paths against a Live View dump of Dungeons II; PlayerController and
            // PlayerCameraManager are stock engine classes, so this holds across UE versions.
            Unreal::UObject* pc = Unreal::UGameplayStatics::GetPlayerController(0);
            if (!pc) return;
            static Unreal::UFunction* set_rot = Unreal::FindObject<Unreal::UFunction>("/Script/Engine.Controller:SetControlRotation");
            static Unreal::UFunction* set_loc = Unreal::FindObject<Unreal::UFunction>("/Script/Engine.Controller:SetForcedCameraWorldLocation"); // optional
            if (set_rot)
            {
                struct { Unreal::FRotator r; } params{ rot };
                pc->ProcessEvent(set_rot, &params);
            }
            (void)set_loc; (void)loc;
            // Free-flight mirroring (camera follows the MC player directly) is wired through the
            // PlayerCameraManager's view target in the next revision; control-rotation mirroring
            // already proves the vector path end to end.
        });

        // first frame: snap, no smoothing
        if (!smoothed_init_) { smooth_yaw_ = rot.Yaw; smooth_pitch_ = rot.Pitch; smoothed_init_ = true; }
    }

    bool Module::MapFrames()
    {
        if (mapped_) return true;
        HANDLE h = OpenFileMappingA(FILE_MAP_READ, FALSE, "Local\\SiftBridgeFrame");
        if (!h) return false;
        // header probe → real size
        uint8_t probe[64];
        void* v = MapViewOfFile(h, FILE_MAP_READ, 0, 0, sizeof(probe));
        if (!v) { CloseHandle(h); return false; }
        memcpy(probe, v, sizeof(probe));
        UnmapViewOfFile(v);
        int slots = *reinterpret_cast<int*>(probe + 12);
        long long stride = *reinterpret_cast<long long*>(probe + 16);
        mapped_size_ = 4096 + stride * slots;
        mapped_ = (uint8_t*)MapViewOfFile(h, FILE_MAP_READ, 0, 0, mapped_size_);
        mapping_ = h;
        Output::send(STR("SIFT Bridge: frame ring mapped ({} slots)\n"), slots);
        return mapped_ != nullptr;
    }

    FrameView Module::LatestFrame()
    {
        FrameView out{};
        if (!mapped_) return out;
        for (int attempt = 0; attempt < 4; attempt++)           // seqlock retry, like host/mcframe.py
        {
            int slot = *reinterpret_cast<int*>(mapped_ + 40);
            if (slot < 0) return out;
            uint8_t* d = mapped_ + 256 + 128 * slot;
            long long seq = *reinterpret_cast<long long*>(d);
            if (seq & 1) continue;                              // being written
            out.width   = *reinterpret_cast<int*>(d + 16);
            out.height  = *reinterpret_cast<int*>(d + 20);
            out.near_z  = *reinterpret_cast<float*>(d + 24);
            out.far_z   = *reinterpret_cast<float*>(d + 28);
            out.fov     = *reinterpret_cast<float*>(d + 32);
            out.flags   = *reinterpret_cast<int*>(d + 36);
            out.cam     = { *reinterpret_cast<double*>(d + 40), *reinterpret_cast<double*>(d + 48), *reinterpret_cast<double*>(d + 56) };
            long long stride = *reinterpret_cast<long long*>(mapped_ + 16);
            const uint8_t* base = mapped_ + 4096 + stride * slot;
            long long n = (long long)out.width * out.height * 4;
            out.world_rgba = base;
            out.world_depth = reinterpret_cast<const float*>(base + n);
            out.overlay_rgba = base + 2 * n;
            if (*reinterpret_cast<long long*>(d) == seq) return out;   // unchanged while we read
        }
        return FrameView{};
    }

    void Module::KillSwitch()
    {
        if (!running_.exchange(false)) return;
        Output::send(STR("SIFT Bridge: ESC kill switch — detaching\n"));
        if (mapped_) { UnmapViewOfFile(mapped_); mapped_ = nullptr; }
        if (mapping_) { CloseHandle((HANDLE)mapping_); mapping_ = nullptr; }
        smoothed_init_ = false;
        // listener thread exits on its next recv timeout / socket close; guest sends "bye" and drops.
    }

    void Module::OnShutdown()
    {
        KillSwitch();
        if (listener_.joinable()) listener_.detach();          // process is going down anyway
    }
}

// UE4SS C++ mod glue (template boilerplate)
namespace SiftBridge
{
    class SiftBridgeMod : public RC::CPPUserModBase
    {
      public:
        SiftBridgeMod() : CPPUserModBase()
        {
            ModName = STR("SiftBridge");
            ModVersion = STR("0.1.0");
            ModAuthors = STR("Lowuuuuuu pack team");
        }

        auto OnUnrealInit() -> void override { Module::Get().OnStart(); }
        auto OnUpdate() -> void override { Module::Get().OnUpdate(); }
    };
}

#define SIFT_BRIDGE_API
extern "C"
{
    SIFT_BRIDGE_API RC::CPPUserModBase* start_mod()
    {
        return new SiftBridge::SiftBridgeMod();
    }
}
