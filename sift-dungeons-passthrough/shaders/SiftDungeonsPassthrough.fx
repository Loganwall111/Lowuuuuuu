/**
 * SiftDungeonsPassthrough.fx
 * ReShade 5.x / 6.x Cross-Engine Passthrough & Depth-Occlusion Compositor Shader
 * (SIFT Overhaul x Minecraft Dungeons / Unreal Engine 4.22+)
 *
 * Adapted from universal-modder/examples/minecraft-gta5-passthrough/shader/MCPassthrough.fx
 * and SkyCraft's alpha-masked sky replacement pipeline.
 *
 * Semantic textures bound by the SIFT / universal-modder ReShade add-on from
 * shared memory ("Local\\MCPassthroughFrame" / "/dev/shm/SiftPassthroughFrame"):
 *   - MCWORLD   (RGBA8): Sliced foreground layer with sky/background alpha = 0
 *   - MCDEPTH   (R32F) : Hardware depth buffer (0..1)
 *   - MCOVERLAY (RGBA8): HUD / UI layer
 */

#include "ReShade.fxh"

texture2D TexMcWorld   : MCWORLD;
texture2D TexMcDepth   : MCDEPTH;
texture2D TexMcOverlay : MCOVERLAY;

sampler2D SamplerMcWorld   { Texture = TexMcWorld;   MagFilter = POINT;  MinFilter = POINT;  };
sampler2D SamplerMcDepth   { Texture = TexMcDepth;   MagFilter = LINEAR; MinFilter = LINEAR; };
sampler2D SamplerMcOverlay { Texture = TexMcOverlay; MagFilter = LINEAR; MinFilter = LINEAR; };

uniform float McNearPlane <
    ui_type = "drag";
    ui_min = 0.01; ui_max = 1.0;
    ui_label = "SIFT Near Plane (m)";
> = 0.05;

uniform float McFarPlane <
    ui_type = "drag";
    ui_min = 32.0; ui_max = 2048.0;
    ui_label = "SIFT Far Plane (m)";
> = 512.0;

uniform float SkyDepthCutoff <
    ui_type = "drag";
    ui_min = 10.0; ui_max = 1000.0;
    ui_label = "Sky / Background Slice Cutoff (m)";
> = 450.0;

uniform float3 SunDirection <
    ui_type = "drag";
    ui_label = "Shared Sun Direction";
> = float3(0.42, 0.78, 0.46);

float LinearizeMcDepth(float d)
{
    float z = d * 2.0 - 1.0;
    return (2.0 * McNearPlane * McFarPlane) /
           (McFarPlane + McNearPlane - z * (McFarPlane - McNearPlane));
}

float4 PS_SiftDungeonsPassthrough(float4 vpos : SV_Position, float2 uv : TEXCOORD) : SV_Target
{
    float3 hostColor = tex2D(ReShade::BackBuffer, uv).rgb;
    float  hostDepth = ReShade::GetLinearizedDepth(uv) * McFarPlane;

    float4 mcWorld   = tex2D(SamplerMcWorld, uv);
    float  mcRawD    = tex2D(SamplerMcDepth, uv).r;
    float  mcLinearD = LinearizeMcDepth(mcRawD);
    float4 mcOverlay = tex2D(SamplerMcOverlay, uv);

    float3 composite = hostColor;

    // 1. Foreground depth test & background sky slice (alpha == 0 or depth >= SkyDepthCutoff reveals Dungeons/SIFT sky)
    bool validForeground = (mcWorld.a > 0.001) && (mcRawD < 0.9999) && (mcLinearD < SkyDepthCutoff);
    if (validForeground && (mcLinearD <= hostDepth || hostDepth >= SkyDepthCutoff))
    {
        // 24-step screen-space contact shadow ray-march along shared sun vector
        float shadow = 1.0;
        float2 sunStepUv = normalize(SunDirection.xy + float2(1e-4, 1e-4)) * (1.5 / BUFFER_WIDTH);
        [unroll]
        for (int i = 1; i <= 24; ++i)
        {
            float2 sampleUv = uv + sunStepUv * float(i);
            float  occluderD = LinearizeMcDepth(tex2D(SamplerMcDepth, sampleUv).r);
            if (occluderD < mcLinearD - 0.15 && occluderD > 0.05)
            {
                shadow = lerp(1.0, 0.62, saturate(1.0 - float(i) / 24.0));
                break;
            }
        }
        composite = lerp(hostColor, mcWorld.rgb * shadow, mcWorld.a);
    }

    // 2. Composite HUD / overlay layer on top
    composite = composite * (1.0 - mcOverlay.a) + mcOverlay.rgb;
    return float4(composite, 1.0);
}

technique SiftDungeonsPassthrough <
    ui_label = "SIFT Overhaul x Minecraft Dungeons Passthrough";
>
{
    pass
    {
        VertexShader = PostProcessVS;
        PixelShader  = PS_SiftDungeonsPassthrough;
    }
}
