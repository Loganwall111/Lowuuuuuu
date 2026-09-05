package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.mixin.ModelPartAccessor;
import net.dabicco.witherstormmod.mixin.ModelPartCubesAccessor;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Cube;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class CubeReveal {
   private static final float MODEL_UNITS = 16.0F;
   private static final Map<ModelPart, Map<Long, Integer>> ORDER_CACHE = new IdentityHashMap<>();
   private static final Map<ModelPart, Integer> ORDER_COUNT = new IdentityHashMap<>();
   private static Map<Long, Integer> currentOrder;
   private static final Map<ModelPart, IdentityHashMap<Cube, Integer>> RESOLVED = new IdentityHashMap<>();
   private static IdentityHashMap<Cube, Integer> currentResolved;
   private static final Integer UNORDERED = Integer.MIN_VALUE;
   private static final float TOUCH_EPSILON = 0.34F;
   private static final float SEED_BAND = 1.2F;
   private static final Map<ModelPart, float[]> BOUNDS_CACHE = new IdentityHashMap<>();
   public static int statSubmits;
   public static int statCubes;

   private static boolean isDegenerate(Cube cube) {
      return cube.maxX - cube.minX <= 1.0E-4F || cube.maxY - cube.minY <= 1.0E-4F || cube.maxZ - cube.minZ <= 1.0E-4F;
   }

   private static boolean isBigSheet(Cube cube) {
      float dx = cube.maxX - cube.minX;
      float dy = cube.maxY - cube.minY;
      float dz = cube.maxZ - cube.minZ;
      float lo = Math.min(dx, Math.min(dy, dz));
      float hi = Math.max(dx, Math.max(dy, dz));
      float mid = dx + dy + dz - lo - hi;
      return mid * hi > 2000.0F && lo < hi * 0.08F;
   }

   private static boolean isOversizedSlab(Cube cube) {
      float hi = Math.max(cube.maxX - cube.minX, Math.max(cube.maxY - cube.minY, cube.maxZ - cube.minZ));
      return hi > 56.0F;
   }

   private static boolean isStraySlab(Cube cube) {
      float dx = cube.maxX - cube.minX;
      float dy = cube.maxY - cube.minY;
      float dz = cube.maxZ - cube.minZ;
      float hi = Math.max(dx, Math.max(dy, dz));
      float lo = Math.min(dx, Math.min(dy, dz));
      float mid = dx + dy + dz - hi - lo;
      return hi > 70.0F && mid > 20.0F && lo > 8.0F;
   }

   private static boolean skip(Cube cube, boolean mirror) {
      return isDegenerate(cube) || isStraySlab(cube);
   }

   private static boolean mirrorArtifact(Cube cube) {
      return isBigSheet(cube) || isOversizedSlab(cube);
   }

   private static Integer orderOf(Cube cube, Matrix4f rootSpace) {
      IdentityHashMap<Cube, Integer> resolved = currentResolved;
      if (resolved != null) {
         Integer hit = resolved.get(cube);
         if (hit != null) {
            return hit.equals(UNORDERED) ? null : hit;
         }
      }

      Integer ord = currentOrder == null ? null : currentOrder.get(geomId(cube, rootSpace));
      if (resolved != null) {
         resolved.put(cube, ord == null ? UNORDERED : ord);
      }

      return ord;
   }

   private static long geomId(Cube c, Matrix4f rootSpace) {
      Vector3f centre = new Vector3f((c.minX + c.maxX) * 0.5F / 16.0F, (c.minY + c.maxY) * 0.5F / 16.0F, (c.minZ + c.maxZ) * 0.5F / 16.0F);
      rootSpace.transformPosition(centre);
      long h = 1469598103934665603L;
      float[] v = new float[]{c.minX, c.minY, c.minZ, c.maxX, c.maxY, c.maxZ, centre.x, centre.y, centre.z};

      for (float f : v) {
         h ^= Float.floatToIntBits(Math.round(f * 16.0F) / 16.0F);
         h *= 1099511628211L;
      }

      return h;
   }

   private static double gapBetween(net.dabicco.witherstormmod.client.CubeReveal.CubeBox a, net.dabicco.witherstormmod.client.CubeReveal.CubeBox b) {
      double dx = Math.max(0.0F, Math.max(a.minX - b.maxX, b.minX - a.maxX));
      double dy = Math.max(0.0F, Math.max(a.minY - b.maxY, b.minY - a.maxY));
      double dz = Math.max(0.0F, Math.max(a.minZ - b.maxZ, b.minZ - a.maxZ));
      return dx * dx + dy * dy + dz * dz;
   }

   private static boolean touches(net.dabicco.witherstormmod.client.CubeReveal.CubeBox a, net.dabicco.witherstormmod.client.CubeReveal.CubeBox b) {
      return a.minX - 0.34F <= b.maxX
         && a.maxX + 0.34F >= b.minX
         && a.minY - 0.34F <= b.maxY
         && a.maxY + 0.34F >= b.minY
         && a.minZ - 0.34F <= b.maxZ
         && a.maxZ + 0.34F >= b.minZ;
   }

   private static Map<Long, Integer> orderFor(ModelPart root, boolean bottomUpInWorld) {
      Map<Long, Integer> cached = ORDER_CACHE.get(root);
      if (cached != null) {
         return cached;
      } else {
         List<net.dabicco.witherstormmod.client.CubeReveal.CubeBox> boxes = new ArrayList<>();
         collectBoxes(root, new PoseStack(), boxes, bottomUpInWorld);
         Map<Long, Integer> order = buildOrder(boxes);
         ORDER_CACHE.put(root, order);
         RESOLVED.put(root, new IdentityHashMap<>());
         ORDER_COUNT.put(root, Math.max(1, boxes.size()));
         return order;
      }
   }

   private static Map<Long, Integer> buildOrder(List<net.dabicco.witherstormmod.client.CubeReveal.CubeBox> boxes) {
      Map<Long, Integer> order = new HashMap<>();
      int n = boxes.size();
      if (n == 0) {
         return order;
      } else {
         float CELL = 2.0F;
         Map<Long, List<Integer>> grid = new HashMap<>();

         for (int i = 0; i < n; i++) {
            net.dabicco.witherstormmod.client.CubeReveal.CubeBox b = boxes.get(i);
            int x0 = (int)Math.floor((b.minX() - 0.34F) / 2.0F);
            int x1 = (int)Math.floor((b.maxX() + 0.34F) / 2.0F);
            int y0 = (int)Math.floor((b.minY() - 0.34F) / 2.0F);
            int y1 = (int)Math.floor((b.maxY() + 0.34F) / 2.0F);
            int z0 = (int)Math.floor((b.minZ() - 0.34F) / 2.0F);
            int z1 = (int)Math.floor((b.maxZ() + 0.34F) / 2.0F);

            for (int x = x0; x <= x1; x++) {
               for (int y = y0; y <= y1; y++) {
                  for (int z = z0; z <= z1; z++) {
                     grid.computeIfAbsent(cellKey(x, y, z), k -> new ArrayList<>()).add(i);
                  }
               }
            }
         }

         boolean[] taken = new boolean[n];
         int placed = 0;
         Integer[] byHeight = new Integer[n];

         for (int i = 0; i < n; i++) {
            byHeight[i] = i;
         }

         Arrays.sort(byHeight, (a, bx) -> Float.compare(boxes.get(a).height(), boxes.get(bx).height()));
         PriorityQueue<Integer> frontier = new PriorityQueue<>((a, bx) -> Float.compare(boxes.get(a).height(), boxes.get(bx).height()));
         float lowest = boxes.get(byHeight[0]).height();

         for (Integer i : byHeight) {
            if (boxes.get(i).height() - lowest > 1.2F) {
               break;
            }

            if (!taken[i]) {
               taken[i] = true;
               frontier.add(i);
            }
         }

         drain(frontier, boxes, grid, taken, order, new int[]{placed});
         int var19 = order.size();

         while (var19 < boxes.size()) {
            int seedIdx = -1;
            double bestGap = Double.MAX_VALUE;

            for (int i = 0; i < boxes.size(); i++) {
               if (!taken[i]) {
                  for (int j = 0; j < boxes.size(); j++) {
                     if (taken[j]) {
                        double gap = gapBetween(boxes.get(i), boxes.get(j));
                        if (gap < bestGap) {
                           bestGap = gap;
                           seedIdx = i;
                        }
                     }
                  }
               }
            }

            if (seedIdx < 0) {
               for (Integer idx : byHeight) {
                  if (!taken[idx]) {
                     seedIdx = idx;
                     break;
                  }
               }
            }

            if (seedIdx < 0) {
               break;
            }

            frontier.clear();
            frontier.add(seedIdx);
            taken[seedIdx] = true;
            int[] counter = new int[]{var19};
            drain(frontier, boxes, grid, taken, order, counter);
            var19 = counter[0];
         }

         return order;
      }
   }

   private static void drain(
      PriorityQueue<Integer> frontier,
      List<net.dabicco.witherstormmod.client.CubeReveal.CubeBox> boxes,
      Map<Long, List<Integer>> grid,
      boolean[] taken,
      Map<Long, Integer> order,
      int[] placed
   ) {
      float CELL = 2.0F;

      while (!frontier.isEmpty()) {
         int cur = frontier.poll();
         order.putIfAbsent(boxes.get(cur).id(), placed[0]);
         placed[0]++;
         net.dabicco.witherstormmod.client.CubeReveal.CubeBox c = boxes.get(cur);
         int x0 = (int)Math.floor((c.minX() - 0.34F) / 2.0F);
         int x1 = (int)Math.floor((c.maxX() + 0.34F) / 2.0F);
         int y0 = (int)Math.floor((c.minY() - 0.34F) / 2.0F);
         int y1 = (int)Math.floor((c.maxY() + 0.34F) / 2.0F);
         int z0 = (int)Math.floor((c.minZ() - 0.34F) / 2.0F);
         int z1 = (int)Math.floor((c.maxZ() + 0.34F) / 2.0F);

         for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
               for (int z = z0; z <= z1; z++) {
                  List<Integer> bucket = grid.get(cellKey(x, y, z));
                  if (bucket != null) {
                     for (int idx : bucket) {
                        if (!taken[idx] && touches(c, boxes.get(idx))) {
                           taken[idx] = true;
                           frontier.add(idx);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static long cellKey(int x, int y, int z) {
      return (long)(x & 2097151) << 42 | (long)(y & 2097151) << 21 | z & 2097151;
   }

   private static void collectBoxes(ModelPart part, PoseStack pose, List<net.dabicco.witherstormmod.client.CubeReveal.CubeBox> out, boolean bottomUpInWorld) {
      pose.pushPose();
      part.translateAndRotate(pose);
      Matrix4f m = pose.last().pose();

      for (Cube cube : cubesOf(part)) {
         if (!isDegenerate(cube)) {
            Vector3f lo = new Vector3f(cube.minX / 16.0F, cube.minY / 16.0F, cube.minZ / 16.0F);
            Vector3f hi = new Vector3f(cube.maxX / 16.0F, cube.maxY / 16.0F, cube.maxZ / 16.0F);
            m.transformPosition(lo);
            m.transformPosition(hi);
            float minX = Math.min(lo.x, hi.x);
            float maxX = Math.max(lo.x, hi.x);
            float minY = Math.min(lo.y, hi.y);
            float maxY = Math.max(lo.y, hi.y);
            float minZ = Math.min(lo.z, hi.z);
            float maxZ = Math.max(lo.z, hi.z);
            float centreY = (minY + maxY) * 0.5F;
            out.add(
               new net.dabicco.witherstormmod.client.CubeReveal.CubeBox(
                  geomId(cube, m), minX, minY, minZ, maxX, maxY, maxZ, bottomUpInWorld ? -centreY : centreY
               )
            );
         }
      }

      for (ModelPart child : childrenOf(part).values()) {
         collectBoxes(child, pose, out, bottomUpInWorld);
      }

      pose.popPose();
   }

   private CubeReveal() {
   }

   public static float[] bounds(ModelPart root) {
      float[] cached = BOUNDS_CACHE.get(root);
      if (cached != null) {
         return cached;
      } else {
         float[] b = new float[]{Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE};
         accumulateBounds(root, new PoseStack(), b);
         if (b[0] > b[1]) {
            b = new float[]{0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F};
         }

         BOUNDS_CACHE.put(root, b);
         return b;
      }
   }

   private static void accumulateBounds(ModelPart part, PoseStack pose, float[] b) {
      pose.pushPose();
      part.translateAndRotate(pose);
      Matrix4f m = pose.last().pose();
      Vector3f v = new Vector3f();

      for (Cube cube : cubesOf(part)) {
         if (!isDegenerate(cube)) {
            for (int c = 0; c < 8; c++) {
               v.set(
                  ((c & 1) == 0 ? cube.minX : cube.maxX) / 16.0F,
                  ((c & 2) == 0 ? cube.minY : cube.maxY) / 16.0F,
                  ((c & 4) == 0 ? cube.minZ : cube.maxZ) / 16.0F
               );
               m.transformPosition(v);
               b[0] = Math.min(b[0], v.x);
               b[1] = Math.max(b[1], v.x);
               b[2] = Math.min(b[2], v.y);
               b[3] = Math.max(b[3], v.y);
               b[4] = Math.min(b[4], v.z);
               b[5] = Math.max(b[5], v.z);
            }
         }
      }

      for (ModelPart child : childrenOf(part).values()) {
         accumulateBounds(child, pose, b);
      }

      pose.popPose();
   }

   private static List<Cube> cubesOf(ModelPart part) {
      return ((ModelPartCubesAccessor)part).dabyws$getCubes();
   }

   private static Map<String, ModelPart> childrenOf(ModelPart part) {
      return ((ModelPartAccessor)part).getChildren();
   }

   public static void render(
      ModelPart root,
      PoseStack poseStack,
      VertexConsumer consumer,
      int light,
      int overlay,
      int color,
      float progress,
      boolean bottomUpInWorld,
      boolean dropSheets
   ) {
      if (!(progress <= 0.0F)) {
         statSubmits++;
         currentOrder = orderFor(root, bottomUpInWorld);
         if (!currentOrder.isEmpty()) {
            currentResolved = RESOLVED.get(root);
            float threshold = ORDER_COUNT.getOrDefault(root, 1).intValue() * progress;
            renderFiltered(root, poseStack, new PoseStack(), consumer, light, overlay, color, threshold, bottomUpInWorld, dropSheets);
         }
      }
   }

   public static void renderMirroredGroups(
      ModelPart root,
      PoseStack poseStack,
      VertexConsumer consumer,
      int light,
      int overlay,
      int color,
      float progress,
      boolean bottomUpInWorld,
      float push,
      int groupDepth
   ) {
      if (!(progress <= 0.0F)) {
         currentOrder = orderFor(root, bottomUpInWorld);
         if (!currentOrder.isEmpty()) {
            currentResolved = RESOLVED.get(root);
            walkMirrored(
               root,
               poseStack,
               new PoseStack(),
               consumer,
               light,
               overlay,
               color,
               ORDER_COUNT.getOrDefault(root, 1).intValue() * progress,
               bottomUpInWorld,
               push,
               groupDepth,
               0
            );
         }
      }
   }

   private static float threshold(float[] sorted, float progress) {
      int idx = Math.min(sorted.length - 1, Math.max(0, (int)(progress * sorted.length)));
      return sorted[idx];
   }

   private static boolean thinnedFromMirror(int order) {
      double detail = DabyWSClientConfig.mirrorBackDetail;
      return detail >= 0.999 ? false : order * 61 % 100 >= (int)(detail * 100.0);
   }

   private static void walkMirrored(
      ModelPart part,
      PoseStack poseStack,
      PoseStack keyPose,
      VertexConsumer consumer,
      int light,
      int overlay,
      int color,
      float threshold,
      boolean bottomUpInWorld,
      float push,
      int groupDepth,
      int depth
   ) {
      if (part.visible) {
         float[] b = bounds(part);
         boolean isGroup = depth >= groupDepth && b[1] > b[0];
         if (isGroup) {
            float zPlane = (b[4] + b[5]) * 0.5F + (b[5] - b[4]) * 0.5F * push;
            float xCentre = (b[0] + b[1]) * 0.5F;
            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, zPlane);
            poseStack.scale(1.0F, 1.0F, -1.0F);
            poseStack.translate(0.0, 0.0, -zPlane);
            poseStack.translate(xCentre, 0.0, 0.0);
            poseStack.scale(-1.0F, 1.0F, 1.0F);
            poseStack.translate(-xCentre, 0.0, 0.0);
            renderFiltered(part, poseStack, keyPose, consumer, light, overlay, color, threshold, bottomUpInWorld, true);
            poseStack.popPose();
         } else {
            poseStack.pushPose();
            keyPose.pushPose();
            part.translateAndRotate(poseStack);
            part.translateAndRotate(keyPose);
            Matrix4f rootSpace = keyPose.last().pose();
            Pose pose = poseStack.last();

            for (Cube cube : cubesOf(part)) {
               if (!skip(cube, true)) {
                  Integer ord = orderOf(cube, rootSpace);
                  if (ord != null && !thinnedFromMirror(ord) && ord.intValue() < threshold) {
                     statCubes++;
                     cube.compile(pose, consumer, light, overlay, color);
                  }
               }
            }

            for (ModelPart child : childrenOf(part).values()) {
               walkMirrored(child, poseStack, keyPose, consumer, light, overlay, color, threshold, bottomUpInWorld, push, groupDepth, depth + 1);
            }

            keyPose.popPose();
            poseStack.popPose();
         }
      }
   }

   private static void collectKeys(ModelPart part, PoseStack pose, List<Float> all, boolean bottomUpInWorld) {
      pose.pushPose();
      part.translateAndRotate(pose);
      Matrix4f m = pose.last().pose();

      for (Cube cube : cubesOf(part)) {
         if (!isDegenerate(cube)) {
            all.add(cubeKey(cube, m, bottomUpInWorld));
         }
      }

      for (ModelPart child : childrenOf(part).values()) {
         collectKeys(child, pose, all, bottomUpInWorld);
      }

      pose.popPose();
   }

   private static float cubeKey(Cube cube, Matrix4f rootSpace, boolean bottomUpInWorld) {
      Vector3f v = new Vector3f((cube.minX + cube.maxX) * 0.5F / 16.0F, (cube.minY + cube.maxY) * 0.5F / 16.0F, (cube.minZ + cube.maxZ) * 0.5F / 16.0F);
      rootSpace.transformPosition(v);
      return bottomUpInWorld ? -v.y : v.y;
   }

   private static void renderFiltered(
      ModelPart part,
      PoseStack poseStack,
      PoseStack keyPose,
      VertexConsumer consumer,
      int light,
      int overlay,
      int color,
      float threshold,
      boolean bottomUpInWorld,
      boolean dropSheets
   ) {
      if (part.visible) {
         poseStack.pushPose();
         keyPose.pushPose();
         part.translateAndRotate(poseStack);
         part.translateAndRotate(keyPose);
         Pose pose = poseStack.last();
         Matrix4f rootSpace = keyPose.last().pose();

         for (Cube cube : cubesOf(part)) {
            if (!skip(cube, dropSheets)) {
               Integer ord = orderOf(cube, rootSpace);
               if (ord != null && (!dropSheets || !thinnedFromMirror(ord)) && ord.intValue() < threshold) {
                  statCubes++;
                  cube.compile(pose, consumer, light, overlay, color);
               }
            }
         }

         for (ModelPart child : childrenOf(part).values()) {
            renderFiltered(child, poseStack, keyPose, consumer, light, overlay, color, threshold, bottomUpInWorld, dropSheets);
         }

         keyPose.popPose();
         poseStack.popPose();
      }
   }

   private record CubeBox(long id, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float height) {
   }
}
