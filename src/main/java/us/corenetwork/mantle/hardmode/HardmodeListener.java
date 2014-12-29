package us.corenetwork.mantle.hardmode;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.server.v1_8_R1.AttributeInstance;
import net.minecraft.server.v1_8_R1.EntityCreature;
import net.minecraft.server.v1_8_R1.EntityInsentient;
import net.minecraft.server.v1_8_R1.EntityWitherSkull;
import net.minecraft.server.v1_8_R1.EntityZombie;
import net.minecraft.server.v1_8_R1.GenericAttributes;
import net.minecraft.server.v1_8_R1.MathHelper;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R1.CraftServer;
import org.bukkit.craftbukkit.v1_8_R1.entity.*;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.netherspawning.NetherSpawner;

public class HardmodeListener implements Listener {

	private static HashSet<Byte> transparentBlocks = new HashSet<Byte>();
	private static Random random = new Random();
	private Set<Zombie> reinforcementZombies = new HashSet<>();

	static
	{
		transparentBlocks.add((byte) 0);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onEntityDamage(EntityDamageEvent event)
	{
		// Do not apply anything to End or void
		if (event.getCause() == DamageCause.VOID || event.getEntity().getWorld().getEnvironment() == Environment.THE_END)
			return;

		if (event instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;

			Entity damager = entityEvent.getDamager();
			Entity victim = entityEvent.getEntity();

			// Teleporting player to the enderman
			if (victim instanceof Enderman && damager instanceof Player && event.getCause() != DamageCause.THORNS)
			{
				// Only perform teleporting if enderman cannot move to the
				// player
				boolean canMove = EndermanTeleport.canMove(damager.getLocation(), victim.getLocation());

				if (!canMove)
				{
					double randomChance = HardmodeSettings.ENDERMAN_TELEPORT_CHANCE.doubleNumber();
					if (MantlePlugin.random.nextDouble() < randomChance)
					{
						event.setCancelled(true);
						EndermanTeleport.teleportPlayer((Player) damager, (Enderman) victim);

						HardmodeModule.applyDamageNode((LivingEntity) damager,
								HardmodeSettings.ENDERMAN_TELEPORT_APPLY_DAMAGE_EFFECT.string());

						return;
					}
				}

			}
			// Wither reset timer
			else if (victim instanceof Wither
					&& (damager instanceof Player || (damager instanceof Projectile && (((Projectile) damager).getShooter() instanceof Player))))
			{
				int newTime = (int) (System.currentTimeMillis() / 1000 + HardmodeSettings.WITHER_TIMEOUT.integer());
				MetadataValue value = new FixedMetadataValue(MantlePlugin.instance, newTime);
				victim.removeMetadata("DespawningTime", MantlePlugin.instance);
				victim.setMetadata("DespawningTime", value);
			}
			// Preventing minions from being damaged by wither
			else if ((damager instanceof Wither || damager instanceof WitherSkull) && victim instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity) victim;
				if (living.getCustomName() != null && living.getCustomName().equalsIgnoreCase("Wither Minion"))
				{
					event.setCancelled(true);
					return;
				}
			} else if (event.getEntityType() == EntityType.VILLAGER)
			{
				int profession = ((CraftVillager) event.getEntity()).getHandle().getProfession();
				if (profession == 5)
				{
					event.setCancelled(true);
					return;
				}
			} else if (damager instanceof Fireball)
			{
				Fireball fireball = (Fireball) damager;
				if (fireball.getShooter() instanceof Ghast)
				{
					double multiplier = HardmodeSettings.GHAST_FIREBALL_DAMAGE_MULTIPLIER.doubleNumber();
					event.setDamage(event.getDamage() * multiplier);
				}
			} else if (victim instanceof Spider && damager instanceof Player)
			{
				for (Entity e : victim.getWorld().getEntitiesByClass(Spider.class))
				{
					if (e.getLocation().distanceSquared(damager.getLocation()) < 48 * 48)
					{
						Spider spider = (Spider) e;
						spider.setTarget((Player) damager);
					}
				}
			}

		}

		if (event.getEntity() instanceof Player)
		{
			final Player player = (Player) event.getEntity();

			// Environmental damage
			DamageNodeParser.parseDamageEvent(event, HardmodeModule.instance.config);
			if (event.getDamage() < 0)
			{
				event.setCancelled(true);
				return;
			}

			// Dismount player from the horse if player shot via arrow
			if (event.getCause() == DamageCause.PROJECTILE && player.isInsideVehicle() && player.getVehicle() instanceof Horse)
			{
				player.leaveVehicle();
			}
		}

		// Dismount player from the horse if horse shot via arrow
		else if (event.getEntity() instanceof Horse)
		{
			Horse horse = (Horse) event.getEntity();

			if (event.getCause() == DamageCause.PROJECTILE && horse.getPassenger() != null)
			{
				horse.eject();
			}
		}

		else if (event.getCause() == DamageCause.WITHER && event.getEntity() instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity) event.getEntity();
			if (living.getCustomName() != null && living.getCustomName().equalsIgnoreCase("Wither Minion"))
			{
				event.setCancelled(true);
				return;
			}
		}

	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event)
	{
		final LivingEntity entity = event.getEntity();

		// Respawned mobs not dropping anything
		// Mobs with name or silverfishes should not drop anything
		if (entity.getMetadata("Respawned").size() > 0 || entity.getType() == EntityType.SILVERFISH
				|| (entity.getCustomName() != null && HardmodeSettings.NAMED_MOBS_NO_DROP.stringList().contains(entity.getCustomName())))
		{
			event.getDrops().clear();
			event.setDroppedExp(0);
			entity.getEquipment().clear();
		}

		if (event.getEntity() instanceof Animals)
		{
			if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)
			{
				Entity lastDamager = ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager();

				try
				{
					Field nmsEntityField = CraftEntity.class.getDeclaredField("entity");
					nmsEntityField.setAccessible(true);
					for (Entity e : event.getEntity().getWorld().getEntitiesByClass(Animals.class))
					{
						if (e.getType() != event.getEntity().getType()
								|| e.getLocation().distanceSquared(event.getEntity().getLocation()) > 25 * 25)
						{
							continue;
						}
						try
						{
							EntityCreature creature = (EntityCreature) nmsEntityField.get(e);
							if (nmsEntityField.get(lastDamager) instanceof net.minecraft.server.v1_8_R1.EntityLiving)
							{
								net.minecraft.server.v1_8_R1.EntityLiving el = (net.minecraft.server.v1_8_R1.EntityLiving) nmsEntityField
										.get(lastDamager);
								creature.b(el);
							}

						} catch (IllegalAccessException e1)
						{
							e1.printStackTrace();
						}
					}
				} catch (NoSuchFieldException e)
				{
					e.printStackTrace();
				}
			}
		}

		// Only drop magma cream when biggest slime is killed
		if (event.getEntityType() == EntityType.MAGMA_CUBE)
		{
			event.getDrops().clear();

			MagmaCube cube = (MagmaCube) event.getEntity();
			if (cube.getSize() == 3 && MantlePlugin.random.nextBoolean())
				event.getDrops().add(new ItemStack(Material.MAGMA_CREAM, 1));
		}

		if(event.getEntityType() == EntityType.WITHER)
		{
			Player killer = event.getEntity().getKiller();

			if(killer != null)
			{
				//MLog.debug(killer.getInventory().getContents().length + "");
				for(ItemStack stack : killer.getInventory().getContents())
				{
					if(stack.getType() == Material.AIR || (stack.getType() == Material.NETHER_STAR && stack.getAmount() < stack.getMaxStackSize()))
					{
						killer.getInventory().addItem(new ItemStack(Material.NETHER_STAR, 1));
						event.getDrops().clear();
						return;
					}
				}
			}
		}
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
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		Entity entity = event.getRightClicked();
		if (entity == null)
			return;

		ItemStack handItem = event.getPlayer().getItemInHand();

		if (entity.getType() == EntityType.COW && entity.getWorld().getEnvironment() == Environment.NETHER && handItem != null
				&& handItem.getType() == Material.BUCKET)
		{
			event.setCancelled(true);
			event.getPlayer().updateInventory();
			Util.Message(HardmodeSettings.MESSAGE_NO_MILKING_NETHER.string(), event.getPlayer());
			return;
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event)
	{
		final Block block = event.getBlock();

		if (onCropDestroyed(block, false))
		{
			event.setCancelled(true);
			return;
		}
		onCropDestroyed(block.getRelative(BlockFace.UP), false);
	}

	// Virtal event that combines multiple events.
	// Triggers when non-solid block (like crops) is about to be destroyed
	public boolean onCropDestroyed(Block block, boolean dark)
	{
		if (block == null)
			return false;

		return false;
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		final Block block = event.getBlock();

		// Prevent wither building
		if (block.getType() == Material.SKULL && block.getY() >= 60 && block.getWorld().getEnvironment() == Environment.NETHER)
		{
			Block centerSkull = null;
			BlockFace oneSkullDirection = null;

			for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH })
			{
				Block neighbour = block.getRelative(face);
				if (neighbour.getType() == Material.SKULL)
				{
					oneSkullDirection = face;

					if (neighbour.getRelative(face).getType() == Material.SKULL)
					{
						centerSkull = neighbour;
						break;
					} else if (block.getRelative(face.getOppositeFace()).getType() == Material.SKULL)
					{
						centerSkull = block;
						break;
					}
				}
			}

			if (centerSkull != null)
			{
				Block belowCenter = centerSkull.getRelative(BlockFace.DOWN);

				if (belowCenter.getType() == Material.SOUL_SAND
						&& belowCenter.getRelative(oneSkullDirection).getType() == Material.SOUL_SAND
						&& belowCenter.getRelative(oneSkullDirection.getOppositeFace()).getType() == Material.SOUL_SAND
						&& belowCenter.getRelative(BlockFace.DOWN).getType() == Material.SOUL_SAND)
				{
					Util.Message(HardmodeSettings.MESSAGE_NO_WITHER_SURFACE.string(), event.getPlayer());
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		if (event.getSpawnReason() == SpawnReason.SPAWNER) //Do not modify any mobs spawned by mob spawners
			return;

		final LivingEntity entity = event.getEntity();

		// Prevent spawning on some nether mobs if there is not enough light
		if (event.getSpawnReason() == SpawnReason.NATURAL && event.getLocation().getWorld().getEnvironment() == Environment.NETHER
				&& event.getEntityType() != EntityType.BLAZE && event.getEntityType() != EntityType.MAGMA_CUBE
				&& event.getEntityType() != EntityType.GHAST)
		{
			if (event.getLocation().getBlock().getLightLevel() > HardmodeSettings.NETHER_MAX_SPAWN_LIGHT_LEVEL.integer())
			{
				event.setCancelled(true);
				return;
			}
		}


		if (entity.getType() == EntityType.ZOMBIE)
		{
			//Fix door breaking
			if (random.nextDouble() < HardmodeSettings.ZOMBIE_DOOR_BREAKING_CHANCE.doubleNumber())
			{
				Zombie zombie = (Zombie) event.getEntity();
				EntityZombie nmsZombie = ((CraftZombie) zombie).getHandle();

				nmsZombie.a(true);
			}
		}


		// Wither timer & replacement
		if (event.getEntityType() == EntityType.WITHER)
		{
			GriefPreventionHandler.enableExplosions(event.getLocation().getWorld());

			if (!NMSWitherManager.isCustomWither(entity))
			{
				Bukkit.getScheduler().runTask(MantlePlugin.instance, new Runnable() {
					@Override
					public void run()
					{
						CustomWither customWither = NMSWitherManager.convert((Wither) entity);
						customWither.n();
					}
				});
			}
			int newTime = (int) (System.currentTimeMillis() / 1000 + HardmodeSettings.WITHER_TIMEOUT.integer());
			MetadataValue value = new FixedMetadataValue(MantlePlugin.instance, newTime);
			entity.setMetadata("DespawningTime", value);

		}
		// Pigmen spawning adjust
		else if (event.getEntityType() == EntityType.PIG_ZOMBIE)
		{
			if (event.getLocation().getWorld().getEnvironment() == Environment.NORMAL)
			{
				event.setCancelled(true);
				return;
			}

			if (event.getSpawnReason() == SpawnReason.JOCKEY)
			{
				event.setCancelled(true);
				return;
			}

			HardmodeModule.applyDamageNode(entity, HardmodeSettings.APPLY_DAMAGE_NODE_ON_PIGMEN_SPAWN.string());

			entity.getEquipment().clear();
			((PigZombie) entity).setAnger(Integer.MAX_VALUE);

			boolean hasSword = MantlePlugin.random.nextDouble() < HardmodeSettings.PIGMEN_SWORD_CHANCE.doubleNumber();
			if (hasSword)
			{
				entity.getEquipment().setItemInHand(new ItemStack(Material.GOLD_SWORD, 1));
			}
		}
		else if (event.getSpawnReason() == SpawnReason.REINFORCEMENTS)
		{
			if (entity instanceof Zombie)
			{
				reinforcementZombies.add((Zombie) entity);
			}
		}
		else if (event.getSpawnReason() == SpawnReason.MOUNT)
		{
			event.setCancelled(true);
			return;
		}

		// assign spiders a random potion effect
		if (event.getSpawnReason() == SpawnReason.NATURAL && event.getEntityType() == EntityType.SPIDER)
		{
			// clear vanilla effects so there aren't multiple on a spider in
			// rare cases.
			for (PotionEffectType type : PotionEffectType.values())
			{
				if (type != null && event.getEntity().hasPotionEffect(type))
				{
					event.getEntity().removePotionEffect(type);
				}
			}
			int sel = random.nextInt(3);
			PotionEffectType type = null;
			switch (sel)
			{
			case 0:
				type = PotionEffectType.SPEED;
				break;
			case 1:
				type = PotionEffectType.INVISIBILITY;
				break;
			case 2:
				type = PotionEffectType.REGENERATION;
				break;
			case 3:
				type = PotionEffectType.INCREASE_DAMAGE;
				break;
			}

			event.getEntity().addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, 1));
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onEntityPortal(EntityPortalEvent event)
	{
		if (event.getEntityType() == EntityType.HORSE)
		{
			String id = event.getEntity().getUniqueId().toString();

			AttributeInstance attributes = ((EntityInsentient) ((CraftLivingEntity) event.getEntity()).getHandle())
					.getAttributeInstance(GenericAttributes.d);

			if (event.getTo().getWorld().getEnvironment() == Environment.NETHER)
			{
				// Horse slowing down disabled
				// double originalSpeed = attributes.getValue();
				// HorseSpeed.setOriginalHorseSpeed(id, originalSpeed);

				// attributes.setValue(HardmodeSettings.NETHER_HORSE_SPEED.doubleNumber());
			} else
			{
				double value = HorseSpeed.getOriginalHorseSpeed(id);
				if (value >= 0)
				{
					attributes.setValue(value);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent event)
	{
		Entity entity = event.getEntity();
		Entity target = event.getTarget();

		if (event.getEntityType() == EntityType.GHAST)
		{
			TargetReason reason = event.getReason();
			if (reason == TargetReason.CLOSEST_PLAYER || reason == TargetReason.RANDOM_TARGET)
			{
				int distanceSquared = Util.flatDistanceSquared(entity.getLocation(), target.getLocation());
				if (distanceSquared > HardmodeSettings.GHAST_MAXIMUM_ATTACK_RANGE.integer())
				{
					event.setCancelled(true);
					return;
				}
			}
		} else if (event.getEntity() instanceof Zombie && reinforcementZombies.contains(event.getEntity())
				&& event.getTarget() instanceof Player)
		{
			Zombie zombie = (Zombie) event.getEntity();
			Vector newDistance = event.getTarget().getLocation().subtract(zombie.getLocation()).toVector().normalize()
					.multiply(HardmodeSettings.REINFORCEMENTS_DISTANCE.doubleNumber());

			Location newLocation = event.getTarget().getLocation().add(newDistance);
			newLocation.setY(newLocation.getWorld().getHighestBlockYAt(newLocation) + 1d);

			zombie.teleport(newLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
			reinforcementZombies.remove(zombie);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onZombieBreakDoor(EntityBreakDoorEvent event)
	{
		Block doorBlock = event.getBlock();

		Block bottomBlock = event.getBlock().getRelative(BlockFace.DOWN);
		if (bottomBlock.getType() == Material.WOODEN_DOOR)
			doorBlock = bottomBlock;

		doorBlock.setData((byte) ((byte) doorBlock.getData() | (byte) 0x4), true);
		doorBlock.getWorld().playSound(doorBlock.getLocation(), Sound.DOOR_OPEN, 1f, 1f);

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamageEntity(EntityDamageEvent event)
	{

		if (event.getEntity().getType() == EntityType.SKELETON && event.getEntity().getWorld().getEnvironment() == Environment.NETHER)
		{
			if (event.getCause() == DamageCause.THORNS)
			{
				event.setCancelled(true);
			}
		}
	}


	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onEntityPrime(ExplosionPrimeEvent event)
	{
		//spawn minions only if customSkull & spawningMinions == true
		if (event.getEntityType() == EntityType.WITHER_SKULL)
		{
			CraftWitherSkull cws = (CraftWitherSkull) event.getEntity();
			if(!(cws.getHandle() instanceof CustomWitherSkull))
				return;
			CustomWitherSkull customSkull = (CustomWitherSkull) cws.getHandle();

			if(customSkull.shouldSpawnMinions)
			{

				final Location location = event.getEntity().getLocation();

				Bukkit.getScheduler().scheduleSyncDelayedTask(MantlePlugin.instance, new Runnable() {

					@Override
					public void run()
					{
						int amount = MantlePlugin.random.nextInt(2);

						//TODO Remove
						//Testing is a pain with all them minions around
						amount = 2;

						for (int i = 0; i < amount; i++)
						{
							Block spawningBlock = location.getBlock();
							if (spawningBlock.getType().isOccluding())
								continue;
							Block aboveBlock = spawningBlock.getRelative(BlockFace.UP);
							if (aboveBlock.getType().isOccluding())
								continue;

							Skeleton minion = NetherSpawner.spawnWitherSkeleton(spawningBlock, SpawnReason.CUSTOM);
							if (minion == null)
								continue;

							minion.setHealth(HardmodeSettings.WITHER_MINION_HEALTH.doubleNumber());

							minion.setCustomName("Wither Minion");
							minion.setCustomNameVisible(false);

						}
					}
				}, 5); // Spawn minions after 5 ticks to ensure they won't be hit by
				// explosion
			}
		} else if (event.getEntityType() == EntityType.WITHER)
		{
			event.setFire(true);
			event.setRadius(HardmodeSettings.WITHER_EXPLOSION_RADIUS.integer());

			// Clear up some obsidian.
			Location wither = event.getEntity().getLocation();
			Block witherBlock = wither.getBlock();
			for (int x = -8; x <= 8; x++)
			{
				for (int y = -8; y <= 8; y++)
				{
					for (int z = -8; z <= 8; z++)
					{

						Block nBlock = witherBlock.getRelative(x, y, z);
						if (nBlock == null
								|| !(nBlock.getType() == Material.OBSIDIAN || nBlock.getType() == Material.ENDER_CHEST
										|| nBlock.getType() == Material.ANVIL || nBlock.getType() == Material.ENCHANTMENT_TABLE))
							continue;

						int range = (int) nBlock.getLocation().distanceSquared(wither);
						if (range > 32 * 32)
							continue;

						if (MantlePlugin.random.nextInt(32 * 32 * 100 / 20) > range)
						{
							nBlock.breakNaturally(new ItemStack(Material.AIR));
						}
					}
				}
			}
		} else if (event.getEntityType() == EntityType.FIREBALL)
		{
			Fireball fireball = (Fireball) event.getEntity();
			if (fireball.getShooter() instanceof Ghast)
			{
				double multiplier = HardmodeSettings.GHAST_FIREBALL_BLAST_RADIUS_MULTIPLIER.doubleNumber();
				event.setRadius((float) (event.getRadius() * multiplier));
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event)
	{
		if (event.getEntity() == null)
		{
			return;
		}

		if (event.getEntityType() == EntityType.WITHER)
		{
			event.setYield(0);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkUnload(ChunkUnloadEvent event)
	{
		// Kill wither when chunk unloads to prevent exploits
		for (Entity e : event.getChunk().getEntities())
		{
			if (e.getType() == EntityType.WITHER)
			{
				e.remove();
				return;
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		String worldName = player.getWorld().getName();

		if (HardmodeSettings.NO_DEATH_DROPS_EXPERIENCE.stringList().contains(worldName))
		{
			event.setDroppedExp(0);
		}

		if (HardmodeSettings.NO_DEATH_DROPS_ITEMS.stringList().contains(worldName))
		{
			event.getDrops().clear();
		}

	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event)
	{
		Entity damager = event.getDamager();
		Entity damaged = event.getEntity();

		if(damaged instanceof Player)
		{
			if(damager instanceof  Wither)
			{
				if(((CraftWither) damager).getHandle() instanceof  CustomWither)
				{
					CustomWither customWither = (CustomWither) (((CraftWither) damager).getHandle());
					if (customWither.shouldKnockback())
					{
						Location loc = damaged.getLocation();
						double diffX = loc.getX() - customWither.locX;
						double diffZ = loc.getZ() - customWither.locZ;

						Vector vec = new Vector(diffX,0, diffZ);
						vec = vec.normalize();
						vec = vec.setY(MathHelper.sqrt(vec.getX() * vec.getX() + vec.getZ() * vec.getZ()));
						vec.normalize();
						vec = vec.multiply(customWither.KNOCKBACK_POWER);
						damaged.setVelocity(vec);
					}
				}
			}
			else
			//off for now, coz weird shit is happenign
			if(damager instanceof WitherSkull && false)
			{
				if(((CraftWitherSkull)damager).getHandle() instanceof CustomWitherSkull)
				{
					CustomWitherSkull cws = (CustomWitherSkull) (((CraftWitherSkull) damager).getHandle());
					if(cws.shooter != null)
					{
						if(cws.shooter instanceof CustomWither)
						{
							CustomWither customWither = (CustomWither) cws.shooter;

							//TODO oh god copypasta, remove that
							if (customWither.shouldKnockback())
							{
								Location loc = damaged.getLocation();
								double diffX = loc.getX() - customWither.locX;
								double diffZ = loc.getZ() - customWither.locZ;

								Vector vec = new Vector(diffX, MathHelper.sqrt(diffX * diffX + diffZ * diffZ), diffZ);
								vec = vec.normalize();
								vec = vec.multiply(customWither.KNOCKBACK_POWER);
								damaged.setVelocity(vec);
							}
						}

					}
				}
			}

		}

	}
}

