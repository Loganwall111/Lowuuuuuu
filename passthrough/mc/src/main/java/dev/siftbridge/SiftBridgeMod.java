package dev.siftbridge;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Marker entrypoint — the whole bridge is client-side ({@code dev.siftbridge.client}). */
public final class SiftBridgeMod implements ModInitializer {
	public static final Logger LOG = LoggerFactory.getLogger("siftbridge");

	@Override
	public void onInitialize() {
		LOG.info("SIFT Bridge: external passthrough guest (no world writes)");
	}
}
