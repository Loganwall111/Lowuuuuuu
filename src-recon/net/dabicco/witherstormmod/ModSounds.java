package net.dabicco.witherstormmod;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
   public static final SoundEvent MUSIC_MINI_A = register("music_mini_a");
   public static final SoundEvent MUSIC_MINI_B = register("music_mini_b");
   public static final SoundEvent MUSIC_MINI_C = register("music_mini_c");
   public static final SoundEvent MUSIC_MAIN_A = register("music_main_a");
   public static final SoundEvent MUSIC_MAIN_B = register("music_main_b");
   public static final SoundEvent MUSIC_MAIN_C = register("music_main_c");
   public static final SoundEvent MUSIC_AFTR_A = register("music_aftr_a");
   public static final SoundEvent MUSIC_AFTR_B = register("music_aftr_b");
   public static final SoundEvent MUSIC_AFTR_C = register("music_aftr_c");
   public static final SoundEvent MUSIC_DISTANT_A = register("music_distant_a");
   public static final SoundEvent MUSIC_DISTANT_B = register("music_distant_b");
   public static final SoundEvent MUSIC_IVOR = register("music_ivor");
   public static final SoundEvent MUSIC_BOWELS = register("music_bowels");
   public static final SoundEvent MUSIC_BOWELS_OVERLAY = register("music_bowels_overlay");
   public static final SoundEvent MUSIC_FINAL = register("music_final");
   public static final SoundEvent MUSIC_WITHERGLOOP = register("music_withergloop");
   public static final SoundEvent CB_DAMAGE = register("cb_damage");
   public static final SoundEvent CB_DAMAGE_FINAL = register("cb_damage_final");
   public static final SoundEvent CB_DESTRUCT = register("cb_destruct");
   public static final SoundEvent CB_HIT = register("cb_hit");
   public static final SoundEvent CB_POWER = register("cb_power");
   public static final SoundEvent CB_UNPOWER = register("cb_unpower");
   public static final SoundEvent CB_POWERUP_FX = register("cb_powerup_fx");
   public static final SoundEvent CB_PULSE_LOOP = register("cb_pulse_loop");
   public static final SoundEvent CB_REACTIVATE = register("cb_reactivate");
   public static final SoundEvent CLUSTER_FX = register("cluster_fx");
   public static final SoundEvent HEAD_CHOMP = register("head_chomp");
   public static final SoundEvent STORM_THUMP = register("storm_thump");
   public static final SoundEvent STORM_THUMP_LARGE = register("storm_thump_large");
   public static final SoundEvent AMBIENCE_LOOP = register("ambience_loop");
   public static final SoundEvent HEAD_ACTIVATE_BEAM = register("head_activate_beam");
   public static final SoundEvent HEAD_BEAM_SNAP = register("head_beam_snap");
   public static final SoundEvent HEAD_BEAM_DEACTIVATE = register("head_beam_deactivate");
   public static final SoundEvent HEAD_BEAM_LOOP = register("head_beam_loop");
   public static final SoundEvent HEAD_GROWL = register("head_growl");
   public static final SoundEvent HEAD_SHORT_GROWL = register("head_short_growl");
   public static final SoundEvent HEAD_SNARL = register("head_snarl");
   public static final SoundEvent HEAD_ROAR = register("head_roar");
   public static final SoundEvent HEAD_HURT = register("head_hurt");
   public static final SoundEvent HEAD_STAB_EYE = register("head_stab_eye");
   public static final SoundEvent HEAD_SHOOT = register("head_shoot");
   public static final SoundEvent HEAD_POWERFUL_ROAR = register("head_powerful_roar");
   public static final SoundEvent TRACTOR_BEAM_GROUND_LOOP = register("tractor_beam_ground_loop");
   public static final SoundEvent TRACTOR_BEAM_GROUND_DISABLE = register("tractor_beam_ground_disable");
   public static final SoundEvent STORM_GROW = register("storm_grow");
   public static final SoundEvent STORM_TORNADO_LOOP = register("storm_tornado_loop");
   public static final SoundEvent FORMIDIBOMB_CREATION = register("formidibomb_creation");
   public static final SoundEvent FORMIDIBOMB_EXPLOSION = register("formidibomb_explosion");
   public static final SoundEvent FORMIDIBOMB_DISTANT_EXPLOSION = register("formidibomb_distant_explosion");
   public static final SoundEvent INFECTED_MOB = register("infected_mob");
   public static final SoundEvent W_WARDEN_SONIC_CHARGE = register("withered_sonic_charge");
   public static final SoundEvent W_WARDEN_SONIC_BOOM = register("withered_sonic_boom");
   public static final SoundEvent W_BEACON_ACTIVATE = register("withered_ability");
   public static final SoundEvent W_AMETHYST_BLOCK_CHIME = register("withered_chime");
   public static final SoundEvent W_STONE_BREAK = register("withered_stone_break");
   public static final SoundEvent W_ARROW_SHOOT = register("withered_arrow_shoot");
   public static final SoundEvent W_WITHER_AMBIENT = register("withered_ambient");
   public static final SoundEvent W_WITHER_SHOOT = register("withered_shoot");

   private static SoundEvent register(String name) {
      Identifier id = Identifier.fromNamespaceAndPath("dabywitherstormmod", name);
      return (SoundEvent)Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
   }

   public static void initialize() {
   }
}
