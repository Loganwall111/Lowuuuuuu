#!/usr/bin/env python3
# Builds the MCSM Ultimate Atmosphere pack v2 (Story Mode Visuals)
import json, os, shutil, zipfile, struct
from PIL import Image, ImageDraw

REPO = '/home/user/Lowuuuuuu'
OUT = f'{REPO}/mcsm-ultimate-atmosphere-fixed'
SRC = '/tmp/rp/assets/fabricskyboxes/textures/sky'   # original pack's gradient art

shutil.rmtree(OUT, ignore_errors=True)
for d in ['assets/forgeskyboxes/textures/sky', 'assets/forgeskyboxes/sky',
          'assets/minecraft/shaders/core', 'assets/minecraft/textures/block',
          'assets/minecraft/optifine', 'assets/mcsm_atmosphere/light']:
    os.makedirs(f'{OUT}/{d}', exist_ok=True)

# ------------------------------------------------------------------ pack icon
icon = Image.open(f'{REPO}/story_mode_icon_raw.png').convert('RGB')
w, h = icon.size
s = min(w, h)
icon = icon.crop(((w-s)//2, (h-s)//2, (w+s)//2, (h+s)//2)).resize((256, 256), Image.LANCZOS)
icon.save(f'{OUT}/pack.png', optimize=True)
print('icon:', icon.size)

# ------------------------------------------------------- sky gradient helpers
CELL, W, H = 512, 1536, 1024

def sample_gradient(path, rows=H):
    im = Image.open(path).convert('RGB')
    sw, sh = im.size
    px = im.load()
    grad = []
    for i in range(rows):
        y = min(sh - 1, int(sh * i / rows))
        r = g = b = n = 0
        for x in range(0, sw, max(1, sw // 64)):
            pr, pg, pb = px[x, y]
            r += pr; g += pg; b += pb; n += 1
        grad.append((r//n, g//n, b//n))
    # gentle smoothing so gradients stay perfectly vertical and seam-free
    for i in range(1, rows-1):
        grad[i] = tuple((grad[i-1][k] + grad[i][k]*2 + grad[i+1][k]) // 4 for k in range(3))
    grad[0] = grad[1]; grad[-1] = grad[-2]
    return grad

def hex2rgb(hx):
    hx = hx.lstrip('#')
    return tuple(int(hx[i:i+2], 16) for i in (0, 2, 4))

def blend(grad, fog, full=0.12, horizon=0.45):
    f = hex2rgb(fog)
    out = []
    for i, c in enumerate(grad):
        t = i / (H - 1)                      # 0 = zenith, 1 = nadir
        k = full + horizon * max(0.0, (t - 0.35) / 0.65)   # more fog toward horizon
        out.append(tuple(int(c[j] * (1 - k) + f[j] * k) for j in range(3)))
    return out

def write_sky(path, grad):
    zenith = grad[0]; nadir = grad[-1]
    row_side = lambda c: bytes(c) * CELL
    buf = bytearray()
    for y in range(H // 2):                                   # top half: west | east | north
        buf += row_side(grad[y]) * 3
    for y in range(H // 2):                                   # bottom half: top | bottom | south
        buf += row_side(zenith) + row_side(nadir) + row_side(grad[H//2 + y])
    Image.frombytes('RGB', (W, H), bytes(buf)).save(path, optimize=True)

base = {t: sample_gradient(f'{SRC}/{t}.png') for t in ('day', 'night', 'sunset')}

# ------------------------------------------------------------------ skyboxes
TIME = {
    'day':    {'in': (0, 2000),      'out': (11000, 13000), 'pri': 10, 'decor': {'showSun': True}},
    'sunset': {'in': (11500, 13200), 'out': (13800, 15000), 'pri': 11, 'decor': {'showSun': True}},
    'night':  {'in': (13500, 15500), 'out': (22000, 23900), 'pri': 12, 'decor': {'showMoon': True, 'showStars': True}},
}

# (day, sunset, night) fog colors per biome family.
# Swamps = dense mossy mist, Deserts/Badlands = golden heat-glare,
# Mountains/Snowy/Taiga = crisp lavender fade.
GROUPS = {
    'plains':   ('#BBD9F0', '#F0B878', '#101828', ['plains', 'sunflower_plains', 'meadow']),
    'forest':   ('#A8D4C8', '#D8A878', '#0E1A18', ['forest', 'birch_forest', 'old_growth_birch_forest', 'dark_forest', 'flower_forest']),
    'cherry':   ('#F2C8D8', '#F0A8A0', '#1C1020', ['cherry_grove']),
    'jungle':   ('#9CD8B0', '#C8A060', '#0C1C14', ['jungle', 'sparse_jungle', 'bamboo_jungle']),
    'desert':   ('#F0DC9C', '#F09030', '#180E18', ['desert']),
    'badlands': ('#F0C898', '#E86830', '#1A0E10', ['badlands', 'eroded_badlands', 'wooded_badlands']),
    'savanna':  ('#E8D8A0', '#F0A040', '#141020', ['savanna', 'savanna_plateau', 'windswept_savanna']),
    'swamp':    ('#7FA878', '#A89060', '#081410', ['swamp', 'mangrove_swamp']),
    'snowy':    ('#DCE8F8', '#E8C8D8', '#0E1830', ['snowy_plains', 'ice_spikes', 'snowy_taiga', 'snowy_beach', 'frozen_river',
                                                    'frozen_ocean', 'deep_frozen_ocean', 'grove', 'snowy_slopes', 'frozen_peaks']),
    'taiga':    ('#B0D8E8', '#C0A8C8', '#0E1824', ['taiga', 'old_growth_pine_taiga', 'old_growth_spruce_taiga']),
    'mountains':('#A8C0E8', '#C8A8D8', '#141028', ['jagged_peaks', 'stony_peaks', 'windswept_hills', 'windswept_gravelly_hills',
                                                    'windswept_forest', 'stony_shore']),
    'ocean':    ('#90C8E8', '#E8A868', '#081828', ['ocean', 'deep_ocean', 'cold_ocean', 'deep_cold_ocean', 'lukewarm_ocean',
                                                    'deep_lukewarm_ocean', 'warm_ocean', 'river', 'beach']),
    'mushroom': ('#C8B8F0', '#D8A0C8', '#181028', ['mushroom_fields']),
    'caves':    ('#C0B8A8', '#A89078', '#0A0A12', ['dripstone_caves', 'lush_caves']),
}

def sky_json(texture, pri, fade, fog_hex=None, biomes=None, decor=None, dims=True):
    o = {
        'schemaVersion': 2,
        'type': 'single-sprite-square-textured',
        'texture': texture,
        'blend': {'type': 'alpha', 'horizonBlend': True},
        'properties': {
            'priority': pri,
            'fade': {'alwaysOn': False, 'startFadeIn': fade[0], 'endFadeIn': fade[1],
                     'startFadeOut': fade[2], 'endFadeOut': fade[3]},
            'transitionInDuration': 40, 'transitionOutDuration': 40,
        },
    }
    if fog_hex:
        r, g, b = hex2rgb(fog_hex)
        o['properties']['changeFog'] = True
        o['properties']['fogColors'] = {'red': r/255, 'green': g/255, 'blue': b/255, 'alpha': 0.95}
    if biomes:
        o['conditions'] = {'biomes': [f'minecraft:{b}' for b in biomes]}
    if dims:
        o.setdefault('conditions', {})['dimensions'] = ['minecraft:overworld']
    if decor:
        o['decorations'] = decor
    return o

count = 0
# default skies (fallback for modded/unknown biomes) — no fog override
for t, cfg in TIME.items():
    write_sky(f'{OUT}/assets/forgeskyboxes/textures/sky/{t}.png', base[t])
    o = sky_json(f'forgeskyboxes:textures/sky/{t}.png', 0,
                 cfg['in'] + cfg['out'], decor=cfg['decor'])
    p = f'{OUT}/assets/forgeskyboxes/sky/default_{t}.json'
    json.dump(o, open(p, 'w'), indent=2); count += 1

# biome-specific skies with unique fog + horizon-tinted textures
for g, (fog_day, fog_set, fog_night, biomes) in GROUPS.items():
    fogs = {'day': fog_day, 'sunset': fog_set, 'night': fog_night}
    for t, cfg in TIME.items():
        tex = f'{OUT}/assets/forgeskyboxes/textures/sky/{g}_{t}.png'
        write_sky(tex, blend(base[t], fogs[t]))
        o = sky_json(f'forgeskyboxes:textures/sky/{g}_{t}.png', cfg['pri'],
                     cfg['in'] + cfg['out'], fog_hex=fogs[t], biomes=biomes, decor=cfg['decor'])
        p = f'{OUT}/assets/forgeskyboxes/sky/{g}_{t}.json'
        json.dump(o, open(p, 'w'), indent=2); count += 1
print('skybox JSONs written:', count)

# ------------------------------------------------------------------ shaders
open(f'{OUT}/assets/minecraft/shaders/core/rendertype_solid.vsh', 'w').write('''#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform int FogShape;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec4 normal;

void main() {
    vec3 pos = Position + ChunkOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    vertexDistance = fog_distance(ModelViewMat, pos, FogShape);

    vec3 lightmap = minecraft_sample_lightmap(Sampler2, UV2).rgb;
    float lum = dot(lightmap, vec3(0.2126, 0.7152, 0.0722));

    // Story Mode style: warm sun, cool shade
    vec3 tint = mix(vec3(0.90, 0.94, 1.06), vec3(1.10, 1.03, 0.92), clamp(lum * 1.25, 0.0, 1.0));

    // soft directional shading from above (kept from the original pack)
    float groundShadow = max(0.5, dot(normalize(Normal), normalize(vec3(0.3, 1.0, 0.2))));

    // baked AO-like contact shading at the base of blocks
    float yFrac = fract(pos.y);
    float contact = 1.0;
    if (Normal.y < -0.5) {
        contact = 0.90;                                        // undersides
    } else if (abs(Normal.y) < 0.5) {
        contact = mix(0.93, 1.0, smoothstep(0.0, 0.22, yFrac)); // wall bases
    }

    vertexColor = vec4(Color.rgb * lightmap * tint * (groundShadow * contact), Color.a);
    texCoord0 = UV0;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
}
''')

open(f'{OUT}/assets/minecraft/shaders/core/rendertype_solid.fsh', 'w').write('''#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
''')

# clouds use position_tex_color_normal in 1.20.1 — override only those files
open(f'{OUT}/assets/minecraft/shaders/core/position_tex_color_normal.vsh', 'w').write('''#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

out vec2 texCoord0;
out float vertexDistance;
out vec4 vertexColor;
out vec4 normal;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    vertexDistance = fog_distance(ModelViewMat, Position, FogShape);
    vertexColor = Color;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
}
''')

open(f'{OUT}/assets/minecraft/shaders/core/position_tex_color_normal.fsh', 'w').write('''#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec2 texCoord0;
in float vertexDistance;
in vec4 vertexColor;
in vec4 normal;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }

    // MCSM Story Mode clouds: round each cloud cell into a soft puff and
    // dissolve its lower part into the atmosphere.
    vec2 c = texCoord0 * 2.0 - 1.0;
    float radial = max(abs(c.x), abs(c.y));
    float puff = smoothstep(1.0, 0.45, radial);
    float bottomFade = mix(0.75, 1.0, smoothstep(0.0, 0.85, texCoord0.y));
    float alpha = color.a * puff * bottomFade;

    // melt into the distance a little harder than terrain does
    float distFade = smoothstep(FogEnd * 0.75, FogEnd, vertexDistance);
    alpha *= 1.0 - 0.4 * distFade;

    fragColor = linear_fog(vec4(color.rgb, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
''')
print('shaders written')

# --------------------------------------------------------- colorful lighting
emitters = {
    'minecraft:torch': '#FFC866', 'minecraft:wall_torch': '#FFC866',
    'minecraft:redstone_torch': '#FF3333', 'minecraft:redstone_wall_torch': '#FF3333',
    'minecraft:soul_torch': '#63F8FF', 'minecraft:soul_wall_torch': '#63F8FF',
    'minecraft:lantern': '#FFD27F', 'minecraft:soul_lantern': '#63F8FF',
    'minecraft:glowstone': '#FFD166', 'minecraft:redstone_lamp': '#FFEECC',
    'minecraft:sea_lantern': '#BFEFFF', 'minecraft:shroomlight': '#FFB366',
    'minecraft:jack_o_lantern': '#FFA940', 'minecraft:end_rod': '#E8F6FF',
    'minecraft:magma_block': '#FF6633', 'minecraft:campfire': '#FFAC4D',
    'minecraft:soul_campfire': '#63F8FF', 'minecraft:lava': '#FF7A1A',
    'minecraft:fire': '#FFA050', 'minecraft:brewing_stand': '#FFE680',
}
json.dump(emitters, open(f'{OUT}/assets/mcsm_atmosphere/light/emitters.json', 'w'), indent=2)
print('emitters written')

# ------------------------------------------------------- OptiFine emissives
open(f'{OUT}/assets/minecraft/optifine/emissive.properties', 'w').write('''# MCSM Story Mode Visuals - emissive mapping (OptiFine on Forge, Continuity on Fabric).
# If you don't use OptiFine/Continuity, delete the "optifine" folder - the pack works without it.
suffix=_e
''')

def emissive(name, draw_fn):
    im = Image.new('RGB', (16, 16), (0, 0, 0))
    d = ImageDraw.Draw(im)
    draw_fn(d)
    im.save(f'{OUT}/assets/minecraft/textures/block/{name}_e.png', optimize=True)

def flame(d, cx, cy, outer, mid, core, stick=(64, 48, 32)):
    d.rectangle([7, 6, 8, 15], fill=stick)                       # torch stick
    d.ellipse([cx-3, cy-3, cx+3, cy+3], fill=outer)
    d.ellipse([cx-2, cy-3, cx+2, cy+2], fill=mid)
    d.ellipse([cx-1, cy-3, cx+1, cy+1], fill=core)

emissive('torch',           lambda d: flame(d, 8, 3, (240, 130, 20), (252, 200, 32), (255, 255, 255)))
emissive('soul_torch',      lambda d: flame(d, 8, 3, (40, 180, 190), (110, 235, 245), (235, 255, 255)))
emissive('redstone_torch',  lambda d: flame(d, 8, 3, (200, 20, 20), (255, 60, 40), (255, 220, 220)))
def lantern_core(d, core, frame=(45, 45, 55)):
    d.rectangle([1, 0, 14, 15], fill=frame)
    d.rectangle([3, 2, 12, 13], fill=core)
emissive('lantern',         lambda d: lantern_core(d, (255, 220, 150)))
emissive('soul_lantern',    lambda d: lantern_core(d, (150, 245, 255)))
for name, col in [('glowstone', (255, 222, 150)), ('redstone_lamp_on', (255, 235, 200)),
                  ('sea_lantern', (200, 240, 255)), ('shroomlight', (255, 180, 120)),
                  ('end_rod', (235, 245, 255)), ('magma_block', (255, 110, 40))]:
    emissive(name, lambda d, col=col: d.rectangle([0, 0, 15, 15], fill=col))
print('emissive textures written')

# ------------------------------------------------------------------- mcmeta
open(f'{OUT}/pack.mcmeta', 'w').write(json.dumps({
    'pack': {'pack_format': 15,
             'description': 'All-in-One MCSM Ultimate Atmosphere Pack for 1.20.1'}
}, indent=2) + '\n')

open(f'{OUT}/PACK_FIX_NOTES.txt', 'w').write('''MCSM ULTIMATE ATMOSPHERE - v2 "Story Mode Visuals" - Minecraft 1.20.1
=============================================================================

WHAT THIS PACK DOES
- Custom day / sunset / night skies that melt into the horizon fog (ForgeSkyboxes /
  Nuit pathway: all sky configs live in assets/forgeskyboxes/).
- UNIQUE FOG + SKY TINT PER AREA, Minecraft Story Mode style: plains, forest,
  cherry grove, jungle, desert, badlands, savanna, swamp, snowy, taiga,
  mountains, ocean, mushroom fields and caves each get their own horizon fog
  color that changes between day, sunset and night.
- Story Mode clouds: a 1.20.1-compatible core shader (position_tex_color_normal)
  rounds each cloud cell into soft puffs and dissolves their lower parts into
  the atmosphere. Solid blocks are untouched (rendertype_solid).
- Story Mode lighting: warm sun / cool shade tinting plus baked AO-like contact
  shading at the base of blocks (rendertype_solid vertex shader).
- Colored point lights via the Colorful Lighting mod (torches, lanterns,
  glowstone, soul fire, lava and more) - assets/mcsm_atmosphere/light/emitters.json.
- Emissive block textures via OptiFine format (assets/minecraft/optifine):
  torches, lanterns, glowstone, sea lantern, shroomlight, redstone lamp,
  end rod, magma block.

REQUIRED MODS (Forge 1.20.1 instance)
- ForgeSkyboxes / Nuit (any loader) - the sky + fog system. Nuit scans
  assets/*/sky/, so the forgeskyboxes namespace works everywhere.
- Colorful Lighting (Forge port / Sodium Compat edition) - the colored lights.
- OptiFine OR Continuity - for the emissive textures only. Without it the
  emissives are skipped and everything else still works.

TRUE DYNAMIC SHADOWS are not possible in a resource pack - Minecraft draws
lighting on the CPU. This pack approximates Story Mode's look with warm/cool
lightmap tinting, directional shading and contact AO. For real shadows add a
shader pack (Oculus / Iris + BSL or Complementary).

HOW TO INSTALL
1. Copy the zip into .minecraft/resourcepacks (or the instance's folder).
2. Enable the pack in Options > Resource Packs, then press F3+T to reload.
3. Fly around: every biome family now has its own fog color in the sky.

Time of day: day 6:00-18:00 | sunset 17:15-19:00 | night 18:45-5:00
''')
print('notes written')

# ---------------------------------------------------------- ship the zips
import zipfile as _z
for zname, desc in [('MCSM_Ultimate_Atmosphere_FIXED.zip',
                     'All-in-One MCSM Ultimate Atmosphere Pack for 1.20.1'),
                    ('Story_Mode_Visuals.zip',
                     'Story Mode Visuals - MCSM (Telltale) atmosphere: biome fog, soft clouds, emissive & colored lighting')]:
    zp = f'{REPO}/{zname}'
    if os.path.exists(zp):
        os.remove(zp)
    with _z.ZipFile(zp, 'w', _z.ZIP_DEFLATED) as z:
        for root, dirs, files in os.walk(OUT):
            for f in sorted(files):
                p = os.path.join(root, f)
                if f == 'pack.mcmeta':
                    z.writestr(os.path.relpath(p, OUT),
                               json.dumps({'pack': {'pack_format': 15, 'description': desc}}, indent=2) + '\n')
                else:
                    z.write(p, os.path.relpath(p, OUT))
    print('wrote', zp)

