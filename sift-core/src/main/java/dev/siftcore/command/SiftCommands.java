package dev.siftcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.siftcore.SiftDimensions;
import dev.siftcore.transfer.SiftTransfer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/** Small operator-only test surface for the standalone dimension slice. */
public final class SiftCommands {
    private SiftCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sift")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(SiftCommands::toggle)
                .then(CommandManager.literal("enter").executes(SiftCommands::enter))
                .then(CommandManager.literal("fall").executes(SiftCommands::fall))
                .then(CommandManager.literal("return").executes(SiftCommands::returnToOverworld)));
    }

    private static int toggle(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return SiftDimensions.isSift(player.getWorld()) ? returnToOverworld(context) : enter(context);
    }

    private static int enter(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        if (SiftTransfer.enterFromCommand(player)) {
            player.sendMessage(Text.literal("Sift-Core: entered The Sift"), false);
            return 1;
        }
        player.sendMessage(Text.literal("Sift-Core: The Sift dimension is not loaded"), false);
        return 0;
    }

    private static int fall(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        if (SiftDimensions.isSift(player.getWorld())) {
            player.sendMessage(Text.literal("Sift-Core: already inside The Sift"), false);
            return 0;
        }

        // The next Entity#tick invokes the real automatic void handshake.
        player.setPosition(player.getX(), SiftTransfer.OVERWORLD_VOID_Y + 1.0D, player.getZ());
        player.setVelocity(0.0D, -0.45D, 0.0D);
        player.velocityModified = true;
        player.setOnGround(false);
        player.sendMessage(Text.literal("Sift-Core: falling test armed at Y -63"), false);
        return 1;
    }

    private static int returnToOverworld(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        if (SiftTransfer.returnToOverworld(player)) {
            player.sendMessage(Text.literal("Sift-Core: returned to the Overworld"), false);
            return 1;
        }
        player.sendMessage(Text.literal("Sift-Core: player is not in The Sift"), false);
        return 0;
    }
}
