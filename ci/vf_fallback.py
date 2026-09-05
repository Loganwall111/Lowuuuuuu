#!/usr/bin/env python3
"""Translate a Vineflower "$VF: Couldn't be decompiled" fallback method into
real Java source, using the bytecode listing Vineflower embeds as comments.

Handles the regular model-builder pattern:
  param.addOrReplaceChild("name", CubeListBuilder.create().texOffs(i,j)
  .addBox(f,f,f,f,f,f, new CubeDeformation(f))..., PartPose.offset*(...));

Usage: vf_fallback.py FILE.java METHODNAME
Prints the replacement method (signature + body + closing brace) to stdout,
and "###REPLACE <first> <last>" (1-based inclusive lines of the OLD method,
signature through closing brace) to stderr.
Exits nonzero on ANY opcode it does not understand, so silent mistranslation
is impossible.
"""
import re
import sys

DESC_ARG = re.compile(r"\((.*?)\)")


def parse_args(desc):
    """Arg slot kinds from a JVM descriptor: list of 'I', 'F', or 'L'."""
    inner = DESC_ARG.search(desc).group(1)
    kinds = []
    i = 0
    while i < len(inner):
        c = inner[i]
        if c == "L":
            kinds.append("L")
            i = inner.index(";", i) + 1
        elif c == "[":
            while inner[i] == "[":
                i += 1
            if inner[i] == "L":
                i = inner.index(";", i) + 1
            else:
                i += 1
            kinds.append("L")
        elif c in "IF":
            kinds.append(c)
            i += 1
        else:
            raise SystemExit("unsupported descriptor type %r in %s" % (c, desc))
    return kinds


def fmt(kind, val):
    """Render a stack value as a Java literal/expression."""
    if kind == "float":
        return val + "F"
    return val  # int, string literal, or already-rendered expression


def translate(comments, param_name):
    stack = []  # entries: (kind, text) kind in int/float/str/expr/new
    for line in comments:
        m = re.match(r"^\s*// [0-9a-f]{4}: (.*)$", line)
        if not m:
            continue
        parts = m.group(1).split(" ", 1)
        op = parts[0]
        arg = parts[1].strip() if len(parts) > 1 else ""
        if op in ("aload", "aload_0"):
            n = "0" if op == "aload_0" else arg
            if n != "0":
                raise SystemExit("only param loads supported (aload %s)" % n)
            stack.append(("expr", param_name))
        elif op in ("ldc", "ldc_w"):
            if arg.startswith('"'):
                stack.append(("str", arg))
            elif "." in arg or "E" in arg or "e" in arg:
                stack.append(("float", arg))
            else:
                stack.append(("int", arg))
        elif op in ("bipush", "sipush"):
            stack.append(("int", arg))
        elif op.startswith("iconst_"):
            stack.append(("int", op[-1]))
        elif op.startswith("fconst_"):
            stack.append(("float", {"0": "0.0", "1": "1.0", "2": "2.0"}[op[-1]]))
        elif op == "new":
            stack.append(("new", arg.rsplit("/", 1)[-1]))
        elif op == "dup":
            stack.append(stack[-1])
        elif op == "invokespecial":
            meth, desc = arg.split(" ", 1)
            if not meth.endswith("<init>") or "CubeDeformation" not in meth:
                raise SystemExit("unsupported ctor %s" % meth)
            kinds = parse_args(desc)
            vals = [stack.pop() for _ in kinds][::-1]
            dupped = stack.pop()
            orig = stack.pop()
            if dupped[0] != "new" or orig[0] != "new":
                raise SystemExit("ctor without matching new/dup pair")
            stack.append(("expr", "new %s(%s)" % (
                orig[1], ", ".join(fmt(k, v) for (k, v), kk in zip(vals, kinds)))))
        elif op in ("invokevirtual", "invokestatic", "invokeinterface"):
            meth, desc = arg.split(" ", 1)
            kinds = parse_args(desc)
            vals = [stack.pop() for _ in kinds][::-1]
            args_s = ", ".join(fmt(k, v) for (k, v), kk in zip(vals, kinds))
            short = meth.split(".")[-1]
            if op == "invokestatic":
                cls = meth.rsplit(".", 1)[0].rsplit("/", 1)[-1]
                stack.append(("expr", "%s.%s(%s)" % (cls, short, args_s)))
            else:
                recv = stack.pop()
                if recv[0] not in ("expr", "str"):
                    raise SystemExit("bad receiver %r" % (recv,))
                stack.append(("expr", "%s.%s(%s)" % (fmt(*recv), short, args_s)))
        elif op == "areturn":
            if len(stack) != 1:
                raise SystemExit("areturn with %d stack slots" % len(stack))
            return stack.pop()
        else:
            raise SystemExit("unsupported opcode %r — refusing to guess" % op)
    raise SystemExit("no areturn reached")


def main():
    path, name = sys.argv[1], sys.argv[2]
    lines = open(path).read().split("\n")
    sig_i = None
    for i, l in enumerate(lines):
        if re.search(r"\b%s\s*\(" % re.escape(name), l) and re.search(r"\b(static|public|private|protected)\b", l):
            sig_i = i
            break
    if sig_i is None:
        raise SystemExit("method %s not found" % name)
    sig = lines[sig_i]
    pm = re.search(r"\(\s*(?:final\s+)?[\w.<>\[\]]+\s+(\w+)\s*\)", sig)
    param_name = pm.group(1) if pm else "param0"
    comments = []
    end_i = None
    for j in range(sig_i + 1, len(lines)):
        if re.match(r"^\s*// [0-9a-f]{4}: ", lines[j]):
            comments.append(lines[j])
        elif lines[j].strip() == "}" and comments:
            end_i = j
            break
    if end_i is None:
        raise SystemExit("no bytecode comment block found under %s" % name)
    kind, body = translate(comments, param_name)
    if kind != "expr":
        raise SystemExit("returned value is not an expression (%s)" % kind)
    body = body.replace(".texOffs(", "\n            .texOffs(").replace(".addBox(", "\n            .addBox(")
    first = body.split("\n", 1)[0]
    rest = body[len(first):]
    print(sig)
    print("      return " + first + rest + ";")
    print("   }")
    print("###REPLACE %d %d" % (sig_i + 1, end_i + 1), file=sys.stderr)


if __name__ == "__main__":
    main()
