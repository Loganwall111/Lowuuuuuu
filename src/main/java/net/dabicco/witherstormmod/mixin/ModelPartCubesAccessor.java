package net.dabicco.witherstormmod.mixin;

import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Cube;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ModelPart.class})
public interface ModelPartCubesAccessor extends ModelPart {
   @Accessor("cubes")
   List<Cube> dabyws$getCubes();
}
