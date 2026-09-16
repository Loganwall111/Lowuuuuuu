package net.mcsm.extras;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * BUILD #416 (D.8, phase 3) -- the boss ladder's health bar.
 *
 * WHY REFLECTION. {@code ServerBossEvent} is real, public API and the base mod
 * already drives one (the Wither Storm's own bar, through a {@code WitherBoss}
 * accessor). This repository, however, is compiled by CI against a FROZEN base
 * release jar plus a hand-checked API index, and the boss bar is the one piece of
 * the phase-3 work whose exact constructor/overlay spelling is not pinned by any
 * shipped caller in that jar -- the only bar in it is inherited from
 * {@code WitherBoss}, never constructed.
 *
 * Rather than gamble a whole build on one constructor signature, the bar is
 * resolved once, lazily, by name: the event class, its colour and overlay enums
 * and the four methods the ladder uses. If ANY of that is not there, {@link #create}
 * returns null and the ladder simply stays quiet -- every announcement is also
 * written into chat, so a boss fight never depends on the bar existing. The
 * reflective path is resolved exactly once per bar and never per frame.
 */
public final class McsmBossBar {

    private static final String EVENT = "net.minecraft.server.level.ServerBossEvent";
    private static final String COLOUR = "net.minecraft.world.BossEvent$BossBarColor";
    private static final String OVERLAY = "net.minecraft.world.BossEvent$BossBarOverlay";

    private final Object event;
    private final Method addPlayer;
    private final Method removePlayer;
    private final Method setProgress;
    private final Method setName;

    private McsmBossBar(Object event, Method addPlayer, Method removePlayer,
            Method setProgress, Method setName) {
        this.event = event;
        this.addPlayer = addPlayer;
        this.removePlayer = removePlayer;
        this.setProgress = setProgress;
        this.setName = setName;
    }

    /**
     * @param name   the bar's title (the boss's name)
     * @param colour one of the base {@code BossBarColor} constants, e.g. "PURPLE"
     * @param notched segment the bar (the ladder's bosses are notched, the swarm is not)
     * @return a bar, or null when this build cannot reach the class
     */
    public static McsmBossBar create(String name, String colour, boolean notched) {
        try {
            Class<?> eventClass = Class.forName(EVENT);
            Class<?> colourClass = Class.forName(COLOUR);
            Class<?> overlayClass = Class.forName(OVERLAY);
            Object colourValue = colourClass.getField(colour == null ? "PURPLE" : colour).get(null);
            Object overlayValue = overlayClass
                    .getField(notched ? "NOTCHED_10" : "PROGRESS").get(null);
            Constructor<?> ctor = eventClass.getConstructor(Component.class, colourClass, overlayClass);
            Object event = ctor.newInstance(Component.literal(name == null ? "?" : name),
                    colourValue, overlayValue);
            return new McsmBossBar(event,
                    method(eventClass, "addPlayer", ServerPlayer.class),
                    method(eventClass, "removePlayer", ServerPlayer.class),
                    method(eventClass, "setProgress", float.class),
                    method(eventClass, "setName", Component.class));
        } catch (Throwable t) {
            return null;
        }
    }

    public void track(ServerPlayer player) {
        invoke(addPlayer, player);
    }

    public void untrack(ServerPlayer player) {
        invoke(removePlayer, player);
    }

    public void progress(float fraction) {
        invoke(setProgress, Float.valueOf(fraction));
    }

    public void title(String name) {
        invoke(setName, Component.literal(name == null ? "?" : name));
    }

    private void invoke(Method method, Object argument) {
        if (event == null || method == null) {
            return;
        }
        try {
            method.invoke(event, argument);
        } catch (Throwable ignored) {
            // a bar that cannot update is still a fight that can be won
        }
    }

    private static Method method(Class<?> owner, String name, Class<?>... parameters) {
        try {
            return owner.getMethod(name, parameters);
        } catch (Throwable t) {
            return null;
        }
    }
}
