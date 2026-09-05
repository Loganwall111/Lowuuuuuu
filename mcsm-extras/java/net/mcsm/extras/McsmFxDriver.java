package net.mcsm.extras;

import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * MCSM 1.9.100 -- the visible half of the Story Mode sequence.
 *
 * The mod's own shockwave and death blast DO fire (whocalls.py shows
 * die() -> deathBlast and addSubGrowth() -> phaseUpShockwave), so this does not
 * replace them -- it adds the signature MCSM staging around them, on the server,
 * where every effect is a plain vanilla particle so there is nothing to
 * desync and nothing for the client to opt into:
 *
 *   - phase 4 rise and phase 7 rise: an expanding ring of dust, ground to sky
 *   - death: a supernova of coloured rings, then a white flash
 *   - during phase 5.5+: purple motes and electric sparks under the storm
 *   - while the storm sweeps: dust waves off the ground
 *   - on death ("the tear closes"): nearby players are healed and the sequence
 *     is marked ended -- MCSM reads this as recovery, not as a loss
 *
 * Every branch is gated by McsmExtrasConfig so the control panel can turn each
 * piece off, and the whole thing is wrapped in a catch: a particle can never
 * break a tick.
 */
public final class McsmFxDriver {

    /** Per-storm scratch: [0] = last seen phase, [1] = 1 once death fx fired. */
    private static final Map<UUID, double[]> STATE = new HashMap<>();

    /** Supernova ring colours, in MCSM order. */
    private static final float[][] RINGS = {
        {0.62f, 0.22f, 0.95f},   // purple
        {1.00f, 0.42f, 0.78f},   // pink
        {0.28f, 0.48f, 1.00f},   // blue
        {1.00f, 0.58f, 0.16f},   // orange
        {0.36f, 0.95f, 0.42f},   // green
        {1.00f, 0.92f, 0.35f},   // yellow
    };

    public static void tick(WitherStormEntity self, Level level, long gt) {
        if (self == null || level == null || level.isClientSide()) {
            return;
        }
        if (!(level instanceof ServerLevel srv)) {
            return;
        }
        try {
            McsmExtrasConfig.load();
            UUID id = self.getUUID();
            double[] st = STATE.computeIfAbsent(id, k -> new double[]{-1.0, 0.0});
            double phase = self.getPhase();

            // ---- phase rise shockwaves at 4 and 7 --------------------------
            double last = st[0];
            if (last >= 0.0) {
                if (last < 4.0 && phase >= 4.0) riseShockwave(srv, self, 4);
                if (last < 7.0 && phase >= 7.0) riseShockwave(srv, self, 7);
            }
            st[0] = phase;

            // ---- death: supernova rings -> flash -> recovery ---------------
            if (st[1] == 0.0 && self.isDeadOrDying()) {
                st[1] = 1.0;
                if (McsmExtrasConfig.deathCinematic) {
                    supernova(srv, self, gt);
                }
                if (McsmExtrasConfig.realityTear) {
                    recover(srv, self);
                }
            }
            if (st[1] > 0.0 && !self.isDeadOrDying()) {
                st[1] = 0.0;     // healed back (creative/command): allow a replay
            }

            // ---- phase 5.5+: purple sky motes + sparks ---------------------
            if (McsmExtrasConfig.purpleSky && phase >= 5.5 && gt % 4L == 0L) {
                purpleMotes(srv, self, gt);
            }

            // ---- dust waves while it sweeps the ground ---------------------
            if (McsmExtrasConfig.dustWaves && gt % 6L == 0L) {
                dustWave(srv, self);
            }

            // ---- smoke screen: heavy smoke pooled under the body -----------
            if (McsmExtrasConfig.smokeScreen && gt % 3L == 0L) {
                smokePool(srv, self);
            }

            // ---- the command wire: core to ground anchor, with a pulse -----
            if (McsmExtrasConfig.commandWire) {
                commandWire(srv, self, gt);
            }

            // ---- briefing: told once, the first time they get close -------
            if (McsmExtrasConfig.mcsmInstructions && gt % 20L == 0L) {
                briefNearby(srv, self);
            }
        } catch (Throwable ignored) {
            // Never let a particle break a tick.
        }
    }

    // MCSM 1.9.101 -- 26.2's ServerLevel.sendParticles takes PARTICLE OPTIONS,
    // not ParticleTypes, and DustParticleOptions is (int packed RGB, float
    // scale), not (Vector3f, float). Every effect here is therefore plain
    // dust with a colour: the closest deterministic spelling of the same
    // staging on the API the 26.2 client actually has (verified from the
    // runner's javap dump, ci/api/level.txt).
    private static DustParticleOptions dust(int rgb, float scale) {
        return new DustParticleOptions(rgb, scale);
    }

