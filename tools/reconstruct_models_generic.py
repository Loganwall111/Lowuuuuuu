#!/usr/bin/env python3
"""Reconstruct a Vineflower bytecode-comment-only method body into real Java source.

Some very large model methods (e.g. createBodyLayer / make_bone*) were too big for
Vineflower to decompile, so their bodies are only bytecode comments and the compiler
reports "missing return statement". This script interprets the bytecode (tracking the
JVM operand stack and local slots) and rebuilds the Java chained calls.

Usage:
    python3 tools/reconstruct_models_generic.py <path> <sig_line> <end_line> \
        [<mesh_var>] [<root_var>]

    sig_line: 1-based line of the method signature (must end with '{')
    end_line: 1-based line of the method's closing brace (inclusive)

Prints the reconstructed method (signature line through closing brace) to stdout.
"""
import re
import sys

reserved = {'mesh', 'root', 'rootdef', 'meshdefinition'}


def parse_ops(lines):
    ops = []
    for ln in lines:
        m = re.match(r'\s*//\s+[0-9a-f]+:\s*(.*)', ln)
        if m:
            ops.append(m.group(1))
    return ops


def fmt_float(f):
    s = repr(f)
    if f == int(f) and abs(f) < 1e15:
        s = str(int(f))
    return s + 'F'


def valid_ident(name):
    # make a valid Java identifier from a part name
    out = re.sub(r'\W', '_', name)
    if not out:
        out = 'part'
    if out[0].isdigit():
        out = 'part_' + out
    return out


