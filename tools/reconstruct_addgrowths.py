#!/usr/bin/env python3
"""Reconstruct the two bytecode-only HunchbackGrowth.addGrowths3/addGrowths4 methods.

These methods build the 'growth9'..'growth14' parts under the hunch mass. Vineflower
OOM'd on them so their bodies are bytecode comments only, and because they're void
methods they compiled fine but did nothing at runtime -- which made the model
constructor's getChild("growthN") throw and black-screen the game. This script
interprets the bytecode (stack + locals) and emits real Java.
"""
import re
import sys

PATH = 'src/main/java/net/dabicco/witherstormmod/entity/model/HunchbackGrowth.java'


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


def reconstruct(ops):
    stack = []
    locals_map = {0: ('PART', 'param0')}
    emitted = []

    def pop():
        if not stack:
            raise RuntimeError('stack underflow')
        return stack.pop()

    for op in ops:
        parts = op.split(' ')
        mnemonic = parts[0]
        if mnemonic == 'wide':
            mnemonic = parts[1]
            operand = parts[2] if len(parts) > 2 else None
        else:
            operand = parts[1] if len(parts) > 1 else None

        if mnemonic == 'new':
            stack.append(('NEW', operand))
        elif mnemonic == 'dup':
            stack.append(stack[-1])
        elif mnemonic in ('fconst_0', 'fconst_1', 'fconst_2', 'fconst_3'):
            stack.append(('F', float(mnemonic[-1])))
        elif mnemonic in ('bipush', 'sipush'):
            stack.append(('I', int(operand)))
        elif mnemonic in ('ldc_w', 'ldc'):
            if operand.startswith('"'):
                stack.append(('STR', operand[1:-1]))
            else:
                stack.append(('F', float(operand)))
        elif mnemonic == 'invokespecial':
            owner = parts[1].split('.')[0]
            desc = parts[2]
            if '<init>' in parts[1]:
                args_desc = desc[1:desc.index(')')]
                for _ in range(len(args_desc)):
                    pop()
                pop()  # objectref
                assert stack and stack[-1][0] == 'NEW'
                simple = owner.split('/')[-1]
                if simple == 'CubeDeformation':
                    stack[-1] = ('OBJ', 'new CubeDeformation(0F)')
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
                stack.append(('POSE', 'PartPose.offsetAndRotation(' + ', '.join(fmt_float(a) for a in args) + ')'))
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
                assert deform[0] == 'OBJ', deform
                args = [pop()[1] for _ in range(6)][::-1]
                rec = pop()
                stack.append(('BUILDER', rec[1] + '.addBox(' + ', '.join(fmt_float(a) for a in args) + ', ' + deform[1] + ')'))
            elif 'CubeListBuilder.mirror' in owner_name:
                if desc.startswith('(Z)'):
                    b = pop(); rec = pop()
                    stack.append(('BUILDER', rec[1] + '.mirror(' + ('true' if b[1] else 'false') + ')'))
                else:
                    rec = pop()
                    stack.append(('BUILDER', rec[1] + '.mirror()'))
            elif 'PartDefinition.addOrReplaceChild' in owner_name:
                pose = pop(); bldr = pop(); name = pop(); parent = pop()
                assert name[0] == 'STR', name
                assert parent[0] == 'PART', parent
                expr = parent[1] + '.addOrReplaceChild("' + name[1] + '", ' + bldr[1] + ', ' + pose[1] + ')'
                stack.append(('PART', name[1]))
                emitted.append((name[1], expr))
            else:
                raise RuntimeError('unknown invokevirtual ' + op)
        elif mnemonic == 'aload':
            slot = int(operand)
            if slot not in locals_map:
                raise RuntimeError('aload of unset slot ' + str(slot) + ' in ' + op)
            stack.append(locals_map[slot])
        elif mnemonic == 'astore':
            slot = int(operand)
            val = pop()
            locals_map[slot] = val
        elif mnemonic in ('return', 'areturn'):
            pass
        else:
            raise RuntimeError('unhandled opcode ' + mnemonic + ' in ' + op)

    return emitted


def method_range(start_line, end_line):
    lines = open(PATH, encoding='utf-8').read().split('\n')
    return lines[start_line:end_line - 1]


if __name__ == '__main__':
    # addGrowths3: sig 1603, ends 10518. addGrowths4: sig 10520, ends 18093.
    ranges = [(1603, 10518, 'addGrowths3'), (10520, 18093, 'addGrowths4')]
    for (sig, end, mname) in ranges:
        body = method_range(sig, end)
        ops = parse_ops(body)
        try:
            emitted = reconstruct(ops)
        except RuntimeError as e:
            print(mname + ': ERROR ' + str(e), file=sys.stderr)
            continue
        print('   private static void ' + mname + '(PartDefinition param0) {')
        for (name, expr) in emitted:
            print('      ' + expr + ';')
        print('   }')
        print()
