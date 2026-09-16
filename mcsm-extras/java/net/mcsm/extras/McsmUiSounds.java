package net.mcsm.extras;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Devouring Storms UI sound events (namespace {@code mcsm}).
 *
 * Registration lives in this <em>common</em> class on purpose: the sound
 * events must be created while the built-in registries are still open, which
 * happens during the mod's own initializer -- see
 * {@code McsmBuiltinPackMixin}, which forces this class to initialize next to
 * {@code McsmEntities.register()}.
 *
 * The previous pass registered lazily from the client-only playback helper,
 * i.e. the first time a button was clicked: by then
 * {@code BuiltInRegistries.SOUND_EVENT} is frozen, {@code Registry.register}
 * throws, the helper swallowed the exception and returned {@code null}, and
 * the menus stayed silent no matter how many times they were clicked. The
 * audio assets also shipped as RIFF {@code .wav}, which Minecraft's sound
 * engine cannot decode at all (Vorbis only). Both halves are fixed here:
 * events are registered at boot and the assets are Ogg Vorbis.
 *
 * The declarations themselves are the base mod's own ModSounds pattern
 * (Registry.register on BuiltInRegistries.SOUND_EVENT with
 * SoundEvent.createVariableRangeEvent).
 */
public final class McsmUiSounds {

    public static final SoundEvent BUTTON_HOVER = register("ds_btn_hover");
    public static final SoundEvent BUTTON_CLICK = register("ds_btn_click");
    public static final SoundEvent MENU_OPEN = register("ds_menu_open");

    private McsmUiSounds() {
    }

    /** Class-init trigger; the registrations above are the side effect. */
    public static void initialize() {
        // no-op: touching the class runs the static initialiser
    }

    private static SoundEvent register(String path) {
        try {
            Identifier id = Identifier.fromNamespaceAndPath("mcsm", path);
            return (SoundEvent) Registry.register(BuiltInRegistries.SOUND_EVENT, id,
                    SoundEvent.createVariableRangeEvent(id));
        } catch (Throwable t) {
            System.err.println("[ds] UI sound " + path + " not registered: " + t);
            return null;
        }
    }
}
