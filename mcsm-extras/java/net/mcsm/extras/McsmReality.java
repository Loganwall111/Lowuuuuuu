package net.mcsm.extras;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Devouring Storms: the Decayed Reality (mandate D.8, phase 1).
 *
 * "I would like it to be a real dimension not the ones they're just teleported
 * somewhere else in the world." That is exactly what this is: a datapack
 * dimension ("mcsm:decayed_reality", shipped in the mod jar under
 * data/mcsm/dimension/), registered by the server as a real {@link ServerLevel}
 * with its own dimension type, its own biome source, its own flat terrain made
 * of the new mcsm blocks, its own sky and fog -- not a teleport to another
 * corner of the overworld.
 *
 * Entry: hold the Rift Key and sneak and the world rips open under you; the
 * same gesture brings you home. The method bodies are the base mod's own
 * portal idiom, copied from BowelsPortal.send -- including the exact
 * teleportTo(ServerLevel, x, y, z, Set.of(), yRot, xRot, false) call and the
 * portal sound -- so every symbol here is one the released jar demonstrably
 * declares. The client-side "open the rift now" path (used by the control
 * panel button) is reflection-only, so a rename can never break compilation.
 */
public final class McsmReality {

    public static final ResourceKey<Level> DECAYED_REALITY = ResourceKey.create(
            Registries.DIMENSION, Identifier.fromNamespaceAndPath("mcsm", "decayed_reality"));

    /** Fallback arrival height if the column scan below cannot run. */
    private static final double ARRIVAL_Y = 72.0D;
    /** The rift gate: the arrival point all travellers share (city centre, phase 2). */
    private static final int GATE_X = 0;
    private static final int GATE_Z = 0;

    private static final Map<UUID, Long> COOLDOWN = new ConcurrentHashMap<>();
    private static final long COOLDOWN_TICKS = 60L;

    private McsmReality() {
    }

    /** Called from the server-side player tick (McsmRiftMixin). */
    public static void tickServer(ServerPlayer player) {
        try {
            if (!McsmExtrasConfig.decayedReality || player == null) {
                return;
            }
            if (!player.isShiftKeyDown() || !holdingKey(player)) {
                return;
            }
            long now = player.level().getGameTime();
            Long last = COOLDOWN.get(player.getUUID());
            if (last != null && now - last < COOLDOWN_TICKS) {
                return;
            }
            COOLDOWN.put(player.getUUID(), now);
            ServerLevel target = player.level().dimension().equals(DECAYED_REALITY)
                    ? home(player)
                    : player.level().getServer() != null
                        ? player.level().getServer().getLevel(DECAYED_REALITY)
                        : null;
            if (target == null) {
                player.sendSystemMessage(Component.literal(
                        "\u00a75The rift does not answer yet \u00a78(the decayed reality is not loaded)"));
                return;
            }
            send(player, target, target.dimension().equals(DECAYED_REALITY));
        } catch (Throwable t) {
            System.err.println("[ds] rift tick failed: " + t);
        }
    }

