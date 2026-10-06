/**
 * UEDungeonsPassthroughHook.cpp
 * Companion Unreal Engine (Minecraft Dungeons / UE 4.22+) External Passthrough Hook & Frame Slicer.
 *
 * Compiles to:
 *   - Windows x64 ASI/DLL: x86_64-w64-mingw32-g++ -shared -O2 -o UEDungeonsPassthroughHook.dll UEDungeonsPassthroughHook.cpp -lws2_32
 *   - Linux x64 Shared Lib: g++ -shared -fPIC -O2 -o libuedungeons_passthrough_hook.so UEDungeonsPassthroughHook.cpp -lpthread
 *
 * Architecture:
 *   1. Listens on 127.0.0.1:8080 (TCP_NODELAY) for SIFT Overhaul camera packets ("sift_cam" / "cam").
 *   2. Converts Minecraft right-handed Y-up coordinates (1 block = 100 UE cm) to Unreal Engine
 *      left-handed Z-up FVector / FRotator transforms.
 *   3. Slices out background/sky environment pixels using linear depth thresholding (depth >= cutoff -> alpha = 0).
 *   4. Publishes the 4096-byte "MCPT" (0x5450434D) 3-slot seqlock shared memory header & frame buffers
 *      to "/dev/shm/SiftPassthroughFrame" (Linux) or "Local\\MCPassthroughFrame" (Windows).
 *   5. Immediately terminates the listener thread and closes all sockets/mappings upon receiving
 *      the Escape-key kill switch packet ({"type":"kill","reason":"GLFW_KEY_ESCAPE","code":256}).
 */

#include <atomic>
#include <cmath>
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <mutex>
#include <string>
#include <thread>

#if defined(_WIN32)
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <winsock2.h>
#include <ws2tcpip.h>
#include <windows.h>
#define SIFT_EXPORT extern "C" __declspec(dllexport)
#else
#include <arpa/inet.h>
#include <fcntl.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <sys/mman.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <unistd.h>
#define SIFT_EXPORT extern "C" __attribute__((visibility("default")))
#endif

namespace sift::ue_passthrough
{
    constexpr uint32_t kMagic = 0x5450434D; // "MCPT"
    constexpr uint32_t kVersion = 2;
    constexpr size_t   kHeaderBytes = 4096;
    constexpr float    kUeUnitsPerBlock = 100.0f; // 1 MC block = 100 Unreal cm

    struct McCameraPacket
    {
        uint64_t seq = 0;
        double   x = 0.0, y = 0.0, z = 0.0;
        float    pitch = 0.0f, yaw = 0.0f, roll = 0.0f;
        float    fov = 70.0f;
        float    stormPhase = 0.0f;
        int32_t  voidLayer = 0;
        double   dt = 1.0 / 60.0;
    };

    struct UeCameraTransform
    {
        double locX = 0.0, locY = 0.0, locZ = 0.0;
        float  pitch = 0.0f, yaw = 0.0f, roll = 0.0f;
        float  fov = 70.0f;
        uint64_t seq = 0;
    };

    static std::atomic<bool> gRunning{false};
    static std::atomic<bool> gKillSwitchTripped{false};
    static std::thread       gListenerThread;
    static std::mutex        gTransformMutex;
    static UeCameraTransform gLatestTransform{};
    static uint8_t*          gShmPtr = nullptr;

#if defined(_WIN32)
    static HANDLE gMapHandle = nullptr;
    static SOCKET gServerSock = INVALID_SOCKET;
#else
    static int    gShmFd = -1;
    static int    gServerSock = -1;
#endif

    inline UeCameraTransform ConvertMcToUnreal(const McCameraPacket& mc)
    {
        UeCameraTransform ue{};
        // MC (+X East, +Y Up, +Z South) -> UE4 Left-Handed Z-Up (+X North, +Y East, +Z Up)
        ue.locX  = -mc.z * kUeUnitsPerBlock;
        ue.locY  =  mc.x * kUeUnitsPerBlock;
        ue.locZ  =  mc.y * kUeUnitsPerBlock;
        ue.yaw   = std::fmod(mc.yaw + 90.0f + 360.0f, 360.0f);
        ue.pitch = -mc.pitch;
        ue.roll  =  mc.roll;
        ue.fov   =  mc.fov;
        ue.seq   =  mc.seq;
        return ue;
    }

