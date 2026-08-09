package com.rewritten.devouringstorms.client;

import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.storm.MassgPhase;
import com.rewritten.devouringstorms.world.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;

/**
 * The sound of it. While the storm lives, vanilla music is silenced and the Devouring Storms
 * score takes over — a theme per phase ("critical" when the storm is devolving or playing dead).
 * Music files are synthesized by tools/generate_assets.py.
 */
public final class StormMusicDirector {

    private static SoundInstance current;
    private static SoundEvent currentEvent;

    private StormMusicDirector() {
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null) {
            stopCurrent(minecraft);
            return;
        }

        SoundEvent wanted = pickTrack(minecraft);

        if (wanted == null) {
            stopCurrent(minecraft);
            return;
        }

        // drown out vanilla music while the storm is the story
        minecraft.getMusicManager().stopPlaying();

        if (wanted != currentEvent || current == null || !minecraft.getSoundManager().isActive(current)) {
            stopCurrent(minecraft);
            currentEvent = wanted;
            current = SimpleSoundInstance.forMusic(wanted);
            minecraft.getSoundManager().play(current);
        }
    }

    private static SoundEvent pickTrack(Minecraft minecraft) {
        // The Decayed Reality always hums its own weather.
        if (minecraft.level.dimension() == ModDimensions.DECAYED_LEVEL_KEY
            && !StormClientState.stormActive) {
            return ModSounds.AMBIENT_DECAYED_LOOP;
        }
        if (!StormClientState.stormActive) return null;
        if (StormClientState.critical) return ModSounds.MUSIC_CRITICAL;

        MassgPhase phase = StormClientState.currentPhase();
        if (phase == null) return null;
        return switch (phase) {
            case SLEEPING -> ModSounds.MUSIC_SIGNAL;
            case SIGNAL -> ModSounds.MUSIC_SIGNAL;
            case HUNGER -> ModSounds.MUSIC_HUNGER;
            case DEVOURER -> ModSounds.MUSIC_DEVOURER;
            case SUNDERER -> ModSounds.MUSIC_SUNDERER;
            case BOWELS -> ModSounds.MUSIC_SUNDERER;   // 5.5 keeps the sunder-theme under the glow
            case GENESIS -> ModSounds.MUSIC_GENESIS;
        };
    }

    private static void stopCurrent(Minecraft minecraft) {
        if (current != null) {
            minecraft.getSoundManager().stop(current);
        }
        current = null;
        currentEvent = null;
    }
}
