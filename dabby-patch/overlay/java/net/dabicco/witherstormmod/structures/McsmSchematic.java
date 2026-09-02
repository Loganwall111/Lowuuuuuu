package net.dabicco.witherstormmod.structures;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * McsmSchematic — reads a legacy MCEdit ".schematic" and stamps it into a world.
 *
 * The 181 official Minecraft: Story Mode builds ship in MCEdit "Alpha" format:
 * a gzipped NBT compound holding Width/Height/Length shorts, a Blocks byte
 * array of legacy numeric IDs, and a Data byte array of nibbles. Numeric IDs
 * were removed from Minecraft in 1.13, so {@link LegacyBlocks} maps them back.
 *
 * WHY NOT VANILLA STRUCTURES
 * --------------------------
 * Vanilla's structure system caps a template at 48x48x48. These builds are far
 * bigger - adv_creepyMansionFull is 489x257x1262, and 142 of the 181 exceed the
 * limit - so they cannot be converted to .nbt templates. Reading the raw
 * .schematic and calling setBlock directly is the only way to place them whole.
 *
 * Placement is chunked across ticks by {@link McsmWorldgen} so a 158-million
 * block mansion does not freeze the server thread.
 */
public final class McsmSchematic {

   public final int width;
   public final int height;
   public final int length;
   public final byte[] blocks;
   public final byte[] data;

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

   /** Index into the flat arrays. MCEdit order is Y, then Z, then X. */
   public int index(int x, int y, int z) {
      return (y * this.length + z) * this.width + x;
   }

   public int blockId(int x, int y, int z) {
      return this.blocks[this.index(x, y, z)] & 255;
   }

   public int blockData(int x, int y, int z) {
      return this.data[this.index(x, y, z)] & 15;
   }

   /* ------------------------------------------------------------------ */
   /* NBT reading                                                         */
   /* ------------------------------------------------------------------ */

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

   /**
    * Load a schematic from the mod's resources.
    *
    * @param path e.g. "MC201/adv_beaconTown.schematic" (relative to
    *             assets/dabywitherstormmod/schematics/)
    */
   public static McsmSchematic load(ResourceManager rm, String path) throws IOException {
      /* The schematics live under assets/, but this is called on the SERVER,
       * whose ResourceManager only ever exposes data/. That mismatch meant
       * every /mcsm build silently failed with "schematic not found".
       *
       * Read straight out of the mod jar instead: that works on both sides,
       * in dev and in production, and needs no datapack duplication. */
      String full = "assets/dabywitherstormmod/schematics/" + path;
      InputStream in = McsmSchematic.class.getClassLoader().getResourceAsStream(full);
      if (in == null && rm != null) {
         // fall back to the resource manager (client / datapack overrides)
         Identifier id = Identifier.fromNamespaceAndPath("dabywitherstormmod", "schematics/" + path);
         var res = rm.getResource(id);
         if (res.isPresent()) {
            in = res.get().open();
         }
      }
      if (in == null) {
         throw new IOException("schematic not found: " + path);
      }
      try (InputStream raw = in) {
         return read(raw);
      }
   }

   public static McsmSchematic read(InputStream raw) throws IOException {
      BufferedInputStream buf = new BufferedInputStream(raw, 1 << 16);
      buf.mark(2);
      int b0 = buf.read();
      int b1 = buf.read();
      buf.reset();
      InputStream body = (b0 == 0x1F && b1 == 0x8B)
                         ? new GZIPInputStream(buf, 1 << 16)
                         : buf;

      DataInputStream in = new DataInputStream(new BufferedInputStream(body, 1 << 16));

      int root = in.readUnsignedByte();
      if (root != TAG_COMPOUND) {
         throw new IOException("not an NBT compound");
      }
      in.skipBytes(in.readUnsignedShort()); // root name

      short w = 0;
      short h = 0;
      short l = 0;
      byte[] blocks = null;
      byte[] data = null;

      while (true) {
         int tag = in.readUnsignedByte();
         if (tag == TAG_END) {
            break;
         }
         int nameLen = in.readUnsignedShort();
         byte[] nb = new byte[nameLen];
         in.readFully(nb);
         String name = new String(nb, java.nio.charset.StandardCharsets.UTF_8);

         switch (tag) {
            case TAG_SHORT -> {
               short v = in.readShort();
               switch (name) {
                  case "Width" -> w = v;
                  case "Height" -> h = v;
                  case "Length" -> l = v;
                  default -> { }
               }
            }
            case TAG_BYTE_ARRAY -> {
               int n = in.readInt();
               byte[] arr = new byte[n];
               in.readFully(arr);
               if ("Blocks".equals(name)) {
                  blocks = arr;
               } else if ("Data".equals(name)) {
                  data = arr;
               }
            }
            default -> skip(in, tag);
         }
      }

      if (blocks == null || w <= 0 || h <= 0 || l <= 0) {
         throw new IOException("schematic missing Blocks/dimensions");
      }
      if (data == null) {
         data = new byte[blocks.length];
      }
      return new McsmSchematic(w, h, l, blocks, data);
   }

   private static void skip(DataInputStream in, int tag) throws IOException {
      switch (tag) {
         case TAG_BYTE -> in.skipBytes(1);
         case TAG_SHORT -> in.skipBytes(2);
         case TAG_INT, TAG_FLOAT -> in.skipBytes(4);
         case TAG_LONG, TAG_DOUBLE -> in.skipBytes(8);
         case TAG_BYTE_ARRAY -> skipFully(in, in.readInt());
         case TAG_STRING -> skipFully(in, in.readUnsignedShort());
         case TAG_LIST -> {
            int st = in.readUnsignedByte();
            int n = in.readInt();
            for (int i = 0; i < n; i++) {
               skip(in, st);
            }
         }
         case TAG_COMPOUND -> {
            while (true) {
               int tt = in.readUnsignedByte();
               if (tt == TAG_END) {
                  return;
               }
               skipFully(in, in.readUnsignedShort());
               skip(in, tt);
            }
         }
         case TAG_INT_ARRAY -> skipFully(in, 4L * in.readInt());
         case TAG_LONG_ARRAY -> skipFully(in, 8L * in.readInt());
         default -> throw new IOException("bad NBT tag " + tag);
      }
   }

   /** skipBytes can short-read on a stream; loop until the count is consumed. */
   private static void skipFully(DataInputStream in, long n) throws IOException {
      long left = n;
      while (left > 0) {
         long got = in.skip(left);
         if (got <= 0) {
            if (in.read() < 0) {
               throw new IOException("unexpected EOF");
            }
            got = 1;
         }
         left -= got;
      }
   }

   /** Convenience for tests/commands: how big this would be in a world. */
   public String describe() {
      return this.width + "x" + this.height + "x" + this.length + " (" + this.volume() + " blocks)";
   }

   /** Unused placeholder so ServerLevel stays imported for future helpers. */
   static void touch(ServerLevel unused) {
   }
}
