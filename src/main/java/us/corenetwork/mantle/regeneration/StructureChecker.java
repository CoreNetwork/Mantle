package us.corenetwork.mantle.regeneration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.CachedSchematic;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Setting;
import us.corenetwork.mantle.Settings;
import us.corenetwork.mantle.generation.VillagerSpawner;


public class StructureChecker implements Runnable {
	RegStructure structure;
	
	private StructureChecker(RegStructure structure)
	{
		this.structure = structure;
	}
	
	public static void schedule()
	{
		for (RegStructure structure : RegenerationModule.instance.structures.values())
		{
			int period = structure.getGenerationInterval() * 20;
			if (period <= 0)
				continue;
			
			int offset = structure.getTimeOffset() * 20;
			
			Bukkit.getServer().getScheduler().runTaskTimer(MantlePlugin.instance, new StructureChecker(structure), offset + 20, period);
		}
	}

	@Override
	public void run() {
		int generated = 0;
		int tries = 0;
		while (generated < structure.getNumberToGenerateAtOnce())
		{
			tries++;
			
			final int now = (int) (System.currentTimeMillis() / 1000);

			try
			{
				PreparedStatement statement = IO.getConnection().prepareStatement("SELECT * FROM regeneration_structures WHERE StructureName = ? ORDER BY LastCheck ASC LIMIT ?");
				statement.setString(1, structure.getName());
				statement.setInt(2, structure.getNumberToGenerateAtOnce());

				ResultSet set = statement.executeQuery();

				while (set.next())
				{
					final int id = set.getInt("id");

					final String worldName = set.getString("World");
					final int cornerX = set.getInt("CornerX");
					final int cornerZ = set.getInt("CornerZ");
					final int xSize = set.getInt("SizeX");
					final int zSize = set.getInt("SizeZ");
					
					World world = Bukkit.getWorld(worldName);

					MLog.info("Checking structure " + structure.getName() + " around " + cornerX + " " + cornerZ);

					int padding = RegenerationSettings.RESORATION_VILLAGE_CHECK_PADDING.integer();
					
					if (isPlayerInside(cornerX, cornerZ, xSize, zSize))
					{
						MLog.info("Will not restore - player inside.");
					}
					else if (!GriefPreventionHandler.containsClaim(world, cornerX, cornerZ, xSize, zSize, padding, false))
					{
						generated++;

						RegenerationUtil.regenerateStructure(id);
						MLog.info("Restored!");
					}		
					else
					{
						MLog.info("Will not restore - claim exist there.");
					}

					statement.close();
					statement = IO.getConnection().prepareStatement("UPDATE regeneration_structures SET LastCheck = ? WHERE ID = ?");
					statement.setInt(1, now);
					statement.setInt(2, id);
					statement.executeUpdate();
					IO.getConnection().commit();
					statement.close();

				}
				
				if (tries > 30)
				{
					MLog.warning("Failed to find enough " + structure.getName() + " to restore after 30 tries! Are all claimed?");
					break;
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
		int padding = RegenerationSettings.RESORATION_VILLAGE_CHECK_PADDING.integer();
		
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
