package com.mcnsa.flatcore;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.PortalCreateEvent.CreateReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.mcnsa.flatcore.admincommands.CreateChestCommand;

public class FlatcoreListener implements Listener {
	private HashMap<Block, FlintSteelData> flintSteelUsage = new HashMap<Block, FlintSteelData>();
	private static HashSet<Byte> transparentBlocks = new HashSet<Byte>();

	static
	{
		transparentBlocks.add((byte) 0);
	}

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

			//Punching fire turns player on fire
			Block target = player.getTargetBlock(transparentBlocks, 7);			
			if (target != null && target.getType() == Material.FIRE)
			{
				int duration = Settings.getInt(Setting.PLAYER_PUNCH_FIRE_DURATION);
				player.setFireTicks(duration);
				player.sendBlockChange(target.getLocation(), Material.FIRE.getId(), (byte) 0);
				event.setCancelled(true);
			}

			return;
		}
		else if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;


		//Investigation tool for portals
		if (hand != null && hand.getTypeId() == Settings.getInt(Setting.INVESTIGATION_TOOL))
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

		//Extinguish fire using bucket in nether
		if (hand != null && hand.getType() == Material.WATER_BUCKET && clicked.getWorld().getEnvironment() == Environment.NETHER)
		{
			Block target = player.getTargetBlock(transparentBlocks, 7);			
			if (target != null && target.getType() == Material.FIRE)
			{
				target.setType(Material.AIR);
			}
		}		
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(final BlockBreakEvent event)
	{
		final Block block = event.getBlock();
		//Do not drop more netherwart
		if (block.getType() == Material.NETHER_WARTS)
		{
			event.setCancelled(true);
			block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.NETHER_STALK, 1));
			block.setType(Material.AIR);
			return;
		}

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

		//Protect admins against evil features
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
		{
			//Netherrack turns into fire
			if (block.getType() == Material.NETHERRACK)
			{
				int chance = Settings.getInt(Setting.NETHERRACK_FIRE_CHANCE);
				if (MCNSAFlatcore.random.nextInt(100) < chance)
				{
					event.setCancelled(true);
					block.setType(Material.FIRE);
					return;
				}
			}

			//Spread fire below if broken
			Block upperBlock = block.getRelative(BlockFace.UP);
			if (upperBlock.getType() == Material.FIRE)
			{
				Block lowerBlock = block.getRelative(BlockFace.DOWN);
				if (lowerBlock != null && lowerBlock.getType().isSolid())
				{
					//Place fire after 1 tick
					Bukkit.getScheduler().scheduleSyncDelayedTask(MCNSAFlatcore.instance, new Runnable() {
						@Override
						public void run() {
							block.setType(Material.FIRE);
						}
					});
				}

			}
		}

		if (onBlockDestroyed(block))
		{
			event.setCancelled(true);
			return;
		}
		onBlockDestroyed(block.getRelative(BlockFace.UP));

	}

	//Virtal event that combines multiple events. 
	//Triggers when non-solid block (like crops) is about to be destroyed
	public boolean onBlockDestroyed(Block block)
	{
		if (block == null)
			return false;

		//Don't drop seeds if not fully grown
		if ((block.getType() == Material.PUMPKIN_STEM || block.getType() == Material.MELON_STEM || block.getType() == Material.CROPS || block.getType() == Material.POTATO || block.getType() == Material.CARROT) && block.getData() < 7)
		{
			block.setType(Material.AIR);
			return true;
		}

		//When fully grown potato is broken, drop 0-2 normal potatoes and 1-2 poisonous ones.
		if (block.getType() == Material.POTATO)
		{
			block.setType(Material.AIR);


			int amount = MCNSAFlatcore.random.nextInt(3);
			if (amount > 0)
				block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.POTATO_ITEM, amount));

			amount = MCNSAFlatcore.random.nextInt(2) + 1;
			block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.POISONOUS_POTATO, amount));

			return true;
		}

		//Carrots should drop 1-2.
		if (block.getType() == Material.CARROT)
		{
			block.setType(Material.AIR);

			int amount = MCNSAFlatcore.random.nextInt(3);
			if (amount > 0)
				block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.CARROT_ITEM, amount));

			return true;
		}

		return false;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		BlockState previousBlock = event.getBlockReplacedState();


		//Spread fire if you replace it with block
		if (previousBlock.getType() == Material.FIRE)
		{
			for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH})
			{
				Block neighbour = previousBlock.getBlock().getRelative(face);

				if (neighbour != null && neighbour.isEmpty())
				{
					Block belowNeighbour = neighbour.getRelative(BlockFace.DOWN);

					if (belowNeighbour != null && belowNeighbour.isEmpty())
						belowNeighbour.setType(Material.FIRE);
					else
						neighbour.setType(Material.FIRE);
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
		if (event.getPlayer().hasPermission("flacore.mod"))
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

		//Environmental damage

		if (!(event.getEntity() instanceof Player))
			return;

		NodeParser.parseDamageEvent(event);
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event)
	{
		final LivingEntity entity = event.getEntity();

		//Mobs with name or silverfishes should not drop anything
		if (entity.getCustomName() != null || entity.getType() == EntityType.SILVERFISH)
		{
			entity.remove();
			return;
		}

		//Pigmen dropping nether wart
		if (entity instanceof PigZombie && Util.isNetherFortress(entity.getLocation()))
		{
			event.getDrops().add(new ItemStack(Material.NETHER_STALK, 1));
		}

		//Respawned mobs not dropping anything
		if (entity.getMetadata("Respawned").size() > 0)
		{
			entity.remove();
		}

		//Respawn zombie
		if (entity.getType() == EntityType.ZOMBIE && entity.getFireTicks() == -1)
		{
			int chance = Settings.getInt(Setting.ZOMBIE_RESPAWN_CHANCE);
			if (MCNSAFlatcore.random.nextInt(100) < chance)
			{
				//Play effect
				Builder builder = FireworkEffect.builder();

				Util.showFirework(entity.getLocation(), builder.trail(false).withColor(Color.BLACK).build());	
				entity.getWorld().playSound(entity.getLocation(), Sound.ZOMBIE_UNFECT, 1, 1);

				//Spawn new zombie

				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MCNSAFlatcore.instance, new Runnable() {

					@Override
					public void run() {
						Zombie newZombie = entity.getWorld().spawn(entity.getLocation(), Zombie.class);
						newZombie.setHealth(newZombie.getMaxHealth() / 2);
						newZombie.setMetadata("Respawned", new FixedMetadataValue(MCNSAFlatcore.instance, true));
					}

				}, 10);


				return;
			}
		}

		//Do not drop ghast tear if killed by bow
		if (event.getEntityType() == EntityType.GHAST && event.getEntity().getLastDamageCause().getCause() == DamageCause.PROJECTILE)
		{
			for (ItemStack stack : event.getDrops().toArray(new ItemStack[0]))
			{
				if (stack.getType() == Material.GHAST_TEAR)
					event.getDrops().remove(stack);
			}
			//event.setDroppedExp(0);
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
//		if (event.getEntityType() != EntityType.PIG_ZOMBIE)
//			return;
		
		//FCLog.info(event.getReason().toString());
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
	
	@EventHandler(ignoreCancelled = true)
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

	@EventHandler(ignoreCancelled = true)
	public void onEntityPortal(EntityPortalEvent event)
	{
		Location destination = PortalUtil.processTeleport(event.getEntity());
		event.setTo(destination);
		event.getPortalTravelAgent().setCanCreatePortal(false);
		event.getPortalTravelAgent().setSearchRadius(0);

	}

	@EventHandler(ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent event)
	{
		Block firstBlock = event.getBlock().getRelative(event.getDirection());
		onBlockDestroyed(firstBlock);
		onBlockDestroyed(firstBlock.getRelative(BlockFace.UP));

		for (Block b : event.getBlocks())
		{
			onBlockDestroyed(b);
			onBlockDestroyed(b.getRelative(BlockFace.UP));
		}

	}

	@EventHandler(ignoreCancelled = true)
	public void onPistonRetract(BlockPistonRetractEvent event)
	{
		if (event.isSticky())
		{
			onBlockDestroyed(event.getRetractLocation().getBlock().getRelative(BlockFace.UP));
		}
	}	
}