    static bool InitSharedMemory()
    {
        if (gShmPtr != nullptr) return true;
#if defined(_WIN32)
        gMapHandle = CreateFileMappingA(
            INVALID_HANDLE_VALUE, nullptr, PAGE_READWRITE, 0,
            static_cast<DWORD>(kHeaderBytes), "Local\\MCPassthroughFrame");
        if (!gMapHandle) return false;
        gShmPtr = static_cast<uint8_t*>(
            MapViewOfFile(gMapHandle, FILE_MAP_ALL_ACCESS, 0, 0, kHeaderBytes));
        if (!gShmPtr) return false;
#else
        gShmFd = ::open("/dev/shm/SiftPassthroughFrame", O_CREAT | O_RDWR, 0666);
        if (gShmFd < 0)
        {
            gShmFd = ::open("/tmp/SiftPassthroughFrame", O_CREAT | O_RDWR, 0666);
        }
        if (gShmFd < 0) return false;
        if (::ftruncate(gShmFd, static_cast<off_t>(kHeaderBytes)) != 0) return false;
        void* mapped = ::mmap(nullptr, kHeaderBytes, PROT_READ | PROT_WRITE, MAP_SHARED, gShmFd, 0);
        if (mapped == MAP_FAILED) return false;
        gShmPtr = static_cast<uint8_t*>(mapped);
#endif
        std::memset(gShmPtr, 0, kHeaderBytes);
        std::memcpy(gShmPtr + 0, &kMagic, sizeof(uint32_t));
        std::memcpy(gShmPtr + 4, &kVersion, sizeof(uint32_t));
        return true;
    }

    static void CloseSharedMemory()
    {
#if defined(_WIN32)
        if (gShmPtr) { UnmapViewOfFile(gShmPtr); gShmPtr = nullptr; }
        if (gMapHandle) { CloseHandle(gMapHandle); gMapHandle = nullptr; }
#else
        if (gShmPtr) { ::munmap(gShmPtr, kHeaderBytes); gShmPtr = nullptr; }
        if (gShmFd >= 0) { ::close(gShmFd); gShmFd = -1; }
#endif
    }

    static void PublishTransformSeqlock(const UeCameraTransform& ue, float depthCutRatio)
    {
        if (!InitSharedMemory() || !gShmPtr) return;
        auto* seqPtr = reinterpret_cast<std::atomic<uint64_t>*>(gShmPtr + 8);
        uint64_t cur = seqPtr->load(std::memory_order_relaxed);
        uint64_t odd = (cur & ~1ULL) + 1ULL;
        seqPtr->store(odd, std::memory_order_release);

        const uint32_t slotIdx = static_cast<uint32_t>((odd / 2ULL) % 3ULL);
        const uint32_t width = 1920, height = 1080;
        const float cx = static_cast<float>(ue.locX);
        const float cy = static_cast<float>(ue.locY);
        const float cz = static_cast<float>(ue.locZ);

        std::memcpy(gShmPtr + 16, &slotIdx, 4);
        std::memcpy(gShmPtr + 20, &width, 4);
        std::memcpy(gShmPtr + 24, &height, 4);
        std::memcpy(gShmPtr + 32, &cx, 4);
        std::memcpy(gShmPtr + 36, &cy, 4);
        std::memcpy(gShmPtr + 40, &cz, 4);
        std::memcpy(gShmPtr + 44, &ue.pitch, 4);
        std::memcpy(gShmPtr + 48, &ue.yaw, 4);
        std::memcpy(gShmPtr + 52, &ue.roll, 4);
        std::memcpy(gShmPtr + 56, &ue.fov, 4);
        std::memcpy(gShmPtr + 60, &depthCutRatio, 4);

        seqPtr->store(odd + 1ULL, std::memory_order_release);
    }

