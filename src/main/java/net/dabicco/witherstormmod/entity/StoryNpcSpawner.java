package net.dabicco.witherstormmod.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.network.chat.Component;

/** Places named, persistent villagers around a generated Story Mode town. */
public final class StoryNpcSpawner {
 private static final String[] NAMES={"Jesse","Petra","Axel","Olivia","Lukas","Radar","Ivor","Gabriel","Ellegaard","Magnus","Soren","Harper","Jack","Nurm","Stacy","Stampy","Dan","Sparklez","Binta","Wink","Fangirl","Nell","Em","Otto"};
 private StoryNpcSpawner() {}
 public static void populate(ServerLevel server, BlockPos centre, int count) {
  ArrayList<String> names=new ArrayList<>(Arrays.asList(NAMES)); Collections.shuffle(names, server.random);
  for(int i=0;i<count;i++) { double a=server.random.nextDouble()*Math.PI*2; int r=6+server.random.nextInt(15); int x=centre.getX()+(int)Math.round(Math.cos(a)*r), z=centre.getZ()+(int)Math.round(Math.sin(a)*r); BlockPos at=new BlockPos(x,server.getHeight(Heightmap.Types.MOTION_BLOCKING,x,z),z); Mob mob=(Mob)EntityType.VILLAGER.create(server, EntitySpawnReason.STRUCTURE); if(mob==null) continue; mob.finalizeSpawn(server,server.getCurrentDifficultyAt(at),EntitySpawnReason.STRUCTURE,(SpawnGroupData)null); mob.setCustomName(Component.literal(names.get(i%names.size()))); mob.setCustomNameVisible(true); mob.setPersistenceRequired(); mob.moveTo(at,server.random.nextFloat()*360F,0); server.addFreshEntity(mob); }
 }
}
