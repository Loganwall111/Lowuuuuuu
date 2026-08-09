package com.rewritten.devouringstorms.util;

import com.rewritten.devouringstorms.entity.MassgEntity;
import com.rewritten.devouringstorms.registry.ModBlocks;
import com.rewritten.devouringstorms.registry.ModEntities;
import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.world.ModDimensions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * THE BELLY OF THE STORM. Once the bowels are open, flying too close to the maw drops
 * you INSIDE the storm: a fleshy pocket void whole worlds away. In the middle, on a
 * pedestal of ribs, beats the anchored command block — the thing keeping the husk whole.
 * Striking it with the Storm Killer counts toward the three that rend the storm, from
 * inside. An exit sphincter (frayed tear) on the wall lets you leave early if you find it.
 */
public final class StomachChamber {

    /** Where inside the belly the chamber is assembled. */
    private static final BlockPos CHAMBER = new BlockPos(0, 32, 0);

    /** Which storm's heart this chamber belongs to right now (runtime binding). */
    private static final Map<UUID, Integer> STOMACH_COOLDOWN = new HashMap<>();
    private static UUID boundStorm = null;
    private static final Map<UUID, Vec3> RETURN_SPOTS = new HashMap<>();
    private static final Map<UUID, ResourceKey<Level>> RETURN_DIMS = new HashMap<>();
    private static boolean chamberBuilt = false;

    private StomachChamber() {
    }

    /** Sucked in. Drops the player into the belly at the chamber and binds it to this storm. */
    public static void enterStomach(Player player, MassgEntity storm) {
        if (!DevouringConfig.getBool("stomach_interior", true)) return;
        if (!(player instanceof ServerPlayer sp)) return;
        ServerLevel source = (ServerLevel) player.level();
        long now = source.getGameTime();
        Long until = null;
        // storms share one cooldown clock per player
        int held = STOMACH_COOLDOWN.getOrDefault(player.getUUID(), 0);
        if (now < held + 100) return;
        STOMACH_COOLDOWN.put(player.getUUID(), (int) now);

        ServerLevel belly = source.getServer().getLevel(ModDimensions.BELLY_LEVEL_KEY);
        if (belly == null) return;

        boundStorm = storm.getUUID();
        RETURN_SPOTS.put(player.getUUID(), player.position());
        RETURN_DIMS.put(player.getUUID(), source.dimension());

        ensureChamber(belly);
        source.playSound(null, storm.blockPosition(), ModSounds.MASSG_DEVOUR, SoundSource.HOSTILE, 3.0f, 0.6f);
        RiftTravel.travelTo(sp, belly, Vec3.atCenterOf(CHAMBER.above(2)));
        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§5§oThe maw takes you. You are falling through something that used to be a sky.§r"));
        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§8The heart of it beats ahead. §7Only the Storm Killer answers in here.§8"));
    }

