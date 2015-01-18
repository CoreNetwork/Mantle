package us.corenetwork.mantle.animalspawning;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.bukkit.World.Environment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.WorldSaveEvent;


public class AnimalSpawningListener implements Listener {
	public static boolean spawningMob = false;
	
	
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		if (!spawningMob)
		{
			if (event.getSpawnReason() == SpawnReason.NATURAL && event.getLocation().getWorld().getEnvironment() == Environment.NORMAL)
			{
				String type = event.getEntityType().toString();
				for (String rejectedType : AnimalSpawningSettings.PREVENT_SPAWNING_ANIMALS.stringList())
				{
					if (type.equalsIgnoreCase(rejectedType))
					{
						event.setCancelled(true);
						break;
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
		    
		    reasonField.set(event, SpawnReason.CUSTOM);
		    AnimalSpawningModule.spawnedAnimals.put(event.getEntity().getUniqueId(), event.getEntityType().toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		spawningMob = false;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDeathEvent(EntityDeathEvent event)
	{
		Entity e = event.getEntity();
		if(e instanceof Animals)
		{
			AnimalSpawningModule.killedAnimals.add(e.getUniqueId());
		}
	}
	
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldSaveEvent(WorldSaveEvent event)
    {
    	if(event.getWorld().getName().equals("world"))
    		AnimalSpawningIO.saveAnimals();
    }
}
