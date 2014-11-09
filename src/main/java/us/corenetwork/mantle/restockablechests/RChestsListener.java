package us.corenetwork.mantle.restockablechests;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.regeneration.RegenerationSettings;
import us.corenetwork.mantle.restockablechests.commands.CreateChestCommand;


public class RChestsListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event)
	{

		Block clicked = event.getClickedBlock();
		Player player = event.getPlayer();
		ItemStack hand = player.getItemInHand();

		
		if(event.getAction() == Action.RIGHT_CLICK_AIR)
		{
			if(hand.getType() == Material.COMPASS)
			{
				
			}
		}
		
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

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
				if (isVillageClaimed(chest.getStructureID()))
				{
					return;
				}		
				else
				{
					if (chest.open(player))
					{
						event.setCancelled(true);
					}
				}
			}
		}

	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBlockBreak(final BlockBreakEvent event)
	{
		final Block block = event.getBlock();

		RestockableChest chest = RestockableChest.getChest(block);
		if (chest != null)
		{
			if (isVillageClaimed(chest.getStructureID()))
			{
				return;
			}		
			else
			{
				event.setCancelled(true);
				Util.Message(RChestSettings.MESSAGE_CHEST_DESTROYED.string(), event.getPlayer());
				return;
			}
			
		}
	}
	
	private boolean isVillageClaimed(int structureID)
	{
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT ID, CornerX, CornerZ, SizeX, SizeZ, World FROM regeneration_structures WHERE ID = ? LIMIT 1");
			statement.setInt(1, structureID);

			ResultSet set = statement.executeQuery();
			if (set.next())
			{
				final int villageX = set.getInt("CornerX");
				final int villageZ = set.getInt("CornerZ");
				final int xSize = set.getInt("SizeX");
				final int zSize = set.getInt("SizeZ");
							
				int padding = RegenerationSettings.RESORATION_VILLAGE_CHECK_PADDING.integer();
				World world = Bukkit.getWorld(set.getString("World"));
				if (GriefPreventionHandler.containsClaim(world, villageX, villageZ, xSize, zSize, padding, false, null))
				{
					return true;
				}		
				else
				{
					return false;
				}

			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onInventoryClose(InventoryCloseEvent event)
	{
		RestockableChest.inventoryClosed((Player) event.getPlayer());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onInventoryClick(InventoryClickEvent event)
	{
		RestockableChest.inventoryClicked(event);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		RestockableChest.inventoryClosed((Player) event.getPlayer());
	}
	
}
