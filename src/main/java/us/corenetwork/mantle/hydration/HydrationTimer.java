package us.corenetwork.mantle.hydration;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.hydration.CachedDrainConfig.WorldLayer;

public class HydrationTimer implements Runnable {

	@Override
	public void run() {
		for (Player player : Bukkit.getServer().getOnlinePlayers())
		{
			PlayerData playerData = PlayerData.getPlayer(player.getUniqueId());

			int oldHydration = (int) Math.round(playerData.hydrationLevel);

			WorldLayer layer = CachedDrainConfig.getWoldLayer(player.getWorld().getName(), player.getLocation().getBlockY());
			if (layer != null)
			{
				double drain = layer.getDrain(player);

				if (drain != 0 && (drain < 0 || player.getGameMode() != GameMode.CREATIVE))
				{

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
				}


				int newHydration = (int) Math.round(playerData.hydrationLevel);

				if (oldHydration != newHydration)
				{
					HydrationUtil.updateScoreboard(player.getName(), newHydration);
					HydrationUtil.notify(playerData, player);
				}
			}

			HydrationUtil.updateNegativeEffects(player, playerData, layer);

		}



	}
}

