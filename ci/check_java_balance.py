#!/usr/bin/env python3
"""check_java_balance.py — a comment/string-aware brace and paren counter.

The sandbox has no JDK, so a syntax error in a Java file costs a full CI round
trip. This is the local net: it walks a .java file the way javac does for the
purposes of delimiters -- line comments, block comments, string literals, char
literals, text blocks and escape sequences are all understood -- and reports

  * braces `{}` / parens `()` / brackets `[]` that do not balance,
  * the line where the imbalance is detected,
  * a final "OK" line with the file's brace depth (must end at 0).

Usage:  python3 ci/check_java_balance.py <file.java> [more.java ...]
Exit:   0 all files balanced, 1 any imbalance.
"""
import sys


def scan(path):
    """Return (ok, message, braces, parens, brackets)."""
    src = open(path, "r", encoding="utf-8", errors="replace").read()
    depth = {"{": 0, "(": 0, "[": 0}
    close = {"}": "{", ")": "(", "]": "["}
    i = 0
    n = len(src)
    line = 1
    stack = []
    while i < n:
        ch = src[i]
        two = src[i:i + 2]
        three = src[i:i + 3]
        if ch == "\n":
            line += 1
            i += 1
            continue
        if two == "//":                      # line comment
            while i < n and src[i] != "\n":
                i += 1
            continue
        if two == "/*":                      # block comment
            i += 2
            while i + 1 < n and src[i:i + 2] != "*/":
                if src[i] == "\n":
                    line += 1
                i += 1
            i += 2
            continue
        if three == '"""':                   # text block
            i += 3
            while i + 2 < n and src[i:i + 3] != '"""':
                if src[i] == "\n":
                    line += 1
                i += 1
            i += 3
            continue
        if ch == '"':                        # string literal
            i += 1
            while i < n and src[i] != '"':
                if src[i] == "\\":
                    i += 2
                    continue
                if src[i] == "\n":
                    line += 1
                i += 1
            i += 1
            continue
        if ch == "'":                        # char literal
            i += 1
            while i < n and src[i] != "'":
                if src[i] == "\\":
                    i += 2
                    continue
                i += 1
            i += 1
            continue
        if ch in "{(}":
            pass
        if ch in "{([":
            depth[ch] += 1
            stack.append((ch, line))
            i += 1
            continue
        if ch in ")]}":
            want = close[ch]
            depth[want] -= 1
            if depth[want] < 0:
                return (False, "line %d: unmatched '%s'" % (line, ch),
                        depth["{"], depth["("], depth["["])
            if stack:
                stack.pop()
            i += 1
            continue
        i += 1
    if stack:
        opener, at = stack[-1]
        return (False, "unclosed '%s' opened on line %d" % (opener, at),
                depth["{"], depth["("], depth["["])
    return (True, "", depth["{"], depth["("], depth["["])


def main(argv):
    bad = 0
    for path in argv:
        ok, msg, braces, parens, brackets = scan(path)
        name = path.split("/")[-1]
        if not ok:
            print("FAIL %-42s %s" % (name, msg))
            bad += 1
        elif braces or parens or brackets:
            print("FAIL %-42s residual depth {}={} ()={} []={}"
                  % (name, braces, parens, brackets))
            bad += 1
        else:
            print("OK   %-42s braces=0 parens=0 brackets=0" % name)
    if bad:
        print("%d file(s) unbalanced" % bad)
        return 1
    print("all %d file(s) balanced" % len(argv))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
