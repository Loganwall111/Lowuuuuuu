package dev.siftbridge.client;

import dev.siftbridge.SiftBridgeMod;
import dev.siftbridge.SiftEvents;
import dev.siftbridge.client.mixin.MinecraftMixin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

/**
 * Client entrypoint: registers the F8 kill-switch key, binds server-side events to the socket,
 * and launches the link. No world files, registries or saves are ever touched — the whole bridge
 * is sockets, RAM and daemon threads.
 */
public final class SiftBridgeClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		KeyMapping disconnect = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.siftbridge.disconnect",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_F8,
			"category.siftbridge"
		));
		MinecraftMixin.siftbridge$setDisconnectKey(disconnect);

		SiftEvents.bind(SiftLink::sendEvent);
		SiftLink.launch();
		SiftBridgeMod.LOG.info("SIFT Bridge ready — F8 disconnects, ESC on the host side disconnects both");
	}
}
