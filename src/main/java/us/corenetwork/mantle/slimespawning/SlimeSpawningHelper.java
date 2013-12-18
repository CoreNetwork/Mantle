package us.corenetwork.mantle.slimespawning;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;


public class SlimeSpawningHelper implements Listener {
	public static boolean spawningMob = false;

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		if (event.getEntityType() != EntityType.SLIME)
			return;

		if (!spawningMob)
		{
			if (event.getSpawnReason() == SpawnReason.NATURAL)
			{
				event.setCancelled(true);
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
