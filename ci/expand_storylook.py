#!/usr/bin/env python3
"""Inline the vanilla 26.2 #moj_import includes (glslcheck/inc262/) into a
Story Look shader so the offline GLSL validator can compile it. Usage:
expand_storylook.py IN.fsh OUT.frag"""
import re
import sys

src = open(sys.argv[1]).read()


def repl(m):
    name = m.group(1).rsplit("/", 1)[-1]
    body = open("glslcheck/inc262/" + name).read()
    return body


out = re.sub(r"#moj_import <minecraft:([A-Za-z0-9_./]+)>", repl, src)
open(sys.argv[2], "w").write(out)
