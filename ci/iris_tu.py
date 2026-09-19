#!/usr/bin/env python3
"""iris_tu.py — offline validation for an Iris shader pack directory.

Usage: python3 ci/iris_tu.py <pack_shaders_dir>

Mirrors the committed glslcheck/tu scheme exactly:
  * strips the pack's `#version 330 compatibility` line and prepends a
    `#version 330 core` shim with the Iris uniform catalog — MINUS every
    uniform the source declares itself (GLSL 330 forbids redeclaration);
  * fragment shims add `out vec4 mcsm_FragData[1];` and rewrite
    gl_FragData/gl_FragColor;
  * vertex shims add the legacy fixed-function stand-ins (mcsm_gl_Vertex,
    ftransform(), ...) and rewrite gl_Vertex/gl_Color/gl_Normal/
    gl_MultiTexCoordN/gl_TextureMatrix/gl_ModelViewMatrix/...;
  * per .fsh, generates the variants asdefault / all_on / all_off /
    flip_<OPT> for every `#define OPT v // [0 1]` toggle (float defines are
    left at their defaults — flipping them is a value change, not a path
    change); .vsh files get a single nodefs variant.
Each generated TU must pass glslang. Exit 0 = whole pack clean, 1 = fail.
"""
import os
import re
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
GLSLANG = os.path.join(HERE, '..', 'glslcheck', 'bin', 'glslang')

# name -> (type, glsl decl chunk). Order preserved in emitted shims.
FRAG_UNIFORMS = [
    ('gbufferModelViewInverse', 'mat4'), ('gbufferModelView', 'mat4'),
    ('gbufferProjection', 'mat4'), ('gbufferProjectionInverse', 'mat4'),
    ('modelViewMatrix', 'mat4'), ('projectionMatrix', 'mat4'),
    ('textureMatrix', 'mat4'), ('modelViewMatrixInverse', 'mat4'),
    ('gbufferPreviousModelView', 'mat4'),
    ('skyColor', 'vec3'), ('fogColor', 'vec3'), ('sunPosition', 'vec3'),
    ('moonPosition', 'vec3'), ('shadowLightPosition', 'vec3'),
    ('cameraPosition', 'vec3'), ('upPosition', 'vec3'),
    ('previousCameraPosition', 'vec3'),
    ('sunAngle', 'float'), ('frameTimeCounter', 'float'),
    ('viewWidth', 'float'), ('viewHeight', 'float'),
    ('rainStrength', 'float'), ('aspectRatio', 'float'),
    ('far', 'float'), ('near', 'float'), ('blindness', 'float'),
    ('nightVision', 'float'), ('wetness', 'float'),
    ('worldTime', 'int'), ('worldDay', 'int'), ('isEyeInWater', 'int'),
    ('heldBlockLightValue', 'int'), ('frameCounter', 'int'), ('entityId', 'int'),
    ('entityColor', 'vec4'),
    ('gtexture', 'sampler2D'), ('lightmap', 'sampler2D'),
    ('colortex0', 'sampler2D'), ('colortex1', 'sampler2D'),
    ('colortex2', 'sampler2D'), ('colortex3', 'sampler2D'),
    ('colortex4', 'sampler2D'), ('depthtex0', 'sampler2D'),
    ('depthtex1', 'sampler2D'), ('noisetex', 'sampler2D'),
    ('gaux1', 'sampler2D'), ('gaux2', 'sampler2D'), ('tex', 'sampler2D'),
]

VERT_UNIFORMS = [u for u in FRAG_UNIFORMS]

VERT_PRELUDE = """in vec4 mcsm_gl_Vertex; in vec4 mcsm_gl_Color; in vec3 mcsm_gl_Normal;
in vec4 mcsm_gl_MultiTexCoord0; in vec4 mcsm_gl_MultiTexCoord1; in vec4 mcsm_gl_MultiTexCoord2;
uniform mat4 mcsm_TextureMatrix[8];
uniform mat4 mcsm_ModelViewMatrix, mcsm_ProjectionMatrix, mcsm_ModelViewProjectionMatrix, mcsm_NormalMatrix4;
mat3 mcsm_NormalMatrix = mat3(mcsm_NormalMatrix4);
vec4 ftransform() { return projectionMatrix * modelViewMatrix * mcsm_gl_Vertex; }
"""

# longest-first so gl_MultiTexCoord0 never gets clipped by a shorter match
VERT_REPLACES = [
    (r'\bgl_ModelViewProjectionMatrix\b', 'mcsm_ModelViewProjectionMatrix'),
    (r'\bgl_TextureMatrix\b', 'mcsm_TextureMatrix'),
    (r'\bgl_ModelViewMatrix\b', 'mcsm_ModelViewMatrix'),
    (r'\bgl_ProjectionMatrix\b', 'mcsm_ProjectionMatrix'),
    (r'\bgl_NormalMatrix\b', 'mcsm_NormalMatrix'),
    (r'\bgl_MultiTexCoord0\b', 'mcsm_gl_MultiTexCoord0'),
    (r'\bgl_MultiTexCoord1\b', 'mcsm_gl_MultiTexCoord1'),
    (r'\bgl_MultiTexCoord2\b', 'mcsm_gl_MultiTexCoord2'),
    (r'\bgl_Vertex\b', 'mcsm_gl_Vertex'),
    (r'\bgl_Color\b', 'mcsm_gl_Color'),
    (r'\bgl_Normal\b', 'mcsm_gl_Normal'),
]