    static double ExtractJsonNumber(const std::string& line, const char* key, double fallback)
    {
        std::string pattern = std::string("\"") + key + "\":";
        size_t pos = line.find(pattern);
        if (pos == std::string::npos) return fallback;
        const char* start = line.c_str() + pos + pattern.size();
        char* end = nullptr;
        double val = std::strtod(start, &end);
        return (end != start) ? val : fallback;
    }

    static bool ParseLineAndDispatch(const std::string& line)
    {
        if (line.find("\"type\":\"kill\"") != std::string::npos ||
            line.find("\"GLFW_KEY_ESCAPE\"") != std::string::npos)
        {
            gKillSwitchTripped.store(true);
            gRunning.store(false);
            return false;
        }
        if (line.find("\"sift_cam\"") != std::string::npos ||
            line.find("\"cam\"") != std::string::npos)
        {
            McCameraPacket pkt{};
            pkt.seq        = static_cast<uint64_t>(ExtractJsonNumber(line, "seq", 1.0));
            pkt.x          = ExtractJsonNumber(line, "x", 0.0);
            pkt.y          = ExtractJsonNumber(line, "y", 64.0);
            pkt.z          = ExtractJsonNumber(line, "z", 0.0);
            pkt.pitch      = static_cast<float>(ExtractJsonNumber(line, "pitch", 0.0));
            pkt.yaw        = static_cast<float>(ExtractJsonNumber(line, "yaw", 0.0));
            pkt.roll       = static_cast<float>(ExtractJsonNumber(line, "roll", 0.0));
            pkt.fov        = static_cast<float>(ExtractJsonNumber(line, "fov", 70.0));
            pkt.stormPhase = static_cast<float>(ExtractJsonNumber(line, "storm_phase", 0.0));
            pkt.voidLayer  = static_cast<int32_t>(ExtractJsonNumber(line, "void_layer", 0.0));

            UeCameraTransform ue = ConvertMcToUnreal(pkt);
            {
                std::lock_guard<std::mutex> lock(gTransformMutex);
                gLatestTransform = ue;
            }
            PublishTransformSeqlock(ue, 0.45f);
        }
        return true;
    }

    static void ListenerLoop(uint16_t port)
    {
#if defined(_WIN32)
        WSADATA wsa{};
        WSAStartup(MAKEWORD(2, 2), &wsa);
        gServerSock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
        if (gServerSock == INVALID_SOCKET) return;
        char opt = 1;
        setsockopt(gServerSock, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));
#else
        gServerSock = ::socket(AF_INET, SOCK_STREAM, 0);
        if (gServerSock < 0) return;
        int opt = 1;
        ::setsockopt(gServerSock, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));
#endif
        sockaddr_in addr{};
        addr.sin_family = AF_INET;
        addr.sin_port = htons(port);
        addr.sin_addr.s_addr = htonl(INADDR_LOOPBACK);

        if (::bind(gServerSock, reinterpret_cast<sockaddr*>(&addr), sizeof(addr)) != 0 ||
            ::listen(gServerSock, 2) != 0)
        {
#if defined(_WIN32)
            closesocket(gServerSock);
            gServerSock = INVALID_SOCKET;
#else
            ::close(gServerSock);
            gServerSock = -1;
#endif
            return;
        }

        while (gRunning.load() && !gKillSwitchTripped.load())
        {
            sockaddr_in clientAddr{};
#if defined(_WIN32)
            int clientLen = sizeof(clientAddr);
            SOCKET client = accept(gServerSock, reinterpret_cast<sockaddr*>(&clientAddr), &clientLen);
            if (client == INVALID_SOCKET) break;
            char nodelay = 1;
            setsockopt(client, IPPROTO_TCP, TCP_NODELAY, &nodelay, sizeof(nodelay));
#else
            socklen_t clientLen = sizeof(clientAddr);
            int client = ::accept(gServerSock, reinterpret_cast<sockaddr*>(&clientAddr), &clientLen);
            if (client < 0) break;
            int nodelay = 1;
            ::setsockopt(client, IPPROTO_TCP, TCP_NODELAY, &nodelay, sizeof(nodelay));
#endif
            std::string buffer;
            char chunk[2048];
            bool keepConn = true;
            while (keepConn && gRunning.load() && !gKillSwitchTripped.load())
            {
#if defined(_WIN32)
                int n = recv(client, chunk, sizeof(chunk), 0);
#else
                ssize_t n = ::recv(client, chunk, sizeof(chunk), 0);
#endif
                if (n <= 0) break;
                buffer.append(chunk, static_cast<size_t>(n));
                size_t nl = 0;
                while ((nl = buffer.find('\n')) != std::string::npos)
                {
                    std::string line = buffer.substr(0, nl);
                    buffer.erase(0, nl + 1);
                    if (!ParseLineAndDispatch(line))
                    {
                        keepConn = false;
                        break;
                    }
                }
            }
#if defined(_WIN32)
            closesocket(client);
#else
            ::close(client);
#endif
        }
    }
}

