package us.corenetwork.mantle.spellbooks;

import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.Setting;
import us.corenetwork.mantle.Settings;
import us.corenetwork.mantle.Util;

public abstract class Spellbook {	
	private String name;
	private HashMap<String, Long> lastBroadcastTime = new HashMap<String, Long>();
	
	public Spellbook(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	
	protected boolean providesOwnMessage()
	{
		return false;
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
				int heldBookSlot = player.getInventory().getHeldItemSlot();
				ItemStack heldBook = player.getInventory().getItem(heldBookSlot);
				if (heldBook.getAmount() >  1)
				{
					heldBook.setAmount(heldBook.getAmount() - 1);
					player.getInventory().setItem(player.getInventory().getHeldItemSlot(), heldBook);
				}
				else
				{
					player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
				}
			}
			
			long end = System.nanoTime();
			if (Settings.getBoolean(Setting.DEBUG))
			{
				player.sendMessage("Book time: " + (end - start) / 1000000.0);
			}
			
			if (!providesOwnMessage())
			{
				String message = SpellbooksSettings.MESSAGE_YOU_USED.string();
				message = message.replace("<Spellbook>", getName());
				Util.Message(message, player);
			}
			messageEverybody(player);
			
			Location playerLoc = player.getLocation();
			MLog.info("Player " + player.getName() + " used " + getName() + " in " + playerLoc.getWorld().getName() + " at " + playerLoc.getBlockX() + ", " + playerLoc.getBlockY() + ", " + playerLoc.getBlockZ());
		}
	}
	
	private void messageEverybody(Player player)
	{
		Long lastBroadcast = lastBroadcastTime.get(player.getName());
		if (lastBroadcast == null)
			lastBroadcast = 0L;
		
		if (System.currentTimeMillis() - lastBroadcast > SpellbooksSettings.USED_BROADCAST_MINIMUM_DELAY_SECONDS.integer() * 1000)
		{
			String message = SpellbooksSettings.MESSAGE_USED.string();
			message = message.replace("<Player>", player.getName());
			message = message.replace("<Spellbook>", getName());
			Util.Broadcast(message, player.getName());

			lastBroadcastTime.put(player.getName(), System.currentTimeMillis());
		}
		
		
	}
	
	protected abstract boolean onActivate(SpellbookItem item, PlayerInteractEvent event);
	protected abstract boolean onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event);

}
