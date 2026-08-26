package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dabicco.witherstormmod.item.RocketRetrieverItem;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemInHandRenderer.class})
public class ItemInHandRendererMixin {
   @Inject(
      method = {"renderItem"},
      at = {@At("HEAD")}
   )
   private void witherstormmod$aimPose(
      LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext, PoseStack pose, SubmitNodeCollector collector, int light, CallbackInfo ci
   ) {
      if (stack.getItem() instanceof RocketRetrieverItem) {
         boolean firstPerson = displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
         if (firstPerson) {
            if (entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
               int used = stack.getUseDuration(player) - player.getUseItemRemainingTicks();
               float p = Mth.clamp((float)used / 4.0F, 0.0F, 1.0F);
               p = p * p * (3.0F - 2.0F * p);
               float side = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -1.0F : 1.0F;
               pose.translate(-0.09F * side * p, 0.13F * p, -0.11F * p);
               pose.mulPose(Axis.XP.rotationDegrees(-9.0F * p));
               pose.mulPose(Axis.YP.rotationDegrees(7.0F * side * p));
               return;
            }
         }
      }
   }
}
