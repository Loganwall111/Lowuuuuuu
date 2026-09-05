package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.bowels.BowelsMawEntity;
import net.dabicco.witherstormmod.bowels.BowelsTentacleEntity;
import net.dabicco.witherstormmod.network.ActionButtonPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ActionButtons {
   private static net.dabicco.witherstormmod.client.ActionButtons.Prompt showing;
   private static final double LOOK_CONE = 0.5;

   private ActionButtons() {
   }

   public static net.dabicco.witherstormmod.client.ActionButtons.Prompt showing() {
      return showing;
   }

   public static void tick(Minecraft client) {
      showing = null;
      Player me = client.player;
      if (me != null && client.level != null) {
         double reach = 24.0;

         for (BowelsMawEntity maw : client.level.getEntitiesOfClass(BowelsMawEntity.class, me.getBoundingBox().inflate(reach))) {
            if (maw.getPrompt() > 0 && maw.getPromptFor() == me.getId()) {
               showing = new net.dabicco.witherstormmod.client.ActionButtons.Prompt(maw.isRightHand(), maw.getPrompt(), 60);
               break;
            }
         }

         if (showing == null) {
            for (BowelsTentacleEntity limb : client.level.getEntitiesOfClass(BowelsTentacleEntity.class, me.getBoundingBox().inflate(reach))) {
               if (limb.getGrabPrompt() > 0 && limb.getGrabFor() == me.getId() && lookingAt(me, limb)) {
                  showing = new net.dabicco.witherstormmod.client.ActionButtons.Prompt(limb.isGrabRight(), limb.getGrabPrompt(), 40, true);
                  break;
               }
            }
         }

         if (showing != null) {
            if (showing.grab()) {
               if (drain(client.options.keyDrop)) {
                  ClientPlayNetworking.send(new ActionButtonPayload(false));
               }
            } else {
               boolean drop = drain(client.options.keyDrop);
               boolean inventory = drain(client.options.keyInventory);
               if (drop || inventory) {
                  ClientPlayNetworking.send(new ActionButtonPayload(inventory));
               }
            }
         }
      }
   }

   private static boolean lookingAt(Player me, BowelsTentacleEntity limb) {
      Vec3 eye = me.getEyePosition();
      AABB box = limb.getBoundingBox();
      Vec3 near = new Vec3(Mth.clamp(eye.x, box.minX, box.maxX), Mth.clamp(eye.y, box.minY, box.maxY), Mth.clamp(eye.z, box.minZ, box.maxZ));
      Vec3 to = near.subtract(eye);
      return to.lengthSqr() < 1.0E-4 ? true : me.getLookAngle().dot(to.normalize()) >= 0.5;
   }

   private static boolean drain(KeyMapping key) {
      boolean any = false;

      while (key.consumeClick()) {
         any = true;
      }

      return any;
   }

   public record Prompt(boolean rightHand, int ticks, int total, boolean grab) {
      public Prompt(boolean rightHand, int ticks, int total) {
         this(rightHand, ticks, total, false);
      }
   }
}
