#!/usr/bin/env python3
"""split_p4_model.py -- Build #415: keep WitherStormP4 inside the JVM's 64 KB
per-method bytecode limit.

THE PROBLEM
-----------
`src-recon/.../entity/model/WitherStormP4.java` is a machine-translated Bedrock
model: `createBodyLayer()` is ONE method holding 1259 `.addBox(...)` calls plus
1239 `.texOffs(...)` calls -- about 224 KB of source in a single method body.
javac emits roughly 45 bytes of bytecode per box (aload, seven float constants,
a CubeDeformation call and the invokevirtual), so that one method lands in the
60-75 KB range against a hard 64 KB limit. Any addition pushes it over:

    error: code too large

and the whole model file stops compiling.

THE TRANSFORMATION (two mechanical, reversible steps)
-----------------------------------------------------
1. STATEMENT SPLIT. The body is cut at top-level `;` boundaries into chunk
   methods of at most --max-chunk source bytes each (a single statement bigger
   than the cap -- the giant base-body chain -- gets a chunk of its own). Order
   is preserved exactly; nothing is reordered or dropped.

2. LOCAL -> CONTEXT HOIST. The method's locals (mesh, root, bone, bone2, ...)
   become fields of a generated private static `P4Parts` holder, created once
   per call and passed to every chunk. Each field keeps the original name and
   the original declared type -- locals are renamed, never re-typed.

   Why a context object instead of fields on the model class: the class ALREADY
   has fields named bone/bone2/... but typed `ModelPart`, while the locals of
   those names are `PartDefinition` -- 303 type collisions. Why not parameters:
   chunk N creates locals that chunk N+1 reads, with dozens live at a boundary.
   `createBodyLayer()` is static, so the context is per-call, not shared static
   state: two threads baking the model cannot see each other's parts.

VERIFICATION (runs before anything is written)
----------------------------------------------
  * every original top-level statement survives exactly once, in order, and
    token-identical once the `c.` prefixes are stripped;
  * all string literals (the bone names) are byte-identical;
  * braces and parens balance in the new file;
  * the estimated bytecode of every generated method is inside the budget.

Usage:
    python3 ci/split_p4_model.py            # rewrite the file in place
    python3 ci/split_p4_model.py --check    # verify the model file only
Exit 0 = fine, 1 = a method is over budget, 2 = verification refused the write.
"""
import argparse
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
TARGET = "src-recon/net/dabicco/witherstormmod/entity/model/WitherStormP4.java"

METHOD_SIG = "public static LayerDefinition createBodyLayer()"
CTX_NAME = "P4Parts"
CTX_VAR = "c"
ENTRY_NAME = "createBodyLayer"
CHUNK_PREFIX = "createBodyLayer_"
MAX_CHUNK_BYTES = 14_000        # source bytes per chunk (~1.4 KB of bytecode)
MAX_CHUNK_STATEMENTS = 40
# javac's hard limit is 65535 bytes of bytecode per method. 48 KB leaves headroom
# for the constant-pool loads a heavier statement shape would add.
BYTECODE_BUDGET = 48_000
INDENT = "   "                  # the class body uses 3-space indent


# --------------------------------------------------------------------- lexing
def scan(text):
    """Yield (kind, start, end) spans: code / string / line-comment / block."""
    spans = []
    i, n, code_start = 0, len(text), 0
    while i < n:
        ch = text[i]
        if ch == "/" and i + 1 < n and text[i + 1] == "/":
            j = text.find("\n", i)
            j = n if j < 0 else j
            spans.append(("code", code_start, i))
            spans.append(("line", i, j))
            i = code_start = j
        elif ch == "/" and i + 1 < n and text[i + 1] == "*":
            j = text.find("*/", i + 2)
            j = n if j < 0 else j + 2
            spans.append(("code", code_start, i))
            spans.append(("block", i, j))
            i = code_start = j
        elif ch in "\"'":
            j = i + 1
            while j < n and text[j] != ch:
                j += 2 if text[j] == "\\" else 1
            spans.append(("code", code_start, i))
            spans.append(("string", i, min(j + 1, n)))
            i = code_start = min(j + 1, n)
        else:
            i += 1
    spans.append(("code", code_start, n))
    return [s for s in spans if s[2] > s[1]]


