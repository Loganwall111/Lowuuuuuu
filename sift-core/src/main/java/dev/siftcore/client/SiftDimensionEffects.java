package dev.siftcore.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public final class SiftDimensionEffects extends DimensionEffects {
    private static final float[] FOG_RGBA = {0.108f, 0.120f, 0.086f, 1.0f};

    public SiftDimensionEffects() {
        super(192.0f, false, SkyType.NORMAL, true, false);
    }

    @Override
    public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
        double shimmer = 0.94D + Math.cos(sunHeight * Math.PI * 2.0D) * 0.03D;
        return new Vec3d(
                FOG_RGBA[0] * shimmer,
                FOG_RGBA[1] * shimmer,
                FOG_RGBA[2] * shimmer
        );
    }

    @Override
    public boolean useThickFog(int camX, int camY) {
        return true;
    }

    @Override
    public float[] getFogColorOverride(float skyAngle, float tickDelta) {
        return FOG_RGBA.clone();
    }
}
