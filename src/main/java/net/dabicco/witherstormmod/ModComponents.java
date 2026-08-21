package net.dabicco.witherstormmod;

import net.dabicco.witherstormmod.item.RetrieverContents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModComponents {
   public static final DataComponentType<RetrieverContents> RETRIEVER_CONTENTS;

   public static void register() {
      Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, DabyWitherStormMod.id("retriever_contents"), RETRIEVER_CONTENTS);
   }

   static {
      RETRIEVER_CONTENTS = DataComponentType.builder().persistent(RetrieverContents.CODEC).networkSynchronized(RetrieverContents.STREAM_CODEC).cacheEncoding().build();
   }
}
