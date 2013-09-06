package us.corenetwork.mantle.hydration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.hydration.CachedDrainConfig.WorldLayer;

public class HydrationTimer implements Runnable {
	private int lastSave = 0;
	public static boolean updated = false;
	
	@Override
	public void run() {	
		lastSave++;
		
		for (Player player : Bukkit.getServer().getOnlinePlayers())
		{
			PlayerData playerData = PlayerData.getPlayer(player.getName());
			
			int oldHydration = (int) Math.round(playerData.hydrationLevel);
			
			WorldLayer layer = CachedDrainConfig.getWoldLayer(player.getWorld().getName(), player.getLocation().getBlockY());
			if (layer != null && layer.drain != 0)
			{
				double drain = layer.drain;
				if (drain > 0 && player.getFireTicks() > 0)
					drain *= HydrationSettings.FIRE_DEHYDRATION_MULTIPLIER.doubleNumber();

				if (drain < 0)
				{
					playerData.hydrationLevel -= drain;
					if (playerData.hydrationLevel > 100)
					{
						playerData.hydrationLevel = 100;
					}
				}
				else
				{
					playerData.saturationLevel -= drain;
					if (playerData.saturationLevel < 0)
					{
						drain = -playerData.saturationLevel;
						playerData.saturationLevel = 0;

						playerData.hydrationLevel -= drain;
						if (playerData.hydrationLevel < 0)
							playerData.hydrationLevel = 0;
					}
				}
				
				int newHydration = (int) Math.round(playerData.hydrationLevel);

				if (oldHydration != newHydration)
				{
					HydrationUtil.updateScoreboard(player.getName(), newHydration);
					HydrationUtil.notify(playerData, player);
				}
				
				updated = true;
				playerData.save();
			} 
			
			updated |= HydrationUtil.upateMineFatigue(player, playerData, layer);
		}
		
		
		if (lastSave > 20 && updated)
		{
			HydrationModule.instance.saveConfig();
			lastSave = 0;
			updated = false;
		}			
	}
}
