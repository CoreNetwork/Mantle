package com.mcnsa.flatcore;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.PortalCreateEvent.CreateReason;
import org.bukkit.inventory.ItemStack;

import com.mcnsa.flatcore.flatcorecommands.CreateChestCommand;
import com.mcnsa.flatcore.rspawncommands.NoDropCommand;
import com.mcnsa.flatcore.rspawncommands.ProtectCommand;

public class FlatcoreListener implements Listener {
	private HashMap<Block, FlintSteelData> flintSteelUsage = new HashMap<Block, FlintSteelData>();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Block clicked = event.getClickedBlock();
		Player player = event.getPlayer();
		ItemStack hand = event.getPlayer().getItemInHand();

		if (event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			if (event.isCancelled())
				return;
		}
		else if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;


		//Investigation tool for portals
		if (hand != null && hand.getTypeId() == Settings.getInt(Setting.INVESTIGATION_TOOL))
		{
			Location source = player.getLocation();
			Claim sourceClaim = GriefPrevention.instance.dataStore.getClaimAt(source, true, null);
			if (sourceClaim == null || sourceClaim.allowBuild(player) == null)
			{
				Location destination = PortalUtil.getOtherSide(clicked.getLocation());
				Claim claim = GriefPrevention.instance.dataStore.getClaimAt(destination, true, null);

				if (claim != null && claim.allowBuild(player) != null)
				{
					String message = Settings.getString(Setting.MESSAGE_CANT_MAKE_PORTAL);
					message = message.replace("<OtherDimension>", clicked.getWorld().getEnvironment() == Environment.NORMAL ? "Nether" : "Overworld");

					Util.Message(message, player);
				}
				else
				{
					Util.Message(Settings.getString(Setting.MESSAGE_CAN_MAKE_PORTAL), player);
				}
			}


		}

		if (event.isCancelled())
			return;

		//Flint and steel detection for portals
		if (hand != null && hand.getType() == Material.FLINT_AND_STEEL)
		{
			FlintSteelData data = new FlintSteelData();
			data.timestamp = System.currentTimeMillis();
			data.player = player.getName();

			flintSteelUsage.put(clicked, data);
		}

