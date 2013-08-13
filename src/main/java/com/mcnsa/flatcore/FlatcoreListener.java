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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.PortalCreateEvent.CreateReason;
import org.bukkit.inventory.ItemStack;

import com.mcnsa.flatcore.portals.PortalUtil;
import com.mcnsa.flatcore.rspawncommands.NoDropCommand;
import com.mcnsa.flatcore.rspawncommands.ProtectCommand;

public class FlatcoreListener implements Listener {

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
