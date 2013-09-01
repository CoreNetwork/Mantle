package us.corenetwork.mantle.spellbooks.books;

import org.bukkit.event.player.PlayerInteractEvent;

import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;

public class GrowthBook extends Spellbook {

	public GrowthBook() {
		super("Spellbook of Growth");
	}

	@Override
	public void onActivate(SpellbookItem item, PlayerInteractEvent event) {
		event.getPlayer().sendMessage("Growing...");
	}

}
