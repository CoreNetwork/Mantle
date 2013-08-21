package com.mcnsa.flatcore;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.inventory.ItemStack;

public class FlatcoreListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event)
	{
		final LivingEntity entity = event.getEntity();

		//Mobs with name or silverfishes should not drop anything
		if (entity.getCustomName() != null || entity.getType() == EntityType.SILVERFISH)
		{
			event.getDrops().clear();
			event.setDroppedExp(0);
			entity.getEquipment().clear();
			return;
		}
	}
	

	@EventHandler(ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event)
	{
		//Do not drop colored sign
		if (event.getBlock().getState() instanceof Sign)
		{
			Sign sign = (Sign) event.getBlock().getState();

			String colorSymbol = "\u00A7";
			for (String line : sign.getLines())
			{
				if (line.contains(colorSymbol))
				{
					event.setCancelled(true);
					return;
				}
			}
		} 
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent event)
	{
		//Restrict pigmen range
		if (event.getReason() == TargetReason.PIG_ZOMBIE_TARGET)
		{
			Location targetLocation = event.getTarget().getLocation();
			Location pigmanLocation = event.getEntity().getLocation();

			int distance = (int) Math.round(pigmanLocation.distance(targetLocation));

			if (distance > Settings.getInt(Setting.PIGMAN_ANGER_RANGE))
			{
				event.setCancelled(true);
				return;
			}
		}
	}
}
