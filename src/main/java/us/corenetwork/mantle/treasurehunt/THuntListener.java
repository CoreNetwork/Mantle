package us.corenetwork.mantle.treasurehunt;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
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
			THuntModule.manager.chestClicked(event.getPlayer(), block.getLocation());
		}
		else if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			Util.Message(THuntSettings.MESSAGE_RIGHT_CLICK.string(), event.getPlayer());
			event.setCancelled(true);
			return;
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
		
		if(THuntModule.manager.isTakingPart(player) == false && THuntModule.manager.shouldMessagePlayer(player))
		{
			if(THuntModule.manager.isRunning())
			{
				Util.Message(THuntSettings.MESSAGE_ENTER_WHILE_RUNNING.string(), player);
				return;
			}
			if(THuntModule.manager.isQueued())
			{
				Util.Message(THuntSettings.MESSAGE_ENTER_WHILE_QUEUED.string().replace("<Time>", THuntModule.manager.getTimeToStartTime() + ""), player);
				return;
			}
		}
	}
	
}
