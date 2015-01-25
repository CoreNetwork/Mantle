package us.corenetwork.mantle.treasurehunt;

import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import us.corenetwork.mantle.Util;

public class THuntListener implements Listener {

	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		final Block block = event.getClickedBlock();
		
		if(THuntModule.manager.isHuntChest(block.getLocation()) == false)
		{
			return;
		}
		
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && THuntModule.manager.isHuntChest(block.getLocation()))
		{
			THuntModule.manager.chestClicked(event.getPlayer(), block.getLocation(), true);
		}
		else if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			THuntModule.manager.chestClicked(event.getPlayer(), block.getLocation(), false);
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockBreak(final BlockBreakEvent event)
	{
		final Block block = event.getBlock();

		if(THuntModule.manager.isHuntChest(block.getLocation()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		
		if(THuntModule.manager.shouldMessagePlayer(player))
		{
			if(THuntModule.manager.isRunning())
			{
				if(THuntModule.manager.isTakingPart(player))
				{
					Util.Message(THuntSettings.MESSAGE_ENTER_WHILE_RUNNING_ALREADY_IN.string(), player);	
				}
				else
				{
					Util.Message(THuntSettings.MESSAGE_ENTER_WHILE_RUNNING.string(), player);
				}
			}
			else if(THuntModule.manager.isQueued())
			{
				if(THuntModule.manager.isTakingPart(player))
				{
					Util.Message(THuntSettings.MESSAGE_ENTER_WHILE_QUEUED_ALREADY_IN.string().replace("<Time>", THuntModule.manager.getTimeToStartNextHunt() + ""), player);
				}
				else
				{
					Util.Message(THuntSettings.MESSAGE_ENTER_WHILE_QUEUED.string().replace("<Time>", THuntModule.manager.getTimeToStartNextHunt() + ""), player);
				}
			}
		}
		}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event)
	{
		Player player = event.getPlayer();
		
		if(player.getWorld().getEnvironment() == Environment.NORMAL && THuntModule.manager.isTakingPart(player))
		{
			if(THuntModule.manager.isRunning())
			{
				THuntModule.manager.messageAboutCurrentWave(player);
			}
		}
	}
}
