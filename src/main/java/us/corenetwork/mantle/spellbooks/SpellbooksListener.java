package us.corenetwork.mantle.spellbooks;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class SpellbooksListener implements Listener {
	
	@EventHandler()
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (event.isCancelled() && event.getAction() != Action.RIGHT_CLICK_AIR)
			return;
		
		if (event.getAction() == Action.RIGHT_CLICK_AIR
			&& event.getItem() != null && event.getItem().getType() == Material.ENCHANTED_BOOK)
		{
			SpellbookItem item = SpellbookItem.parseSpellbook(event.getItem());
			if (item != null)
			{
				item.getSpellbook().activate(item, event);
				
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