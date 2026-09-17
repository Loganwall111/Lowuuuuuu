package net.dabicco.witherstormmod.bowels.client;

import java.util.List;
import net.dabicco.witherstormmod.entity.model.Tentacle;
import net.minecraft.client.model.geom.ModelPart;

public final class BowelsLimbPose {
   private BowelsLimbPose() {
   }

   public static void apply(Tentacle model, float[][] joints, int bones, int from, float scale) {
      List<ModelPart> chain = model.chain();
      ModelPart base = model.base();

      for (ModelPart bone : chain) {
         bone.resetPose();
         bone.visible = true;
         bone.skipDraw = false;
      }

      base.x = 0.0F;
      base.y = 0.0F;
      base.z = 0.0F;
      base.zRot = 0.0F;

      for (int i = 0; i < chain.size(); i++) {
         ModelPart bone = chain.get(i);
         if (i >= bones) {
            bone.visible = false;
         } else {
            if (i < joints.length) {
               bone.xRot = joints[i][0];
               bone.yRot = joints[i][1];
            }

            if (i < from) {
               bone.skipDraw = true;
            }
         }
      }

      base.xScale = scale;
      base.yScale = scale;
      base.zScale = scale;
   }
}
