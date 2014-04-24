package us.corenetwork.mantle.spellbooks.books;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;
import us.corenetwork.mantle.spellbooks.SpellbooksSettings;


public class DecayBook extends Spellbook {

	private static final int EFFECT_RADIUS = 32 / 2;
	
	public DecayBook() {
		super("Decay");
	}

	@Override
	public boolean onActivate(SpellbookItem item, PlayerInteractEvent event) {
		Location playerLoc = event.getPlayer().getLocation();
		
		if (GriefPreventionHandler.containsClaim(playerLoc.getWorld(), playerLoc.getBlockX(), playerLoc.getBlockZ(), 0, 0, EFFECT_RADIUS, false, event.getPlayer()))
		{
			Util.Message(SpellbooksSettings.MESSAGE_NO_PERMISSION.string(), event.getPlayer());
			return false;
		}
		
		FireworkEffect effect = FireworkEffect.builder().withColor(Color.OLIVE).withFade(Color.OLIVE).build();
		Location effectLoc = SpellbookUtil.getPointInFrontOfPlayer(event.getPlayer().getEyeLocation(), 2);
		Util.showFirework(effectLoc, effect);
		
		effectLoc.getWorld().playSound(effectLoc, Sound.SKELETON_DEATH, 1f, 1f);
		
		Block baseBlock = event.getPlayer().getLocation().getBlock();
		
		for (int x = -EFFECT_RADIUS; x <= EFFECT_RADIUS; x++)
		{
			for (int y = -EFFECT_RADIUS; y <= EFFECT_RADIUS; y++)
			{
				for (int z = -EFFECT_RADIUS; z <= EFFECT_RADIUS; z++)
				{
					Block block = baseBlock.getRelative(x, y, z);
					processBlock(block);
				}
			}
		}
		
		
		return true;
	}
	private void processBlock(Block block)
	{
		if (block.getType() == Material.LEAVES)
			block.breakNaturally();
		else if (block.getType() == Material.GRASS)
			block.setType(Material.DIRT);
	}

	@Override
	protected boolean onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return false;
	}
}
