package dev.siftbridge.client.mixin;

import dev.siftbridge.client.SiftLink;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The kill switch. ESC semantics are distributed: the host's ESC arrives as
 * {@code {"t":"key","k":"escape","down":true}} and runs {@link SiftLink#shutdown()}.
 * Locally, F8 (registered by the entrypoint) does the same, and we always say a clean
 * goodbye when the client itself closes, so the mapping and socket never outlive us.
 */
@Mixin(Minecraft.class)
abstract class MinecraftMixin {
	@Unique
	private static KeyMapping siftbridge$disconnect;

	/** Wired by {@code SiftBridgeClient} after key registration. */
	public static void siftbridge$setDisconnectKey(final KeyMapping key) {
		siftbridge$disconnect = key;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void siftbridge$killSwitch(final CallbackInfo ci) {
		KeyMapping key = siftbridge$disconnect;
		if (key != null) {
			while (key.consumeClick()) {
				SiftLink.shutdown();
			}
		}
	}

	@Inject(method = "destroy", at = @At("HEAD"))
	private void siftbridge$goodbye(final CallbackInfo ci) {
		SiftLink.shutdown();
	}
}
