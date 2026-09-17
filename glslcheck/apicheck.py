#!/usr/bin/env python3
"""MCSM 1.9.100 -- static API check for the java we cannot compile.

There is no JDK in this sandbox, so every reference to the upstream mod
(net.dabicco.*) is unverified until the GitHub runner compiles it -- one typo
and the whole build is red. This reads the imported mod classes out of the
shipped jar and checks that every `Class.member` or `Class.method(` we use
actually exists in that class's field/method table, and reports the type so a
boolean/double mix-up is caught here instead of on the runner.

Usage:
    python3 glslcheck/apicheck.py <jar> <java-dir>
"""
import os, re, sys, struct, zipfile

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import whocalls as W


def members_with_flags(data, p):
    cnt = struct.unpack_from('>H', data, p)[0]; p += 2
    out = []
    for _ in range(cnt):
        a, ni, di = struct.unpack_from('>HHH', data, p); p += 6
        ac = struct.unpack_from('>H', data, p)[0]; p += 2
        for _ in range(ac):
            ln = struct.unpack_from('>I', data, p + 2)[0]; p += 6 + ln
        out.append((a, W.utf(W.cp_of(data), ni), W.utf(W.cp_of(data), di)))
    return out, p


def class_members(data):
    cp, p = W.cp_parse(data)
    W._CACHE[id(data)] = cp
    acc, this, sup = struct.unpack_from('>HHH', data, p); p += 6
    ic = struct.unpack_from('>H', data, p)[0]; p += 2 + 2 * ic
    fields, p = members_with_flags(data, p)
    methods, p = members_with_flags(data, p)
    return {'fields': {n: (a, t) for a, n, t in fields},
            'methods': {n: [t for _, m, t in methods if m == n] for n in {m for _, m, _ in methods}}}


def main():
    jar, srcdir = sys.argv[1], sys.argv[2]
    z = zipfile.ZipFile(jar)
    cache = {}

    def load(cn):
        if cn in cache: return cache[cn]
        try:
            data = z.read(cn.replace('.', '/') + '.class')
            cache[cn] = class_members(data)
        except KeyError:
            cache[cn] = None
        return cache[cn]

    bad = good = 0
    for root, _, files in os.walk(srcdir):
        for fn in files:
            if not fn.endswith('.java'): continue
            path = os.path.join(root, fn)
            src = open(path).read()
            imports = set(re.findall(r'import\s+(net\.dabicco\.[\w.$]+)\s*;', src))
            if not imports: continue
            simple = {c.split('.')[-1]: c for c in imports}
            for cls, fq in sorted(simple.items()):
                info = load(fq)
                if info is None:
                    print('  MISSING CLASS  %-30s %s' % (cls, fq)); bad += 1; continue
                for m in re.finditer(r'\b%s\.(\w+)(\s*\()?' % re.escape(cls), src):
                    name = m.group(1)
                    if name == 'class':
                        continue          # `Foo.class` is a class literal
                    if m.group(2) and name in info['methods']:
                        good += 1; continue
                    if name in info['fields']:
                        a, t = info['fields'][name]
                        if not a & 0x1:
                            print('  NOT PUBLIC     %-30s %s.%s' % (fn, cls, name)); bad += 1
                        elif a & 0x10:
                            print('  FINAL (no set) %-30s %s.%s' % (fn, cls, name)); bad += 1
                        else:
                            good += 1
                    elif name in info['methods']:
                        good += 1
                    else:
                        print('  UNKNOWN MEMBER %-30s %s.%s' % (fn, cls, name)); bad += 1
    print('\n%d ok, %d problem(s)' % (good, bad))
    return 1 if bad else 0


if __name__ == '__main__':
    sys.exit(main())
