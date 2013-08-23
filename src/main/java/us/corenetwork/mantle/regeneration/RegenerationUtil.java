package us.corenetwork.mantle.regeneration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import us.corenetwork.mantle.CachedSchematic;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.generation.VillagerSpawner;


public class RegenerationUtil {

	public static void regenerateStructure(int id)
	{
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT StructureName, Schematic, World, CornerX, CornerZ, PastingY FROM regeneration_structures WHERE id = ? LIMIT 1");
			statement.setInt(1, id);

			ResultSet set = statement.executeQuery();
			if (!set.next())
				return;
			else
			{
				String structureName = set.getString("StructureName");
				String schematicName = set.getString("Schematic");
				String worldName = set.getString("World");
				int cornerX = set.getInt("CornerX");
				int cornerZ = set.getInt("CornerZ");
				int pastingY = set.getInt("PastingY");

				final CachedSchematic schematic = new CachedSchematic(schematicName);
				World world = Bukkit.getWorld(worldName);

				final Location pastingLocation = new Location(world, cornerX, pastingY, cornerZ);
				RegStructure structure = RegenerationModule.instance.structures.get(structureName);

				if (structure.shouldRespawnVillagers())
					schematic.findVillagers();


				schematic.place(pastingLocation, structure.shouldIgnoreAir());

				if (structure.shouldRespawnVillagers())
				{
					schematic.findVillagers();

					Bukkit.getServer().getScheduler().runTask(MantlePlugin.instance, new Runnable() {

						@Override
						public void run() {
							schematic.clearVillagers(pastingLocation);

							VillagerSpawner villagerSpawner = new VillagerSpawner();
							schematic.spawnVillagers(pastingLocation, villagerSpawner);
							villagerSpawner.close();
						}
					});	

				}

			}
			
			statement.close();
			
			statement = IO.getConnection().prepareStatement("UPDATE regeneration_structures SET LastRestore = ? WHERE ID = ?");
			statement.setInt(1, (int) (System.currentTimeMillis() / 1000));
			statement.setInt(2, id);
			statement.executeUpdate();
			IO.getConnection().commit();
			statement.close();

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

	}

	public static StructureData pickNearestStructure(Location location)
	{
		int x = location.getBlockX();
		int z = location.getBlockZ();
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT ID, ((CornerX - ? + sizeX / 2) * (CornerX - ? + sizeX / 2) + (CornerZ - ? + sizeZ / 2) * (CornerZ - ? + sizeZ / 2)) as dist FROM regeneration_structures ORDER BY dist ASC LIMIT 1");
			statement.setInt(1, x);
			statement.setInt(2, x);
			statement.setInt(3, z);
			statement.setInt(4, z);

			ResultSet set = statement.executeQuery();
			if (!set.next())
				return null;
			else
			{
				int distanceSquared = set.getInt("dist");
				int id = set.getInt("id");
				int distance = (int) Math.sqrt(distanceSquared);

				StructureData data = new StructureData();
				data.id = id;
				data.distance = distance;

				return data;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return null;
	}

}
