#!/usr/bin/env python3
"""
Builds the Story Mode Visuals SHADER PACK (Iris/Oculus, 1.20.1).

Pipeline:
  1. Rebuild the resource-pack module (tools/build_story_mode_pack.py)
  2. Merge its assets/ + pack.png into the shader pack root
  3. INLINE all #include directives into the shipped shaders (the zip ships
     zero-include, fully self-contained GLSL - no loader include quirks)
  4. Deep-validate every shipped shader:
       - first line is #version 120
       - braces balanced, #ifdef/#ifndef balanced with #endif
       - no GLSL 150+ "texture()" calls (texture2D only)
       - every risky uniform name USED is DECLARED in the same file
       - every library helper USED is DEFINED in the inlined file
       - DRAWBUFFERS comment covers every gl_FragData index written
  5. Zip the result to the repo root as:
       StoryMode_Visuals.zip            (lowercase workspace archive)
       Story_Mode_Visuals_Shader.zip    (named shader pack)
"""
import json, os, re, shutil, subprocess, sys, zipfile

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PACK = f'{REPO}/mcsm-ultimate-atmosphere-fixed'
SHADER = f'{REPO}/story-mode-visuals-shader'
TOOLS = f'{REPO}/tools'

RISK_UNIFORMS = [
    'depthtex0', 'depthtex1', 'depthtex2', 'colortex0', 'colortex1', 'colortex2',
    'colortex3', 'shadowtex0', 'shadowtex1', 'shadowcolor0', 'shadowcolor1',
    'gbufferModelView', 'gbufferModelViewInverse', 'gbufferProjection',
    'gbufferProjectionInverse', 'shadowProjection', 'shadowModelView',
    'cameraPosition', 'sunPosition', 'moonPosition', 'upPosition', 'viewWidth',
    'viewHeight', 'near', 'far', 'frameTimeCounter', 'sunAngle', 'rainStrength',
    'wetness', 'nightVision', 'blindness', 'darknessFactor', 'worldTime',
    'CLOUD_SPEED', 'SUNSET', 'MOONSHINE', 'SKY_FOG_MIX', 'MOON_SIZE',
    'CLOUD_COVER', 'CLOUD_DENSITY', 'CLOUD_COLORIZE', 'RAIN_STR',
    'TERRAIN_AO_STR', 'TORCH_SAT', 'ENT_AO', 'HAND_LIGHT', 'FOG_STR',
    'VIGNETTE_STR', 'SATURATION', 'STYLE', 'SKY_PRESET',
]

HELPERS = ['hash12', 'hash13', 'hash33', 'hash22', 'vnoise', 'fbm3', 'rgb2hsv',
           'hsv2rgb', 'sstep', 'toLinear', 'toGamma', 'sdrTonemap', 'grade',
           'starLayer', 'moonGlow', 'fogProfile', 'biomeWeights', 'biomeMatch',
           'sampledFog', 'getShadow', 'getCloudShadow', 'getContactAO',
           'getWorldPos']

def run(cmd):
    r = subprocess.run(cmd, shell=True, cwd=REPO)
    if r.returncode != 0:
        sys.exit(f'FAILED: {cmd}')

def inline_includes(text, base, stack=()):
    out_lines = []
    for line in text.splitlines():
        m = re.match(r'^\s*#include\s+"([^"]+)"', line)
        if not m:
            out_lines.append(line)
            continue
        inc = m.group(1).lstrip('/')
        path = os.path.join(base, inc)
        if inc in stack or not os.path.exists(path):
            raise SystemExit(f'bad include {inc} in {stack}')
        with open(path, encoding='utf-8') as f:
            out_lines.append(f'// === inlined {inc} ===')
            out_lines.append(inline_includes(f.read(), base, stack + (inc,)))
    return '\n'.join(out_lines)

def strip_comments(text):
    # remove // comments and /* */ blocks for structural checks
    text = re.sub(r'/\*.*?\*/', '', text, flags=re.S)
    return re.sub(r'//[^\n]*', '', text)

