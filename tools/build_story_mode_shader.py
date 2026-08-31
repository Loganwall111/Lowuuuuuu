#!/usr/bin/env python3
"""
Builds the Story Mode Visuals SHADER PACK (Iris/Oculus, 1.20.1).

Pipeline:
  1. Rebuild the resource-pack module (tools/build_story_mode_pack.py)
  2. Merge its assets/ + pack.png into the shader pack root
  3. Validate every GLSL file (braces, #ifdef balance, includes exist)
  4. Zip the result to the repo root as:
       StoryMode_Visuals.zip            (lowercase workspace archive)
       Story_Mode_Visuals_Shader.zip    (named shader pack)

The resource-pack zips are refreshed by step 1 automatically.
"""
import json, os, shutil, subprocess, sys, zipfile

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PACK = f'{REPO}/mcsm-ultimate-atmosphere-fixed'
SHADER = f'{REPO}/story-mode-visuals-shader'
TOOLS = f'{REPO}/tools'

def run(cmd):
    r = subprocess.run(cmd, shell=True, cwd=REPO)
    if r.returncode != 0:
        sys.exit(f'FAILED: {cmd}')

def glsl_check(path):
    s = open(path, encoding='utf-8', errors='replace').read()
    errs = []
    # strip comments & strings crudely for brace counting
    depth = 0
    for ch in s:
        if ch == '{': depth += 1
        elif ch == '}': depth -= 1
        if depth < 0: errs.append('unbalanced }'); break
    if depth != 0: errs.append(f'unbalanced braces ({depth})')
    if s.count('#ifdef') != s.count('#endif') and '#include' not in s[:400]:
        # only check shaders that actually use ifdefs
        if '#ifdef' in s and s.count('#ifdef') != s.count('#endif'):
            errs.append('ifdef/endif mismatch')
    for inc in ('/lib.glsl', '/worldpos.glsl'):
        if inc in s:
            target = f'{SHADER}/shaders{inc}'
            if not os.path.exists(target):
                errs.append(f'missing include {inc}')
    if errs:
        raise SystemExit(f'{os.path.basename(path)}: {"; ".join(errs)}')

def main():
    # 1) resource-pack module first (also refreshes the two resource-pack zips)
    run(f'{sys.executable} {TOOLS}/build_story_mode_pack.py')

    # 2) merge payload into shader pack
    run(f'cp -r {PACK}/assets {SHADER}/')
    run(f'cp {PACK}/pack.png {SHADER}/pack.png')

    # 3) validate GLSL
    shaders = f'{SHADER}/shaders'
    for f in sorted(os.listdir(shaders)):
        if f.endswith(('.fsh', '.vsh', '.glsl')):
            glsl_check(f'{shaders}/{f}')
    # properties sanity
    props = open(f'{shaders}/shader.properties').read()
    assert 'screen=composite' in props and 'shaders=' in props, 'properties broken'
    print('GLSL + properties validation OK')

    # 4) zip
    for name in ('StoryMode_Visuals.zip', 'Story_Mode_Visuals_Shader.zip'):
        out = f'{REPO}/{name}'
        if os.path.exists(out): os.remove(out)
        with zipfile.ZipFile(out, 'w', zipfile.ZIP_DEFLATED) as z:
            for root, dirs, files in os.walk(SHADER):
                dirs[:] = [d for d in dirs if d != 'archive']   # skip scratch
                for f in sorted(files):
                    p = os.path.join(root, f)
                    z.write(p, os.path.relpath(p, SHADER))
        print(f'wrote {out}')

    # verify
    for name in ('StoryMode_Visuals.zip', 'Story_Mode_Visuals_Shader.zip'):
        with zipfile.ZipFile(f'{REPO}/{name}') as z:
            names = z.namelist()
            assert 'shaders/shaders.properties' not in names
            assert any(n == 'shaders/shaders.properties' or n == 'shaders/shader.properties' for n in names)
            assert any(n.endswith('gbuffers_terrain.fsh') for n in names)
            assert any(n == 'shaders/final.fsh' for n in names)
            assert any(n.endswith('pack.mcmeta') for n in names)
            print(f'{name}: {len(names)} files, OK')

if __name__ == '__main__':
    main()
