package com.mcnsa.flatcore;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class VillageChecker implements Runnable {

	public static void schedule()
	{
		int period = Settings.getInt(Setting.RESTORATION_VILLAGE_CHECK_PERIOD) * 20;
		Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(MCNSAFlatcore.instance, new VillageChecker(), 20, period);
	}

	@Override
	public void run() {
		boolean generated = false;
		while (!generated)
		{
			boolean fasterNextTry = false;
			final int now = (int) (System.currentTimeMillis() / 1000);

			try
			{
				PreparedStatement statement = IO.getConnection().prepareStatement("SELECT * FROM Villages WHERE LastCheck < ? LIMIT 1");
				statement.setInt(1, now - 604800);
				ResultSet set = statement.executeQuery();

				if (set.next())
				{
					final int id = set.getInt("id");

					final int type = set.getInt("type");
					final int villageX = set.getInt("centerX");
					final int villageZ = set.getInt("centerZ");
					final int xSize = set.getInt("SizeX") / 2;
					final int zSize = set.getInt("SizeZ") / 2;
					int lastRestore = set.getInt("lastRestore");

					FCLog.info("Checking village around " + villageX + " " + villageZ);

					if (isPlayerInside(villageX, villageZ, xSize, zSize))
					{
						FCLog.info("Will not restore - player inside.");
						fasterNextTry = true;
					}
					else if (!GriefPreventionHandler.containsClaim(villageX, villageZ, xSize, zSize))
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
								village.place(villageX, y, villageZ, 0);
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
					statement.setInt(1, fasterNextTry ? (601200 + now) : now);
					statement.setInt(2, lastRestore);
					statement.setInt(3, id);
					statement.executeUpdate();
					IO.getConnection().commit();
					statement.close();

				}
				else
					generated = true;
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
		
		int minX = centerX - (xSize + padding);
		int maxX = centerX + (xSize + padding);
		int minZ = centerZ - (zSize + padding);
		int maxZ = centerZ + (zSize + padding);
		
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
