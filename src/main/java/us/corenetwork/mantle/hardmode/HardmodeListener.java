package us.corenetwork.mantle.hardmode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import net.minecraft.server.v1_6_R3.AttributeInstance;
import net.minecraft.server.v1_6_R3.EntityInsentient;
import net.minecraft.server.v1_6_R3.GenericAttributes;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.GrassSpecies;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftVillager;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
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
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;

import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;


public class HardmodeListener implements Listener {

	private static HashSet<Byte> transparentBlocks = new HashSet<Byte>();
	protected static HashMap<String, Long> lastWitherHits = new HashMap<String, Long>();

	static
	{
		transparentBlocks.add((byte) 0);
	}



	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onEntityDamage(EntityDamageEvent event)
	{
		//Do not apply anything to End or void
		if (event.getCause() == DamageCause.VOID || event.getEntity().getWorld().getEnvironment() == Environment.THE_END)
			return;
		
		if (event instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;

			Entity damager = entityEvent.getDamager();
			Entity victim = entityEvent.getEntity();

			//Teleporting player away from enderman
			if (victim instanceof Enderman && damager instanceof Player)
			{
				//Only perform teleporting if enderman cannot move to the player
				boolean canMove = EndermanTeleport.canMove(damager.getLocation(), victim.getLocation());

				if (!canMove)
				{
					double randomChance = HardmodeSettings.ENDERMAN_TELEPORT_CHANCE.doubleNumber();
					if (MantlePlugin.random.nextDouble() < randomChance)
					{
						event.setCancelled(true);
						EndermanTeleport.teleportPlayer((Player) damager, (Enderman) victim);

						HardmodeModule.applyDamageNode((LivingEntity) damager, HardmodeSettings.ENDERMAN_TELEPORT_APPLY_DAMAGE_EFFECT.string()); 

						return;
					}
				}

			}
			//Wither reset timer
			else if (victim instanceof Wither && (damager instanceof Player || 
					(damager instanceof Projectile && (((Projectile) damager).getShooter() instanceof Player))))
			{
				int newTime = (int) (System.currentTimeMillis() / 1000 + HardmodeSettings.WITHER_TIMEOUT.integer());
				MetadataValue value = new FixedMetadataValue(MantlePlugin.instance, newTime);
				victim.removeMetadata("DespawningTime", MantlePlugin.instance);
				victim.setMetadata("DespawningTime", value);
			}
			else if (victim instanceof Player && damager instanceof Skeleton)
			{
				Skeleton skeleton = (Skeleton) damager;
				if (skeleton.getSkeletonType() == SkeletonType.WITHER)
				{
					ItemStack weapon = skeleton.getEquipment().getItemInHand();

					//Remember last wither skeleton attack 
					if (weapon != null && weapon.getType() == Material.IRON_SWORD)
					{
						lastWitherHits.put(((Player) victim).getName(), System.currentTimeMillis());
					}
				}
			}
			else if (victim instanceof Player && (damager instanceof WitherSkull || damager instanceof Wither))
			{
				lastWitherHits.put(((Player) victim).getName(), System.currentTimeMillis());
			}
			//Preventing minions from being damaged by wither
			else if ((damager instanceof Wither || damager instanceof WitherSkull) && victim instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity) victim;
				if (living.getCustomName() != null && living.getCustomName().equalsIgnoreCase("Wither Minion"))
				{
					event.setCancelled(true);
					return;
				}
			}
			else if (event.getEntityType() == EntityType.VILLAGER)
			{
				int profession = ((CraftVillager) event.getEntity()).getHandle().getProfession();
				if (profession == 5)
				{
					event.setCancelled(true);
					return;
				}
			}


		}

		if (event.getEntity() instanceof Player)
		{
			final Player player = (Player) event.getEntity();

			//Environmental damage
			DamageNodeParser.parseDamageEvent(event, HardmodeModule.instance.config);
			if (event.getDamage() < 0)
			{
				event.setCancelled(true);
				return;
			}

			//Dismount player from the horse if player shot via arrow
			if (event.getCause() == DamageCause.PROJECTILE && player.isInsideVehicle() && player.getVehicle() instanceof Horse)
			{
				player.leaveVehicle();
			}

			//Remove wither if inflicted by invalid skeleton
			if (event.getCause() == DamageCause.WITHER)
			{
				Long lastWitherHit = lastWitherHits.get(player.getName());
				if (lastWitherHit == null || lastWitherHit < System.currentTimeMillis() - 20000)
				{
					Bukkit.getScheduler().runTask(MantlePlugin.instance, new Runnable() {
						
						@Override
						public void run() {
							player.removePotionEffect(PotionEffectType.WITHER);
						}
					});
					
					event.setCancelled(true);
					return;
				}
			}

		}

