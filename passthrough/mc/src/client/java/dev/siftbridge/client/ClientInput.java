package dev.siftbridge.client;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

/** Host → Minecraft input injection, executed on the client thread via {@code Minecraft.execute}. */
final class ClientInput {
	private ClientInput() {
	}

	static void key(final JsonObject m) {
		Minecraft mc = Minecraft.getInstance();
		boolean down = m.get("down").getAsBoolean();
		switch (m.get("k").getAsString()) {
			case "use" -> mc.options.keyUse.setDown(down);
			case "attack" -> mc.options.keyAttack.setDown(down);
			case "pick" -> mc.options.keyPickItem.setDown(down);
			case "drop" -> mc.options.keyDrop.setDown(down);
			case "inventory" -> {
				if (down) {
					mc.options.keyInventory.consumeClick();
				}
			}
			case "swap" -> {
				if (down && mc.player != null) {
					mc.player.getInventory().swapPaint(0.0);
				}
			}
			// the host's ESC mirrors ours: it is the distributed kill switch
			case "escape" -> {
				if (down) {
					SiftLink.shutdown();
				}
			}
			default -> {
			}
		}
	}

	static void misc(final JsonObject m) {
		Minecraft mc = Minecraft.getInstance();
		switch (m.get("t").getAsString()) {
			case "slot" -> {
				if (mc.player != null) {
					mc.player.getInventory().selected = m.get("n").getAsInt() & 7;
				}
			}
			case "scroll" -> {
				if (mc.player != null) {
					mc.player.getInventory().swapPaint(-m.get("d").getAsInt());
				}
			}
			case "hud" -> mc.options.hideGui = m.get("hidden").getAsBoolean();
			default -> {
			}
		}
	}
}