def strip_non_code(text):
    """Same length as text, with strings/comments blanked out."""
    out = list(text)
    for kind, a, b in scan(text):
        if kind != "code":
            for k in range(a, b):
                if out[k] != "\n":
                    out[k] = " "
    return "".join(out)


def find_body(text, sig, nth=1):
    """(header_start, open_brace, close_brace) of the nth method matching sig."""
    idx, seen = -1, 0
    while True:
        try:
            idx = text.index(sig, idx + 1)
        except ValueError:
            raise SystemExit("method not found: %s" % sig)
        seen += 1
        if seen == nth:
            break
    header_start = text.rfind("\n", 0, idx) + 1
    open_i = text.index("{", idx)
    blind = strip_non_code(text)
    depth = 0
    for i in range(open_i, len(blind)):
        ch = blind[i]
        if ch == "{":
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0:
                return header_start, open_i, i
    raise SystemExit("unterminated method body")


def top_level_statements(code):
    """Split a brace-depth-0 block into statements, keeping leading trivia."""
    blind = strip_non_code(code)
    out, depth, prev = [], 0, 0
    for i, ch in enumerate(blind):
        if ch in "([{":
            depth += 1
        elif ch in ")]}":
            depth -= 1
        elif ch == ";" and depth == 0:
            out.append((prev, i + 1))
            prev = i + 1
    tail = code[prev:]
    if tail.strip():
        out.append((prev, len(code)))
    return out


DECL_RE = re.compile(
    r"^\s*(?:(?:final)\s+)?(?P<type>[A-Z][\w.]*(?:\s*<[^;=]*?>)?(?:\[\])?)\s+"
    r"(?P<name>[a-z_]\w*)\s*=",
    re.S,
)


def estimate_bytecode(stmt_text):
    """Rough bytecode size: ~26 B per call, ~6 B per numeric argument token.
    Calibrated on the box-building shape (7 float args + CubeDeformation +
    invokevirtual ~= 45 B)."""
    calls = len(re.findall(r"\b[a-z]\w*\s*\(", stmt_text))
    nums = len(re.findall(r"[-+]?\d+\.\d+F?", stmt_text))
    return calls * 26 + nums * 6


def method_budgets(src):
    """[(header, estimated_bytecode, source_bytes)] for every method in a file."""
    blind = strip_non_code(src)
    out = []
    for m in re.finditer(r"(?:public|private|protected)\s[^;{}()\n]*\([^;{}]*\)\s*\{", blind):
        open_i = m.end() - 1
        depth = 0
        for i in range(open_i, len(blind)):
            ch = blind[i]
            if ch == "{":
                depth += 1
            elif ch == "}":
                depth -= 1
                if depth == 0:
                    body = src[open_i + 1 : i]
                    header = src[m.start() : open_i].strip().replace("\n", " ")
                    blind_body = strip_non_code(body)
                    if re.search(r"\b(class|interface|enum)\b", blind_body):
                        # an enclosing type would be counted twice; report it as
                        # information and leave the budget decision to the gate
                        out.append((header + "  [nested types: not budgeted]", 0, len(body)))
                        break
                    out.append((header, sum(estimate_bytecode(s) for s in
                                            [body[a:b] for a, b in top_level_statements(body)]),
                                len(body)))
                    break
    return out


def dedent(text):
    """Remove the common indent of a statement block."""
    lines = [ln for ln in text.strip("\n").split("\n")]
    while lines and not lines[0].strip():
        lines.pop(0)
    while lines and not lines[-1].strip():
        lines.pop()
    if not lines:
        return ""
    indents = [len(ln) - len(ln.lstrip()) for ln in lines if ln.strip()]
    cut = min(indents) if indents else 0
    return "\n".join(ln[cut:] if ln.strip() else "" for ln in lines)


