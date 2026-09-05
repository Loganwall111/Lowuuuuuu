#!/usr/bin/env python3
"""Widen access flags of specific members/classes inside a jar (compile-only).

The published mod bytecode calls these vanilla members directly and works at
runtime, but javac refuses the same calls when compiling the recovered source
against the raw client jar ("has private/protected access"). Loom solves this
with access wideners; we solve it by patching the COMPILE-ONLY copy of the
jar. The shipped jar and the runtime are untouched.

Usage: python3 ci/widen_members.py in.jar out.jar widen.txt
widen.txt lines: Owner#Name  (Owner = FQ class name with dots, or simple name;
Name = method/field name, or a nested class simple name).
Both "member of Owner" and "nested class Owner$Name" are widened when present.
Pure stdlib; only rewrites entries that change.
"""
import struct
import sys
import zipfile

ACC_PUBLIC = 0x0001
ACC_PRIVATE = 0x0002
ACC_PROTECTED = 0x0004


def widen_flags(f):
    return (f & ~(ACC_PRIVATE | ACC_PROTECTED)) | ACC_PUBLIC


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
    if r.u4() != 0xCAFEBABE:
        raise ValueError("not a class file")
    r.u2()
    r.u2()
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
            i += 1
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            cp[i] = (tag, r.take(4))
        else:
            raise ValueError(f"bad cp tag {tag}")
        i += 1
    return cp


def utf8(cp, idx):
    e = cp.get(idx)
    return e[1] if e and e[0] == 1 else b""


def skip_attrs(r):
    n = r.u2()
    for _ in range(n):
        r.u2()
        length = r.u4()
        r.take(length)


def widen_class_bytes(data, member_names, class_level):
    """Returns (new_bytes_or_None, patched_members, patched_class)."""
    r = Reader(data)
    cp = read_cp(r)
    changed = False
    patched_members = set()

    access = r.u2()
    this_idx = r.u2()
    super_idx = r.u2()
    if class_level:
        new_access = widen_flags(access)
        if new_access != access:
            changed = True
        access = new_access

    ifc_n = r.u2()
    r.take(2 * ifc_n)

    r2 = Reader(data)
    _ = read_cp(r2)
    access2 = r2.u2()
    this2 = r2.u2()
    super2 = r2.u2()
    if class_level:
        access2 = widen_flags(access2)
        changed = True
    ifc2 = r2.u2()
    ifaces = r2.take(2 * ifc2)

    def members(names):
        nonlocal changed
        n = r2.u2()
        parts = [struct.pack(">H", n)]
        for _ in range(n):
            m_acc = r2.u2()
            name_idx = r2.u2()
            desc_idx = r2.u2()
            name = utf8(cp, name_idx).decode("utf-8", "replace")
            if names and name in names:
                new_acc = widen_flags(m_acc)
                if new_acc != m_acc:
                    changed = True
                    patched_members.add(name)
                m_acc = new_acc
            attr_start = r2.i
            skip_attrs(r2)
            parts.append(struct.pack(">HHHH", m_acc, name_idx, desc_idx, 0)[:-2])
            parts.append(data[attr_start:r2.i])
        return b"".join(parts)

    fields = members(member_names)
    methods = members(member_names)
    attrs_start = r2.i
    skip_attrs(r2)
    class_attrs = data[attrs_start:r2.i]

    if not changed:
        return None, set(), False

    # rebuild whole file: prefix up to access flags + patched tail
    p = Reader(data)
    _ = read_cp(p)
    prefix = data[:p.i]
    body = bytearray(prefix)
    body += struct.pack(">HHH", access2, this2, super2)
    body += struct.pack(">H", ifc2)
    body += ifaces
    body += fields
    body += methods
    body += class_attrs
    return bytes(body), patched_members, class_level and changed


def main():
    src, dst, widen_file = sys.argv[1], sys.argv[2], sys.argv[3]
    try:
        entries = [l.strip() for l in open(widen_file) if l.strip() and "#" in l]
    except FileNotFoundError:
        entries = []

    targets = []  # (path_predicate, member_names, class_level)
    by_path = {}
    for e in entries:
        owner, name = e.rsplit("#", 1)
        targets.append((owner, name))

    def owner_paths(owner):
        """Jar entry paths that could hold this owner (FQ or simple)."""
        if "." in owner or "/" in owner:
            return [owner.replace(".", "/") + ".class", owner.replace(".", "/").replace("$", "/") + ".class"]
        return None  # simple name: basename scan

    changed_entries = 0
    patched_report = []
    with zipfile.ZipFile(src) as zin, zipfile.ZipFile(dst, "w", zipfile.ZIP_DEFLATED) as zout:
        names = zin.namelist()
        basename_index = {}
        for n in names:
            if n.endswith(".class"):
                basename_index.setdefault(n.rsplit("/", 1)[-1][:-6], []).append(n)

        # map each class entry -> (members to widen, class-level widen?)
        plan = {}
        for owner, name in targets:
            cands = []
            op = owner_paths(owner)
            if op:
                cands = [p for p in op if p in basename_index or p in names]
                cands = [p for p in op if p in set(names)]
            if not cands:
                simple = owner.rsplit(".", 1)[-1]
                cands = basename_index.get(simple, [])
            for c in cands:
                plan.setdefault(c, set()).add(name)
            # nested class form: Owner$Name.class
            simple = owner.rsplit(".", 1)[-1]
            for nested in basename_index.get(simple + "$" + name, []):
                plan.setdefault(nested, set()).add(None)  # None = class-level

        for item in zin.infolist():
            data = zin.read(item.filename)
            if item.filename in plan:
                want = plan[item.filename]
                member_names = {w for w in want if w is not None}
                class_level = None in want
                try:
                    new, patched, cls_patch = widen_class_bytes(data, member_names, class_level)
                except Exception as e:
                    print(f"[widen] parse-skip {item.filename}: {e}")
                    new = None
                if new is not None:
                    data = new
                    changed_entries += 1
                    patched_report.append(f"{item.filename}: members={sorted(patched)} class={cls_patch}")
            zout.writestr(item, data)

    print(f"[widen] targets={len(targets)} entries_changed={changed_entries}")
    for line in patched_report[:20]:
        print("[widen] " + line)


if __name__ == "__main__":
    main()
