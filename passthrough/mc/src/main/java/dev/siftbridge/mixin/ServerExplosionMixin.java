package dev.siftbridge.mixin;

import dev.siftbridge.SiftEvents;
import java.util.Locale;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Every server explosion (TNT, creepers, beds — and the Wither Storm's presence bursts in the
 * pack) is mirrored to the host as an {@code explosion} plus a {@code rift} pulse, so Dungeons II
 * can flash a Sift rift where the storm detonates. Mixin target as proven upstream for 26.3.
 */
@Mixin(ServerExplosion.class)
abstract class ServerExplosionMixin {
	@Shadow
	public abstract Vec3 center();

	@Shadow
	public abstract float radius();

	@Shadow
	public abstract Entity getDirectSourceEntity();

	@Inject(method = "explode", at = @At("HEAD"))
	private void siftbridge$report(final CallbackInfoReturnable<Integer> cir) {
		Vec3 at = this.center();
		float r = this.radius();
		SiftEvents.emit(String.format(Locale.ROOT,
			"{\"t\":\"explosion\",\"pos\":[%.3f,%.3f,%.3f],\"r\":%.2f}", at.x, at.y, at.z, r));
		SiftEvents.emit(String.format(Locale.ROOT,
			"{\"t\":\"rift\",\"pos\":[%.3f,%.3f,%.3f],\"r\":%.2f,\"kind\":\"sift\"}", at.x, at.y, at.z, Math.min(r / 4.0, 1.0)));
	}
}
