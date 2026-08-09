package com.rewritten.devouringstorms.item;

import com.rewritten.devouringstorms.entity.WatcherEntity;
import com.rewritten.devouringstorms.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * THE WATCHER'S EYE (Decayed Human Eye). It still sees.
 * Crush it in your fist: for a moment, wherever IT is — you see it too.
 */
public class WatcherEyeItem extends Item {

    public WatcherEyeItem(Properties props) {
        super(props);
    }

    @Override
    protected InteractionResult use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        if (level instanceof ServerLevel server) {
            var watchers = server.getEntitiesOfClass(WatcherEntity.class, player.getBoundingBox().inflate(96.0));
            for (WatcherEntity watcher : watchers) {
                watcher.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0));
            }
            if (!watchers.isEmpty()) {
                server.playSound(null, player.blockPosition(), ModSounds.WATCHER_WHISPER, SoundSource.PLAYERS, 1.0f, 0.6f);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
