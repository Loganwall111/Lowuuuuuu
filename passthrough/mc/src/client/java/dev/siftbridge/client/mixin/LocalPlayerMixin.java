package dev.siftbridge.client.mixin;

import dev.siftbridge.client.HostState;
import dev.siftbridge.client.SiftLink;
import java.util.Locale;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tick-rate bridge:
 * <ul>
 * <li>in {@code hosted} mode the host camera places the local player (yaw/pitch/pos, tick-level;
 *     frame-level smoothing lives in a later revision, upstream does it in the camera mixin);</li>
 * <li>every tick the player's pose goes out as {@code pos} (20 Hz, complements the 60 Hz cam).</li>
 * </ul>
 */
@Mixin(LocalPlayer.class)
abstract class LocalPlayerMixin {
	@Inject(method = "tick", at = @At("HEAD"))
	private void siftbridge$bridge(final CallbackInfo ci) {
		LocalPlayer player = (LocalPlayer)(Object)this;
		HostState.Pose p = HostState.live();
		if (p != null && HostState.hosted()) {
			player.setYRot(p.yaw());
			player.setXRot(p.pitch());
			player.yRotO = p.yaw();
			player.xRotO = p.pitch();
			player.yHeadRot = player.yHeadRotO = p.yaw();
			double x = p.x(), y = p.firstPerson() ? p.y() - player.getEyeHeight() : p.y(), z = p.z();
			if (player.distanceToSqr(x, y, z) > 4096.0) {
				player.xo = x; player.yo = y; player.zo = z;
			}
			player.setPos(x, y, z);
		}

		Vec3 v = player.getDeltaMovement();
		SiftLink.sendEvent(String.format(Locale.ROOT,
			"{\"t\":\"pos\",\"p\":[%.4f,%.4f,%.4f],\"v\":[%.3f,%.3f,%.3f],\"yaw\":%.2f,\"bodyYaw\":%.2f,\"onGround\":%b,\"ts\":%d}",
			player.getX(), player.getY(), player.getZ(), v.x * 20.0, v.y * 20.0, v.z * 20.0,
			player.getYRot(), player.yBodyRot, player.onGround(), System.nanoTime()));
	}
}