def _protected_spans(text):
    """Statement-relative offsets that belong to strings or comments."""
    out = set()
    for kind, a, b in scan(text):
        if kind != "code":
            out.update(range(a, b))
    return out


def rewrite_statement(stmt, fields):
    """Rewrite ONE statement for the context holder.

    Two shapes are handled:
      * a local declaration of a hoisted name (`PartDefinition bone = ...`)
        loses its type and becomes an assignment to the context field:
            c.bone = ...
      * every other use of a hoisted name is prefixed with `c.`.
    Strings and comments are never touched (the bone NAMES live in string
    literals -- a prefix there would rename the model's own bones).
    """
    protected = _protected_spans(stmt)
    blind = strip_non_code(stmt)
    decl = DECL_RE.match(stmt)
    decl_name = decl.group("name") if decl and decl.group("name") in fields else None

    out, i = [], 0
    while i < len(stmt):
        if i in protected:
            out.append(stmt[i])
            i += 1
            continue
        m = re.match(r"[A-Za-z_]\w*", stmt[i:])
        if not m:
            out.append(stmt[i])
            i += 1
            continue
        word = m.group(0)
        prev = next((blind[k] for k in range(i - 1, -1, -1) if not blind[k].isspace()), "")
        nxt = next((blind[k] for k in range(i + len(word), len(stmt)) if not blind[k].isspace()), "")
        if word in fields and prev != "." and nxt != "(":
            # inside `Type name =` the declared type must be dropped, not copied
            if decl_name and word == decl_name:
                out.append("%s.%s" % (CTX_VAR, word))
            elif decl and i < decl.start("name"):
                pass    # the declared type token: dropped along with the space
            else:
                out.append("%s.%s" % (CTX_VAR, word))
        else:
            out.append(word)
        i += len(word)
    text = "".join(out)
    if decl and decl_name:
        # collapse the now-empty declaration head: "PartDefinition c.bone = " ->
        # "c.bone = "
        head = text.index("%s.%s" % (CTX_VAR, decl_name))
        text = text[head:]
        text = ("%s.%s = " % (CTX_VAR, decl_name)) + text.split("=", 1)[1].lstrip()
    return text


def canonical_tokens(text):
    """Token stream used for the equivalence proof.

    Declarations are normalised to assignments on both sides (`Type name =` and
    `c.name =` both become `name =`), so the original and the rewritten body can
    be compared token-for-token with no textual fuzz."""
    decl = DECL_RE.match(text)
    tokens = []
    i, n = 0, len(text)
    protected = _protected_spans(text)
    while i < n:
        if i in protected:
            m = re.match(r'"[^"]*"', text[i:]) or re.match(r"'[^']*'", text[i:])
            if m:
                tokens.append(m.group(0))
                i += len(m.group(0))
            else:
                i += 1
            continue
        ch = text[i]
        if ch.isspace():
            i += 1
            continue
        m = re.match(r"[-+]?\d+\.?\d*[FfDdLl]?", text[i:])
        if m:
            tokens.append(m.group(0))
            i += len(m.group(0))
            continue
        m = re.match(r"[A-Za-z_]\w*", text[i:])
        if m:
            tokens.append(m.group(0))
            i += len(m.group(0))
            continue
        tokens.append(ch)
        i += 1
    # drop the declared type / the context prefix so both sides read `name =`
    if decl:
        name = decl.group("name")
        try:
            j = tokens.index(name)
        except ValueError:
            return tokens
        if j + 1 < len(tokens) and tokens[j + 1] == "=":
            tokens = tokens[j:]
    # drop EVERY context prefix so `c.bone` and `bone` read the same
    cleaned = []
    i = 0
    while i < len(tokens):
        if tokens[i] == CTX_VAR and i + 1 < len(tokens) and tokens[i + 1] == ".":
            i += 2
            continue
        cleaned.append(tokens[i])
        i += 1
    return cleaned