def validate(name, raw):
    errs = []
    # 1) version first (only real programs; .glsl files are includes)
    if name.endswith(('.fsh', '.vsh')):
        first = next((l for l in raw.splitlines() if l.strip() and not l.strip().startswith('//')), '')
        if not first.startswith('#version 120'):
            errs.append(f'first line is not #version 120 ({first[:40]})')
    # 2) braces
    depth = 0
    for ch in strip_comments(raw):
        if ch == '{': depth += 1
        elif ch == '}': depth -= 1
        if depth < 0: errs.append('unbalanced }'); break
    if depth != 0: errs.append(f'unbalanced braces ({depth})')
    # 3) preprocessor balance
    opens = len(re.findall(r'#\s*ifdef', raw)) + len(re.findall(r'#\s*ifndef', raw))
    if opens != len(re.findall(r'#\s*endif', raw)):
        errs.append('ifdef/ifndef vs endif mismatch')
    code = strip_comments(raw)
    # 4) GLSL 120 style only
    if re.search(r'\btexture\s*\(', code):
        errs.append('uses texture() - must be texture2D() in GLSL 120')
    if name.endswith('.glsl'):
        # includes are resolved in program scope; structural checks only
        if errs:
            raise SystemExit(f'{name}: ' + '; '.join(errs))
        return
    # 5) risky uniforms must be declared when used
    decls = set(re.findall(r'uniform\s+[A-Za-z0-9_]+\s+([A-Za-z_][A-Za-z0-9_]*)\s*;', code))
    for u in RISK_UNIFORMS:
        if re.search(rf'\b{u}\b', code) and u not in decls:
            errs.append(f'uniform {u} used but not declared')
    # 6) helpers must be defined when used
    for h in HELPERS:
        used = re.search(rf'\b{h}\s*\(', code)
        defined = re.search(rf'\b(?:float|vec2|vec3|vec4|void|mat2|mat3|mat4)\s+{h}\s*\(', code)
        if used and not defined:
            errs.append(f'helper {h}() used but not defined')
    # 7) DRAWBUFFERS covers written indices
    written = {int(g) for g in re.findall(r'gl_FragData\[(\d)\]', code)}
    if written:
        db = re.search(r'DRAWBUFFERS\s*:\s*([0-9]+)', raw)
        if not db or not written <= {int(c) for c in db.group(1)}:
            errs.append(f'gl_FragData{written} written but DRAWBUFFERS missing/insufficient')
    if errs:
        raise SystemExit(f'{name}: ' + '; '.join(errs))

