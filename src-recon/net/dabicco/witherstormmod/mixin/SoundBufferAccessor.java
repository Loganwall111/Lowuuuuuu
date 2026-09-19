package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.audio.SoundBuffer;
import java.nio.ByteBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({SoundBuffer.class})
public interface SoundBufferAccessor {
   @Accessor("data")
   ByteBuffer dabyws$getData();
}
