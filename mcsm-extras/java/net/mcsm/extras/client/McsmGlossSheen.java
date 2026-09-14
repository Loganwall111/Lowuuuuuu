package net.mcsm.extras.client;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.witherstormmod.client.GlowRenderTypes;
import net.dabicco.witherstormmod.client.StormSkins;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * Build #368 - native Tattletale-style gloss sheen for the Wither Storm body.
 *
 * While the base renderer submits the storm body, a second translucent pass
 * re-renders the same model part against the scrolling storm_gloss.png sheet
 * (GlowRenderTypes.translucent), so the blocky body reads as a shiny,
 * reflective wet-obsidian shell right out of the box - no external shader
 * pack required. The sheet sweeps around the body using the native
 * glossUOffset / glossVOffset time drivers from StormSkins, which is what
 * makes the sheen read as moving Tattletale gloss instead of a static coat.
 *
 * The pass is strictly cosmetic: any failure is swallowed so the storm's
 * real rendering pass can never be broken by the sheen.
 */
public final class McsmGlossSheen {
    /** One active storm render frame (collector + state), for recursion safety. */
    private static final class Frame {
        final WitherStormRenderState state;
        final SubmitNodeCollector collector;

        Frame(WitherStormRenderState state, SubmitNodeCollector collector) {
            this.state = state;
            this.collector = collector;
        }
    }

    private static final ThreadLocal<Deque<Frame>> FRAMES = ThreadLocal.withInitial(ArrayDeque::new);
    /** Model class -> body part the base renderToBuffer actually draws. */
    private static final Map<Class<?>, ModelPart> BODY_PARTS = new ConcurrentHashMap<>();
    private static final String[] BODY_PART_NAMES = {"upperBodyPart1", "witherstormbody", "root"};

    private static RenderType cachedGlossType;

    private McsmGlossSheen() {
    }

    /** Pushed at the HEAD of WitherStormRenderer.submit. */
    public static void begin(WitherStormRenderState state, SubmitNodeCollector collector) {
        if (state == null || collector == null) {
            return;
        }
        FRAMES.get().push(new Frame(state, collector));
    }

    /** Popped at the TAIL of WitherStormRenderer.submit. */
    public static void end() {
        Deque<Frame> frames = FRAMES.get();
        if (frames.isEmpty()) {
            FRAMES.remove();
        } else {
            frames.pop();
        }
    }

    private static Frame frame() {
        Deque<Frame> frames = FRAMES.get();
        return frames.isEmpty() ? null : frames.peek();
    }

    public static boolean active() {
        if (frame() == null) {
            return false;
        }
        try {
            McsmExtrasConfig.load();
            return McsmExtrasConfig.nativeGlossSheen;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Called from the body model's renderToBuffer TAIL, in the model's live
     * pose frame (scale/roll/collapse already applied). Re-renders the same
     * body part into a translucent buffer carrying the gloss sheet, with the
     * sheet swept around the body so the shine appears to scroll.
     */
    public static void coatFor(Object model, PoseStack poseStack, int light, int overlay) {
        if (!active() || model == null || poseStack == null) {
            return;
        }
        try {
            Frame frame = frame();
            ModelPart part = bodyPart(model);
            RenderType type = glossRenderType();
            if (frame == null || part == null || type == null) {
                return;
            }
            // Build #374: never coat the pre-phase-4 stages (command block /
            // hunchback) — their native pose must stay untouched, and the coat
            // ghost made the Phase 1 blocks read as floating apart in air.
            if (!frame.state.phase4) {
                return;
            }
            // The coat re-renders the body part in the model's LIVE pose,
            // exactly aligned with the base pass. The old implementation
            // swept the pose around Y by up to a full 360° per gloss cycle
            // (rotating the ghost coat, not the texture) and even rendered
            // with the outer stack instead of the collector's pose — both
            // artifacts are gone.
            frame.collector.submitCustomGeometry(poseStack, type, (pose, consumer) ->
                    part.render(pose, consumer, light, overlay));
        } catch (Throwable ignored) {
            // Cosmetic pass only - never let the sheen take down the storm.
        }
    }

    private static RenderType glossRenderType() {
        if (cachedGlossType == null) {
            try {
                cachedGlossType = GlowRenderTypes.translucent(StormSkins.glossTexture());
            } catch (Throwable t) {
                return null;
            }
        }
        return cachedGlossType;
    }

    /** Resolves the exact part the base renderToBuffer draws (cached per class). */
    private static ModelPart bodyPart(Object model) {
        Class<?> cls = model.getClass();
        ModelPart cached = BODY_PARTS.get(cls);
        if (cached != null) {
            return cached;
        }
        ModelPart found = null;
        for (Class<?> c = cls; c != null && found == null; c = c.getSuperclass()) {
            for (String name : BODY_PART_NAMES) {
                try {
                    Field f = c.getDeclaredField(name);
                    f.setAccessible(true);
                    Object value = f.get(model);
                    if (value instanceof ModelPart part) {
                        found = part;
                        break;
                    }
                } catch (Throwable ignored) {
                }
            }
        }
        if (found != null) {
            BODY_PARTS.put(cls, found);
        }
        return found;
    }
}
