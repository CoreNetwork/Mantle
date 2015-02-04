package us.corenetwork.mantle.netherspawning;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import us.corenetwork.mantle.MantlePlugin;


public class NetherSpawningListener implements Listener {
	public static boolean spawningMob = false;

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		if (!spawningMob)
		{
			if (event.getLocation().getWorld().getEnvironment() == Environment.NETHER)
			{
                //cancelling of normal spawns, depending on reason and mob type
				SpawnReason reason = event.getSpawnReason();
				if (reason == SpawnReason.NATURAL || reason == SpawnReason.REINFORCEMENTS || reason == SpawnReason.MOUNT)
				{
					String type = event.getEntityType().toString();
					for (String rejectedType : NetherSpawningSettings.PREVENT_SPAWNING_NETHER.stringList())
					{
						if (type.equalsIgnoreCase(rejectedType))
						{
							event.setCancelled(true);
                            return;
							//break;
						}
					}


				}


                Block spawnBlock = event.getLocation().getBlock();
                double random, chance;

                //run spawn of another mob/chance/roll/etc
                if(reason == SpawnReason.NATURAL && event.getEntityType() == EntityType.PIG_ZOMBIE)
                {
                    boolean alreadySpawned = false;

                    boolean canSpawnBlaze = NetherSpawner.canSpawnBlaze(spawnBlock);
                    //run random for Magma size
                    int size = MantlePlugin.random.nextInt(3);
                    boolean canSpawnMagma = NetherSpawner.canSpawnMagmaCube(spawnBlock, size);
                    boolean canSpawnWitherSkeleton = NetherSpawner.canSpawnWitherSkeleton(spawnBlock, SpawnReason.NATURAL);



                    //for each mob, run a roll if canSpawn = true
                    //if roll passed, spawn
                    //if not, try another mob

                    if(canSpawnBlaze)
                    {
                        random = MantlePlugin.random.nextDouble();
                        chance = NetherSpawningSettings.BLAZE_SPAWN_CHANCE.doubleNumber();

                        if(random <= chance)
                        {
                            NetherSpawner.spawnBlaze(spawnBlock);
                            alreadySpawned = true;
                        }
                    }
                    if(canSpawnMagma && !alreadySpawned)
                    {
                        random = MantlePlugin.random.nextDouble();
                        chance = NetherSpawningSettings.MAGMA_CUBE_SPAWN_CHANCE.doubleNumber();

                        if(random <= chance)
                        {
                            NetherSpawner.spawnMagmaCube(spawnBlock, size);
                            alreadySpawned = true;
                        }
                    }
                    if(canSpawnWitherSkeleton && !alreadySpawned)
                    {
                        random = MantlePlugin.random.nextDouble();
                        chance = NetherSpawningSettings.WITHER_SKELETON_SPAWN_CHANCE.doubleNumber();

                        if(random <= chance)
                        {
                            NetherSpawner.spawnWitherSkeleton(spawnBlock, SpawnReason.NATURAL);
                            alreadySpawned = true;
                        }
                    }

                    if(alreadySpawned)
                    {
                        event.setCancelled(true);
                    }

                }
                //Handle vanilla ghast spawn - move up and run
                else if(reason == SpawnReason.NATURAL && event.getEntityType() == EntityType.GHAST)
                {
                    event.setCancelled(true);

                    Block actualSpawnBlock = spawnBlock.getRelative(BlockFace.UP, MantlePlugin.random.nextInt(NetherSpawningSettings.GHAST_MAX_MOVE_UP.integer() - NetherSpawningSettings.GHAST_MIN_MOVE_UP.integer() +1) + NetherSpawningSettings.GHAST_MIN_MOVE_UP.integer());

                    boolean canSpawnGhast = NetherSpawner.canSpawnGhast(spawnBlock, actualSpawnBlock);

                    if(canSpawnGhast)
                    {
                        random = MantlePlugin.random.nextDouble();
                        chance = NetherSpawningSettings.GHAST_SPAWN_CHANCE.doubleNumber();

                        if (random <= chance)
                        {
                            NetherSpawner.spawnGhast(actualSpawnBlock);
                        }
                    }

                }
			}

			return;
		}	
		try
		{			
			Field reasonField = CreatureSpawnEvent.class.getDeclaredField("spawnReason");
			reasonField.setAccessible(true);
			
			Field modifiersField =  Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
		    modifiersField.setInt(reasonField, reasonField.getModifiers() & ~Modifier.FINAL);
		    
		    reasonField.set(event, SpawnReason.NATURAL);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		spawningMob = false;
	}


}
