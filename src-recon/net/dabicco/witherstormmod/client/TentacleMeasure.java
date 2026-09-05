package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;

public final class TentacleMeasure {
   private static final int BINS = 10;
   private static final int LIMBS = 2;
   private static final double[] SUM = new double[60];
   private static final int[] COUNT = new int[20];
   private static int limb = -1;
   private static double rootX;
   private static double rootY;
   private static double rootZ;
   private static boolean haveRoot;
   private static double maxReach;
   private static long lastSentTick = Long.MIN_VALUE;
   private static String lastStatus = "";
   private static final VertexConsumer SINK = new VertexConsumer() {
      public VertexConsumer addVertex(float x, float y, float z) {
         if (net.dabicco.witherstormmod.client.TentacleMeasure.limb < 0) {
            return this;
         } else {
            if (!net.dabicco.witherstormmod.client.TentacleMeasure.haveRoot) {
               net.dabicco.witherstormmod.client.TentacleMeasure.rootX = x;
               net.dabicco.witherstormmod.client.TentacleMeasure.rootY = y;
               net.dabicco.witherstormmod.client.TentacleMeasure.rootZ = z;
               net.dabicco.witherstormmod.client.TentacleMeasure.haveRoot = true;
            }

            double dx = x - net.dabicco.witherstormmod.client.TentacleMeasure.rootX;
            double dy = y - net.dabicco.witherstormmod.client.TentacleMeasure.rootY;
            double dz = z - net.dabicco.witherstormmod.client.TentacleMeasure.rootZ;
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (d > net.dabicco.witherstormmod.client.TentacleMeasure.maxReach) {
               net.dabicco.witherstormmod.client.TentacleMeasure.maxReach = d;
            }

            int bin = Math.min(9, (int)(d / Math.max(1.0, net.dabicco.witherstormmod.client.TentacleMeasure.maxReach) * 10.0));
            int i = net.dabicco.witherstormmod.client.TentacleMeasure.limb * 10 + bin;
            double[] var10000 = net.dabicco.witherstormmod.client.TentacleMeasure.SUM;
            var10000[i * 3] = var10000[i * 3] + x;
            var10000 = net.dabicco.witherstormmod.client.TentacleMeasure.SUM;
            var10000[i * 3 + 1] = var10000[i * 3 + 1] + y;
            var10000 = net.dabicco.witherstormmod.client.TentacleMeasure.SUM;
            var10000[i * 3 + 2] = var10000[i * 3 + 2] + z;
            net.dabicco.witherstormmod.client.TentacleMeasure.COUNT[i]++;
            return this;
         }
      }

      public VertexConsumer setColor(int r, int g, int b, int a) {
         return this;
      }

      public VertexConsumer setColor(int packed) {
         return this;
      }

      public VertexConsumer setUv(float u, float v) {
         return this;
      }

      public VertexConsumer setUv1(int u, int v) {
         return this;
      }

      public VertexConsumer setUv2(int u, int v) {
         return this;
      }

      public VertexConsumer setNormal(float x, float y, float z) {
         return this;
      }

      public VertexConsumer setLineWidth(float w) {
         return this;
      }
   };
   private static final boolean ENABLED = false;

   private TentacleMeasure() {
   }

   private static void status(String reason) {
      if (!reason.equals(lastStatus)) {
         lastStatus = reason;
         System.out.println("[dabywitherstormmod][carve/client] " + reason);
      }
   }

   public static void measure(PoseStack pose, Model model, int stormId) {
   }
}
