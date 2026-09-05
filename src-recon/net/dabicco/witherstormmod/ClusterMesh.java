package net.dabicco.witherstormmod.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class ClusterMesh {
   private final List<net.dabicco.witherstormmod.client.ClusterMesh.Group> groups;
   private final int quadCount;

   private ClusterMesh(List<net.dabicco.witherstormmod.client.ClusterMesh.Group> groups, int quadCount) {
      this.groups = groups;
      this.quadCount = quadCount;
   }

   public List<net.dabicco.witherstormmod.client.ClusterMesh.Group> groups() {
      return this.groups;
   }

   public int quadCount() {
      return this.quadCount;
   }

   public static net.dabicco.witherstormmod.client.ClusterMesh bake(
      List<BlockState> blocks, List<BlockPos> offsets, List<boolean[]> faceVisibility, ClientLevel level, BlockPos base
   ) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.getModelManager() == null) {
         return new net.dabicco.witherstormmod.client.ClusterMesh(List.of(), 0);
      } else {
         BlockStateModelSet models = mc.getModelManager().getBlockStateModelSet();
         BlockColors colors = mc.getBlockColors();
         Map<RenderType, List<net.dabicco.witherstormmod.client.ClusterMesh.Piece>> byType = new LinkedHashMap<>();
         List<BlockStateModelPart> parts = new ArrayList<>();
         RandomSource random = RandomSource.create();
         int total = 0;

         for (int i = 0; i < blocks.size() && i < offsets.size(); i++) {
            BlockState state = blocks.get(i);
            if (state.getRenderShape() == RenderShape.MODEL) {
               BlockPos offset = offsets.get(i);
               BlockPos worldPos = base.offset(offset);
               boolean[] visible = i < faceVisibility.size() ? faceVisibility.get(i) : null;
               parts.clear();
               random.setSeed(offset.asLong() * 31L + 17L);
               models.get(state).collectParts(random, parts);

               for (BlockStateModelPart part : parts) {
                  total += collect(byType, part.getQuads((Direction)null), offset, state, colors, level, worldPos);

                  for (Direction direction : Direction.values()) {
                     if (visible == null || visible[direction.ordinal()]) {
                        total += collect(byType, part.getQuads(direction), offset, state, colors, level, worldPos);
                     }
                  }
               }
            }
         }

         List<net.dabicco.witherstormmod.client.ClusterMesh.Group> groups = new ArrayList<>(byType.size());

         for (Entry<RenderType, List<net.dabicco.witherstormmod.client.ClusterMesh.Piece>> e : byType.entrySet()) {
            groups.add(new net.dabicco.witherstormmod.client.ClusterMesh.Group(e.getKey(), List.copyOf(e.getValue())));
         }

         return new net.dabicco.witherstormmod.client.ClusterMesh(List.copyOf(groups), total);
      }
   }

   private static int collect(
      Map<RenderType, List<net.dabicco.witherstormmod.client.ClusterMesh.Piece>> byType,
      List<BakedQuad> quads,
      BlockPos offset,
      BlockState state,
      BlockColors colors,
      ClientLevel level,
      BlockPos worldPos
   ) {
      for (BakedQuad quad : quads) {
         int tintIndex = quad.materialInfo().tintIndex();
         int color = -1;
         if (tintIndex != -1) {
            BlockTintSource source = colors.getTintSource(state, tintIndex);
            if (source != null) {
               int rgb = level != null ? source.colorInWorld(state, level, worldPos) : source.color(state);
               color = 0xFF000000 | rgb & 16777215;
            }
         }

         byType.computeIfAbsent(quad.materialInfo().itemRenderType(), k -> new ArrayList<>())
            .add(new net.dabicco.witherstormmod.client.ClusterMesh.Piece(translate(quad, offset), color, state.getLightEmission()));
      }

      return quads.size();
   }

   private static BakedQuad translate(BakedQuad quad, BlockPos offset) {
      float ox = offset.getX();
      float oy = offset.getY();
      float oz = offset.getZ();
      return new BakedQuad(
         shift(quad.position0(), ox, oy, oz),
         shift(quad.position1(), ox, oy, oz),
         shift(quad.position2(), ox, oy, oz),
         shift(quad.position3(), ox, oy, oz),
         quad.packedUV0(),
         quad.packedUV1(),
         quad.packedUV2(),
         quad.packedUV3(),
         quad.direction(),
         quad.materialInfo()
      );
   }

   private static Vector3fc shift(Vector3fc v, float x, float y, float z) {
      return new Vector3f(v.x() + x, v.y() + y, v.z() + z);
   }

   public record Group(RenderType renderType, List<net.dabicco.witherstormmod.client.ClusterMesh.Piece> pieces) {
   }

   public record Piece(BakedQuad quad, int color, int emission) {
   }
}
