package net.dabicco.witherstormmod.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyReturnValue;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dabicco.witherstormmod.structures.McsmWorldgen;
import net.minecraft.server.level.ServerLevel;

/**
 * Mega-phase 7: structures land WHOLE, and Sky City goes up among the
 * cloud decks (user orders: "structures never in segments", "no scattered
 * Sky City fragments", "Sky City ~1000-10,000 blocks up, fall through
 * 5-15 cloud layers").
 *
 * The base mod places every schematic through a static queue with a 24k
 * blocks/tick budget, slicing towns upward over many ticks (the visible
 * "segments"), and that static queue survives world loads, so leftovers
 * from the previous world keep placing into the new one (the "scattered
 * fragments"). Both are fixed here: the queue is cleared whenever the
 * level instance changes, and the budget is raised so each schematic
 * completes in about a tick.
 *
 * The floating sites (Sky City y=296 and siblings) are raised +3904:
 * Sky City ends at y=4200, above the 3500 cloud deck - jumping off falls
 * through seven of the story decks on the way down.
 */
@Mixin(McsmWorldgen.class)
public abstract class McsmWorldgenPatch {

    private static ServerLevel lastLevel;

    @Inject(method = "tick", at = @At("HEAD"), remap = false, require = 0)
    private static void dabyws$wholeStructures(ServerLevel level, CallbackInfo ci) {
        if (lastLevel != level) {
            lastLevel = level;
            McsmWorldgen.clear();
        }
        McsmWorldgen.setBudget(900000);
    }

    @ModifyReturnValue(method = "layout", at = @At("RETURN"), remap = false, require = 0)
    private static List<McsmWorldgen.Site> dabyws$skyCityAltitude(List<McsmWorldgen.Site> in) {
        List<McsmWorldgen.Site> out = new ArrayList<>(in.size());
        for (McsmWorldgen.Site s : in) {
            if (s.floating() && s.y() < 1000) {
                out.add(new McsmWorldgen.Site(s.path(), s.x(), s.y() + 3904, s.z(), s.label(), true));
            } else {
                out.add(s);
            }
        }
        return out;
    }
}
