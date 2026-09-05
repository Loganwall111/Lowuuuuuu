package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen;
import net.dabicco.witherstormmod.config.ClientConfigCommandPayload;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.config.DabyWSClientConfig.Key;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ClientConfigCommandHandler {
   public static void handle(Minecraft mc, ClientConfigCommandPayload payload) {
      if (mc.player != null) {
         switch (payload.mode()) {
            case 0:
               Key key = (Key)DabyWSClientConfig.KEYS.get(payload.key());
               if (key == null) {
                  return;
               }

               MutableComponent line = Component.literal(key.name() + " = ")
                  .withStyle(ChatFormatting.GRAY)
                  .append(Component.literal(format(key, key.get().getAsDouble())).withStyle(ChatFormatting.WHITE));
               if (!key.description().isEmpty()) {
                  line.append(Component.literal("  (" + key.description() + ")").withStyle(ChatFormatting.DARK_GRAY));
               }

               mc.player.sendSystemMessage(line);
               break;
            case 1:
               Key key1 = (Key)DabyWSClientConfig.KEYS.get(payload.key());
               if (key1 == null) {
                  return;
               }

               double clamped = key1.clamp(payload.value());
               key1.set().accept(clamped);
               DabyWSClientConfig.save();
               mc.player
                  .sendSystemMessage(
                     Component.literal("[client] " + key1.name() + " set to ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(format(key1, clamped)).withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(" (only affects you)").withStyle(ChatFormatting.DARK_GRAY))
                  );
               break;
            case 2:
               mc.player.sendSystemMessage(Component.literal("Wither Storm client config (only you):").withStyle(ChatFormatting.AQUA));

               for (Key k : DabyWSClientConfig.KEYS.values()) {
                  mc.player
                     .sendSystemMessage(
                        Component.literal("  " + k.name() + " = ")
                           .withStyle(ChatFormatting.GRAY)
                           .append(Component.literal(format(k, k.get().getAsDouble())).withStyle(ChatFormatting.WHITE))
                     );
               }
               break;
            case 3:
               mc.gui.setScreen(new WitherStormConfigScreen((Screen)null));
         }
      }
   }

   private static String format(Key key, double value) {
      if (key.toggle()) {
         return value >= 0.5 ? "on" : "off";
      } else {
         return String.format("%.2f", value);
      }
   }
}
