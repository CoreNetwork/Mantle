package us.corenetwork.mantle.spellbooks;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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
	
	public void activate(SpellbookItem item, PlayerInteractEvent event)
	{
		long start = System.nanoTime();
		
		if (item.isSoulbound() && !event.getPlayer().getName().equalsIgnoreCase(item.getSoulboundOwner()))
		{
			Util.Message(SpellbooksSettings.MESSAGE_NOT_AUTHORIZED.string(), event.getPlayer());
			return;
		}
		
		if (onActivate(item, event))
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
}