    private static int pack(float r, float g, float b) {
        int ri = (int) (Math.max(0.0f, Math.min(1.0f, r)) * 255.0f);
        int gi = (int) (Math.max(0.0f, Math.min(1.0f, g)) * 255.0f);
        int bi = (int) (Math.max(0.0f, Math.min(1.0f, b)) * 255.0f);
        return (ri << 16) | (gi << 8) | bi;
    }

    /** Direct mixin hook: phase-up shockwave fired from addSubGrowth(), not only from tick polling. */
    public static void phaseShockwave(WitherStormEntity self, Level level, int phase) {
        if (self == null || level == null || level.isClientSide()) return;
        McsmExtrasConfig.load();
        if (!McsmExtrasConfig.enableRiseFx && !McsmExtrasConfig.supernovaRings) return;
        if (!(level instanceof ServerLevel srv)) return;
        try {
            riseShockwave(srv, self, phase);
        } catch (Throwable ignored) {
        }
    }

    /** Direct mixin hook: death blast must happen while die() still has a live entity position. */
    public static void deathCinematic(WitherStormEntity self, Level level) {
        if (self == null || level == null || level.isClientSide()) return;
        if (!(level instanceof ServerLevel srv)) return;
        try {
            McsmExtrasConfig.load();
            long gt = level.getGameTime();
            if (McsmExtrasConfig.deathCinematic || McsmExtrasConfig.supernovaRings) {
                supernova(srv, self, gt);
            }
            if (McsmExtrasConfig.realityTear) {
                recover(srv, self);
            }
            STATE.computeIfAbsent(self.getUUID(), k -> new double[]{self.getPhase(), 0.0})[1] = 1.0;
        } catch (Throwable ignored) {
        }
    }

    /** Expanding dust ring, ground to sky, plus the grey smoke of the impact. */
    private static void riseShockwave(ServerLevel srv, WitherStormEntity self, int phase) {
        double x = self.getX(), y = self.getY(), z = self.getZ();
        float[] c = (phase >= 7) ? new float[]{1.0f, 0.45f, 0.80f}
                                 : new float[]{0.65f, 0.30f, 0.95f};
        DustParticleOptions puff = dust(pack(c[0], c[1], c[2]), 2.4f);
        for (int ring = 0; ring < 3; ring++) {
            double r = 6.0 + ring * 7.0;
            for (int i = 0; i < 48; i++) {
                double a = (i / 48.0) * Math.PI * 2.0;
                double px = x + Math.cos(a) * r;
                double pz = z + Math.sin(a) * r;
                srv.sendParticles(puff, px, y - 1.0 + ring * 0.6, pz, 1, 0.0, 0.35, 0.0, 0.0);
                if (i % 6 == 0) {
                    srv.sendParticles(dust(0x9aa0a6, 2.2f), px, y, pz, 2, 1.2, 1.0, 1.2, 0.03);
                    srv.sendParticles(dust(0xb8b8b8, 1.8f), px, y + 0.5, pz, 1, 1.0, 0.8, 1.0, 0.04);
                }
            }
        }
        srv.sendParticles(dust(0xd8e6ff, 0.7f), x, y + 2.0, z, 60, 6.0, 3.0, 6.0, 0.3);
    }

    /** Six expanding rings, one per MCSM colour, then a white flash. */
    private static void supernova(ServerLevel srv, WitherStormEntity self, long gt) {
        double x = self.getX(), y = self.getY(), z = self.getZ();
        for (int k = 0; k < RINGS.length; k++) {
            DustParticleOptions puff = dust(pack(RINGS[k][0], RINGS[k][1], RINGS[k][2]), 3.0f);
            double r = 4.0 + k * 3.4;
            double yy = y + k * 1.1;
            for (int i = 0; i < 64; i++) {
                double a = (i / 64.0) * Math.PI * 2.0 + (k * 0.13);
                srv.sendParticles(puff,
                    x + Math.cos(a) * r, yy, z + Math.sin(a) * r,
                    1, 0.0, 0.6, 0.0, 0.0);
            }
        }
        srv.sendParticles(dust(0xffffff, 4.0f), x, y + 3.0, z, 2, 0.0, 0.0, 0.0, 0.0);
        srv.sendParticles(dust(0xd8e6ff, 0.7f), x, y + 3.0, z, 120, 8.0, 5.0, 8.0, 0.4);
        srv.sendParticles(dust(0x9aa0a6, 2.2f), x, y + 2.0, z, 80, 7.0, 4.0, 7.0, 0.12);
    }