UNIFORM_DECL = re.compile(r'uniform\s+(\w+)\s+([^;]+);')
TOGGLE_DEF = re.compile(r'^(\s*#define\s+(\w+)\s+)(\d+)(\s*//\s*\[0 1\].*)$', re.M)


def declared_uniforms(src):
    names = set()
    for m in UNIFORM_DECL.finditer(src):
        for part in m.group(2).split(','):
            names.add(part.strip().split('[')[0].strip())
    return names


def shim_for(src, stage):
    have = declared_uniforms(src)
    cat = FRAG_UNIFORMS if stage == 'frag' else VERT_UNIFORMS
    lines = ['#version 330 core']
    by_type = {}
    for name, typ in cat:
        if name in have:
            continue
        by_type.setdefault(typ, []).append(name)
    # emit in catalog order, grouped by type like the committed tu files
    for typ in ['mat4', 'vec3', 'float', 'int', 'vec4', 'sampler2D']:
        names = by_type.get(typ)
        if names:
            lines.append('uniform %s %s;' % (typ, ', '.join(names)))
    if stage == 'frag':
        lines.append('out vec4 mcsm_FragData[1];')
    else:
        lines.append(VERT_PRELUDE.rstrip('\n'))
    return '\n'.join(lines) + '\n'


def adapt_source(src, stage):
    # drop the version line (the shim carries `#version 330 core`)
    src = re.sub(r'^\s*#version[^\n]*\n', '', src, count=1)
    if stage == 'frag':
        src = src.replace('gl_FragData[0]', 'mcsm_FragData[0]')
        src = re.sub(r'\bgl_FragColor\b', 'mcsm_FragData[0]', src)
    else:
        for pat, rep in VERT_REPLACES:
            src = re.sub(pat, rep, src)
    return src


def set_toggles(src, values):
    def sub(m):
        name = m.group(2)
        if name in values:
            return m.group(1) + str(values[name]) + m.group(4)
        return m.group(0)
    return TOGGLE_DEF.sub(sub, src)


def toggle_names(src):
    return [m.group(2) for m in TOGGLE_DEF.finditer(src)]


def defaults_map(src):
    return {m.group(2): int(m.group(3)) for m in TOGGLE_DEF.finditer(src)}


def build_tu(path):
    stage = 'frag' if path.endswith('.fsh') else 'vert'
    src = open(path).read()
    body = adapt_source(src, stage)
    header = shim_for(src, stage)
    return header + '\n' + body


def variants(path):
    src = open(path).read()
    toggles = toggle_names(src)
    out = []
    if path.endswith('.vsh') or not toggles:
        out.append(('nodefs', build_tu(path)))
        return out
    dflt = defaults_map(src)
    out.append(('asdefault', build_tu_with(src, path, dflt)))
    out.append(('all_on', build_tu_with(src, path, {k: 1 for k in toggles})))
    out.append(('all_off', build_tu_with(src, path, {k: 0 for k in toggles})))
    for name in toggles:
        vals = dict(dflt)
        vals[name] = 1 - vals[name]
        out.append(('flip_%s' % name, build_tu_with(src, path, vals)))
    return out


def build_tu_with(src, path, values):
    stage = 'frag' if path.endswith('.fsh') else 'vert'
    body = adapt_source(set_toggles(src, values), stage)
    header = shim_for(src, stage)
    return header + '\n' + body


def main():
    if len(sys.argv) != 2:
        print(__doc__)
        return 2
    pack = sys.argv[1]
    files = sorted(f for f in os.listdir(pack)
                   if f.endswith('.fsh') or f.endswith('.vsh'))
    if not files:
        print('::error title=iris pack::no shaders found in %s' % pack)
        return 1
    fails = 0
    n = 0
    tmpdir = tempfile.mkdtemp(prefix='mcsm-iris-tu-')
    for f in files:
        path = os.path.join(pack, f)
        stage = 'frag' if f.endswith('.fsh') else 'vert'
        for variant, tu in variants(path):
            n += 1
            tu_path = os.path.join(tmpdir, '%s.%s.%s.glsl' % (f, variant, stage))
            with open(tu_path, 'w') as fh:
                fh.write(tu)
            r = subprocess.run([GLSLANG, '-S', stage, tu_path],
                               capture_output=True, text=True)
            if r.returncode != 0:
                fails += 1
                print('FAIL %s [%s]' % (f, variant))
                print(r.stdout)
                print(r.stderr)
            else:
                print('ok   %s [%s]' % (f, variant))
    print('[iris] %d translation units, %d failures' % (n, fails))
    return 1 if fails else 0


if __name__ == '__main__':
    sys.exit(main())
