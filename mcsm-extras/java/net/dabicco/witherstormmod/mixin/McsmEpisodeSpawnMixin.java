package net.dabicco.witherstormmod.mixin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dabicco.witherstormmod.structures.McsmSchematic;
import net.dabicco.witherstormmod.structures.McsmWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Devouring Storms: Item 4 slice 2 - you SPAWN INSIDE ENDERCON.
 *
 * Episode One opens at the fair, so the fair is where you arrive:
 *
 *  - On the first overworld tick with players present, the EnderCon Town
 *    Fair site is sampled. If it is not built yet, its schematic is
 *    queued through the base mod's own incremental builder (24k blocks
 *    per tick) instead of making anyone run /mcsm build.
 *  - While it builds, nobody is moved - the client-side MCSM episode
 *    card ("Episode One / A NEW ORDER", 1.9.129) plays over the wait,
 *    exactly like the Telltale loading sequence.
 *  - The moment the queue drains, every freshly-joined player (once per
 *    session each) is teleported into the fair with the episode intro
 *    lines in chat - you never watch EnderCon assemble from world spawn.
 *
 * All calls are the base mod's own verified 26.2 forms: layout() sites,
 * McsmSchematic.load(server.getResourceManager(), path), enqueue,
 * pending(), level.players(), player.teleportTo(level, x, y, z, Set.of(),
 * yRot, xRot, false), sendSystemMessage, dimension() == Level.OVERWORLD.
 */
@Mixin(McsmWorldgen.class)
public abstract class McsmEpisodeSpawnMixin {

    @Unique
    private static int dabyws$phase = 0; // 0 = survey, 1 = building, 2 = fair ready
    @Unique
    private static McsmWorldgen.Site dabyws$fair = null;
    @Unique
    private static final Set<UUID> DABYWS$ARRIVED = new HashSet<>();

    @Inject(method = "tick", at = @At("HEAD"), remap = false)
    private static void dabyws$episodeOneSpawn(ServerLevel level,
            CallbackInfoReturnable<Integer> cir) {
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }

        if (dabyws$phase == 0) {
            if (level.players().isEmpty()) {
                return; // wait until somebody is actually in the story
            }
            for (McsmWorldgen.Site s : McsmWorldgen.layout()) {
                if ("EnderCon Town Fair".equals(s.label())) {
                    dabyws$fair = s;
                    break;
                }
            }
            if (dabyws$fair == null) {
                dabyws$phase = 2; // layout changed under us; never block play
                return;
            }
            boolean built = false;
            for (int dx = 2; dx <= 18 && !built; dx += 8) {
                for (int dz = 2; dz <= 18 && !built; dz += 8) {
                    BlockPos probe = new BlockPos(
                            dabyws$fair.x() + dx, dabyws$fair.y() + 2, dabyws$fair.z() + dz);
                    if (!level.getBlockState(probe).isAir()) {
                        built = true;
                    }
                }
            }
            if (built) {
                dabyws$phase = 2;
            } else {
                try {
                    McsmSchematic sch = McsmSchematic.load(
                            level.getServer().getResourceManager(), dabyws$fair.path());
                    McsmWorldgen.enqueue(sch,
                            new BlockPos(dabyws$fair.x(), dabyws$fair.y(), dabyws$fair.z()),
                            dabyws$fair.label());
                    dabyws$phase = 1;
                } catch (Exception e) {
                    dabyws$phase = 2; // no schematic in pack -> don't trap anyone
                }
            }
        } else if (dabyws$phase == 1) {
            if (McsmWorldgen.pending() > 0) {
                return; // still building; the episode card covers the wait
            }
            dabyws$phase = 2;
        }

        if (dabyws$phase == 2 && dabyws$fair != null) {
            for (ServerPlayer p : level.players()) {
                if (DABYWS$ARRIVED.add(p.getUUID())) {
                    p.teleportTo(level,
                            dabyws$fair.x() + 0.5D, dabyws$fair.y() + 2, dabyws$fair.z() + 0.5D,
                            Set.of(), p.getYRot(), p.getXRot(), false);
                    p.sendSystemMessage(Component.literal(
                            "\u00a75\u00a7lEpisode One \u00a78\u2014 \u00a7d\u00a7lA New Order"));
                    p.sendSystemMessage(Component.literal(
                            "\u00a77You spawn inside \u00a7fEnderCon\u00a77 \u2014 the fair is already alive around you."));
                }
            }
        }
    }
}
