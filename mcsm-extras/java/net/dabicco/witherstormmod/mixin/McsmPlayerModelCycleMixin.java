package net.dabicco.witherstormmod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Build #374: one-key player model version cycle (F8).
 *
 * The three EMF player model versions (Telltale Hero / Scout / Storm
 * Guardian) are selected per player through the scoreboard objective
 * "mcsmplyr" (0/absent = vanilla model, 1/2/3 = the versions). This key
 * walks that score: 0 -> 1 -> 2 -> 3 -> 0, sending the scoreboard commands
 * client-side through the real 26.2 command channel
 * (ServerboundChatCommandPacket — the class 26.2 moved commands to, now
 * dump-verified in ci/api/client.txt), and announces the current version
 * in chat.
 *
 * Same proven hotkey plumbing as the Shift+C / Shift+A console hotkey
 * (Minecraft.tick TAIL + GLFW glfwGetKey poll + edge detection + debounce),
 * in-game only.
 */
@Mixin(Minecraft.class)
public abstract class McsmPlayerModelCycleMixin {

    @Unique private static boolean mcsm$modelKeyWasDown = false;
    @Unique private static long mcsm$modelKeyLastMs = 0L;
    @Unique private static int mcsm$modelVersion = 0;

    private static final String[] NAMES = {
            "vanilla model", "Telltale Hero (1)", "Scout (2)", "Storm Guardian (3)"
    };

    @Inject(method = "tick", at = @At("TAIL"))
    private void mcsm$cyclePlayerModel(CallbackInfo ci) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null || mc.level == null) {
                return;
            }
            boolean down = mcsm$isF8Down(mc);
            if (!down) {
                mcsm$modelKeyWasDown = false;
                return;
            }
            if (mcsm$modelKeyWasDown) {
                return;
            }
            mcsm$modelKeyWasDown = true;

            long now = System.currentTimeMillis();
            if (now - mcsm$modelKeyLastMs < 350L) {
                return;
            }
            mcsm$modelKeyLastMs = now;

            mcsm$modelVersion = (mcsm$modelVersion + 1) % NAMES.length;
            LocalPlayer player = mc.player;
            mcsm$sendCommand(player, "/scoreboard objectives add mcsmplyr dummy");
            mcsm$sendCommand(player, "/scoreboard players set @s mcsmplyr " + mcsm$modelVersion);
            mcsm$chat(player, "[ds] Player model: " + NAMES[mcsm$modelVersion]);
            System.out.println("[MCSM] F8 cycled player model -> " + NAMES[mcsm$modelVersion]);
        } catch (Throwable t) {
            System.err.println("[MCSM] Player model cycle failed: " + t);
        }
    }

    @Unique
    private static void mcsm$sendCommand(LocalPlayer player, String command) {
        try {
            // 26.2: commands are ServerboundChatCommandPacket over the
            // client connection (LocalPlayer.sendCommand no longer exists).
            player.connection.send(new ServerboundChatCommandPacket(command));
        } catch (Throwable ignored) {
            // Fall through: the manual /scoreboard commands still work.
        }
    }

    @Unique
    private static void mcsm$chat(LocalPlayer player, String line) {
        try {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(line));
        } catch (Throwable ignored) {
        }
    }

    /**
     * GLFW polled by reflection — LWJGL is intentionally NOT on the compile
     * classpath (the same constraint the Shift+C console hotkey works under).
     */
    @Unique
    private static boolean mcsm$isF8Down(Minecraft mc) {
        try {
            long handle = mcsm$windowHandle(mc);
            if (handle == 0L) {
                return false;
            }
            Class<?> glfw = Class.forName("org.lwjgl.glfw.GLFW");
            java.lang.reflect.Method getKey = glfw.getMethod("glfwGetKey", long.class, int.class);
            int press = ((Number) glfw.getField("GLFW_PRESS").get(null)).intValue();
            int f8 = ((Number) glfw.getField("GLFW_KEY_F8").get(null)).intValue();
            return ((Number) getKey.invoke(null, handle, f8)).intValue() == press;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Unique
    private static long mcsm$windowHandle(Minecraft mc) {
        try {
            Object windowObj = mc.getWindow();
            if (windowObj == null) {
                return 0L;
            }
            long handle = 0L;
            for (var m : windowObj.getClass().getMethods()) {
                if (m.getParameterCount() == 0 && m.getName().equals("getWindow")) {
                    Object v = m.invoke(windowObj);
                    if (v instanceof Number) {
                        handle = ((Number) v).longValue();
                    }
                    break;
                }
            }
            return handle;
        } catch (Throwable ignored) {
            return 0L;
        }
    }
}
