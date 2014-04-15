package us.corenetwork.mantle.spellbooks.books;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.slimespawning.IgnoredSlimeChunks;
import us.corenetwork.mantle.slimespawning.SlimeSpawner;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbooksSettings;


public class UnslimingBook extends Spellbook {	
	@SuppressWarnings("deprecation")
	public UnslimingBook() {
		super("Spellbook of Unsliming");		
	}
		
	@Override
	public boolean onActivate(SpellbookItem item, PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Chunk chunk = player.getLocation().getBlock().getChunk();
		
		if (SlimeSpawner.isSlimeChunk(chunk) && !IgnoredSlimeChunks.isIgnored(chunk.getX(), chunk.getZ()))
		{
			Util.Message(SpellbooksSettings.MESSAGE_UNSLIMING_SLIME_CHUNK.string(), player);
			IgnoredSlimeChunks.addChunk(chunk.getX(), chunk.getZ());
		}
		else
		{
			Util.Message(SpellbooksSettings.MESSAGE_UNSLIMING_NOT_SLIME_CHUNK.string(), player);
		}
		
		return true;
		
	}
	
	@Override
	protected boolean onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return false;
	}
	
	@Override
	protected boolean providesOwnMessage()
	{
		return true;
	}


}
