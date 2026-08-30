package net.dabicco.witherstormmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

/** Persistent survivor NPC used by the overhaul's story encounters. */
public class StormSurvivorNPC extends PathfinderMob {
   public StormSurvivorNPC(EntityType<? extends PathfinderMob> type, Level level) { super(type, level); }
}