def main():
    # 1) resource-pack module first (also refreshes the two resource-pack zips)
    run(f'{sys.executable} {TOOLS}/build_story_mode_pack.py')

    # 2) merge payload into shader pack
    run(f'cp -r {PACK}/assets {SHADER}/')
    run(f'cp {PACK}/pack.png {SHADER}/pack.png')

    # v9: LEGACY RESOURCE-PACK TRAIL REMOVED FROM THE SHADER ZIP.
    # The resource-pack module ships vanilla core-shader overrides
    # (rendertype_solid, soft clouds) for shader-off mode, but inside a
    # shader pack those files live at assets/minecraft/shaders/core/ and
    # become a resource-pack folder trail that fights the mod loader over
    # environment hooks. The whole subtree is stripped from the shader
    # pack; the standalone resource packs keep it. Clouds render ONLY
    # through shaders/gbuffers_clouds.* (fully self-contained, zero
    # texture samplers), and the verbatim user cloud GLSL is preserved at
    # pack-root clouds_reference/ - outside assets/, so the resource
    # manager never sees it.
    run(f'rm -rf {SHADER}/assets/minecraft/shaders')
    assert os.path.exists(f'{SHADER}/clouds_reference/rendertype_clouds.vsh'), \
        'verbatim cloud reference missing'
    assert os.path.exists(f'{SHADER}/shaders/gbuffers_clouds.vsh'), 'gbuffers_clouds.vsh missing'
    assert os.path.exists(f'{SHADER}/shaders/gbuffers_clouds.fsh'), 'gbuffers_clouds.fsh missing'

    # 3) properties sanity + menu identity
    shaders = f'{SHADER}/shaders'
    props = open(f'{shaders}/shader.properties').read()
    assert 'screen=composite' in props and 'shaders=' in props, 'properties broken'
    assert 'buffers=shadow:' in props, 'buffers must use colon syntax'
    assert 'colortex2' in props, 'colortex2 (gnormal) missing from textures list'
    assert 'id=story_mode_menu' in props, 'menu wrapper id tag missing'
    assert 'gbuffers_skybasic' not in props and 'gbuffers_skytextured' not in props, \
        'sky programs must stay unregistered (native sky path)'
    lang = open(f'{shaders}/lang/en_us.lang', encoding='utf-8').read()
    assert 'screen.MAIN=' in lang, 'settings screen identity missing from lang'

    settings = re.findall(r'^(?:SETTINGS|DEFINE)\.([A-Z0-9_]+)', props, re.M)
    opts = set(re.findall(r'^option\.([A-Z0-9_]+)=', lang, re.M))
    missing = [k for k in settings if k != 'PRESET' and k not in opts]
    if missing:
        raise SystemExit(f'lang missing entries for: {missing}')
    n_toggles = len(re.findall(r'^DEFINE\.', props, re.M))
    n_sliders = len(re.findall(r'^SETTINGS\.[A-Z0-9_]+=', props, re.M))
    print(f'menu: {n_toggles} toggles, {n_sliders} sliders, '
          f'{len(re.findall("^SETTINGS.PRESET.", props, re.M))} preset values | id token: story_mode_menu')

    # 4) inline + validate GLSL (shipped copies only)
    shippable = {}
    for f in sorted(os.listdir(shaders)):
        if f.endswith(('.fsh', '.vsh', '.glsl')):
            raw = open(f'{shaders}/{f}', encoding='utf-8').read()
            inlined = inline_includes(raw, shaders)
            validate(f, inlined)
            shippable[f] = inlined
    print('GLSL + properties + lang validation OK')

    # 5) zip (inlined, include-free shaders)
    for name in ('StoryMode_Visuals.zip', 'Story_Mode_Visuals_Shader.zip'):
        out = f'{REPO}/{name}'
        if os.path.exists(out): os.remove(out)
        with zipfile.ZipFile(out, 'w', zipfile.ZIP_DEFLATED) as z:
            for root, dirs, files in os.walk(SHADER):
                dirs[:] = [d for d in dirs if d != 'archive']
                for f in sorted(files):
                    p = os.path.join(root, f)
                    rel = os.path.relpath(p, SHADER)
                    # Fixed epoch timestamp + attrs -> byte-identical zips
                    # every build (no per-run mtime drift between the two
                    # deliverable names).
                    zi = zipfile.ZipInfo(rel, date_time=(2020, 1, 1, 0, 0, 0))
                    zi.compress_type = zipfile.ZIP_DEFLATED
                    zi.external_attr = 0o644 << 16
                    if f in shippable:
                        z.writestr(zi, shippable[f])
                    else:
                        with open(p, 'rb') as src:
                            z.writestr(zi, src.read())
        print(f'wrote {out}')

    # 6) post-zip sanity
    for name in ('StoryMode_Visuals.zip', 'Story_Mode_Visuals_Shader.zip'):
        with zipfile.ZipFile(f'{REPO}/{name}') as z:
            names = z.namelist()
            assert all(n == n.lower() for n in names), 'non-lowercase path in zip'
            assert 'shaders/shader.properties' in names
            assert any(n.endswith('gbuffers_terrain.fsh') for n in names)
            assert any(n.endswith('final.fsh') for n in names)
            assert any(n.endswith('pack.mcmeta') for n in names)
            assert 'shaders/gbuffers_clouds.vsh' in names and 'shaders/gbuffers_clouds.fsh' in names
            assert not any('skybasic' in n or 'skytextured' in n or 'sky_basic' in n for n in names)
            assert not any('assets/minecraft/shaders/' in n for n in names), \
                'legacy core-shader trail leaked into shader zip'
            assert 'clouds_reference/rendertype_clouds.vsh' in names, \
                'verbatim cloud reference missing from zip'
            clouds_fsh = z.read('shaders/gbuffers_clouds.fsh').decode()
            assert '#include' not in clouds_fsh, 'clouds not self-contained'
            assert 'sampler2D' not in clouds_fsh, 'clouds has vanilla texture dependency'
            for n in names:
                if n.startswith('shaders/') and n.endswith(('.fsh', '.vsh', '.glsl')):
                    body = z.read(n).decode()
                    assert '#include' not in body, f'include leaked into {n}'
                    assert re.search(r'\btexture\s*\(', body) is None, f'texture() leaked into {n}'
            print(f'{name}: {len(names)} files, pipeline include-free, OK')

if __name__ == '__main__':
    main()
