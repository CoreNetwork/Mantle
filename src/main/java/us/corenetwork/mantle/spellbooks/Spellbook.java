package us.corenetwork.mantle.spellbooks;

import org.bukkit.event.player.PlayerInteractEvent;

import us.corenetwork.core.Util;

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
		if (item.isSoulbound() && !event.getPlayer().getName().equalsIgnoreCase(item.getSoulboundOwner()))
		{
			Util.Message(SpellbooksSettings.MESSAGE_NOT_AUTHORIZED.string(), event.getPlayer());
			return;
		}
		
		onActivate(item, event);
	}
	
	protected abstract void onActivate(SpellbookItem item, PlayerInteractEvent event);
}
