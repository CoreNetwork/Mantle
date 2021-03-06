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
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.regeneration.RegenerationSettings;
import us.corenetwork.mantle.restockablechests.commands.CreateChestCommand;


public class RChestsListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event)
	{

		Block clicked = event.getClickedBlock();
		Player player = event.getPlayer();
		ItemStack hand = player.getItemInHand();

		
		if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& hand.getType() == Material.COMPASS  && player.getWorld().getEnvironment() == World.Environment.NORMAL)
		{
			player.openInventory(new GUICategoryPicker(player));
			event.setCancelled(true);
			return;
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
				//This can probably be simplified, but my brain doesnt work
				//#S04
				if (chest.getBlock().getWorld().getEnvironment() == World.Environment.NORMAL)
				{
					if (isVillageClaimed(chest.getStructureID()))
					{
						Util.Message(RChestSettings.MESSAGE_OPENING_CHEST_IN_CLAIMED_VILLAGE.string(), player);
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
			boolean isReal = chest.isReal();
			if (isVillageClaimed(chest.getStructureID()))
			{
				return;
			}		
			else
			{
				if(isReal)
				{
					event.setCancelled(true);
					Util.Message(RChestSettings.MESSAGE_CHEST_DESTROYED.string(), event.getPlayer());
					return;
				}
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
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		ItemStack itemInHand = event.getPlayer().getItemInHand();
		if (itemInHand == null || itemInHand.getType() != Material.COMPASS)
			return;
		
		//MLog.info("Compass in hand!");
		
		CompassDestination destination = CompassDestination.destinations.get(event.getPlayer().getUniqueId());
		if (destination != null && event.getPlayer().getWorld().equals(Bukkit.getWorld("world")))
			destination.playerMoved(event);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		//Destination already in memory
		if(CompassDestination.destinations.containsKey(player.getUniqueId()))
		{
			CompassDestination.destinations.get(player.getUniqueId()).refreshCompassTarget(player);
			return;
		}
		
		String category = null;
		int chestID = 0;
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT diminishTotal, CompassCategory, CompassChestID FROM playerTotal WHERE PlayerUUID = ? LIMIT 1");
			
			statement.setString(1, player.getUniqueId().toString());
			
			ResultSet set = statement.executeQuery();
			if(set.next())
			{
				category = set.getString("CompassCategory");
				chestID = set.getInt("CompassChestID");
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		//No selected compass target, nothing to do
		if(category == null)
		{
			return;
		}
		
		
		//Add a proper destination to in-memory collection
		Category cat = RChestsModule.categories.get(category);
		int x = 0, z = 0;
		VillageInfoHelper vih = null;
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT ch.X, ch.Z, v.ID, v.CornerX, v.CornerZ, v.SizeX, v.SizeZ, v.World FROM chests ch, regeneration_structures v WHERE ch.ID = ? AND ch.StructureID = v.ID");
			
			statement.setInt(1, chestID);
			
			ResultSet set = statement.executeQuery();
			if(set.next())
			{
				x = set.getInt(1);
				z = set.getInt(2);
				
				vih = new VillageInfoHelper(set.getInt(3), set.getInt(4), set.getInt(5), set.getInt(6), set.getInt(7), 0, set.getString(8));
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		//if vih == null, then the chest doesnt exist anymore - village got regenerated when player was offline
		//we will clear his value in the db, and do nothing here.
		if(vih != null)
		{
			CompassDestination.addDestination(player, new CompassDestination(x, z, vih, cat));
		}
		else
		{
			Util.Message(RChestSettings.MESSAGE_COMPASS_VILLAGE_REGENRATED_WHILE_GONE.string(), player);
			try
			{
				PreparedStatement statement = IO.getConnection().prepareStatement("UPDATE playerTotal SET CompassCategory = ?, CompassChestID = 0 WHERE PlayerUUID = ?");
				statement.setString(2, player.getUniqueId().toString());
				statement.setString(1, null);	
				statement.executeUpdate();
				statement.close();
				
				IO.getConnection().commit();
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerQuitEvent(PlayerQuitEvent event)
	{
		CompassDestination.removeDestinationSlow(event.getPlayer().getUniqueId());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerChangedWorldEvent(final PlayerChangedWorldEvent event)
	{
		//Refeshing compass after coming back from nether.
		//Nether causes compass to loss direction.
		if (event.getPlayer().getWorld().getEnvironment() == World.Environment.NORMAL)
		{
			CompassDestination destination = CompassDestination.destinations.get(event.getPlayer().getUniqueId());
			if(destination != null)
				destination.refreshCompassTarget(event.getPlayer());
			else
				CompassDestination.resetCompassTarget(event.getPlayer());
		}
	}


	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerBedLeaveEvent(final PlayerBedLeaveEvent event)
	{
		//Refresh compass destination (if none set by the actual compass tracking) to the bed location
		if (event.getPlayer().getWorld().getEnvironment() == World.Environment.NORMAL)
		{
			CompassDestination destination = CompassDestination.destinations.get(event.getPlayer().getUniqueId());
			if(destination == null)
			{
				//We need new bed location, which is assigned to a player after the event

				Bukkit.getScheduler().scheduleSyncDelayedTask(MantlePlugin.instance, new Runnable() {
					@Override
					public void run()
					{
						CompassDestination.resetCompassTarget(event.getPlayer());
					}
				}, 1);
			}
		}
	}
}
