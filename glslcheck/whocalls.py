#!/usr/bin/env python3
"""MCSM 1.9.100 -- who calls this method?

Twice now a "missing feature" turned out to be code that exists in the jar but
is never invoked from anywhere (StormSkyGradient.update() was the first). This
scans every class in a jar and reports the real invoke sites of a method by
name, so we can tell "not written" apart from "written but dead" before writing
a single line of Java.

Usage:
    python3 glslcheck/whocalls.py <jar-or-dir> <method-name> [more names...]
"""
import sys, os, zipfile, struct

INVOKE = {0xb6: 'virtual', 0xb7: 'special', 0xb8: 'static', 0xb9: 'interface', 0xba: 'dynamic'}


def cp_parse(data):
    p = 8
    n = struct.unpack_from('>H', data, p)[0]; p += 2
    cp = [None] * n
    i = 1
    while i < n:
        tag = data[p]; p += 1
        if tag == 1:
            ln = struct.unpack_from('>H', data, p)[0]; p += 2
            cp[i] = ('u', data[p:p + ln].decode('utf-8', 'replace')); p += ln
        elif tag in (3, 4):
            cp[i] = ('n', None); p += 4
        elif tag in (5, 6):
            cp[i] = ('n', None); p += 8
        elif tag == 15:
            cp[i] = ('n', None); p += 3
        elif tag in (7, 8, 16, 19, 20):
            cp[i] = ('c', struct.unpack_from('>H', data, p)[0]); p += 2
        elif tag in (9, 10, 11, 18):        # Fieldref/Methodref/InterfaceMethodref/InvokeDynamic
            a, b = struct.unpack_from('>HH', data, p); p += 4
            cp[i] = ('r', a, b)
        elif tag == 12:                      # NameAndType
            a, b = struct.unpack_from('>HH', data, p); p += 4
            cp[i] = ('nt', a, b)
        else:
            cp[i] = ('n', None); p += 4
        i += 2 if tag in (5, 6) else 1
    return cp, p


def utf(cp, i):
    return cp[i][1] if 0 <= i < len(cp) and cp[i] and cp[i][0] == 'u' else '?'


def mref_name(cp, i):
    e = cp[i] if 0 <= i < len(cp) else None
    if not e or e[0] != 'r': return None
    nt = cp[e[2]] if e[2] < len(cp) else None
    if not nt or nt[0] != 'nt': return None
    return utf(cp, nt[1])


def members(data, p):
    cnt = struct.unpack_from('>H', data, p)[0]; p += 2
    out = []
    for _ in range(cnt):
        a, ni, di = struct.unpack_from('>HHH', data, p); p += 6
        acnt = struct.unpack_from('>H', data, p)[0]; p += 2
        code = None
        for _ in range(acnt):
            ani = struct.unpack_from('>H', data, p)[0]
            ln = struct.unpack_from('>I', data, p + 2)[0]
            body = data[p + 6:p + 6 + ln]
            if utf(data and cp_of(data), ani) == 'Code' and ln > 8:
                clen = struct.unpack_from('>I', body, 4)[0]
                code = body[8:8 + clen]
            p += 6 + ln
        out.append((utf(cp_of(data), ni), utf(cp_of(data), di), code))
    return out, p


_CACHE = {}


def cp_of(data):
    if id(data) not in _CACHE:
        _CACHE[id(data)] = cp_parse(data)[0]
    return _CACHE[id(data)]


def scan(data, targets):
    cp, p = cp_parse(data)
    _CACHE[id(data)] = cp
    acc, this, sup = struct.unpack_from('>HHH', data, p); p += 6
    ic = struct.unpack_from('>H', data, p)[0]; p += 2
    p += 2 * ic
    fields, p = members(data, p)
    methods, p = members(data, p)
    selfname = utf(cp, cp[this][1]) if cp[this] and cp[this][0] == 'c' else '?'
    hits = {}
    for mname, mdesc, code in methods:
        if not code: continue
        for i in range(len(code) - 2):
            op = code[i]
            if op in INVOKE:
                idx = (code[i + 1] << 8) | code[i + 2]
                nm = mref_name(cp, idx)
                if nm in targets:
                    hits.setdefault(nm, []).append(mname)
    return selfname.replace('/', '.'), hits


def main():
    src = sys.argv[1]
    targets = set(sys.argv[2:])
    if os.path.isdir(src):
        files = [(os.path.join(r, f), open(os.path.join(r, f), 'rb').read())
                 for r, _, fs in os.walk(src) for f in fs if f.endswith('.class')]
    else:
        z = zipfile.ZipFile(src)
        files = [(n, z.read(n)) for n in z.namelist() if n.endswith('.class')]
    total = {t: [] for t in targets}
    global FAILS; FAILS = []
    for name, data in files:
        try:
            cls, hits = scan(data, targets)
        except Exception as e:
            FAILS.append((name, repr(e)))
            continue
        for m, callers in hits.items():
            total[m].append((cls, callers))
    if FAILS:
        print('!! %d/%d classes failed to parse (first 3):' % (len(FAILS), len(files)))
        for n, e in FAILS[:3]: print('   %s -> %s' % (n, e))
    for t in sorted(targets):
        print('\n=== %s ===' % t)
        if not total[t]:
            print('  NO invoke site anywhere -- this code is dead (never called)')
        for cls, callers in total[t]:
            uniq = sorted(set(callers))
            print('  %-70s <- %d call site(s) in %s' % (cls, len(callers), ', '.join(uniq[:6])))


if __name__ == '__main__':
    main()
