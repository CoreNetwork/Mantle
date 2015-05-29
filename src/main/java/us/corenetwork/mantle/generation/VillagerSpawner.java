package us.corenetwork.mantle.generation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftVillager;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import us.corenetwork.mantle.MantlePlugin;


public class VillagerSpawner implements Listener {
	private static Integer nextVillagerProfession;

	public VillagerSpawner()
	{
		Bukkit.getServer().getPluginManager().registerEvents(this, MantlePlugin.instance);

	}

	public void spawnVillager(Location location, int profession)
	{
		nextVillagerProfession = profession;

		location.getWorld().spawnEntity(location, EntityType.VILLAGER);
		
		nextVillagerProfession = null;
	}

	public void close()
	{
		HandlerList.unregisterAll(this);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		if (event.getEntityType() == EntityType.VILLAGER && nextVillagerProfession != null)
		{
			((CraftVillager) event.getEntity()).getHandle().setProfession(nextVillagerProfession);
		}
	}
}