		//Dismount player from the horse if horse shot via arrow
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

		//Respawned mobs not dropping anything
		//Mobs with name or silverfishes should not drop anything
		if (entity.getMetadata("Respawned").size() > 0 || entity.getType() == EntityType.SILVERFISH ||
				(entity.getCustomName() != null && HardmodeSettings.NAMED_MOBS_NO_DROP.stringList().contains(entity.getCustomName())))
		{
			event.getDrops().clear();
			event.setDroppedExp(0);
			entity.getEquipment().clear();
		}

		//Respawn zombie
		if (entity.getType() == EntityType.ZOMBIE && entity.getFireTicks() == -1)
		{
			int chance = HardmodeSettings.ZOMBIE_RESPAWN_CHANCE.integer();
			if (MantlePlugin.random.nextInt(100) < chance)
			{
				//Play effect
				Builder builder = FireworkEffect.builder();

				Util.showFirework(entity.getLocation(), builder.trail(false).withColor(Color.BLACK).build());	
				entity.getWorld().playSound(entity.getLocation(), Sound.ZOMBIE_UNFECT, 1, 1);

				//Spawn new zombie

				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MantlePlugin.instance, new Runnable() {

					@Override
					public void run() {
						Zombie newZombie = entity.getWorld().spawn(entity.getLocation(), Zombie.class);


						newZombie.setHealth(newZombie.getMaxHealth() / 2);
						newZombie.setMetadata("Respawned", new FixedMetadataValue(MantlePlugin.instance, true));
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
		}		

		//Wither skeleton only dropping stuff if he has iron sword
		if (event.getEntityType() == EntityType.SKELETON && ((Skeleton) entity).getSkeletonType() == SkeletonType.WITHER)
		{
			Iterator<ItemStack> iterator = event.getDrops().iterator();
			while (iterator.hasNext())
			{
				ItemStack stack = iterator.next();
				if (stack.getType() == Material.SKULL_ITEM)
					iterator.remove();
			}

			ItemStack itemInHand = entity.getEquipment().getItemInHand();
			//Check if skeleton is rare one
			if (itemInHand != null && itemInHand.getType() == Material.IRON_SWORD)
			{
				event.getDrops().add(new ItemStack(Material.SKULL_ITEM, 1, (short) 1));
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
		if (entity != null && entity.getType() == EntityType.COW && entity.getWorld().getEnvironment() == Environment.NETHER)
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

		//Drop sticks when destroying dead shrub
		if (block.getType() == Material.LONG_GRASS && block.getData() == GrassSpecies.DEAD.getData())
		{
			int sticksDropped = MantlePlugin.random.nextInt(3);
			if (sticksDropped > 0)
				block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.STICK, sticksDropped));
			block.setType(Material.AIR);

			return true;
		}

		return false;
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		final Block block = event.getBlock();

		//Prevent wither building
		if (block.getType() == Material.SKULL && block.getY() >= 60 && block.getWorld().getEnvironment() == Environment.NETHER)
		{
			Block centerSkull = null;
			BlockFace oneSkullDirection = null;

			for (BlockFace face : new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH })
			{
				Block neighbour = block.getRelative(face);
				if (neighbour.getType() == Material.SKULL)
				{
					oneSkullDirection = face;

					if (neighbour.getRelative(face).getType() == Material.SKULL)
					{
						centerSkull = neighbour;
						break;
					}
					else if (block.getRelative(face.getOppositeFace()).getType() == Material.SKULL)
					{
						centerSkull = block;
						break;
					}
				}
			}

			if (centerSkull != null)
			{
				Block belowCenter = centerSkull.getRelative(BlockFace.DOWN);

				if (belowCenter.getType() == Material.SOUL_SAND && 
						belowCenter.getRelative(oneSkullDirection).getType() == Material.SOUL_SAND &&
						belowCenter.getRelative(oneSkullDirection.getOppositeFace()).getType() == Material.SOUL_SAND &&
						belowCenter.getRelative(BlockFace.DOWN).getType() == Material.SOUL_SAND)
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
		LivingEntity entity = event.getEntity();

		if (event.getSpawnReason() == SpawnReason.NATURAL && event.getLocation().getWorld().getEnvironment() == Environment.NETHER)
		{
			if (event.getLocation().getY() >= HardmodeSettings.NETHER_IGNORE_LIGHT_UNDER_Y.integer() && event.getLocation().getBlock().getLightLevel() > HardmodeSettings.NETHER_MAX_SPAWN_LIGHT_LEVEL.integer())
			{
				event.setCancelled(true);
				return;
			}
		}

		//Wither timer
		if (event.getEntityType() == EntityType.WITHER)
		{
			int newTime = (int) (System.currentTimeMillis() / 1000 + HardmodeSettings.WITHER_TIMEOUT.integer());
			MetadataValue value = new FixedMetadataValue(MantlePlugin.instance, newTime);
			entity.setMetadata("DespawningTime", value);
		}
		//Reduced ghast spawning
		else if (event.getEntityType() == EntityType.GHAST)
		{
			if (MantlePlugin.random.nextDouble() > HardmodeSettings.GHAST_SPAWNING_CHANCE.doubleNumber() || event.getLocation().getY() < HardmodeSettings.GHAST_MINIMUM_SPAWNING_Y.integer())
			{
				event.setCancelled(true);
				return;
			}
		}
		//Pigmen spawning adjust
		else if (event.getEntityType() == EntityType.PIG_ZOMBIE)
		{
			if (event.getLocation().getWorld().getEnvironment() != Environment.NETHER)
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
		//Add slowness to nether villagers
		else if (entity.getType() == EntityType.VILLAGER)
		{
			if (event.getLocation().getWorld().getEnvironment() == Environment.NETHER)
			{
				HardmodeModule.applyDamageNode(event.getEntity(), HardmodeSettings.NETHER_VILLAGER_APPLY_DAMAGE_NODE_ON_SPAWN.string());
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onEntityPortal(EntityPortalEvent event)
	{
		if (event.getEntityType() == EntityType.HORSE)
		{
			String id = event.getEntity().getUniqueId().toString();

			AttributeInstance attributes = ((EntityInsentient)((CraftLivingEntity) event.getEntity()).getHandle()).getAttributeInstance(GenericAttributes.d);

			if (event.getTo().getWorld().getEnvironment() == Environment.NETHER)
			{
				double originalSpeed = attributes.getValue();
				HorseSpeed.setOriginalHorseSpeed(id, originalSpeed);

				attributes.setValue(HardmodeSettings.NETHER_HORSE_SPEED.doubleNumber());
			}
			else
			{
				double value = HorseSpeed.getOriginalHorseSpeed(id);
				if (value < 0)
				{
					MLog.severe("Horse speed not saved!");
					value = 1.0;
				}
				attributes.setValue(value);
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

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onEntityPrime(ExplosionPrimeEvent event)
	{
		if (event.getEntityType() == EntityType.WITHER_SKULL)
		{
			final Location location = event.getEntity().getLocation();

			Bukkit.getScheduler().scheduleSyncDelayedTask(MantlePlugin.instance, new Runnable() {

				@Override
				public void run() {
					int amount = MantlePlugin.random.nextInt(2);
					for (int i = 0; i < amount; i++)
					{
						MagmaCube minion = location.getWorld().spawn(location, MagmaCube.class);
						minion.setSize(MantlePlugin.random.nextInt(3));
						//minion.setSkeletonType(SkeletonType.WITHER);

						minion.setCustomName("Wither Minion");
						minion.setCustomNameVisible(false);
					}
				}
			}, 5); //Spawn minions after 5 ticks to ensure they won't be hit by explosion
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkUnload(ChunkUnloadEvent event)
	{		
		//Kill wither when chunk unloads to prevent exploits
		for (Entity e : event.getChunk().getEntities())
		{
			if (e.getType() == EntityType.WITHER)
			{
				e.remove();
				return;
			}
		}
	}
}
