package us.corenetwork.mantle.spellbooks.books;

import me.ryanhamshire.GriefPrevention.Claim;

import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.slimespawning.IgnoredSlimeChunks;
import us.corenetwork.mantle.slimespawning.SlimeSpawner;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;
import us.corenetwork.mantle.spellbooks.SpellbooksSettings;


public class UnslimingBook extends Spellbook {	

	private static final String SETTING_MESSAGE_NOT_SLIME_CHUNK = "Messages.UseNotSlimeChunk";
	private static final String SETTING_MESSAGE_SLIME_CHUNK = "Messages.UseSlimeChunk";

	
	@SuppressWarnings("deprecation")
	public UnslimingBook() {
		super("Unsliming");		
		
		settings.setDefault(SETTING_TEMPLATE, "spell-unsliming");
		settings.setDefault(SETTING_MESSAGE_NOT_SLIME_CHUNK, "Your current chunk is not slimed!");
		settings.setDefault(SETTING_MESSAGE_SLIME_CHUNK, "That slime chunk has been purged! Those little bastards won't bugger you anymore.");
	}
		
	@Override
	public BookFinishAction onActivate(SpellbookItem item, PlayerInteractEvent event) {
		Player player = event.getPlayer();
		
		Claim claim = GriefPreventionHandler.getClaimAt(player.getLocation());
		if (claim != null && claim.allowBuild(player) != null)
		{
			Util.Message(SpellbooksSettings.MESSAGE_NO_PERMISSION.string(), event.getPlayer());
			return BookFinishAction.NOTHING;
		}

		
		Chunk chunk = player.getLocation().getBlock().getChunk();		
		boolean slimeChunk = SlimeSpawner.isSlimeChunk(chunk) && !IgnoredSlimeChunks.isIgnored(chunk.getX(), chunk.getZ()); 
		if (slimeChunk)
		{
			Util.Message(settings.getString(SETTING_MESSAGE_SLIME_CHUNK), player);
			IgnoredSlimeChunks.addChunk(chunk.getX(), chunk.getZ());
		}
		else
		{
			Util.Message(settings.getString(SETTING_MESSAGE_NOT_SLIME_CHUNK), player);
		}
		
		Color slimeColor = Color.fromRGB(0x8bbb79);
		FireworkEffect effect = FireworkEffect.builder().withColor(slimeColor).withFade(slimeColor).build();
		Location effectLoc = SpellbookUtil.getPointInFrontOfPlayer(player.getEyeLocation(), 2);
		Util.showFirework(effectLoc, effect);
		effectLoc.getWorld().playSound(effectLoc, Sound.SLIME_WALK2, 2f, 1f);
		
		if (slimeChunk)
			return BookFinishAction.BROADCAST_AND_CONSUME;
		else
			return BookFinishAction.CONSUME;
		
	}
	
	@Override
	protected BookFinishAction onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return BookFinishAction.NOTHING;
	}
	
	@Override
	protected boolean providesOwnMessage()
	{
		return true;
	}


}