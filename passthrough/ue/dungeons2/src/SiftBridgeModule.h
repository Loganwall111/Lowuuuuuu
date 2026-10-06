#pragma once
// SIFT Bridge — host-side plugin for Minecraft Dungeons II (UE, UE4SS C++ mod).
//
// Listens on ws://127.0.0.1:8080, receives Minecraft's crosshair/camera vector stream,
// mirrors the game camera onto it, and reads the shared-memory frame ring
// ("Local\SiftBridgeFrame") for depth compositing. ESC detaches everything cleanly.
//
// Build against the UE4SS C++ API (github.com/UE4SS-RE/RE-UE4SS). MIT, see NOTICE.

#include <atomic>
#include <cstdint>
#include <string>
#include <thread>

namespace SiftBridge
{
    struct Vec3 { double x, y, z; };

    // One decoded "cam" message from Minecraft (Minecraft coordinates).
    struct CamPacket
    {
        uint64_t frame = 0;
        Vec3 pos{};
        float yaw = 0.f, pitch = 0.f, roll = 0.f;
        float fov = 70.f;
        bool first_person = true;
        int64_t ts = 0;
        bool valid = false;
    };

    // Snapshot of the newest completed frame slot (zero-copy view into the mapping).
    struct FrameView
    {
        int width = 0, height = 0;
        float near_z = 0.f, far_z = 0.f, fov = 0.f;
        int flags = 0;
        Vec3 cam{};
        const uint8_t* world_rgba = nullptr;   // width*height*4, premultiplied
        const float*   world_depth = nullptr;  // raw depth-buffer values
        const uint8_t* overlay_rgba = nullptr;
        bool valid = false;
    };

    class Module
    {
      public:
        static Module& Get();

        // UE4SS mod callbacks
        void OnStart();
        void OnUpdate();       // game thread, every frame
        void OnShutdown();

      private:
        void ListenThreadMain();                       // winsock WS server on 127.0.0.1:8080
        void HandleMessage(const std::string& json);   // parse + publish latest CamPacket
        bool MapFrames();                              // OpenFileMapping + seqlock validation
        FrameView LatestFrame();                       // seqlock read, copy-free
        void ApplyCamera(const CamPacket& p);          // ExecuteInGameThread: mirror POV
        void KillSwitch();                             // ESC: stop listener, unmap, detach

        std::thread listener_;
        std::atomic<bool> running_{false};
        std::atomic<CamPacket> latest_{};              // written by listener, read by game thread
        void* mapping_ = nullptr;                      // HANDLE from OpenFileMappingA
        uint8_t* mapped_ = nullptr;
        size_t mapped_size_ = 0;
        bool smoothed_init_ = false;
        float smooth_yaw_ = 0.f, smooth_pitch_ = 0.f;
    };
}
