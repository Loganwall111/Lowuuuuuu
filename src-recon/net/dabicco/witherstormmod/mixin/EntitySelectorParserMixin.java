package net.dabicco.witherstormmod.mixin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntitySelectorParser.class})
public abstract class EntitySelectorParserMixin {
   @Shadow
   @Final
   private StringReader reader;
   @Shadow
   private boolean usesSelectors;

   @Shadow
   public abstract void setMaxResults(int var1);

   @Shadow
   public abstract void setIncludesEntities(boolean var1);

   @Shadow
   public abstract void setOrder(BiConsumer<Vec3, List<? extends Entity>> var1);

   @Shadow
   public abstract void addPredicate(Predicate<Entity> var1);

   @Shadow
   public abstract void setSuggestions(BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> var1);

   @Shadow
   private CompletableFuture<Suggestions> suggestOpenOptions(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer) {
      throw new AssertionError();
   }

   @Shadow
   private CompletableFuture<Suggestions> suggestOptionsKeyOrClose(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer) {
      throw new AssertionError();
   }

   @Shadow
   protected abstract void parseOptions() throws CommandSyntaxException;

   @Inject(
      method = {"fillSelectorSuggestions"},
      at = {@At("TAIL")}
   )
   private static void dabywitherstormmod$suggestAtW(SuggestionsBuilder builder, CallbackInfo ci) {
      builder.suggest("@w", Component.literal("Nearest Wither Storm"));
   }

   @Inject(
      method = {"parseSelector"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabywitherstormmod$parseAtW(CallbackInfo ci) throws CommandSyntaxException {
      if (this.reader.canRead() && this.reader.peek() == 'w') {
         this.usesSelectors = true;
         this.reader.skip();
         this.setMaxResults(1);
         this.setIncludesEntities(true);
         this.setOrder(EntitySelectorParser.ORDER_NEAREST);
         this.addPredicate(e -> e instanceof WitherStormEntity && e.isAlive());
         this.setSuggestions(this::suggestOpenOptions);
         if (this.reader.canRead() && this.reader.peek() == '[') {
            this.reader.skip();
            this.setSuggestions(this::suggestOptionsKeyOrClose);
            this.parseOptions();
         }

         ci.cancel();
      }
   }
}
