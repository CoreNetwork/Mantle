package com.mcnsa.flatcore.hardmode;

import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import com.mcnsa.flatcore.MCNSAFlatcore;

public class HardmodeListener implements Listener {
	
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event)
	{
		
		if (event instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
			
			Entity damager = entityEvent.getDamager();
			Entity victim = entityEvent.getEntity();
		
			
			//Teleporting player away from enderman
			if (victim instanceof Enderman && damager instanceof Player)
			{
				//Only perform teleporting if enderman cannot move to the player
				boolean canMove = EndermanTeleport.canMove(damager.getLocation(), victim.getLocation());

				if (!canMove)
				{
					double randomChance = HardmodeSettings.ENDERMAN_TELEPORT_CHANCE.doubleNumber();
					if (MCNSAFlatcore.random.nextDouble() < randomChance)
					{
						event.setCancelled(true);
						EndermanTeleport.teleportPlayer((Player) damager, (Enderman) victim);
						return;
					}
				}
				
			}
		}
	}
	
}
