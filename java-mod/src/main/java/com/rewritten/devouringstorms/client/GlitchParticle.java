package com.rewritten.devouringstorms.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * THE GLITCH. A two-frame error in reality: a square of shrieking magenta/cyan static
 * that lives for 10 ticks and is never seen the same way twice.
 */
public class GlitchParticle extends SingleQuadParticle {

    protected GlitchParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, sprites);
        this.lifetime = 10;
        this.quadSize = 0.2f + this.random.nextFloat() * 0.25f;
        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();
        // strobe between corrupted hues every tick — that IS the particle
        int pick = this.random.nextInt(4);
        switch (pick) {
            case 0 -> { this.rCol = 1.0f; this.gCol = 0.2f; this.bCol = 1.0f; }
            case 1 -> { this.rCol = 0.2f; this.gCol = 1.0f; this.bCol = 1.0f; }
            case 2 -> { this.rCol = 0.7f; this.gCol = 0.3f; this.bCol = 1.0f; }
            default -> { this.rCol = 0.1f; this.gCol = 0.1f; this.bCol = 0.1f; }
        }
        this.alpha = 0.4f + this.random.nextFloat() * 0.6f;
        this.yd += 0.002;
    }

    @Override
    protected ParticleRenderType getLayer() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /** Matches Fabric's PendingParticleFactory contract: (SpriteSet) -> ParticleProvider. */
    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed,
                                       net.minecraft.util.RandomSource random) {
            GlitchParticle particle = new GlitchParticle(level, x, y, z, this.sprites);
            particle.pickSprite(this.sprites);
            particle.xd = xSpeed;
            particle.yd = ySpeed;
            particle.zd = zSpeed;
            return particle;
        }
    }
}
