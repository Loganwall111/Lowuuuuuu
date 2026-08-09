package com.rewritten.devouringstorms.item;

import com.rewritten.devouringstorms.util.RiftTravel;
import com.rewritten.devouringstorms.world.ModDimensions;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * THE ROCKET KEY. "not one planet — several, sir. ecosystems included."
 * The EPA could never stop building ships, so this is what's left of the flight manifest:
 * a brass key punched with three destinations. Use it to cycle the ring:
 *    HOME → AURTH (the stone age had a morning) → VOLMAR (iron and fire)
 *    → NEXUS (the age that reads its own broadcast) → HOME.
 * Take-off is instant. Landing is approximate.
 */
public class RocketKeyItem extends Item {

    private record Hop(ResourceKey<Level> dim, String title) { }

    private static final List<Hop> RING = List.of(
        new Hop(Level.OVERWORLD, "§7Home"),
        new Hop(ModDimensions.AURTH_LEVEL_KEY, "§ePLANET AURTH — age I: Stone"),
        new Hop(ModDimensions.VOLMAR_LEVEL_KEY, "§6PLANET VOLMAR — age III: Iron"),
        new Hop(ModDimensions.NEXUS_LEVEL_KEY, "§dNEXUS — the Multiverse Age")
    );

    public RocketKeyItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) return InteractionResult.PASS;
        int idx = 0;
        for (int i = 0; i < RING.size(); i++) {
            if (server.dimension().equals(RING.get(i).dim())) { idx = i; break; }
        }
        Hop next = RING.get((idx + 1) % RING.size());
        ServerLevel destination = server.getServer().getLevel(next.dim());
        if (destination == null) return InteractionResult.PASS;
        int y = destination.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0) + 1;
        if (y < destination.getMinY() + 2) y = destination.getSeaLevel() + 1;
        player.teleportTo(destination, 0.5, y + 0.5, 0.5, java.util.Set.of(),
            player.getYRot(), player.getXRot(), false);
        player.sendSystemMessage(Component.literal("§f§o>><< — launch logged. Next stop: §r" + next.title()));
        player.playNotifySound(net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_LAUNCH,
            net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 0.8f);
        return InteractionResult.CONSUME;
    }
}