    /** Panel button / hotkey entry. Client-safe: works through the local server. */
    public static boolean enterFromClient() {
        try {
            Object mc = Class.forName("net.minecraft.client.Minecraft").getMethod("getInstance").invoke(null);
            if (mc == null) {
                return false;
            }
            Object local = mc.getClass().getMethod("getSingleplayerServer").invoke(mc);
            if (local == null) {
                return false; // a dedicated server: use the sneak gesture in-game
            }
            Object player = mc.getClass().getField("player").get(mc);
            if (player == null) {
                return false;
            }
            Object uuid = player.getClass().getMethod("getUUID").invoke(player);
            Object list = local.getClass().getMethod("getPlayerList").invoke(local);
            Object serverPlayer = list.getClass().getMethod("getPlayer", UUID.class).invoke(list, uuid);
            if (serverPlayer instanceof ServerPlayer sp) {
                ServerLevel target = sp.level().getServer().getLevel(DECAYED_REALITY);
                if (target != null) {
                    send(sp, target, true);
                    return true;
                }
            }
            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Gives the player the kit the rift expects (Rift Key + a starter set) and
     * spells out the /give fallback. Everything goes through reflection against
     * the local singleplayer server, so this can never break compilation and it
     * degrades to "print the commands" anywhere else.
     */
    public static void giveStarterKit() {
        java.util.List<String> ids = java.util.List.of(
                "mcsm:rift_key", "mcsm:reality_ripper", "mcsm:reality_glass",
                "mcsm:glitch_lamp", "mcsm:city_bricks", "mcsm:rift_shard");
        Object sp = localServerPlayer();
        if (sp != null) {
            try {
                Object inv = sp.getClass().getMethod("getInventory").invoke(sp);
                java.lang.reflect.Method add = null;
                for (java.lang.reflect.Method m : inv.getClass().getMethods()) {
                    if (m.getName().equals("add") && m.getParameterCount() == 1
                            && m.getParameterTypes()[0].getName().endsWith("ItemStack")) {
                        add = m;
                        break;
                    }
                }
                Object itemRegistry = Class.forName("net.minecraft.core.registries.BuiltInRegistries")
                        .getField("ITEM").get(null);
                for (String id : ids) {
                    Object rl = net.minecraft.resources.Identifier.fromNamespaceAndPath(
                            "mcsm", id.substring("mcsm:".length()));
                    Object item = itemRegistry.getClass().getMethod("getValue", rl.getClass())
                            .invoke(itemRegistry, rl);
                    if (item == null) {
                        continue;
                    }
                    Object stack = Class.forName("net.minecraft.world.item.ItemStack")
                            .getConstructor(Class.forName("net.minecraft.world.item.ItemLike"))
                            .newInstance(item);
                    if (add != null) {
                        add.invoke(inv, stack);
                    }
                }
                sendChat(sp, "\u00a7dDevouring Storms \u00a78\u00b7 starter kit delivered \u00a77(hold the Rift Key and sneak)");
                return;
            } catch (Throwable t) {
                System.err.println("[ds] starter kit reflection path failed: " + t);
            }
        }
        sendChat(sp, "\u00a7dDevouring Storms \u00a78\u00b7 /give @s " + String.join(" \u00b7 /give @s ", ids));
    }

    /** True when the player is inside the decayed reality. */
    public static boolean inside(Object level) {
        try {
            Method m = level.getClass().getMethod("dimension");
            Object key = m.invoke(level);
            return key != null && key.toString().contains("decayed_reality");
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Highest solid block at the arrival column, so opening the rift can never
     * drop a player through the world. The scan is the base mod's own
     * getBlockState(pos).isAir() idiom (BowelsEndRoom).
     */
    private static double landing(ServerLevel level, int x, int z) {
        try {
            for (int y = 300; y > level.getMinY(); y--) {
                if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                    return y + 1.0D;
                }
            }
        } catch (Throwable ignored) {
            // fall through to the constant
        }
        return ARRIVAL_Y;
    }

    /** The local singleplayer ServerPlayer, or null (reflection only). */
    private static Object localServerPlayer() {
        try {
            Object mc = Class.forName("net.minecraft.client.Minecraft").getMethod("getInstance").invoke(null);
            if (mc == null) {
                return null;
            }
            Object local = mc.getClass().getMethod("getSingleplayerServer").invoke(mc);
            if (local == null) {
                return null;
            }
            Object player = mc.getClass().getField("player").get(mc);
            if (player == null) {
                return null;
            }
            Object uuid = player.getClass().getMethod("getUUID").invoke(player);
            Object list = local.getClass().getMethod("getPlayerList").invoke(local);
            return list.getClass().getMethod("getPlayer", UUID.class).invoke(list, uuid);
        } catch (Throwable t) {
            return null;
        }
    }

    private static void sendChat(Object target, String text) {
        try {
            if (target instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.literal(text));
            } else {
                System.out.println(text.replaceAll("\u00a7.", ""));
            }
        } catch (Throwable ignored) {
        }
    }

    private static boolean holdingKey(ServerPlayer player) {
        try {
            return (player.getMainHandItem().getItem() == McsmContent.RIFT_KEY)
                    || (player.getOffhandItem().getItem() == McsmContent.RIFT_KEY);
        } catch (Throwable t) {
            return false;
        }
    }

    private static ServerLevel home(ServerPlayer player) {
        try {
            if (player.level().getServer() == null) {
                return null;
            }
            Object overworld = player.level().getServer().overworld();
            return overworld instanceof ServerLevel sl ? sl : null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static void send(ServerPlayer player, ServerLevel target, boolean leaving) {
        boolean intoReality = target.dimension().equals(DECAYED_REALITY);
        double x = intoReality ? GATE_X + 0.5D : player.getX() + 0.5D;
        double z = intoReality ? GATE_Z + 0.5D : player.getZ() + 0.5D;
        double y = intoReality ? landing(target, GATE_X, GATE_Z) + 2.0D
                : Math.max(player.getY(), target.getMinY() + 8.0D);
        player.teleportTo(target, x, y, z, Set.of(), player.getYRot(), 0.0F, false);
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        target.playSound((Entity) null, x, y, z, SoundEvents.PORTAL_TRAVEL,
                SoundSource.PLAYERS, 1.0F, 0.7F);
        player.sendSystemMessage(Component.literal(leaving
                ? "\u00a75\u00a7lTHE RIFT OPENS \u00a78\u00b7 the decayed reality answers"
                : "\u00a75\u00a7lTHE RIFT CLOSES \u00a78\u00b7 you are spat back into the world"));
        if (leaving) {
            // phase 2: point the player at the nearest abandoned district
            player.sendSystemMessage(Component.literal("\u00a78\u00b7 the ruins lie \u00a7f"
                    + McsmCities.guidance((int) x, (int) z)
                    + "\u00a78 \u00b7 look for the rift monument"));
        }
    }
}
