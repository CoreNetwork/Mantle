package us.corenetwork.mantle.spellbooks;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;

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
			}
		}
	}
	
	@EventHandler()
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (event.isCancelled() && event.getAction() != Action.RIGHT_CLICK_AIR)
			return;
		
		if (event.getItem() != null )
		{
			SpellbookItem spellbookItem = SpellbookItem.parseSpellbook(event.getItem());
			if (spellbookItem != null)
			{
				if  (event.getAction() == Action.RIGHT_CLICK_AIR || //Prevent accidentally activating something when right clicking, unless that is container block
					(spellbookItem.getSpellbook().usesContainers() && event.getClickedBlock() != null && Util.isInventoryContainer(event.getClickedBlock().getTypeId()))) 
				{
					spellbookItem.getSpellbook().activate(spellbookItem, event);
				}
				
				event.setCancelled(true);
			}
		}
	}

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerChangeGamemode(final PlayerGameModeChangeEvent event)
    {
        Bukkit.getScheduler().runTaskLater(MantlePlugin.instance, new Runnable()
        {
            @Override
            public void run()
            {
                event.getPlayer().updateInventory();
            }
        }, 1);
    }
}
