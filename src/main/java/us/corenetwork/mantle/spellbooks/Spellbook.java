package us.corenetwork.mantle.spellbooks;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Bukkit;
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
import us.corenetwork.mantle.hardmode.HardmodeModule;
import us.corenetwork.mantle.spellbooks.books.BookFinishAction;

public abstract class Spellbook {	
	
	public static final String SETTING_TEMPLATE = "Template";
	public static final String SETTING_BROADCAST_COOLDOWN_SECONDS = "BroadcastCooldownSeconds";
	public static final String SETTING_BOOST_ON_BROADCAST = "BoostOnBroadcast";
	public static final String SETTING_USE_MESSAGE = "Messages.Use";
	public static final String SETTING_HINT_TO_CASTER_WHEN_BROADCASTED_MESSAGE = "Messages.HintToCasterWhenBroadcasted";
	public static final String SETTING_BROADCAST_MESSAGES = "Messages.Broadcast";

	private String name;
	private HashMap<String, Long> lastBroadcastTime = new HashMap<String, Long>();
	public BookSettings settings;
	
	public Spellbook(String name)
	{
		this.name = name;
		this.settings = new BookSettings(name);
		
		settings.setDefault(SETTING_BROADCAST_COOLDOWN_SECONDS, 5);
		if (!providesOwnMessage()) settings.setDefault(SETTING_USE_MESSAGE,  "You used Spell of " + name + "!");
		settings.setDefault(SETTING_BROADCAST_MESSAGES, Arrays.asList(new String[] { "Player <Player> used Spell of " + name + "!" }));
		settings.setDefault(SETTING_HINT_TO_CASTER_WHEN_BROADCASTED_MESSAGE, "You just gave everybody some magic spell!");

	}
	
	public String getName()
	{
		return name;
	}
	
	protected boolean providesOwnMessage()
	{
		return false;
	}
	
	protected boolean usesContainers()
	{
		return false;
	}
	
	public void activate(SpellbookItem item, PlayerEvent event)
	{
		long start = System.nanoTime();
				
		BookFinishAction action = BookFinishAction.NOTHING;
		if (event instanceof PlayerInteractEntityEvent)
			action = onActivateEntity(item, (PlayerInteractEntityEvent) event);
		else if (event instanceof PlayerInteractEvent)
			action = onActivate(item, (PlayerInteractEvent) event);
		
		Player player = event.getPlayer();
		
		if (action == BookFinishAction.CONSUME || action == BookFinishAction.BROADCAST_AND_CONSUME)
		{
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
				
				if (action == BookFinishAction.BROADCAST_AND_CONSUME)
					messageEverybody(player);
			}
						
			if (!providesOwnMessage())
			{
				String message = settings.getString(SETTING_USE_MESSAGE);
				message = message.replace("<Spellbook>", getName());
				Util.Message(message, player);
			}
							
			Location playerLoc = player.getLocation();
			MLog.info("Player " + player.getName() + " used spell of " + getName() + " in " + playerLoc.getWorld().getName() + " at " + playerLoc.getBlockX() + ", " + playerLoc.getBlockY() + ", " + playerLoc.getBlockZ());		
		}
		
		long end = System.nanoTime();
		if (Settings.getBoolean(Setting.DEBUG))
		{
			player.sendMessage("Book time: " + (end - start) / 1000000.0);
		}
	}
	
	private void messageEverybody(Player player)
	{
		Long lastBroadcast = lastBroadcastTime.get(player.getName());
		if (lastBroadcast == null)
			lastBroadcast = 0L;
		
		
		if (System.currentTimeMillis() - lastBroadcast > settings.getInt(SETTING_BROADCAST_COOLDOWN_SECONDS) * 1000)
		{
			for (String message : settings.getStringList(SETTING_BROADCAST_MESSAGES))
			{
				message = message.replace("<Player>", player.getName());
				message = message.replace("<Spellbook>", getName());
				Util.Broadcast(message, player.getName());
			}

			lastBroadcastTime.put(player.getName(), System.currentTimeMillis());
			
			Object positiveEffectNode = settings.getProperty(SETTING_BOOST_ON_BROADCAST, false);
			if (positiveEffectNode != null)
			{
				String effectName = (String) positiveEffectNode;
				for (Player onlinePlayer : Bukkit.getOnlinePlayers())
				{
					HardmodeModule.applyDamageNode(onlinePlayer, effectName);

				}
			}
			
			Util.Message(settings.getString(SETTING_HINT_TO_CASTER_WHEN_BROADCASTED_MESSAGE), player);
		}
		

	}
	
	protected abstract BookFinishAction onActivate(SpellbookItem item, PlayerInteractEvent event);
	protected abstract BookFinishAction onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event);

}
