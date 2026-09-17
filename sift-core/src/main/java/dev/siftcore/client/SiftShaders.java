package dev.siftcore.client;

import dev.siftcore.SiftCore;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.Uniform;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

/** Handles the three small procedural programs used by the first visual slice. */
public final class SiftShaders {
    public static ShaderProgram SKY;
    public static ShaderProgram RIFT;
    public static ShaderProgram FINAL;

    private SiftShaders() {
    }

    /**
     * Registers each program independently. A bad optional shader must not abort
     * the client resource reload: the sky renderer has a vanilla position-color
     * fallback and the other passes can simply remain absent.
     */
    public static void register(CoreShaderRegistrationCallback.RegistrationContext context) {
        registerOne(context, SiftCore.id("sky"), VertexFormats.POSITION, shader -> SKY = shader);
        registerOne(context, SiftCore.id("rift"), VertexFormats.POSITION_TEXTURE_COLOR, shader -> RIFT = shader);
        registerOne(context, SiftCore.id("final"), VertexFormats.POSITION_TEXTURE_COLOR, shader -> FINAL = shader);
    }

    private static void registerOne(
            CoreShaderRegistrationCallback.RegistrationContext context,
            Identifier id,
            VertexFormat format,
            Consumer<ShaderProgram> consumer
    ) {
        try {
            context.register(id, format, consumer);
        } catch (Exception exception) {
            SiftCore.LOGGER.error("Sift-Core shader {} is unavailable; continuing with safe fallback", id, exception);
        }
    }

    public static void set(ShaderProgram shader, String name, float value) {
        Uniform uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(value);
        }
    }

    public static void set(ShaderProgram shader, String name, float first, float second) {
        Uniform uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(first, second);
        }
    }

    public static void setScreenSize(ShaderProgram shader) {
        MinecraftClient client = MinecraftClient.getInstance();
        set(
                shader,
                "ScreenSize",
                (float) client.getWindow().getFramebufferWidth(),
                (float) client.getWindow().getFramebufferHeight()
        );
    }
}
