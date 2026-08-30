package net.dabicco.witherstormmod.world;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/** Registry-backed procedural city structure type. Placement is supplied by its datapack structure set. */
public class AbandonedCityStructure extends Structure {
   public static final MapCodec<AbandonedCityStructure> CODEC = simpleCodec(AbandonedCityStructure::new);
   public AbandonedCityStructure(StructureSettings settings) { super(settings); }
   @Override protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) { return Optional.empty(); }
   @Override public StructureType<?> type() { return ModWorldgen.ABANDONED_CITY; }
}