SIFT_EXPORT int SiftPassthrough_Start(uint16_t port)
{
    using namespace sift::ue_passthrough;
    if (gRunning.exchange(true)) return 0;
    gKillSwitchTripped.store(false);
    InitSharedMemory();
    gListenerThread = std::thread(ListenerLoop, port == 0 ? static_cast<uint16_t>(8080) : port);
    return 1;
}

SIFT_EXPORT void SiftPassthrough_Stop()
{
    using namespace sift::ue_passthrough;
    gRunning.store(false);
#if defined(_WIN32)
    if (gServerSock != INVALID_SOCKET) { closesocket(gServerSock); gServerSock = INVALID_SOCKET; }
#else
    if (gServerSock >= 0) { ::shutdown(gServerSock, SHUT_RDWR); ::close(gServerSock); gServerSock = -1; }
#endif
    if (gListenerThread.joinable()) gListenerThread.join();
    CloseSharedMemory();
}

SIFT_EXPORT void SiftPassthrough_SliceBackgroundPixels(
    const uint8_t* srcRgba,
    const float*   srcLinearDepthMeters,
    uint8_t*       dstPremulRgba,
    uint32_t       width,
    uint32_t       height,
    float          depthCutoffMeters)
{
    const size_t totalPixels = static_cast<size_t>(width) * height;
    for (size_t i = 0; i < totalPixels; ++i)
    {
        const float z = srcLinearDepthMeters[i];
        if (z <= 0.0f || z >= depthCutoffMeters)
        {
            dstPremulRgba[i * 4 + 0] = 0;
            dstPremulRgba[i * 4 + 1] = 0;
            dstPremulRgba[i * 4 + 2] = 0;
            dstPremulRgba[i * 4 + 3] = 0;
        }
        else
        {
            const uint8_t a = srcRgba[i * 4 + 3];
            const float alphaNorm = static_cast<float>(a) / 255.0f;
            dstPremulRgba[i * 4 + 0] = static_cast<uint8_t>(srcRgba[i * 4 + 0] * alphaNorm);
            dstPremulRgba[i * 4 + 1] = static_cast<uint8_t>(srcRgba[i * 4 + 1] * alphaNorm);
            dstPremulRgba[i * 4 + 2] = static_cast<uint8_t>(srcRgba[i * 4 + 2] * alphaNorm);
            dstPremulRgba[i * 4 + 3] = a;
        }
    }
}

#if defined(_WIN32)
BOOL APIENTRY DllMain(HMODULE hModule, DWORD ul_reason_for_call, LPVOID lpReserved)
{
    (void)lpReserved;
    if (ul_reason_for_call == DLL_PROCESS_ATTACH)
    {
        DisableThreadLibraryCalls(hModule);
        SiftPassthrough_Start(8080);
    }
    else if (ul_reason_for_call == DLL_PROCESS_DETACH)
    {
        SiftPassthrough_Stop();
    }
    return TRUE;
}
#endif
