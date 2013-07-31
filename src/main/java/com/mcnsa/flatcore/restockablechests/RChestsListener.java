package com.mcnsa.flatcore.restockablechests;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;
import com.mcnsa.flatcore.restockablechests.commands.CreateChestCommand;

public class RChestsListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		Block clicked = event.getClickedBlock();
		Player player = event.getPlayer();
		ItemStack hand = player.getItemInHand();


		if (Util.isInventoryContainer(clicked.getTypeId()))
		{

			if (hand == null || hand.getTypeId() == 0)
			{
				if (CreateChestCommand.playerHitChestArm(player, clicked))
				{
					event.setCancelled(true);
					return;
				}
			}

			RestockableChest chest = RestockableChest.getChest(clicked);
			if (chest != null)
			{
				if (chest.open(player))
				{
					event.setCancelled(true);
				}
			}
		}

	}
	
	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event)
	{
		final Block block = event.getBlock();

		RestockableChest chest = RestockableChest.getChest(block);
		if (chest != null)
		{
			chest.delete();

			Util.Message(Settings.getString(Setting.MESSAGE_CHEST_DELETED), event.getPlayer());
			return;
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event)
	{
		RestockableChest.inventoryClosed((Player) event.getPlayer());
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		RestockableChest.inventoryClosed((Player) event.getPlayer());
	}
	
}
