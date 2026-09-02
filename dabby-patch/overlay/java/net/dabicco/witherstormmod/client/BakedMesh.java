package net.dabicco.witherstormmod.client;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStreamReader;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.phys.Vec3;

/** Tiny cached JSON mesh loader for debris that is too irregular for billboard particles. */
public final class BakedMesh {
 public record Mesh(float[] tris,float[] uvs,float[] normals) {}
 private static final Map<String,Mesh> CACHE=new HashMap<>();
 public static Mesh mesh(String name) { return CACHE.computeIfAbsent(name,BakedMesh::load); }
 private static Mesh load(String n) { try { Optional<Resource> r=Minecraft.getInstance().getResourceManager().getResource(Identifier.fromNamespaceAndPath("dabywitherstormmod","meshes/"+n+".json")); if(r.isEmpty()) return new Mesh(new float[0],new float[0],new float[0]); JsonObject o=JsonParser.parseReader(new InputStreamReader(r.get().open())).getAsJsonObject(); return new Mesh(values(o.getAsJsonArray("tris")),values(o.getAsJsonArray("uvs")),values(o.getAsJsonArray("normals"))); } catch(Exception e) { return new Mesh(new float[0],new float[0],new float[0]); } }
 private static float[] values(JsonArray a) { float[] x=new float[a.size()]; for(int i=0;i<x.length;i++)x[i]=a.get(i).getAsFloat(); return x; }
 public static void emit(VertexConsumer c, PoseStack.Pose p, Mesh m, Vec3 pos, float yaw, float tumble, float scale, int r,int g,int b,int a,int light) { double y=Math.toRadians(yaw), x=Math.toRadians(tumble); for(int i=0;i<m.tris.length/9;i++){ float nx=m.normals[i*3],ny=m.normals[i*3+1],nz=m.normals[i*3+2]; float lam=(float)(.66+.34*Math.max(0,Math.min(1,nx*.25+ny*.88+nz*.38))); for(int q=0;q<4;q++){int j=i*9+Math.min(q,2)*3,u=i*6+Math.min(q,2)*2; float px=m.tris[j]*scale,pz=m.tris[j+2]*scale,py=m.tris[j+1]*scale; float rx=(float)(px*Math.cos(y)-pz*Math.sin(y)),rz=(float)(px*Math.sin(y)+pz*Math.cos(y)); float ry=(float)(py*Math.cos(x)-rz*Math.sin(x)); rz=(float)(py*Math.sin(x)+rz*Math.cos(x)); c.addVertex(p,(float)pos.x+rx,(float)pos.y+ry,(float)pos.z+rz).setColor((int)(r*lam),(int)(g*lam),(int)(b*lam),a).setUv(m.uvs[u],m.uvs[u+1]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,nx,ny,nz); } } }
}
