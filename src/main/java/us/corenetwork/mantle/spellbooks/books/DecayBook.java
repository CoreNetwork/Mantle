package us.corenetwork.mantle.spellbooks.books;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import us.core_network.cornel.common.Messages;
import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;
import us.corenetwork.mantle.spellbooks.SpellbooksSettings;
import us.core_network.cornel.items.InventoryUtil;


public class DecayBook extends Spellbook {

	private static final int EFFECT_RADIUS = 32 / 2;
	
	public DecayBook() {
		super("Decay");
		
		settings.setDefault(SETTING_TEMPLATE, "spell-decay");
		settings.setDefault(SETTING_NO_ITEMS, "No rotten flesh");
	}

	@Override
	protected boolean usesContainers() {
		return true;
	}
	
	@Override
	public BookFinishAction onActivate(SpellbookItem item, PlayerInteractEvent event) {
		
		Player player = event.getPlayer();

		boolean playerHasRottenFlesh = InventoryUtil.getAmountOfItems(player.getInventory(), Material.ROTTEN_FLESH, Short.MAX_VALUE) >= 128;
		if (playerHasRottenFlesh)
		{
			InventoryUtil.removeItems(player.getInventory(), Material.ROTTEN_FLESH, Short.MAX_VALUE, 128);
		}
		else
		{
            Messages.send(settings.getString(SETTING_NO_ITEMS), event.getPlayer());
			return BookFinishAction.NOTHING;
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && Util.isInventoryContainer(event.getClickedBlock().getTypeId()))
		{
			//Check for claim if clicking on chest
			Claim claim = GriefPreventionHandler.getClaimAt(player.getLocation());
			if (claim != null && claim.allowContainers(player) != null)
			{
                Messages.send(SpellbooksSettings.MESSAGE_NO_PERMISSION.string(), event.getPlayer());
				return BookFinishAction.NOTHING;
			}

			//Only clean wool colors
			InventoryHolder container = (InventoryHolder) event.getClickedBlock().getState();
			removeWoolColors(container.getInventory());
		}
		else
		{
			Location playerLoc = player.getLocation();
			//Check for claims in effect area
			if (GriefPreventionHandler.containsClaim(playerLoc.getWorld(), playerLoc.getBlockX() - EFFECT_RADIUS, playerLoc.getBlockZ() - EFFECT_RADIUS, 0, 0, EFFECT_RADIUS * 2, false, event.getPlayer()))
			{
                Messages.send(SpellbooksSettings.MESSAGE_NO_PERMISSION.string(), event.getPlayer());
				return BookFinishAction.NOTHING;
			}
			
			boolean removeGrass = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.GRASS;
			
			//Decay world around
			Block baseBlock = player.getLocation().getBlock();
			
			for (int x = -EFFECT_RADIUS; x <= EFFECT_RADIUS; x++)
			{
				for (int y = -EFFECT_RADIUS; y <= EFFECT_RADIUS; y++)
				{
					for (int z = -EFFECT_RADIUS; z <= EFFECT_RADIUS; z++)
					{
						Block block = baseBlock.getRelative(x, y, z);
						
						if (block.getType() == Material.LEAVES || block.getType() == Material.YELLOW_FLOWER || block.getType() == Material.RED_ROSE || (block.getType() == Material.DOUBLE_PLANT && block.getData() != 2) || block.getType() == Material.HUGE_MUSHROOM_1 || block.getType() == Material.HUGE_MUSHROOM_2)
							block.breakNaturally();
						else if (block.getType() == Material.LONG_GRASS || (block.getType() == Material.DOUBLE_PLANT && block.getData() == 2))
							block.setType(Material.AIR);
						else if (block.getType() == Material.GRASS && removeGrass)
							block.setType(Material.DIRT);
					}
				}
			}
			
			removeWoolColors(player.getInventory());
			player.updateInventory();
		}

		FireworkEffect effect = FireworkEffect.builder().withColor(Color.OLIVE).withFade(Color.OLIVE).build();
		Location effectLoc = SpellbookUtil.getPointInFrontOfPlayer(player.getEyeLocation(), 2);
		Util.showFirework(effectLoc, effect);			
		effectLoc.getWorld().playSound(effectLoc, Sound.SKELETON_DEATH, 1f, 1f);		
		
		return BookFinishAction.BROADCAST_AND_CONSUME;
	}
	
	private void removeWoolColors(Inventory inventory)
	{
		for (int i = 0; i < inventory.getSize(); i++)
		{
			ItemStack stack = inventory.getItem(i);
			if (stack != null && stack.getType() == Material.WOOL)
			{
				Wool materialData = (Wool) stack.getData();
				materialData.setColor(DyeColor.WHITE);
				stack.setDurability(materialData.getData());

				inventory.setItem(i, stack);
			}
		}
	}
	
	@Override
	protected BookFinishAction onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return BookFinishAction.NOTHING;
	}
}
