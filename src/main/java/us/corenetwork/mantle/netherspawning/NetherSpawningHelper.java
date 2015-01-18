package us.corenetwork.mantle.netherspawning;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;


public class NetherSpawningHelper implements Listener {
	public static boolean spawningMob = false;

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		if (!spawningMob)
		{
			if (event.getLocation().getWorld().getEnvironment() == Environment.NETHER)
			{
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