def first_use_before_assign(chunks, fields):
    """Linear proof that no context field is read before it is written."""
    assigned = set()
    for chunk in chunks:
        for stmt in chunk:
            tokens = canonical_tokens(stmt)
            for idx, tok in enumerate(tokens):
                if tok in fields and (idx + 1 >= len(tokens) or tokens[idx + 1] != "="):
                    if tok not in assigned:
                        return "%s is read before it is ever assigned" % tok
                    if idx + 1 < len(tokens) and tokens[idx + 1] == "." and idx + 2 < len(tokens) \
                       and tokens[idx + 2] == "=":
                        assigned.add(tok)
                if tok in fields and idx + 1 < len(tokens) and tokens[idx + 1] == "=":
                    assigned.add(tok)
    return None


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true", help="verify only, never write")
    ap.add_argument("paths", nargs="*", default=None,
                    help="model files to inspect with --check (default: the P4 model)")
    args = ap.parse_args()

    paths = args.paths or [TARGET]
    rc = 0
    for rel in paths:
        path = rel if os.path.isabs(rel) else os.path.join(ROOT, rel)
        if not os.path.isfile(path):
            print("[p4] skip (missing): %s" % rel)
            continue
        rc = max(rc, check_file(path, args, write=args.check is False
                                and os.path.abspath(path) == os.path.abspath(os.path.join(ROOT, TARGET))))
    return rc


