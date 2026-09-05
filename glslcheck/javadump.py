#!/usr/bin/env python3
"""MCSM 1.9.100 -- minimal .class inspector.

There is no JDK in this sandbox (no javap, no javac), and every MCSM java
feature has to be written against the mod's own API blind. This parses the
constant pool + member tables of a .class (optionally straight out of a jar)
and prints the superclass, interfaces, fields and methods with descriptors,
which is exactly what javap -p would tell us.

Usage:
    python3 glslcheck/javadump.py <jar-or-dir> <class-name-or-path> [filter]

  filter is a case-insensitive substring matched against member names; use
  '.' to print everything.
"""
import sys, zipfile, struct, os


def parse(data):
    if data[:4] != b'\xca\xfe\xba\xbe':
        raise SystemExit('not a class file')
    p = 4 + 2 + 2               # magic, minor, major
    cpc, p = struct.unpack_from('>H', data, p)[0], p + 2
    cp = [None] * cpc
    i = 1
    while i < cpc:
        tag = data[p]; p += 1
        if tag == 1:
            n, p = struct.unpack_from('>H', data, p)[0], p + 2
            cp[i] = ('utf8', data[p:p + n].decode('utf-8', 'replace')); p += n
        elif tag in (3, 4):     cp[i] = ('int', struct.unpack_from('>i', data, p)[0]); p += 4
        elif tag in (5, 6):     cp[i] = ('long', None); p += 8
        elif tag in (7, 8, 16, 19, 20): cp[i] = ('ref', struct.unpack_from('>H', data, p)[0]); p += 2
        elif tag == 15:         cp[i] = ('mh', None); p += 3
        else:                   cp[i] = ('ref', struct.unpack_from('>H', data, p)[0]); p += 4
        if tag in (5, 6): i += 2
        else: i += 1

    def utf(idx):
        v = cp[idx] if 0 <= idx < len(cp) else None
        return v[1] if v and v[0] == 'utf8' else None

    acc, this, sup, p = struct.unpack_from('>HHH', data, p) + (p + 6,)
    sclazz = utf(cp[sup][1]) if cp[sup] and cp[sup][0] == 'ref' else '?'
    selfc = utf(cp[this][1]) if cp[this] and cp[this][0] == 'ref' else '?'
    ic, p = struct.unpack_from('>H', data, p)[0], p + 2
    ifaces = []
    for _ in range(ic):
        idx, p = struct.unpack_from('>H', data, p)[0], p + 2
        ifaces.append(utf(cp[idx][1]) if cp[idx] else '?')

    def members(p):
        cnt = struct.unpack_from('>H', data, p)[0]; p += 2
        out = []
        for _ in range(cnt):
            a, n, d = struct.unpack_from('>HHH', data, p); p += 6
            p += skip_attrs(data, p)
            out.append((a, utf(n) or '?', utf(d) or '?'))
        return out, p

    def skip_attrs(data, p):
        cnt = struct.unpack_from('>H', data, p)[0]; p += 2
        for _ in range(cnt):
            ln = struct.unpack_from('>I', data, p + 2)[0]
            p += 6 + ln
        return p - p  # placeholder, replaced below
    return None


def members(data, p):
    cnt = struct.unpack_from('>H', data, p)[0]; p += 2
    out = []
    for _ in range(cnt):
        a, n, d = struct.unpack_from('>HHH', data, p); p += 6
        acnt = struct.unpack_from('>H', data, p)[0]; p += 2
        for _ in range(acnt):
            ln = struct.unpack_from('>I', data, p + 2)[0]
            p += 6 + ln
        out.append((a, n, d))
    return out, p


def cp_parse(data):
    p = 8
    cpc = struct.unpack_from('>H', data, p)[0]; p += 2
    cp = [None] * cpc
    i = 1
    while i < cpc:
        tag = data[p]; p += 1
        if tag == 1:
            n = struct.unpack_from('>H', data, p)[0]; p += 2
            cp[i] = ('utf8', data[p:p + n].decode('utf-8', 'replace')); p += n
        elif tag in (3, 4):
            cp[i] = ('int', struct.unpack_from('>i', data, p)[0]); p += 4
        elif tag in (5, 6):
            cp[i] = ('long', None); p += 8
        elif tag == 15:
            cp[i] = ('mh', None); p += 3
        elif tag in (7, 8, 16, 19, 20):
            cp[i] = ('ref', struct.unpack_from('>H', data, p)[0]); p += 2
        else:
            cp[i] = ('ref', struct.unpack_from('>H', data, p)[0]); p += 4
        i += 2 if tag in (5, 6) else 1
    return cp, p


def dump(data):
    cp, p = cp_parse(data)
    utf = lambda i: (cp[i][1] if 0 <= i < len(cp) and cp[i] and cp[i][0] == 'utf8' else '?')
    acc, this, sup = struct.unpack_from('>HHH', data, p); p += 6
    ic = struct.unpack_from('>H', data, p)[0]; p += 2
    ifaces = []
    for _ in range(ic):
        idx = struct.unpack_from('>H', data, p)[0]; p += 2
        ifaces.append(utf(idx))
    fields, p = members(data, p)
    methods, p = members(data, p)
    return dict(this=utf(cp[this][1] if cp[this] and cp[this][0] == 'ref' else 0),
                sup=utf(cp[sup][1] if cp[sup] and cp[sup][0] == 'ref' else 0),
                ifaces=ifaces,
                fields=[(utf(n), utf(d)) for _, n, d in fields],
                methods=[(utf(n), utf(d)) for _, n, d in methods])


def main():
    src, name = sys.argv[1], sys.argv[2]
    filt = (sys.argv[3] if len(sys.argv) > 3 else '.').lower()
    if os.path.isdir(src):
        data = open(os.path.join(src, name.replace('.', '/') + '.class'), 'rb').read()
    else:
        z = zipfile.ZipFile(src)
        key = name.replace('.', '/') + '.class'
        if key not in z.namelist():
            hits = [n for n in z.namelist() if n.endswith(name + '.class')]
            if not hits:
                raise SystemExit('class not found: %s' % name)
            key = hits[0]
        data = z.read(key)
    d = dump(data)
    print('class %s extends %s %s' % (d['this'], d['sup'], ('implements ' + ', '.join(d['ifaces'])) if d['ifaces'] else ''))
    print('-- fields --')
    for n, t in d['fields']:
        if filt == '.' or filt in n.lower(): print('  %s : %s' % (n, t))
    print('-- methods --')
    for n, t in d['methods']:
        if filt == '.' or filt in n.lower(): print('  %s %s' % (n, t))


if __name__ == '__main__':
    main()
