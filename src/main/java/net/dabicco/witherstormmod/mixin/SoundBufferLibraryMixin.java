package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.audio.SoundBuffer;
import java.util.concurrent.CompletableFuture;
import net.dabicco.witherstormmod.client.sound.MonoAudioStream;
import net.dabicco.witherstormmod.client.sound.MonoDownmix;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({SoundBufferLibrary.class})
public class SoundBufferLibraryMixin {
   @Inject(
      method = {"getCompleteBuffer"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void dabyws$monoBuffer(Identifier id, CallbackInfoReturnable<CompletableFuture<SoundBuffer>> cir) {
      if (id.getNamespace().equals("dabywitherstormmod")) {
         cir.setReturnValue(((CompletableFuture<SoundBuffer>)cir.getReturnValue()).thenApply(MonoDownmix::toMono));
      }
   }

   @Inject(
      method = {"getStream"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void dabyws$monoStream(Identifier id, boolean looping, CallbackInfoReturnable<CompletableFuture<AudioStream>> cir) {
      if (id.getNamespace().equals("dabywitherstormmod")) {
         cir.setReturnValue(
            ((CompletableFuture<AudioStream>)cir.getReturnValue())
               .thenApply(stream -> (AudioStream)(MonoDownmix.isStereo16(stream.getFormat()) ? new MonoAudioStream(stream) : stream))
         );
      }
   }
}
