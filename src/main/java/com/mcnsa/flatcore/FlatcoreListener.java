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
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
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

import com.mcnsa.flatcore.flatcorecommands.CreateChestCommand;
import com.mcnsa.flatcore.rspawncommands.ProtectCommand;

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

		//Protect admins against evil features and don't execute evil features in end
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE && event.getPlayer().getWorld().getEnvironment() != Environment.THE_END)
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

		if (onCropDestroyed(block, false))
		{
			event.setCancelled(true);
			return;
		}
		onCropDestroyed(block.getRelative(BlockFace.UP), false);

	}

	//Virtal event that combines multiple events. 
	//Triggers when non-solid block (like crops) is about to be destroyed
	public boolean onCropDestroyed(Block block, boolean dark)
	{
		if (block == null)
			return false;

		//Don't drop seeds if not fully grown
		if ((block.getType() == Material.PUMPKIN_STEM || block.getType() == Material.MELON_STEM || block.getType() == Material.CROPS || block.getType() == Material.POTATO || block.getType() == Material.CARROT) && block.getData() < 7)
		{
			block.setType(Material.AIR);
			return true;
		}

		//Do not drop more netherwart
		if (!dark && block.getType() == Material.NETHER_WARTS)
		{
			block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.NETHER_STALK, 1));
			block.setType(Material.AIR);
			return true;
		}

		return false;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		//Do not apply anything to End
		if (event.getBlock().getWorld().getEnvironment() == Environment.THE_END)
			return;

		//Protect admins against evil features
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;


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
		if (event.getPlayer().hasPermission("mcnsaflatcore.mod"))
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
		//Do not apply anything to End or void
		if (event.getCause() == DamageCause.VOID || event.getEntity().getWorld().getEnvironment() == Environment.THE_END)
			return;

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
					if (!(event.getEntity() instanceof Player))
						event.getEntity().remove();

					Util.Message(Settings.getString(Setting.MESSAGE_SPAWN_PROTECTION_DONT_ABUSE), damager);

					return;
				}

			}
		}

		//Environmental damage
		if (!(event.getEntity() instanceof Player))
			return;

		//Check for spawn protection
		Player player = (Player) event.getEntity();
		if (ProtectCommand.protectedPlayers.containsKey(player.getName()))
		{
			event.setCancelled(true);
			return;
		}

		NodeParser.parseDamageEvent(event);
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event)
	{
		final LivingEntity entity = event.getEntity();

		//Mobs with name or silverfishes should not drop anything
		if (entity.getCustomName() != null || entity.getType() == EntityType.SILVERFISH)
		{
			event.getDrops().clear();
			event.setDroppedExp(0);
			entity.getEquipment().clear();
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
			event.getDrops().clear();
			event.setDroppedExp(0);
			entity.getEquipment().clear();
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

		//		if (event.getBlock().getLightLevel() < 9)
		//		{
		//			if (onCropDestroyed(event.getBlock(), true))
		//			{
		//				event.setCancelled(true);
		//				return;
		//			}
		//		}

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
		//		if (event.getEntityType() != EntityType.PIG_ZOMBIE)
		//			return;

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
	public void onPistonExtend(BlockPistonExtendEvent event)
	{
		Block firstBlock = event.getBlock().getRelative(event.getDirection());
		onCropDestroyed(firstBlock, false);
		onCropDestroyed(firstBlock.getRelative(BlockFace.UP), false);

		for (Block b : event.getBlocks())
		{
			onCropDestroyed(b, false);
			onCropDestroyed(b.getRelative(BlockFace.UP), false);
		}

	}

	@EventHandler(ignoreCancelled = true)
	public void onPistonRetract(BlockPistonRetractEvent event)
	{
		if (event.isSticky())
		{
			onCropDestroyed(event.getRetractLocation().getBlock().getRelative(BlockFace.UP), false);
		}
	}	

	@EventHandler(ignoreCancelled = true)
	public void onLiquidMove(BlockFromToEvent event)
	{
		onCropDestroyed(event.getToBlock(), false);
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityInteract(EntityInteractEvent event)
	{
		if (onCropDestroyed(event.getBlock(), false))
		{
			event.setCancelled(true);
			return;
		}

		Block aboveBlock = event.getBlock().getRelative(BlockFace.UP);
		if (aboveBlock != null && onCropDestroyed(aboveBlock, false))
		{
			event.setCancelled(true);
			return;
		}

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
}
