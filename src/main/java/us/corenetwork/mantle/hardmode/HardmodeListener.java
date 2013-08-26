package us.corenetwork.mantle.hardmode;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;


public class HardmodeListener implements Listener {

	private static HashSet<Byte> transparentBlocks = new HashSet<Byte>();
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
		}

		//Environmental damage
		if (event.getEntity() instanceof Player)
		{
			DamageNodeParser.parseDamageEvent(event, HardmodeModule.instance.config);
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

		return false;
	}

}
