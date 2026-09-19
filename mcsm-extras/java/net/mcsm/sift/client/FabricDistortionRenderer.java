package net.mcsm.sift.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Fabric of Reality distortion - world herds inwards creating distortion
 * Gravitational scratches, jumping stretches fabric and warps like walking on gigantic trampoline
 * Footless look, radical, allergy storage under met - creative with radical
 * 
 * When player jumps on fabric, world distorts inwards, stretch effect, really cool effect
 * Similar to images when player whole world starts to herd inwards creating distortion
 */
public final class FabricDistortionRenderer {

    public static final FabricDistortionRenderer INSTANCE = new FabricDistortionRenderer();

    private static class Distortion {
        BlockPos pos;
        float intensity;
        float age;
        float maxAge = 40f; // 2 seconds
        float radius = 8f;

        Distortion(BlockPos pos, float intensity) {
            this.pos = pos;
            this.intensity = intensity;
        }

        boolean tick() {
            age++;
            return age < maxAge;
        }

        float getProgress() {
            return age / maxAge;
        }

        float getCurrentIntensity() {
            float progress = getProgress();
            // Ease out - strong at start, fades
            float fade = 1f - progress;
            fade = fade * fade; // quadratic
            return intensity * fade;
        }
    }

    private final List<Distortion> distortions = new ArrayList<>();
    private float globalWarp = 0f;

    private FabricDistortionRenderer() {}

    public static void triggerDistortion(BlockPos pos, float intensity) {
        INSTANCE.distortions.add(new Distortion(pos, Mth.clamp(intensity, 0.2f, 3f)));
        // Also trigger screen shake / FOV warp
        INSTANCE.globalWarp = Math.max(INSTANCE.globalWarp, intensity * 0.3f);
    }

    public void tick() {
        Iterator<Distortion> it = distortions.iterator();
        while (it.hasNext()) {
            Distortion d = it.next();
            if (!d.tick()) {
                it.remove();
            }
        }
        globalWarp = Mth.lerp(0.1f, globalWarp, 0f);
    }

    /**
     * Called from final.fsh uniform injection - provides distortion amount for shader
     * World distorts inwards - amazing exploring shameless effect
     */
    public float getDistortionAt(double x, double y, double z) {
        float total = 0f;
        for (Distortion d : distortions) {
            double dx = x - (d.pos.getX() + 0.5);
            double dy = y - (d.pos.getY() + 0.5);
            double dz = z - (d.pos.getZ() + 0.5);
            double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
            
            if (dist < d.radius) {
                float factor = 1f - (float)(dist / d.radius);
                factor = factor * factor; // falloff
                total += d.getCurrentIntensity() * factor;
            }
        }
        return Mth.clamp(total, 0f, 2f);
    }

    public float getGlobalWarp() {
        return globalWarp;
    }

    /**
     * Renders screen-space distortion - world herds inwards
     * Called from world renderer or overlay
     */
    public void renderDistortionOverlay(PoseStack poseStack, float partialTicks) {
        if (distortions.isEmpty() && globalWarp < 0.01f) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Simple screen warp overlay - would be more complex with shader
        // For now, render vignette that pulses with distortion
        float warp = getGlobalWarp();
        if (warp < 0.01f) return;

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();

        // Radial distortion vignette - world herds inwards
        buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        
        float centerX = mc.getWindow().getGuiScaledWidth() / 2f;
        float centerY = mc.getWindow().getGuiScaledHeight() / 2f;
        
        // Center - transparent
        buf.vertex(matrix, centerX, centerY, 0).color(0.5f, 0.2f, 1f, 0f).endVertex();
        
        // Outer ring - purple tint with warp intensity
        int segments = 16;
        for (int i = 0; i <= segments; i++) {
            float angle = (float)i / segments * Mth.TWO_PI;
            float x = centerX + Mth.cos(angle) * centerX * 1.5f;
            float y = centerY + Mth.sin(angle) * centerY * 1.5f;
            float a = warp * 0.3f;
            // Cosmic purple
            buf.vertex(matrix, x, y, 0).color(0.6f, 0.2f, 1f, a).endVertex();
        }
        tess.end();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    /**
     * Calculates trampoline physics - stretch effect
     * When you jump on fabric, it stretches and warps like trampoline
     */
    public static float calculateTrampolineStretch(BlockPos fabricPos, BlockPos playerPos, float jumpVelocity) {
        double dist = Math.sqrt(fabricPos.distSqr(playerPos));
        float stretch = (float)(1.0 / (1.0 + dist * 0.1)) * jumpVelocity * 0.5f;
        return Mth.clamp(stretch, 0f, 1.5f);
    }

    public void clear() {
        distortions.clear();
        globalWarp = 0f;
    }
}