		//Restockable chests
		if (clicked.getType() == Material.CHEST || clicked.getType() == Material.TRAPPED_CHEST)
		{

			if (player.getItemInHand() == null || player.getItemInHand().getTypeId() == 0)
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

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(final BlockBreakEvent event)
	{
		final Block block = event.getBlock();

		//Restockable chest
		RestockableChest chest = RestockableChest.getChest(block);
		if (chest != null)
		{
			chest.delete();

			Util.Message(Settings.getString(Setting.MESSAGE_CHEST_DELETED), event.getPlayer());
			return;
		}

		//Do not drop colored sign
		if (block.getState() instanceof Sign)
		{
			Sign sign = (Sign) block.getState();

			String colorSymbol = "\u00A7";
			for (String line : sign.getLines())
			{
				if (line.contains(colorSymbol))
				{
					block.setType(Material.AIR);
					event.setCancelled(true);
					return;
				}
			}
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

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event)
	{		
		if (Util.hasPermission(event.getPlayer(), "mcnsaflatcore.mod"))
		{
			try
			{
				PreparedStatement statement = IO.getConnection().prepareStatement("SELECT (SELECT COUNT(*) FROM villages WHERE lastCheck = lastRestore) AS claimed, (SELECT COUNT(*) FROM villages) AS every");
				ResultSet set = statement.executeQuery();

				set.next();
				int total = set.getInt("every");
				int claimed = set.getInt("claimed");
				set.close();

				int percentage = total == 0 ? 0 : (claimed * 100 / total);

				if (percentage >= Settings.getInt(Setting.RESTORATION_WARN_PERCENTAGE))
				{
					String message = Settings.getString(Setting.MESSAGE_LOGIN_WARN);
					message = message.replace("<Claimed>", Integer.toString(claimed));
					message = message.replace("<Total>", Integer.toString(total));
					message = message.replace("<Percentage>", Integer.toString(percentage));

					Util.Message(message, event.getPlayer());
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event)
	{

		//Check for spawn protection abuse
		if (event instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent newEvent = (EntityDamageByEntityEvent) event;
			if (newEvent.getDamager() instanceof Player)
			{
				Player damager = (Player) newEvent.getDamager();
				if (ProtectCommand.protectedPlayers.containsKey(damager.getName()))
				{
					event.setCancelled(true);

					Util.Message(Settings.getString(Setting.MESSAGE_SPAWN_PROTECTION_DONT_ABUSE), damager);

					return;
				}

			}
		}

	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event)
	{
		final LivingEntity entity = event.getEntity();

		if (entity.getType() == EntityType.SILVERFISH ||
				(entity.getCustomName() != null && Settings.getList(Setting.NO_DROP_NAMES).contains(entity.getCustomName())))
		{
			event.getDrops().clear();
			event.setDroppedExp(0);
			entity.getEquipment().clear();
		}

		//Pigmen dropping nether wart
		if (entity instanceof PigZombie && Util.isNetherFortress(entity.getLocation()))
		{
			event.getDrops().add(new ItemStack(Material.NETHER_STALK, 1));
		}


	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event)
	{
		//Do not drop colored sign
		if (event.getBlock().getState() instanceof Sign)
		{
			Sign sign = (Sign) event.getBlock().getState();

			String colorSymbol = "\u00A7";
			for (String line : sign.getLines())
			{
				if (line.contains(colorSymbol))
				{
					event.setCancelled(true);
					return;
				}
			}
		} 
	}

	@EventHandler(ignoreCancelled = true)
	public void onPortalCreate(PortalCreateEvent event)
	{

		if (event.getReason() == CreateReason.FIRE)
		{
			//Prevent creating portal out of boundaries
			int minX;
			int maxX;
			int minZ;
			int maxZ;
			int minY;
			int maxY;
			if (event.getBlocks().get(0).getWorld().getEnvironment() == Environment.NETHER)
			{
				minX = Settings.getInt(Setting.NETHER_MIN_X);
				maxX = Settings.getInt(Setting.NETHER_MAX_X);
				minZ = Settings.getInt(Setting.NETHER_MIN_Z);
				maxZ = Settings.getInt(Setting.NETHER_MAX_Z);
				minY = Settings.getInt(Setting.NETHER_PORTAL_MIN_Y);
				maxY = Settings.getInt(Setting.NETHER_PORTAL_MAX_Y);

			}
			else
			{
				minX = Settings.getInt(Setting.MAP_MIN_X);
				maxX = Settings.getInt(Setting.MAP_MAX_X);
				minZ = Settings.getInt(Setting.MAP_MIN_Z);
				maxZ = Settings.getInt(Setting.MAP_MAX_Z);
				minY = Settings.getInt(Setting.MAP_PORTAL_MIN_Y);
				maxY = Settings.getInt(Setting.MAP_PORTAL_MAX_Y);
			}


			for (Block b : event.getBlocks())
			{
				if (b.getX() < minX || b.getX() > maxX || b.getZ() < minZ || b.getZ() > maxZ || b.getY() < minY || b.getY() > maxY)
				{
					Util.placeSign(Util.findBestSignLocation(event.getBlocks()), Settings.getString(Setting.SIGN_PORTAL_OUT_OF_BOUNDARIES));

					FCLog.debug("Portal out of bounds ! " + b);
					FCLog.debug("limits: " + minX + " " + maxX + " " + minZ + " " + maxZ + " " + minY + " " + maxY);
					FCLog.debug("conditionals: " + (b.getX() < minX) + " " + (b.getX() > maxX) + " " + (b.getZ() < minZ) + " " + (b.getZ() > maxZ) + " " + (b.getY() < minY) + " " + (b.getY() > maxY));

					event.setCancelled(true);
					return;
				}

			}

			//Prevent creating portals into other claims
			Location destination = PortalUtil.getOtherSide(event.getBlocks().get(0).getLocation());
			Claim claim = GriefPrevention.instance.dataStore.getClaimAt(destination, true, null);

			if (claim != null)
			{
				FlintSteelData creator = null;

				for (Block b : event.getBlocks())
				{
					FlintSteelData data = flintSteelUsage.get(b);
					if (data != null && System.currentTimeMillis() - data.timestamp < 1000)
					{
						creator = data;
						break;
					}
				}


				if (creator == null)
				{
					Util.placeSign(Util.findBestSignLocation(event.getBlocks()), Settings.getString(Setting.SIGN_OVERLAP_CLAIM));

					event.setCancelled(true);
					return;
				}

				Player player = Bukkit.getServer().getPlayerExact(creator.player);

				if (player == null || claim.allowBuild(player) != null)
				{
					Util.placeSign(Util.findBestSignLocation(event.getBlocks()), Settings.getString(Setting.SIGN_OVERLAP_CLAIM));

					event.setCancelled(true);
					return;
				}
			}



		}
	}	

	@EventHandler(ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent event)
	{
		if (event.getTarget() instanceof Player)
		{
			//Check for spawn protection
			Player player = (Player) event.getTarget();
			if (ProtectCommand.protectedPlayers.containsKey(player.getName()))
			{
				event.setCancelled(true);
				return;
			}
		}


		//Restrict pigmen range
		if (event.getReason() == TargetReason.PIG_ZOMBIE_TARGET)
		{
			Location targetLocation = event.getTarget().getLocation();
			Location pigmanLocation = event.getEntity().getLocation();

			int distance = (int) Math.round(pigmanLocation.distance(targetLocation));

			if (distance > Settings.getInt(Setting.PIGMAN_ANGER_RANGE))
			{
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPlayerPortal(PlayerPortalEvent event)
	{
		//Controlled nether portals
		if (event.getCause() == TeleportCause.NETHER_PORTAL)
		{
			Location destination = PortalUtil.processTeleport(event.getPlayer());
			event.setTo(destination);
			event.getPortalTravelAgent().setCanCreatePortal(false);
			event.getPortalTravelAgent().setSearchRadius(0);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onEntityPortal(EntityPortalEvent event)
	{
		Location destination = PortalUtil.processTeleport(event.getEntity());
		event.setTo(destination);
		event.getPortalTravelAgent().setCanCreatePortal(false);
		event.getPortalTravelAgent().setSearchRadius(0);

	}

	@EventHandler(ignoreCancelled = true)
	public void onFoodchange(FoodLevelChangeEvent event)
	{
		//Check for spawn protection
		Player player = (Player) event.getEntity();
		if (ProtectCommand.protectedPlayers.containsKey(player.getName()))
		{
			event.setCancelled(true);
			return;
		}

	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		if (NoDropCommand.blockedPlayers.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}
}
