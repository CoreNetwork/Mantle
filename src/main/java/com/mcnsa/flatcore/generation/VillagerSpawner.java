package com.mcnsa.flatcore.generation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import com.mcnsa.flatcore.MCNSAFlatcore;

public class VillagerSpawner implements Listener {
	private static Profession nextVillagerProfession;

	public VillagerSpawner()
	{
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAFlatcore.instance);

	}

	public void spawnVillager(Location location, Profession profession)
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
			((Villager) event.getEntity()).setProfession(nextVillagerProfession);
		}
	}
}
