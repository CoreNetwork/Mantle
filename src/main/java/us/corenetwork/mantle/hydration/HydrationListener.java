package us.corenetwork.mantle.hydration;


import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import us.corenetwork.mantle.MantlePlugin;


public class HydrationListener implements Listener {
	
	public static HashMap<String, Long> lavaPlayer = new HashMap<String, Long>();
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event)
	{		
		final Player player = event.getPlayer();

		final PlayerData playerData = PlayerData.getPlayer(player.getName());

		HydrationUtil.updateScoreboard(player.getName(), (int) Math.round(playerData.hydrationLevel));
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerConsume(final PlayerItemConsumeEvent event)
	{
		ItemStack consumed = event.getItem();
		CachedPotionConfig potion = CachedPotionConfig.getPotionConfig(consumed);
		if (potion == null)
			return;

		final PlayerData data = PlayerData.getPlayer(event.getPlayer().getName());
		if (data.hydrationLevel < 100)
		{
			data.hydrationLevel += potion.normal;
			if (data.hydrationLevel > 100)
				data.hydrationLevel = 100;

			HydrationUtil.updateScoreboard(event.getPlayer().getName(), (int) Math.round(data.hydrationLevel));

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
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (event.getCause() == DamageCause.LAVA && event.getEntityType() == EntityType.PLAYER)
		{
			Player player = (Player) event.getEntity();
			lavaPlayer.put(player.getName(), System.currentTimeMillis());
		}
	}
}
