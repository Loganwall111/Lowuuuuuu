package net.dabicco.witherstormmod.structures;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class McsmSchematic {
   public final int width;
   public final int height;
   public final int length;
   public final byte[] blocks;
   public final byte[] data;
   private static final int TAG_END = 0;
   private static final int TAG_BYTE = 1;
   private static final int TAG_SHORT = 2;
   private static final int TAG_INT = 3;
   private static final int TAG_LONG = 4;
   private static final int TAG_FLOAT = 5;
   private static final int TAG_DOUBLE = 6;
   private static final int TAG_BYTE_ARRAY = 7;
   private static final int TAG_STRING = 8;
   private static final int TAG_LIST = 9;
   private static final int TAG_COMPOUND = 10;
   private static final int TAG_INT_ARRAY = 11;
   private static final int TAG_LONG_ARRAY = 12;

   private McsmSchematic(int w, int h, int l, byte[] blocks, byte[] data) {
      this.width = w;
      this.height = h;
      this.length = l;
      this.blocks = blocks;
      this.data = data;
   }

   public int volume() {
      return this.width * this.height * this.length;
   }

   public int index(int x, int y, int z) {
      return (y * this.length + z) * this.width + x;
   }

   public int blockId(int x, int y, int z) {
      return this.blocks[this.index(x, y, z)] & 0xFF;
   }

   public int blockData(int x, int y, int z) {
      return this.data[this.index(x, y, z)] & 15;
   }

   public static net.dabicco.witherstormmod.structures.McsmSchematic load(ResourceManager rm, String path) throws IOException {
      String full = "assets/dabywitherstormmod/schematics/" + path;
      InputStream in = net.dabicco.witherstormmod.structures.McsmSchematic.class.getClassLoader().getResourceAsStream(full);
      if (in == null && rm != null) {
         Identifier id = Identifier.fromNamespaceAndPath("dabywitherstormmod", "schematics/" + path);
         Optional<Resource> res = rm.getResource(id);
         if (res.isPresent()) {
            in = res.get().open();
         }
      }

      if (in == null) {
         throw new IOException("schematic not found: " + path);
      } else {
         InputStream raw = in;

         net.dabicco.witherstormmod.structures.McsmSchematic var10;
         try {
            var10 = read(raw);
         } catch (Throwable var8) {
            if (in != null) {
               try {
                  raw.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (in != null) {
            in.close();
         }

         return var10;
      }
   }

   public static net.dabicco.witherstormmod.structures.McsmSchematic read(InputStream raw) throws IOException {
      BufferedInputStream buf = new BufferedInputStream(raw, 65536);
      buf.mark(2);
      int b0 = buf.read();
      int b1 = buf.read();
      buf.reset();
      InputStream body = (InputStream)(b0 == 31 && b1 == 139 ? new GZIPInputStream(buf, 65536) : buf);
      DataInputStream in = new DataInputStream(new BufferedInputStream(body, 65536));
      int root = in.readUnsignedByte();
      if (root != 10) {
         throw new IOException("not an NBT compound");
      } else {
         in.skipBytes(in.readUnsignedShort());
         short w = 0;
         short h = 0;
         short l = 0;
         byte[] blocks = null;
         byte[] data = null;

         while (true) {
            int tag = in.readUnsignedByte();
            if (tag == 0) {
               if (blocks != null && w > 0 && h > 0 && l > 0) {
                  if (data == null) {
                     data = new byte[blocks.length];
                  }

                  return new net.dabicco.witherstormmod.structures.McsmSchematic(w, h, l, blocks, data);
               }

               throw new IOException("schematic missing Blocks/dimensions");
            }

            int nameLen = in.readUnsignedShort();
            byte[] nb = new byte[nameLen];
            in.readFully(nb);
            String name = new String(nb, StandardCharsets.UTF_8);
            switch (tag) {
               case 2:
                  short v = in.readShort();
                  switch (name) {
                     case "Width":
                        w = v;
                        continue;
                     case "Height":
                        h = v;
                        continue;
                     case "Length":
                        l = v;
                     default:
                        continue;
                  }
               case 7:
                  int n = in.readInt();
                  byte[] arr = new byte[n];
                  in.readFully(arr);
                  if ("Blocks".equals(name)) {
                     blocks = arr;
                  } else if ("Data".equals(name)) {
                     data = arr;
                  }
                  break;
               default:
                  skip(in, tag);
            }
         }
      }
   }

   private static void skip(DataInputStream in, int tag) throws IOException {
      switch (tag) {
         case 1:
            in.skipBytes(1);
            break;
         case 2:
            in.skipBytes(2);
            break;
         case 3:
         case 5:
            in.skipBytes(4);
            break;
         case 4:
         case 6:
            in.skipBytes(8);
            break;
         case 7:
            skipFully(in, in.readInt());
            break;
         case 8:
            skipFully(in, in.readUnsignedShort());
            break;
         case 9:
            int st = in.readUnsignedByte();
            int n = in.readInt();

            for (int i = 0; i < n; i++) {
               skip(in, st);
            }
            break;
         case 10:
            while (true) {
               int tt = in.readUnsignedByte();
               if (tt == 0) {
                  return;
               }

               skipFully(in, in.readUnsignedShort());
               skip(in, tt);
            }
         case 11:
            skipFully(in, 4L * in.readInt());
            break;
         case 12:
            skipFully(in, 8L * in.readInt());
            break;
         default:
            throw new IOException("bad NBT tag " + tag);
      }
   }

   private static void skipFully(DataInputStream in, long n) throws IOException {
      long left = n;

      while (left > 0L) {
         long got = in.skip(left);
         if (got <= 0L) {
            if (in.read() < 0) {
               throw new IOException("unexpected EOF");
            }

            got = 1L;
         }

         left -= got;
      }
   }

   public String describe() {
      return this.width + "x" + this.height + "x" + this.length + " (" + this.volume() + " blocks)";
   }

   static void touch(ServerLevel unused) {
   }
}
