package us.corenetwork.mantle.spellbooks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class SpellbooksListener implements Listener {
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		ItemStack item  = event.getPlayer().getItemInHand();
		
		if (item != null)
		{
			SpellbookItem spellbookItem = SpellbookItem.parseSpellbook(item);
			if (spellbookItem != null)
			{
				spellbookItem.getSpellbook().activate(spellbookItem, event);
				
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler()
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (event.isCancelled() && event.getAction() != Action.RIGHT_CLICK_AIR)
			return;
		
		if (event.getAction() == Action.RIGHT_CLICK_AIR && event.getItem() != null )
		{
			SpellbookItem spellbookItem = SpellbookItem.parseSpellbook(event.getItem());
			if (spellbookItem != null)
			{
				spellbookItem.getSpellbook().activate(spellbookItem, event);
				
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerItemConsumed(PlayerItemConsumeEvent event)
	{
		
	}

}