    /** First visit: assemble the flesh chamber out of nothing. */
    public static void ensureChamber(ServerLevel belly) {
        if (chamberBuilt) return;
        chamberBuilt = true;
        int cx = CHAMBER.getX(), cy = CHAMBER.getY(), cz = CHAMBER.getZ();
        double r = 9.0;
        for (int x = -10; x <= 10; x++) {
            for (int y = -10; y <= 10; y++) {
                for (int z = -10; z <= 10; z++) {
                    double d = Math.sqrt(x * x + y * y + z * z);
                    if (d > r || d < r - 2.4) continue;
                    BlockPos p = new BlockPos(cx + x, cy + y, cz + z);
                    BlockState wall;
                    int pick = Math.floorMod(x * 31 + y * 17 + z * 7, 23);
                    if (pick == 0 || pick == 3) {
                        wall = ModBlocks.DECAY_BLOCK.defaultBlockState();           // weeping tissue
                    } else if (pick % 4 == 0) {
                        wall = ModBlocks.ROT_LOG.defaultBlockState();               // ribs
                    } else if (pick % 3 == 0) {
                        wall = ModBlocks.DECAYED_SOIL.defaultBlockState();
                    } else {
                        wall = ModBlocks.DECAYED_STONE.defaultBlockState();
                    }
                    belly.setBlock(p, wall, 3);
                }
            }
        }
        // floor apron so nobody falls into the void
        BlockPos down = CHAMBER.below(8);
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                belly.setBlock(down.offset(x, 0, z), ModBlocks.DECAYED_STONE.defaultBlockState(), 3);
            }
        }
        // the heart-pedestal: ribs around the anchored command block that keeps the husk whole
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                belly.setBlock(CHAMBER.offset(dx, -1, dz), obsidian, 3);
            }
        }
        for (int dx = -2; dx <= 2; dx += 4) {
            for (int h = 0; h < 3; h++) {
                belly.setBlock(CHAMBER.offset(dx, h, 0), ModBlocks.ROT_LOG.defaultBlockState(), 3);
                belly.setBlock(CHAMBER.offset(0, h, dx), ModBlocks.ROT_LOG.defaultBlockState(), 3);
            }
        }
        belly.setBlock(CHAMBER, ModBlocks.CORRUPTED_COMMAND_BLOCK.defaultBlockState(), 3);
        // purple glass veins where the light gets in from outside
        for (int i = 0; i < 14; i++) {
            double a = i * 0.45;
            belly.setBlock(CHAMBER.offset((int) (Math.cos(a) * 8), (int) (Math.sin(a) * 8) + 2, 0),
                Blocks.PURPLE_STAINED_GLASS.defaultBlockState(), 3);
        }
        // the exit sphincter
        belly.setBlock(CHAMBER.offset(6, 0, 0), ModBlocks.FRAYED_TEAR.defaultBlockState(), 3);
    }

    /** A Storm Killer strike on the anchored core inside the belly. Shared counter, shared rend. */
    public static boolean tryStrikeCore(ServerLevel level, BlockPos pos, Player player) {
        if (!level.dimension().equals(ModDimensions.BELLY_LEVEL_KEY)) return false;
        if (!pos.equals(CHAMBER)) return false;
        if (boundStorm == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§8The core stares back. Nothing on the outside is listening right now."));
            return true;
        }
        ServerLevel any = level.getServer().getLevel(RETURN_DIMS.getOrDefault(player.getUUID(), ModDimensions.DECAYED_LEVEL_KEY));
        MassgEntity storm = null;
        if (any != null) {
            for (MassgEntity candidate : any.getEntities(ModEntities.MASSG, net.minecraft.world.phys.AABB.ofSize(
                    net.minecraft.world.phys.Vec3.atCenterOf(pos), 100000, 1000, 100000), e -> e.getUUID().equals(boundStorm))) {
                storm = candidate;
                break;
            }
        }
        if (storm == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§8The core stares back. Its storm is gone — the belly is only meat now."));
            return true;
        }
        if (storm.getPhase() != com.rewritten.devouringstorms.storm.MassgPhase.HUSK) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§8Not yet. §7It only becomes strikable when the storm has fallen out of the sky."));
            return true;
        }
        storm.applyStormKillerHit(player, (ServerLevel) storm.level());
        postStrikeEject(player, level);
        return true;
    }

    /** After the hit lands, the belly shudders; on the third, it throws you back out. */
    private static void postStrikeEject(Player player, ServerLevel belly) {
        if (!(player instanceof ServerPlayer sp)) return;
        Vec3 ret = RETURN_SPOTS.get(player.getUUID());
        ResourceKey<Level> dim = RETURN_DIMS.getOrDefault(player.getUUID(), ModDimensions.DECAYED_LEVEL_KEY);
        if (ret == null) return;
        ServerLevel home = belly.getServer().getLevel(dim);
        if (home == null) return;
        sp.setDeltaMovement(Vec3.ZERO);
        RiftTravel.travelTo(sp, home, ret.add(0, 0.2, 0));
        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§d§oThe belly contracts around the wound. It throws you back into the sky.§r"));
        RETURN_SPOTS.remove(player.getUUID());
        RETURN_DIMS.remove(player.getUUID());
    }
}
