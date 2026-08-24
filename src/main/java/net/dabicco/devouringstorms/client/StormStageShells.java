package net.dabicco.devouringstorms.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Loads compact runtime-safe ports of the shaded Blockbench stage bodies.
 *
 * The original .bbmodel filenames in Traced_shading_Textures include spaces and
 * huge embedded PNG payloads, which makes them awkward to load directly in game.
 * These shell JSON files keep only the BBModel geometry/UV/texture references so
 * the renderer can swap in the shaded Stage B/C/D silhouettes on top of the live
 * storm body without replacing the entire animation system in one step.
 */
public final class StormStageShells {
   private static final Map<String, Shell> CACHE = new HashMap<>();
   private static final int FULL_BRIGHT = 15728880;

   private StormStageShells() {
   }

   public static String shellForPhase(double phase) {
      if (phase >= 6.0) {
         return "stage_d_center_massive_shaded";
      } else if (phase >= 5.8) {
         return "stage_c_massive_shaded";
      } else if (phase >= 5.0) {
         return "stage_c_big_shaded";
      } else {
         return phase >= 4.15 ? "stage_b_shaded" : null;
      }
   }

   public static float targetHeight(double phase) {
      if (phase >= 6.0) {
         return 68.0F;
      } else if (phase >= 5.0) {
         return 64.0F;
      } else {
         return 48.0F;
      }
   }

   public static float targetWidth(double phase) {
      if (phase >= 6.0) {
         return 58.0F;
      } else if (phase >= 5.8) {
         return 54.0F;
      } else if (phase >= 5.0) {
         return 50.0F;
      } else {
         return 34.0F;
      }
   }

   public static void submit(String shellName, double phase, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int tint, float phaseAlpha, boolean emissiveBoost) {
      if (shellName != null && phaseAlpha > 0.01F) {
         Shell shell = shell(shellName);
         if (shell != null && shell.groups.length != 0) {
            float scale = Math.min(targetHeight(phase) / shell.height, targetWidth(phase) / shell.width);
            int baseA = Math.max(0, Math.min(255, (int)(ARGB.alpha(tint) * phaseAlpha)));
            int r = ARGB.red(tint);
            int g = ARGB.green(tint);
            int b = ARGB.blue(tint);

            for (Group group : shell.groups) {
               collector.submitCustomGeometry(poseStack, group.emissive ? GlowRenderTypes.emitterMark(group.texture) : FoglessRenderTypes.bodyCutout(group.texture), (pose, consumer) -> {
                  emitGroup(consumer, pose, group, scale, r, g, b, group.emissive ? Math.max(baseA, ShaderPackCompat.emissiveAlphaFloor(emissiveBoost)) : baseA, group.emissive ? FULL_BRIGHT : packedLight);
               });
            }
         }
      }
   }

   public static void captureShadow(String shellName, double phase, PoseStack poseStack) {
      if (shellName != null) {
         Shell shell = shell(shellName);
         if (shell != null && shell.groups.length != 0) {
            float scale = Math.min(targetHeight(phase) / shell.height, targetWidth(phase) / shell.width);
            for (Group group : shell.groups) {
               StormShadowMap.captureCustomQuads(poseStack, group.quads, group.normals, scale);
            }
         }
      }
   }

   private static void emitGroup(VertexConsumer consumer, PoseStack.Pose pose, Group group, float scale, int r, int g, int b, int a, int packedLight) {
      float[] q = group.quads;
      float[] uv = group.uvs;
      float[] n = group.normals;
      for (int face = 0; face < group.faceCount; face++) {
         float nx = n[face * 3];
         float ny = n[face * 3 + 1];
         float nz = n[face * 3 + 2];
         int qBase = face * 12;
         int uvBase = face * 8;
         for (int corner = 0; corner < 4; corner++) {
            int p = qBase + corner * 3;
            int t = uvBase + corner * 2;
            consumer.addVertex(pose, q[p] * scale, q[p + 1] * scale, q[p + 2] * scale)
               .setColor(r, g, b, a)
               .setUv(uv[t], uv[t + 1])
               .setOverlay(OverlayTexture.NO_OVERLAY)
               .setLight(packedLight)
               .setNormal(pose, nx, ny, nz);
         }
      }
   }

   private static Shell shell(String name) {
      return CACHE.computeIfAbsent(name, StormStageShells::load);
   }

   private static Shell load(String name) {
      try {
         Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(Identifier.fromNamespaceAndPath("devouringstorms", "stage_shells/" + name + ".json"));
         if (resource.isEmpty()) {
            return Shell.EMPTY;
         }

         JsonObject root = JsonParser.parseReader(new InputStreamReader(resource.get().open())).getAsJsonObject();
         JsonArray min = root.getAsJsonArray("min");
         JsonArray max = root.getAsJsonArray("max");
         float centreX = (float)((min.get(0).getAsDouble() + max.get(0).getAsDouble()) * 0.5);
         float centreZ = (float)((min.get(2).getAsDouble() + max.get(2).getAsDouble()) * 0.5);
         float baseY = min.get(1).getAsFloat();
         float width = root.get("width").getAsFloat();
         float height = root.get("height").getAsFloat();
         JsonArray rawGroups = root.getAsJsonArray("groups");
         Group[] groups = new Group[rawGroups.size()];
         for (int i = 0; i < rawGroups.size(); i++) {
            JsonObject g = rawGroups.get(i).getAsJsonObject();
            float[] quads = values(g.getAsJsonArray("quads"));
            for (int p = 0; p < quads.length; p += 3) {
               quads[p] -= centreX;
               quads[p + 1] -= baseY;
               quads[p + 2] -= centreZ;
            }
            groups[i] = new Group(
               Identifier.fromNamespaceAndPath("devouringstorms", g.get("texture").getAsString()),
               g.get("emissive").getAsBoolean(),
               quads,
               values(g.getAsJsonArray("uvs")),
               values(g.getAsJsonArray("normals")),
               g.get("faces").getAsInt()
            );
         }

         return new Shell(width, height, groups);
      } catch (Exception e) {
         System.out.println("[devouringstorms] failed to load stage shell '" + name + "': " + String.valueOf(e));
         return Shell.EMPTY;
      }
   }

   private static float[] values(JsonArray array) {
      float[] out = new float[array.size()];
      int i = 0;
      for (JsonElement element : array) {
         out[i++] = element.getAsFloat();
      }
      return out;
   }

   private record Group(Identifier texture, boolean emissive, float[] quads, float[] uvs, float[] normals, int faceCount) {
   }

   private static final class Shell {
      static final Shell EMPTY = new Shell(1.0F, 1.0F, new Group[0]);
      final float width;
      final float height;
      final Group[] groups;

      Shell(float width, float height, Group[] groups) {
         this.width = Math.max(width, 1.0F);
         this.height = Math.max(height, 1.0F);
         this.groups = groups;
      }
   }
}
