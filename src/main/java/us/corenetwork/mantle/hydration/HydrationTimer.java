package us.corenetwork.mantle.hydration;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.hydration.CachedDrainConfig.WorldLayer;

public class HydrationTimer implements Runnable {
	
	@Override
	public void run() {	
		boolean updated = false;

		try
		{
			PreparedStatement delStatement = IO.getConnection().prepareStatement("DELETE FROM hydration WHERE Player = ?");
			PreparedStatement insertStatement = IO.getConnection().prepareStatement("INSERT INTO hydration (Player, Hydration, Saturation, FatigueLevel, FatigueLevelStart, DeliveredMessages) VALUES (?,?,?,?,?,?)");

			for (Player player : Bukkit.getServer().getOnlinePlayers())
			{
				PlayerData playerData = PlayerData.getPlayer(player.getName());
				boolean playerUpdated = playerData.waitingToSave;
				
				int oldHydration = (int) Math.round(playerData.hydrationLevel);
				
				WorldLayer layer = CachedDrainConfig.getWoldLayer(player.getWorld().getName(), player.getLocation().getBlockY());
				if (layer != null && layer.drain != 0 && (layer.drain < 0 || player.getGameMode() != GameMode.CREATIVE))
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
					
					playerUpdated = true;
				} 
				
				playerUpdated |= HydrationUtil.upateMineFatigue(player, playerData, layer);
				
				if (playerUpdated || playerData.waitingToSave)
				{
					playerData.save(delStatement, insertStatement);
					delStatement.addBatch();
					insertStatement.addBatch();
					
					updated = true;
				}
			}
			
			if (updated)
			{
				delStatement.executeBatch();
				insertStatement.executeBatch();
				
				IO.getConnection().commit();
			}
			
			delStatement.close();
			insertStatement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
				
	}
}