def reconstruct(ops, mesh_var='mesh', root_var='root'):
    stack = []
    locals_map = {}
    stmts = []          # pending (varname, expr) waiting for astore
    stmts_emitted = []
    ret = None
    used_names = set()
    name_of = {}        # slot -> varname for PART locals

    def pop():
        if not stack:
            raise RuntimeError('stack underflow')
        return stack.pop()

    for op in ops:
        parts = op.split(' ')
        mnemonic = parts[0]
        if mnemonic == 'wide':
            # wide aload N / wide astore N / wide bipush etc.
            mnemonic = parts[1]
            operand = parts[2] if len(parts) > 2 else None
        else:
            operand = parts[1] if len(parts) > 1 else None

        if mnemonic == 'new':
            stack.append(('NEW', operand))
        elif mnemonic == 'dup':
            stack.append(stack[-1])
        elif mnemonic in ('fconst_0', 'fconst_1', 'fconst_2'):
            stack.append(('F', float(int(mnemonic[-1]))))
        elif mnemonic in ('bipush', 'sipush'):
            stack.append(('I', int(operand)))
        elif mnemonic in ('ldc_w', 'ldc'):
            val = operand
            if val.startswith('"'):
                stack.append(('STR', val[1:-1]))
            else:
                stack.append(('F', float(val)))
        elif mnemonic == 'invokespecial':
            owner = parts[1].split('.')[0]
            desc = parts[2]
            if '<init>' in parts[1]:
                args_desc = desc[1:desc.index(')')]
                argcount = len(args_desc) if args_desc else 0
                ctor_args = []
                for _ in range(argcount):
                    ctor_args.append(pop())
                objref = pop()
                assert objref[0] == 'NEW', objref
                assert stack and stack[-1][0] == 'NEW'
                simple = owner.split('/')[-1]
                if simple == 'CubeDeformation':
                    a = fmt_float(ctor_args[-1][1])
                    stack[-1] = ('OBJ', 'new CubeDeformation(' + a + ')')
                elif simple == 'MeshDefinition':
                    stack[-1] = ('OBJ', 'new MeshDefinition()')
                else:
                    raise RuntimeError('unknown ctor ' + op)
            else:
                raise RuntimeError('unknown invokespecial ' + op)
        elif mnemonic == 'invokestatic':
            owner_name = parts[1]
            if 'CubeListBuilder.create' in owner_name:
                stack.append(('BUILDER', 'CubeListBuilder.create()'))
            elif 'PartPose.offsetAndRotation' in owner_name:
                args = [pop()[1] for _ in range(6)][::-1]
                pose = 'PartPose.offsetAndRotation(' + ', '.join(fmt_float(a) for a in args) + ')'
                stack.append(('POSE', pose))
            elif 'PartPose.offset' in owner_name:
                args = [pop()[1] for _ in range(3)][::-1]
                pose = 'PartPose.offset(' + ', '.join(fmt_float(a) for a in args) + ')'
                stack.append(('POSE', pose))
            elif 'LayerDefinition.create' in owner_name:
                height = pop()[1]
                width = pop()[1]
                mesh = pop()
                assert mesh[0] in ('OBJ', 'MESH'), mesh
                stack.append(('LAYER', 'LayerDefinition.create(' + mesh[1] + ', ' + str(width) + ', ' + str(height) + ')'))
            else:
                raise RuntimeError('unknown invokestatic ' + op)
        elif mnemonic == 'invokevirtual':
            owner_name = parts[1]
            desc = parts[2]
            if 'CubeListBuilder.texOffs' in owner_name:
                b = pop(); a = pop(); rec = pop()
                stack.append(('BUILDER', rec[1] + '.texOffs(' + str(a[1]) + ', ' + str(b[1]) + ')'))
            elif 'CubeListBuilder.addBox' in owner_name:
                deform = pop()
                assert deform[0] == 'OBJ'
                args = [pop()[1] for _ in range(6)][::-1]
                rec = pop()
                call = '.addBox(' + ', '.join(fmt_float(a) for a in args) + ', ' + deform[1] + ')'
                stack.append(('BUILDER', rec[1] + call))
            elif 'CubeListBuilder.mirror' in owner_name:
                if desc.startswith('(Z)'):
                    b = pop(); rec = pop()
                    val = 'true' if b[1] else 'false'
                    stack.append(('BUILDER', rec[1] + '.mirror(' + val + ')'))
                else:
                    rec = pop()
                    stack.append(('BUILDER', rec[1] + '.mirror()'))
            elif 'PartDefinition.addOrReplaceChild' in owner_name:
                pose = pop(); bldr = pop(); name = pop(); parent = pop()
                assert name[0] == 'STR' and parent[0] == 'PART', (name, parent)
                child_expr = parent[1] + '.addOrReplaceChild("' + name[1] + '", ' + bldr[1] + ', ' + pose[1] + ')'
                stack.append(('PART', name[1]))
                stmts.append((name[1], child_expr))
            elif 'MeshDefinition.getRoot' in owner_name:
                mesh = pop()
                assert mesh[0] in ('OBJ', 'MESH'), mesh
                stack.append(('OBJ', 'PartDefinition'))
            else:
                raise RuntimeError('unknown invokevirtual ' + op)
        elif mnemonic == 'aload':
            slot = int(operand)
            v = locals_map.get(slot)
            if v is None:
                raise RuntimeError('aload of unset slot ' + str(slot) + ' in ' + op)
            stack.append(v)
        elif mnemonic == 'astore':
            slot = int(operand)
            val = pop()
            if val[0] == 'PART':
                raw_name, expr = stmts.pop()
                varname = valid_ident(raw_name)
                base = varname
                n = 2
                while varname in used_names or varname in reserved:
                    varname = base + str(n)
                    n += 1
                used_names.add(varname)
                locals_map[slot] = ('PART', varname)
                stmts_emitted.append('   PartDefinition ' + varname + ' = ' + expr + ';')
            elif val[0] == 'OBJ' and val[1] == 'new MeshDefinition()':
                locals_map[slot] = ('MESH', mesh_var)
                stmts_emitted.append('   MeshDefinition ' + mesh_var + ' = new MeshDefinition();')
            elif val[0] == 'OBJ':
                locals_map[slot] = ('PART', root_var)
                stmts_emitted.append('   PartDefinition ' + root_var + ' = ' + mesh_var + '.getRoot();')
            else:
                raise RuntimeError('astore of ' + str(val) + ' in ' + op)
        elif mnemonic == 'areturn':
            val = pop()
            if val[0] == 'LAYER':
                ret = '   return ' + val[1] + ';'
            else:
                raise RuntimeError('areturn of ' + str(val))
        else:
            raise RuntimeError('unhandled opcode ' + mnemonic + ' in ' + op)

    if ret is None:
        raise RuntimeError('no return found')
    return stmts_emitted + [ret]


if __name__ == '__main__':
    path = sys.argv[1]
    sig_line = int(sys.argv[2])
    end_line = int(sys.argv[3])
    mesh_var = sys.argv[4] if len(sys.argv) > 4 else 'mesh'
    root_var = sys.argv[5] if len(sys.argv) > 5 else 'root'
    lines = open(path, encoding='utf-8').read().split('\n')
    sig = lines[sig_line - 1]
    if not sig.rstrip().endswith('{'):
        # signature may be multi-line; search upwards for the '{'
        i = sig_line - 1
        while i >= 0 and '{' not in lines[i]:
            i -= 1
        sig_line = i + 1
        sig = lines[sig_line - 1]
    body_lines = lines[sig_line:end_line - 1]
    ops = parse_ops(body_lines)
    emitted = reconstruct(ops, mesh_var=mesh_var, root_var=root_var)
    print(sig)
    for s in emitted:
        print(s)
    print('   }')