    /** The tear closes: MCSM reads survival as recovery, so heal and cleanse. */
    private static void recover(ServerLevel srv, WitherStormEntity self) {
        double x = self.getX(), y = self.getY(), z = self.getZ();
        for (ServerPlayer sp : srv.getPlayers(p -> p.isAlive()
                && p.distanceToSqr(x, y, z) < 96.0 * 96.0)) {
            sp.heal(12.0f);
            srv.sendParticles(dust(0x7ddf64, 0.8f), sp.getX(), sp.getY() + 1.0, sp.getZ(),
                              24, 0.8, 0.8, 0.8, 0.2);
            srv.sendParticles(dust(0xff4d6d, 1.0f), sp.getX(), sp.getY() + 1.6, sp.getZ(),
                              8, 0.5, 0.5, 0.5, 0.1);
        }
        srv.sendParticles(dust(0xffd76a, 1.2f), x, y + 2.0, z, 160, 4.0, 3.0, 4.0, 0.35);
    }

    private static void purpleMotes(ServerLevel srv, WitherStormEntity self, long gt) {
        double x = self.getX(), y = self.getY(), z = self.getZ();
        double r = self.getBoundingBox().getXsize() * 0.5 + 14.0;
        long seed = gt * 2654435761L;
        for (int i = 0; i < 10; i++) {
            double a = ((seed >>> (i * 3)) % 360) / 360.0 * Math.PI * 2.0;
            double rr = r + ((seed >>> (i * 5)) % 24);
            double py = y + ((seed >>> (i * 7)) % 40) - 12.0;
            srv.sendParticles(dust(0x9d6bff, 1.4f), x + Math.cos(a) * rr, py, z + Math.sin(a) * rr,
                              3, 0.6, 1.4, 0.6, 0.02);
        }
        if (gt % 40L == 0L) {
            srv.sendParticles(dust(0xd8e6ff, 0.7f), x, y + 18.0, z, 40, 10.0, 6.0, 10.0, 0.35);
        }
    }

    private static void dustWave(ServerLevel srv, WitherStormEntity self) {
        double x = self.getX(), y = self.getY(), z = self.getZ();
        double r = self.getBoundingBox().getXsize() * 0.5 + 6.0;
        double py = Math.max(self.getBoundingBox().minY, y - 22.0);
        for (int i = 0; i < 8; i++) {
            double a = (i / 8.0) * Math.PI * 2.0;
            srv.sendParticles(dust(0x9aa0a6, 2.2f), x + Math.cos(a) * r, py, z + Math.sin(a) * r,
                              3, 1.6, 0.7, 1.6, 0.05);
        }
    }

    private static void smokePool(ServerLevel srv, WitherStormEntity self) {
        double x = self.getX(), y = self.getY(), z = self.getZ();
        double r = self.getBoundingBox().getXsize() * 0.55;
        double py = Math.max(self.getBoundingBox().minY, y - 30.0) + 1.0;
        srv.sendParticles(dust(0x5a5a5a, 4.0f), x, py, z, 6, r * 0.7, 1.2, r * 0.7, 0.02);
        srv.sendParticles(dust(0x777777, 3.0f), x, py + 2.0, z, 8, r * 0.9, 2.0, r * 0.9, 0.03);
    }

    /**
     * The wire: a taut line of end-rod motes from the storm's core down to its
     * ground anchor, a pulse running down it, and a sparking node where it
     * lands. The anchor is the bounding-box floor -- already used by the dust
     * wave, so it needs no heightmap lookup.
     */
    private static void commandWire(ServerLevel srv, WitherStormEntity self, long gt) {
        double x = self.getX(), y = self.getY(), z = self.getZ();
        double gy = self.getBoundingBox().minY + 0.5;
        double len = y - gy;
        if (len < 2.0) {
            return;
        }
        int steps = 26;
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            srv.sendParticles(dust(0xbfffe8, 0.9f), x, y - len * t, z, 1, 0.03, 0.0, 0.03, 0.0);
        }
        double pt = (gt % 30L) / 30.0;
        srv.sendParticles(dust(0xffb347, 1.2f), x, y - len * pt, z, 2, 0.05, 0.05, 0.05, 0.0);
        if (gt % 20L == 0L) {
            for (int i = 0; i < 16; i++) {
                double a = (i / 16.0) * Math.PI * 2.0;
                srv.sendParticles(dust(0xd8e6ff, 0.7f),
                    x + Math.cos(a) * 1.6, gy + 0.3, z + Math.sin(a) * 1.6,
                    1, 0.1, 0.2, 0.1, 0.02);
            }
        }
    }

    /** Brief anyone who has come within 120 blocks and not been told yet. */
    private static void briefNearby(ServerLevel srv, WitherStormEntity self) {
        double x = self.getX(), y = self.getY(), z = self.getZ();
        for (ServerPlayer sp : srv.getPlayers(p -> p.isAlive()
                && p.distanceToSqr(x, y, z) < 120.0 * 120.0)) {
            McsmStory.brief(sp);
        }
    }

    /** Forget a storm (entity removed) so the map cannot grow without bound. */
    public static void forget(UUID id) {
        STATE.remove(id);
    }

    private McsmFxDriver() {}
}
