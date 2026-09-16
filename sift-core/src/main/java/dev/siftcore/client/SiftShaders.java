package dev.siftcore.client;

import dev.siftcore.SiftCore;
import java.io.IOException;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.Uniform;
import net.minecraft.client.render.VertexFormats;

/** Handles the three small procedural programs used by the first visual slice. */
public final class SiftShaders {
    public static ShaderProgram SKY;
    public static ShaderProgram RIFT;
    public static ShaderProgram FINAL;

    private SiftShaders() {
    }

    public static void register(CoreShaderRegistrationCallback.RegistrationContext context) throws IOException {
        context.register(SiftCore.id("sky"), VertexFormats.POSITION, shader -> SKY = shader);
        context.register(SiftCore.id("rift"), VertexFormats.POSITION_TEXTURE_COLOR, shader -> RIFT = shader);
        context.register(SiftCore.id("final"), VertexFormats.POSITION_TEXTURE_COLOR, shader -> FINAL = shader);
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
