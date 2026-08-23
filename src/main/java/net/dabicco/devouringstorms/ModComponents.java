package net.dabicco.devouringstorms;

import net.dabicco.devouringstorms.item.RetrieverContents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModComponents {
   public static final DataComponentType<RetrieverContents> RETRIEVER_CONTENTS;

   public static void register() {
      Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, DevouringStormsMod.id("retriever_contents"), RETRIEVER_CONTENTS);
   }

   static {
      RETRIEVER_CONTENTS = DataComponentType.<RetrieverContents>builder().persistent(RetrieverContents.CODEC).networkSynchronized(RetrieverContents.STREAM_CODEC).cacheEncoding().build();
   }
}
