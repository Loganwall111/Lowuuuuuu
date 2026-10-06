package dev.siftbridge.client.mixin;

import dev.siftbridge.client.FrameExporter;
import dev.siftbridge.client.Telemetry;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Data Streamer tap. After {@code Camera.update} has settled the final view for the frame we
 * snapshot position, yaw, pitch and fov into {@link Telemetry} (a single atomic store) and stamp
 * the frame slot. No JSON, no socket, no allocation of consequence: serialisation happens on the
 * link thread, so the render tick is untouched.
 */
@Mixin(Camera.class)
abstract class CameraMixin {
	@Shadow private float xRot;
	@Shadow private float yRot;

	@Shadow
	public abstract Vec3 getPosition();

	@Shadow
	protected abstract float calculateFov(final float partialTicks);

	@Shadow
	private float depthFar;

	@Unique
	private long siftbridge$frame;

	@Inject(method = "update", at = @At("TAIL"))
	private void siftbridge$publish(final DeltaTracker deltaTracker, final CallbackInfo ci) {
		Vec3 at = this.getPosition();
		float fov = this.calculateFov(deltaTracker.getGameTimeDeltaPartialTick(true));
		boolean fp = true;   // refined by third-person detection in a later revision
		Telemetry.capture(++this.siftbridge$frame, at.x, at.y, at.z, this.yRot, this.xRot, 0.0F, fov, fp);
		FrameExporter.onFrame(at.x, at.y, at.z, this.yRot, this.xRot, 0.0F, fov, fp);
	}

	@Inject(method = "update", at = @At("TAIL"))
	private void siftbridge$far(final DeltaTracker deltaTracker, final CallbackInfo ci) {
		FrameExporter.setFar(this.depthFar);
	}
}
