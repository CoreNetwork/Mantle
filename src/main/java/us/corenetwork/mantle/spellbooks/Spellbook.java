package us.corenetwork.mantle.spellbooks;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.Setting;
import us.corenetwork.mantle.Settings;
import us.corenetwork.mantle.Util;

public abstract class Spellbook {	
	private String name;
	
	public Spellbook(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	
	public void activate(SpellbookItem item, PlayerEvent event)
	{
		long start = System.nanoTime();
				
		boolean activated = false;
		if (event instanceof PlayerInteractEntityEvent)
			activated = onActivateEntity(item, (PlayerInteractEntityEvent) event);
		else if (event instanceof PlayerInteractEvent)
			activated = onActivate(item, (PlayerInteractEvent) event);
		
		if (activated)
		{
			Player player = event.getPlayer();
			if (player.getGameMode() != GameMode.CREATIVE)
			{
				player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
			}
			
			long end = System.nanoTime();
			if (Settings.getBoolean(Setting.DEBUG))
			{
				player.sendMessage("Book time: " + (end - start) / 1000000.0);
			}
			
			String message = SpellbooksSettings.MESSAGE_USED.string();
			message = message.replace("<Player>", player.getName());
			message = message.replace("<Spellbook>", getName());
			Util.Broadcast(message);
			
			Location playerLoc = player.getLocation();
			MLog.info("Player " + player.getName() + " used " + getName() + " in " + playerLoc.getWorld().getName() + " at " + playerLoc.getBlockX() + ", " + playerLoc.getBlockY() + ", " + playerLoc.getBlockZ());
		}
	}
	
	protected abstract boolean onActivate(SpellbookItem item, PlayerInteractEvent event);
	protected abstract boolean onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event);

}
