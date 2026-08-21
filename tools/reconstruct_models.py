#!/usr/bin/env python3
"""Reconstruct make_bone* methods from Vineflower bytecode-comment bodies.

Two methods in HugeAssBackModel.java (make_bone239, make_bone146) were too large for
Vineflower to decompile, so their bodies are just bytecode comments and the compiler
reports "missing return statement". This script parses the bytecode and rebuilds the
Java chained call:
    return param0.addOrReplaceChild("name", CubeListBuilder.create().texOffs(..).addBox(..)... , PartPose.offsetAndRotation(..));
"""
import re, sys

def parse_bytecode(lines):
    ops = []
    for ln in lines:
        m = re.match(r'\s*//\s+[0-9a-f]+:\s*(.*)', ln)
        if m:
            ops.append(m.group(1))
    return ops

def fmt(v):
    if isinstance(v, float):
        # keep as Java float literal
        s = repr(v)
        if v == int(v) and abs(v) < 1e15:
            s = str(int(v))
        return s + "F"
    if isinstance(v, int):
        return str(v)
    return v

def reconstruct(ops):
    stack = []  # each entry: ('lit', value) or ('expr', str) or ('builder', str) or ('type', name)
    builder = None
    name = None
    pose = None
    result = None
    for op in ops:
        if op.startswith('aload '):
            # push receiver param0
            stack.append(('expr', 'param0'))
        elif op.startswith('ldc_w ') or op.startswith('ldc '):
            val = op.split(' ', 1)[1]
            if val.startswith('"'):
                stack.append(('expr', val))
            else:
                f = float(val)
                stack.append(('lit', f))
        elif op.startswith('bipush ') or op.startswith('sipush '):
            stack.append(('lit', int(op.split(' ',1)[1])))
        elif op.startswith('fconst_0'):
            stack.append(('lit', 0.0))
        elif op.startswith('fconst_1'):
            stack.append(('lit', 1.0))
        elif op.startswith('fconst_2'):
            stack.append(('lit', 2.0))
        elif op.startswith('new '):
            t = op.split(' ',1)[1]
            stack.append(('type', t))
        elif op.startswith('dup'):
            stack.append(stack[-1])
        elif op.startswith('invokespecial'):
            # CubeDeformation.<init>(F)V — stack was [.., type, type, 0.0]
            if 'CubeDeformation.<init>' in op:
                stack.pop()  # 0.0 arg
                stack.pop()  # objectref type
                stack.pop()  # remaining dup'd type
                stack.append(('expr', 'new CubeDeformation(0.0F)'))
        elif op.startswith('invokestatic'):
            if 'CubeListBuilder.create' in op:
                stack.append(('builder', 'CubeListBuilder.create()'))
            elif 'PartPose.offsetAndRotation' in op:
                # pop 6 floats, receiver none
                vals = [stack.pop()[1] for _ in range(6)][::-1]
                pose = 'PartPose.offsetAndRotation(' + ', '.join(fmt(v) for v in vals) + ')'
                stack.append(('expr', pose))
        elif op.startswith('invokevirtual'):
            if 'CubeListBuilder.texOffs' in op:
                b = stack.pop()  # int (arg2)
                a = stack.pop()  # int (arg1)
                rec = stack.pop()  # builder receiver
                if b[0] != 'lit' or a[0] != 'lit':
                    raise ValueError('texOffs non-lit ' + str(a)+str(b))
                nb = rec[1] + '.texOffs(' + str(int(a[1])) + ', ' + str(int(b[1])) + ')'
                stack.append(('builder', nb))
            elif 'CubeListBuilder.addBox' in op:
                deform = stack.pop()  # expr new CubeDeformation(0.0F) is on TOP
                args = []
                for _ in range(6):
                    args.append(stack.pop()[1])
                rec = stack.pop()  # builder receiver
                args = args[::-1]
                call = '.addBox(' + ', '.join(fmt(a) for a in args) + ', ' + deform[1] + ')'
                stack.append(('builder', rec[1] + call))
            elif 'CubeListBuilder.mirror' in op:
                rec = stack.pop()
                stack.append(('builder', rec[1] + '.mirror()'))
            elif 'PartDefinition.addOrReplaceChild' in op:
                pose_e = stack.pop()  # pose expr
                bldr = stack.pop()    # builder
                nm = stack.pop()      # name string
                rec = stack.pop()     # param0
                result = rec[1] + '.addOrReplaceChild(' + nm[1] + ', ' + bldr[1] + ', ' + pose_e[1] + ')'
                stack.append(('expr', result))
        elif op.startswith('astore'):
            pass
        elif op.startswith('areturn'):
            pass
        else:
            # ignore unknown
            pass
    return result

def extract_method(path, start, end):
    lines = open(path, encoding='utf-8').read().split('\n')
    # start/end are 1-based line numbers (inclusive of the signature & closing brace)
    body_lines = lines[start-1:end-1]  # 0-based, exclusive of closing brace line
    bc = [l for l in body_lines if re.match(r'\s*//\s+[0-9a-f]+:', l)]
    ops = parse_bytecode(bc)
    return ops

if __name__ == '__main__':
    path = 'src/main/java/net/dabicco/witherstormmod/entity/model/HugeAssBackModel.java'
    # make_bone239: signature line 1591, ends 5110. make_bone146: 9692, ends 13211.
    for (name, sig, end) in [('make_bone239', 1591, 5110), ('make_bone146', 9692, 13211)]:
        lines = open(path, encoding='utf-8').read().split('\n')
        # get param name from signature
        sigline = lines[sig-1]
        body = '\n'.join(lines[sig:end-1])
        bc = [l for l in body.split('\n') if re.match(r'\s*//\s+[0-9a-f]+:', l)]
        ops = parse_bytecode(bc)
        result = reconstruct(ops)
        print(f"=== {name} ===")
        print(sigline)
        print("   return " + result + ";")
        print("   }")
        print()
