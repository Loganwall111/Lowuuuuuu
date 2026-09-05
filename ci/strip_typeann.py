#!/usr/bin/env python3
"""Strip type-annotation attributes from class files inside a jar.

Why: the raw Minecraft 26.2 client jar carries JSpecify type annotations in
class-file positions javac refuses ("error: Cannot attach type annotations
@org.jspecify.annotations.Nullable to FriendlyByteBuf.readNullable"), which
hard-fails any compile that loads the affected class. Removing the
Runtime(In)VisibleTypeAnnotations attributes makes the same class files
compile cleanly; runtime behaviour is untouched (type annotations are
advisory metadata only).

Usage: python3 ci/strip_typeann.py in.jar out.jar
Pure stdlib. Rewrites only entries that actually change.
"""
import struct
import sys
import zipfile

TYPE_ANN_ATTRS = (b"RuntimeVisibleTypeAnnotations", b"RuntimeInvisibleTypeAnnotations")


class Reader:
    def __init__(self, data):
        self.d = data
        self.i = 0

    def u1(self):
        v = self.d[self.i]
        self.i += 1
        return v

    def u2(self):
        v = struct.unpack_from(">H", self.d, self.i)[0]
        self.i += 2
        return v

    def u4(self):
        v = struct.unpack_from(">I", self.d, self.i)[0]
        self.i += 4
        return v

    def take(self, n):
        v = self.d[self.i:self.i + n]
        self.i += n
        return v


def read_cp(r):
    """Returns (constant pool entries list indexed 1..n, end offset)."""
    if r.u4() != 0xCAFEBABE:
        raise ValueError("not a class file")
    r.u2()  # minor
    r.u2()  # major
    count = r.u2()
    cp = {}
    i = 1
    while i < count:
        tag = r.u1()
        if tag == 1:
            cp[i] = (tag, r.take(r.u2()))
        elif tag in (7, 8, 16, 19, 20):
            cp[i] = (tag, r.u2())
        elif tag == 15:
            cp[i] = (tag, r.take(3))
        elif tag in (5, 6):
            cp[i] = (tag, r.take(8))
            i += 1  # long/double occupy two slots
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            cp[i] = (tag, r.take(4))
        else:
            raise ValueError(f"bad cp tag {tag} at index {i}")
        i += 1
    return cp


def cp_utf8(cp, idx):
    e = cp.get(idx)
    if e and e[0] == 1:
        return e[1]
    return b""


def transform_attrs(r, cp, out, depth=0):
    """Copy an attribute table, dropping type-annotation attrs; recurses into Code."""
    n = r.u2()
    kept = []
    dropped = 0
    for _ in range(n):
        name_idx = r.u2()
        length = r.u4()
        payload = r.take(length)
        name = cp_utf8(cp, name_idx)
        if name in TYPE_ANN_ATTRS:
            dropped += 1
            continue
        if name == b"Code":
            payload = transform_code(payload, cp)
        kept.append((name_idx, payload))
    out.append((kept, dropped))


def transform_code(payload, cp):
    """Rebuild a Code attribute minus nested type-annotation attrs."""
    r = Reader(payload)
    head = r.take(8)  # max_stack u2, max_locals u2, code_length u4
    code_len = struct.unpack(">I", head[4:8])[0]
    code = r.take(code_len)
    et_count = r.u2()
    exc_table = r.take(8 * et_count)
    sub = []
    # Code's own attribute table sits in the remaining bytes
    rest_start = r.i
    r2 = Reader(payload[rest_start:])
    transform_attrs(r2, cp, sub)
    kept, _ = sub[0]
    out = bytearray(head)
    out += code
    out += struct.pack(">H", et_count)
    out += exc_table
    out += struct.pack(">H", len(kept))
    for name_idx, pl in kept:
        out += struct.pack(">HI", name_idx, len(pl))
        out += pl
    return bytes(out)


def strip_class(data):
    """Returns transformed class bytes, or None if nothing changed."""
    r = Reader(data)
    cp = read_cp(r)
    total_dropped = 0
    # access_flags, this, super
    header_after_cp = r.take(6)
    ifc_count = r.u2()
    ifaces = r.take(2 * ifc_count)
    fields_count = r.u2()
    members = []
    for _ in range(fields_count):
        fh = r.take(6)  # access, name, descriptor
        sub = []
        transform_attrs(r, cp, sub)
        kept, dropped = sub[0]
        total_dropped += dropped
        members.append((fh, kept))
    methods_count = r.u2()
    methods = []
    for _ in range(methods_count):
        mh = r.take(6)
        sub = []
        transform_attrs(r, cp, sub)
        kept, dropped = sub[0]
        total_dropped += dropped
        methods.append((mh, kept))
    sub = []
    transform_attrs(r, cp, sub)
    class_attrs, dropped = sub[0]
    total_dropped += dropped
    if total_dropped == 0:
        return None
    # rebuild: cp + everything up to fields is untouched; easiest is full rebuild
    # from original prefix up to (and including) interfaces, then rewritten members.
    prefix_end = None
    # recompute prefix: magic..interfaces
    r = Reader(data)
    _ = read_cp(r)
    r.take(6)
    n = r.u2()
    r.take(2 * n)
    prefix = data[:r.i]
    out = bytearray(prefix)
    out += struct.pack(">H", fields_count)
    for fh, kept in members:
        out += fh
        out += struct.pack(">H", len(kept))
        for name_idx, pl in kept:
            out += struct.pack(">HI", name_idx, len(pl))
            out += pl
    out += struct.pack(">H", methods_count)
    for mh, kept in methods:
        out += mh
        out += struct.pack(">H", len(kept))
        for name_idx, pl in kept:
            out += struct.pack(">HI", name_idx, len(pl))
            out += pl
    out += struct.pack(">H", len(class_attrs))
    for name_idx, pl in class_attrs:
        out += struct.pack(">HI", name_idx, len(pl))
        out += pl
    return bytes(out)


def main():
    src, dst = sys.argv[1], sys.argv[2]
    changed = 0
    scanned = 0
    with zipfile.ZipFile(src) as zin, zipfile.ZipFile(dst, "w", zipfile.ZIP_DEFLATED) as zout:
        for item in zin.infolist():
            data = zin.read(item.filename)
            if item.filename.endswith(".class"):
                scanned += 1
                try:
                    new = strip_class(data)
                except Exception as e:  # never block the build on a parse quirk
                    print(f"[strip] parse-skip {item.filename}: {e}")
                    new = None
                if new is not None:
                    changed += 1
                    data = new
            zout.writestr(item, data)
    print(f"[strip] scanned {scanned} classes, stripped type annotations from {changed}")


if __name__ == "__main__":
    main()
