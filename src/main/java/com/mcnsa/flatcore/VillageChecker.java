package com.mcnsa.flatcore;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.generation.VillagerSpawner;

public class VillageChecker implements Runnable {

	public static void schedule()
	{
		//Disabled for now
		int period = 0;//Settings.getInt(Setting.RESTORATION_VILLAGE_CHECK_PERIOD) * 20;
		if (period > 0)
			Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(MCNSAFlatcore.instance, new VillageChecker(), 20, period);
	}

	@Override
	public void run() {
		World firstWorld = Bukkit.getWorlds().get(0);
		boolean generated = false;
		int tries = 0;
		while (!generated)
		{
			tries++;
			
			final int now = (int) (System.currentTimeMillis() / 1000);

			try
			{
				PreparedStatement statement = IO.getConnection().prepareStatement("SELECT * FROM Villages ORDER BY LastCheck ASC LIMIT 1");
				ResultSet set = statement.executeQuery();

				if (set.next())
				{
					final int id = set.getInt("id");

					final int type = set.getInt("type");
					final int villageX = set.getInt("CornerX");
					final int villageZ = set.getInt("CornerZ");
					final int xSize = set.getInt("SizeX");
					final int zSize = set.getInt("SizeZ");
					int lastRestore = set.getInt("lastRestore");

					FCLog.info("Checking village around " + villageX + " " + villageZ);

					if (isPlayerInside(villageX, villageZ, xSize, zSize))
					{
						FCLog.info("Will not restore - player inside.");
					}
					else if (!GriefPreventionHandler.containsClaim(firstWorld, villageX, villageZ, xSize, zSize, false))
					{
						lastRestore = now;

						String villageIdString = type >= 9 ? Integer.toString(type + 1) : ("0" + (type + 1));
						String fileName = "village-" + villageIdString + ".schematic";

						final CachedSchematic village = new CachedSchematic(fileName);
						final int y = Settings.getInt(Setting.VILLAGE_PASTING_Y);

						village.findVillagers();

						generated = true;

						Bukkit.getServer().getScheduler().runTask(MCNSAFlatcore.instance, new Runnable() {

							@Override
							public void run() {
								//Comment until village system redone
								Location center = null; //village.placeAtCorner(villageX, y, villageZ);
								village.clearVillagers(center);
								
								VillagerSpawner villagerSpawner = new VillagerSpawner();
								village.spawnVillagers(center, villagerSpawner);
								villagerSpawner.close();
								
								FCLog.info("Village restored.");

							}
						});	
						
						
					}		
					else
					{
						FCLog.info("Will not restore - claim exist there.");
					}

					statement.close();
					statement = IO.getConnection().prepareStatement("UPDATE Villages SET LastCheck = ?, LastRestore = ? WHERE ID = ?");
					statement.setInt(1, now);
					statement.setInt(2, lastRestore);
					statement.setInt(3, id);
					statement.executeUpdate();
					IO.getConnection().commit();
					statement.close();

				}
				
				if (tries > 30)
				{
					FCLog.warning("Failed to find village to restore after 30 tries! Are all villages claimed?");
					generated = true;
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}	
		}
	}

	private static boolean isPlayerInside(int centerX, int centerZ, int xSize, int zSize)
	{
		int padding = Settings.getInt(Setting.RESORATION_VILLAGE_CHECK_PADDING);
		
		int minX = centerX - padding;
		int maxX = centerX + xSize + padding;
		int minZ = centerZ - padding;
		int maxZ = centerZ + zSize + padding;
		
		for (Player player : Bukkit.getServer().getOnlinePlayers())
		{
			Location loc = player.getLocation();
			if (player.getGameMode() != GameMode.CREATIVE && ((loc.getBlockX() >= minX && loc.getBlockX() <= maxX) && (loc.getBlockZ() >= minZ && loc.getBlockZ() < maxZ)))
			{
				return true;
			}
		}

		return false;
	}

}
