package us.corenetwork.mantle.hydration;


import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.MantlePlugin;


public class HydrationListener implements Listener {
	
	public static HashMap<UUID, Long> lavaPlayer = new HashMap<UUID, Long>();
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event)
	{		
		final Player player = event.getPlayer();

		final PlayerData playerData = PlayerData.getPlayer(player.getUniqueId());

		HydrationUtil.updateScoreboard(player.getName(), playerData);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerConsume(final PlayerItemConsumeEvent event)
	{
		ItemStack consumed = event.getItem();
		CachedPotionConfig potion = CachedPotionConfig.getPotionConfig(consumed);
		if (potion == null)
			return;

		final PlayerData data = PlayerData.getPlayer(event.getPlayer().getUniqueId());
		if (data.hydrationLevel < 100)
		{
			data.hydrationLevel += potion.normal;
			if (data.hydrationLevel > 100)
				data.hydrationLevel = 100;

			HydrationUtil.updateScoreboard(event.getPlayer().getName(), data);

			data.saturationLevel += potion.saturation;
			if (data.saturationLevel > 100)
				data.saturationLevel = 100;
			
			data.save();
		}

		Bukkit.getScheduler().runTask(MantlePlugin.instance, new Runnable() {
			@Override
			public void run() {
				HydrationUtil.updateNegativeEffects(event.getPlayer(), data, null);
			}
		});
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (event.getCause() == DamageCause.LAVA && event.getEntityType() == EntityType.PLAYER)
		{
			lavaPlayer.put(event.getEntity().getUniqueId(), System.currentTimeMillis());
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onWorldSave(WorldSaveEvent event)
	{
		if (event.getWorld().getName().equals("world")) //All worlds save at the same time and we only need to save once
			PlayerData.saveAll();
	}
}
