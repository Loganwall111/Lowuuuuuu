package net.mcsm.extras;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BUILD #444 -- the shared build queue.
 *
 * <p>The district generator, the adams field and now the mazes and the server
 * rooms all do the same job: work out a few thousand blocks of a structure,
 * write a slice of it per tick so a player never sees a stall, and do something
 * once the last block is down (fill the crates, wake what lives there). This is
 * that job, in one place.
 *
 * <p>A {@link Planner} collects a structure into flat int arrays -- one block per
 * {@code x, y, z, state} quad, one container or marker per {@code x, y, z, tag}
 * quad -- and a {@link Queue} writes them at a fixed budget per tick. Everything
 * is plain arrays on purpose: this runs inside the level tick, and an allocation
 * storm inside a tick is a stall the player can see.
 *
 * <p>The tag on a {@link Plan#spots} quad means whatever the system that planned
 * it says it means: the maze reads a chest, the server room reads a lamp it will
 * later blink, adams reads what it is keeping in the chamber. Nothing here
 * interprets it.
 */
public final class McsmBuildQueue {

    /** A planned structure, being filled in. */
    public static final class Planner {
        private int[] ops;
        private int opsCursor;
        private int[] crates = new int[0];
        private int cratesCursor;
        private int[] spots = new int[0];
        private int spotsCursor;

        public Planner(int blockCapacity) {
            this.ops = new int[Math.max(16, blockCapacity) * 4];
        }

        /** One block. Silently skipped past capacity: a rail, not a crash. */
        public void put(int x, int y, int z, int state) {
            if (opsCursor + 4 > ops.length) {
                return;
            }
            ops[opsCursor++] = x;
            ops[opsCursor++] = y;
            ops[opsCursor++] = z;
            ops[opsCursor++] = state;
        }

        public void box(int x0, int y0, int z0, int x1, int y1, int z1, int state) {
            for (int y = y0; y <= y1; y++) {
                for (int x = x0; x <= x1; x++) {
                    for (int z = z0; z <= z1; z++) {
                        put(x, y, z, state);
                    }
                }
            }
        }

        /** A container to fill once the structure is standing. */
        public void container(int x, int y, int z, int tag) {
            crates = grow(crates, cratesCursor);
            if (cratesCursor + 4 > crates.length) {
                return;
            }
            crates[cratesCursor++] = x;
            crates[cratesCursor++] = y;
            crates[cratesCursor++] = z;
            crates[cratesCursor++] = tag;
        }

        /** Anything else the system wants to remember about this structure. */
        public void spot(int x, int y, int z, int tag) {
            spots = grow(spots, spotsCursor);
            if (spotsCursor + 4 > spots.length) {
                return;
            }
            spots[spotsCursor++] = x;
            spots[spotsCursor++] = y;
            spots[spotsCursor++] = z;
            spots[spotsCursor++] = tag;
        }

        private int[] grow(int[] array, int cursor) {
            if (cursor + 4 <= array.length) {
                return array;
            }
            int[] next = new int[Math.max(8, array.length * 2)];
            System.arraycopy(array, 0, next, 0, cursor);
            return next;
        }

        public int blocks() {
            return opsCursor / 4;
        }

        public Plan plan(long key) {
            return new Plan(key,
                    Arrays.copyOf(ops, opsCursor),
                    Arrays.copyOf(crates, cratesCursor),
                    Arrays.copyOf(spots, spotsCursor));
        }
    }

    /** One planned structure: the blocks, then the things inside it. */
    public static final class Plan {
        public final long key;
        public final int[] ops;
        public final int[] containers;
        public final int[] spots;
        private int cursor;

        Plan(long key, int[] ops, int[] containers, int[] spots) {
            this.key = key;
            this.ops = ops;
            this.containers = containers;
            this.spots = spots;
        }

        public boolean done() {
            return cursor >= ops.length;
        }

        public int remaining() {
            return Math.max(0, (ops.length - cursor) / 4);
        }
    }

    /** The per-level queue: plan in, blocks out, one callback at the end. */
    public static final class Queue {
        private final ConcurrentLinkedQueue<Plan> plans = new ConcurrentLinkedQueue<>();
        private final int maxPending;

        public Queue(int maxPending) {
            this.maxPending = maxPending;
        }

        public boolean full() {
            return plans.size() >= maxPending;
        }

        public void add(Plan plan) {
            plans.add(plan);
        }

        public int size() {
            return plans.size();
        }

        /**
         * Write for up to {@code budget} blocks and, when the head plan finishes,
         * hand it to {@code onDone}. Stops early on the first completed plan, so a
         * structure's own finishing touches (its loot, its lights) never land in
         * the same tick as the last block that needed them.
         */
        public void pump(ServerLevel level, int budget, BlockState[] palette,
                         Consumer<Plan> onDone) {
            Plan head = plans.peek();
            if (head == null) {
                return;
            }
            int left = budget;
            while (left-- > 0 && !head.done()) {
                int i = head.cursor;
                head.cursor += 4;
                int state = head.ops[i + 3];
                if (state < 0 || state >= palette.length) {
                    continue;
                }
                BlockState block = palette[state];
                if (block != null) {
                    level.setBlock(new BlockPos(head.ops[i], head.ops[i + 1], head.ops[i + 2]),
                            block, 2);
                }
            }
            if (head.done()) {
                plans.poll();
                onDone.accept(head);
            }
        }
    }
}