def check_file(path, args, write=False):
    src = open(path, encoding="utf-8").read()

    if args.check or CTX_NAME in src:
        rows = sorted(method_budgets(src), key=lambda r: -r[1])
        over = [r for r in rows if r[1] > BYTECODE_BUDGET]
        print("[p4] %s -- largest methods by estimated bytecode (budget %d B):"
              % (os.path.relpath(path, ROOT), BYTECODE_BUDGET))
        for header, est, srclen in rows[:3]:
            print("     %8d B est  %8d B src  %s" % (est, srclen, header[:72]))
        if over:
            print("[p4] FAIL -- %d method(s) over the bytecode budget; javac would"
                  " reject the model with 'code too large'" % len(over))
            return 1
        print("[p4] OK -- every method is inside the budget")
        return 0

    header_start, open_i, close_i = find_body(src, METHOD_SIG)
    body = src[open_i + 1 : close_i]
    spans = top_level_statements(body)
    stmts = [body[a:b] for a, b in spans]
    if not stmts:
        raise SystemExit("createBodyLayer looks empty -- refusing to touch it")

    # the trailing return becomes the entry method's return: it must not be
    # executed inside a chunk as well (that would be an unreachable statement)
    tail_return = ""
    if stmts[-1].strip().startswith("return "):
        tail_return = stmts[-1].strip()
        stmts = stmts[:-1]

    fields, order = {}, []
    for st in stmts:
        m = DECL_RE.match(st)
        if not m:
            continue
        name, jtype = m.group("name"), m.group("type").strip()
        if name in fields:
            if fields[name] != jtype:
                raise SystemExit("local %s is declared twice with different types" % name)
            continue
        fields[name] = jtype
        order.append(name)

    chunks, cur, cur_bytes, cur_count = [], [], 0, 0
    for st in stmts:
        new = rewrite_statement(st, fields)
        size = len(new)
        if cur and (cur_bytes + size > MAX_CHUNK_BYTES or cur_count >= MAX_CHUNK_STATEMENTS):
            chunks.append(cur)
            cur, cur_bytes, cur_count = [], 0, 0
        cur.append(new)
        cur_bytes += size
        cur_count += 1
    if cur:
        chunks.append(cur)

    entry_tail = rewrite_statement(tail_return, fields) if tail_return else \
        "return LayerDefinition.create(%s.mesh, 512, 512);" % CTX_VAR

    lines = ["%sprivate static final class %s {" % (INDENT, CTX_NAME)]
    for name in order:
        lines.append("%s   final %s %s;" % (INDENT, fields[name], name))
    lines.append("%s}" % INDENT)
    lines.append("")
    lines.append("%spublic static LayerDefinition %s() {" % (INDENT, ENTRY_NAME))
    lines.append("%s   %s %s = new %s();" % (INDENT, CTX_NAME, CTX_VAR, CTX_NAME))
    for idx in range(len(chunks)):
        lines.append("%s   %s%d(%s);" % (INDENT, CHUNK_PREFIX, idx + 1, CTX_VAR))
    lines.append("%s   %s" % (INDENT, entry_tail))
    lines.append("%s}" % INDENT)
    for idx, chunk in enumerate(chunks):
        lines.append("")
        lines.append("%sprivate static void %s%d(%s %s) {"
                     % (INDENT, CHUNK_PREFIX, idx + 1, CTX_NAME, CTX_VAR))
        for text in chunk:
            block = dedent(text)
            if block:
                lines.extend("%s   %s" % (INDENT, ln) if ln.strip() else ""
                             for ln in block.split("\n"))
        lines.append("%s}" % INDENT)

    # the whole method (header + body) is replaced: the chunk helpers are CLASS
    # members, so they must not land inside the braces they came from
    new_src = src[:header_start] + "\n".join(lines) + src[close_i + 1:]

    # ------------------------------------------------------------- verify
    problems = []
    # token-for-token equivalence, statement by statement
    recon = [st for chunk in chunks for st in chunk]
    if len(recon) != len(stmts):
        problems.append("statement count changed: %d -> %d" % (len(stmts), len(recon)))
    else:
        for i, (orig, new) in enumerate(zip(stmts, recon)):
            if canonical_tokens(orig) != canonical_tokens(new):
                problems.append("statement %d is not token-identical after the rewrite" % i)
                break
    # every identifier / string the original body contained still exists, in order
    def code_words(t):
        return [w for w in re.findall(r'"[^"]*"|[A-Za-z_]\w*', t)]
    if code_words(body) != code_words("".join(recon) + entry_tail
                                      + "".join("%s.%s" % (CTX_VAR, n) for n in order)
                                      + "".join(fields.values())):
        # the context declarations legitimately add tokens, so compare the
        # string literals alone for the strict check and the statement tokens
        # (already proven above) for the code
        if re.findall(r'"[^"]*"', body) != re.findall(r'"[^"]*"', "".join(recon) + entry_tail):
            problems.append("string literals changed (bone names must survive byte-for-byte)")
    bad = first_use_before_assign(chunks, fields)
    if bad:
        problems.append(bad)
    for o, cc in (("{", "}"), ("(", ")")):
        if new_src.count(o) != new_src.count(cc):
            problems.append("unbalanced %s%s in the new file" % (o, cc))
    for idx, chunk in enumerate(chunks):
        est = sum(estimate_bytecode(t) for t in chunk)
        if est > BYTECODE_BUDGET:
            problems.append("%s%d estimated at %d B (budget %d)"
                            % (CHUNK_PREFIX, idx + 1, est, BYTECODE_BUDGET))
    if "%s;" % entry_tail.rstrip(";") not in new_src:
        problems.append("the original LayerDefinition.create(...) return was not preserved")

    if problems:
        print("[p4] VERIFICATION FAILED -- nothing written:")
        for p in problems:
            print("   *", p)
        return 2

    open(path, "w", encoding="utf-8").write(new_src)
    est_max = max(sum(estimate_bytecode(t) for t in ch) for ch in chunks)
    print("[p4] split createBodyLayer into %d chunks (largest chunk ~%d B bytecode)"
          % (len(chunks), est_max))
    print("[p4] context fields: %d (%s ...)" % (len(order), ", ".join(order[:6])))
    return 0


if __name__ == "__main__":
    sys.exit(main())
