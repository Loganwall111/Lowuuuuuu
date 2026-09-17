package net.dabicco.witherstormmod.client.sound;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.sounds.AudioStream;

public final class MonoAudioStream implements AudioStream {
   private final AudioStream delegate;
   private final AudioFormat monoFormat;

   public MonoAudioStream(AudioStream delegate) {
      this.delegate = delegate;
      this.monoFormat = MonoDownmix.monoFormat(delegate.getFormat());
   }

   public AudioFormat getFormat() {
      return this.monoFormat;
   }

   public ByteBuffer read(int size) throws IOException {
      int stereoBytes = size * 2 + 3 & -4;
      ByteBuffer stereo = this.delegate.read(stereoBytes);
      return stereo == null ? null : MonoDownmix.stereoToMono(stereo);
   }

   public void close() throws IOException {
      this.delegate.close();
   }
}
