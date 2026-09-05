package net.dabicco.witherstormmod.client.sound;

import com.mojang.blaze3d.audio.SoundBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioFormat;
import net.dabicco.witherstormmod.mixin.SoundBufferAccessor;

public final class MonoDownmix {
   private MonoDownmix() {
   }

   public static boolean isStereo16(AudioFormat format) {
      return format != null && format.getChannels() == 2 && format.getSampleSizeInBits() == 16;
   }

   public static AudioFormat monoFormat(AudioFormat stereo) {
      return new AudioFormat(stereo.getSampleRate(), 16, 1, true, false);
   }

   public static ByteBuffer stereoToMono(ByteBuffer stereo) {
      ByteBuffer src = stereo.duplicate().order(ByteOrder.LITTLE_ENDIAN);
      int frames = src.remaining() / 4;
      ByteBuffer out = ByteBuffer.allocateDirect(frames * 2).order(ByteOrder.LITTLE_ENDIAN);

      for (int i = 0; i < frames; i++) {
         int l = src.getShort();
         int r = src.getShort();
         out.putShort((short)((l + r) / 2));
      }

      out.flip();
      return out;
   }

   public static SoundBuffer toMono(SoundBuffer buffer) {
      AudioFormat format = buffer.format();
      if (!isStereo16(format)) {
         return buffer;
      } else {
         ByteBuffer data = ((SoundBufferAccessor)buffer).dabyws$getData();
         return data == null ? buffer : new SoundBuffer(stereoToMono(data), monoFormat(format));
      }
   }
}
