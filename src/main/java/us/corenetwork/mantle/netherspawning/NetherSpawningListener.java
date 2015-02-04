package us.corenetwork.mantle.netherspawning;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.bukkit.Location;
import org.bukkit.World.Environment;
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
							break;
						}
					}
				}

                //run spawn of another mob/chance/roll/etc
                if(reason == SpawnReason.NATURAL && event.getEntityType() == EntityType.PIG_ZOMBIE)
                {
                    double random = MantlePlugin.random.nextDouble();

                    double chance = NetherSpawningSettings.BLAZE_SPAWN_CHANCE.doubleNumber();

                    //Blaze
                    if(random <= chance)
                    {
                        NetherSpawner.spawn(event.getLocation().getBlock(), EntityType.BLAZE);
                        event.setCancelled(true);
                        return;
                    }
                    random -= chance;
                    chance = NetherSpawningSettings.WITHER_SKELETON_SPAWN_CHANCE.doubleNumber();
                    if(random <= chance)
                    {
                        NetherSpawner.spawn(event.getLocation().getBlock(), EntityType.SKELETON);
                        event.setCancelled(true);
                        return;
                    }

                    random -= chance;
                    chance = NetherSpawningSettings.MAGMA_CUBE_SPAWN_CHANCE.doubleNumber();
                    if(random <= chance)
                    {
                        NetherSpawner.spawn(event.getLocation().getBlock(), EntityType.MAGMA_CUBE);
                        event.setCancelled(true);
                        return;
                    }

                    //Do nothing, spawn normal pigman
                }
                else if(reason == SpawnReason.NATURAL && event.getEntityType() == EntityType.GHAST)
                {
                    Location ghastSpawnLocation = event.getLocation();
                    event.setCancelled(true);

                    double random = MantlePlugin.random.nextDouble();
                    double chance = NetherSpawningSettings.GHAST_SPAWN_CHANCE.doubleNumber();

                    //Blaze
                    if(random > chance)
                    {
                        return;
                    }

                    if (ghastSpawnLocation.getY() <= NetherSpawningSettings.GHAST_MAX_Y.integer() && ghastSpawnLocation.getY() >= NetherSpawningSettings.GHAST_MIN_Y.integer())
                    {
                        NetherSpawner.spawnGhast(event.getLocation().getBlock().getRelative(BlockFace.UP, MantlePlugin.random.nextInt(NetherSpawningSettings.GHAST_MAX_MOVE_UP.integer() - NetherSpawningSettings.GHAST_MIN_MOVE_UP.integer() +1) + NetherSpawningSettings.GHAST_MIN_MOVE_UP.integer()));
                    }
                    //spawn is ok, move him up


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
