package com.mcnsa.flatcore.checkpoints;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.mcnsa.flatcore.Util;

public class CheckpointsListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if ((event.getFrom().getBlockX() != event.getTo().getBlockX() || 
				event.getFrom().getBlockY() != event.getTo().getBlockY() || 
				event.getFrom().getBlockZ() != event.getTo().getBlockZ()) &&
				CheckpointsModule.scheduledTeleports.containsKey(event.getPlayer().getName()) )
		{
			cancelTeleport(event.getPlayer());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Player && CheckpointsModule.scheduledTeleports.containsKey(((Player) event.getEntity()).getName()))
		{
			cancelTeleport((Player) event.getEntity());
			return;
		}
		
		if (event instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent newEvent = (EntityDamageByEntityEvent) event;
			
			if (newEvent.getDamager() instanceof Player && CheckpointsModule.scheduledTeleports.containsKey(((Player) newEvent.getDamager()).getName()))
			{
				cancelTeleport((Player) newEvent.getDamager());
				return;
			}
		}
	}
	private static void cancelTeleport(Player player)
	{
		CheckpointsModule.scheduledTeleports.remove(player.getName());

		String message = CheckpointsSettings.MESSAGE_TELEPORT_CANCELLED.string();
		message = message.replace("<Player>", player.getName());

		Util.Message(message, player);
	}

}
