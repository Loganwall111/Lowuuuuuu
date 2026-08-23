package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.item.RocketRetrieverItem;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AvatarRenderer.class})
public class AvatarRendererMixin {
   @Inject(
      method = {"getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private static void witherstormmod$aimPose(Avatar avatar, ItemStack stack, InteractionHand hand, CallbackInfoReturnable<ArmPose> cir) {
      if (stack.getItem() instanceof RocketRetrieverItem && avatar.isUsingItem() && avatar.getUseItem() == stack) {
         cir.setReturnValue(ArmPose.BOW_AND_ARROW);
      }
   }
}
