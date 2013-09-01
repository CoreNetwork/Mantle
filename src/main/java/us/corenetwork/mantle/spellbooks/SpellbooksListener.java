package us.corenetwork.mantle.spellbooks;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SpellbooksListener implements Listener {
	
	@EventHandler()
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (event.isCancelled() && event.getAction() != Action.RIGHT_CLICK_AIR)
			return;
		
		if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
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
}
