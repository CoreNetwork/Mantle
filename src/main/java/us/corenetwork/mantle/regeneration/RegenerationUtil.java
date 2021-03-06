package us.corenetwork.mantle.regeneration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import us.corenetwork.mantle.CachedSchematic;
import us.corenetwork.mantle.CachedSchematic.ChestInfo;
import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.generation.GenerationModule;
import us.corenetwork.mantle.generation.VillagerSpawner;
import us.corenetwork.mantle.restockablechests.RestockableChest;


public class RegenerationUtil {


	private static Map<CachedSchematic, RegStructure> mapp = new HashMap<CachedSchematic, RegStructure>();
	private static Map<CachedSchematic, Integer> mappId = new HashMap<CachedSchematic, Integer>();

	public static void regenerateStructure(int id)
	{
		regenerateStructure(id, (int) (System.currentTimeMillis() / 1000), false);
	}
	
	public static void regenerateStructure(int id, int time, boolean clearClaims)
	{
		Util.debugTime("RG Start");
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT StructureName, Schematic, Rotation, World, CornerX, CornerZ, SizeX, SizeZ, PastingY FROM regeneration_structures WHERE id = ? LIMIT 1");
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
				final int xSize = set.getInt("SizeX");
				final int zSize = set.getInt("SizeZ");
				int pastingY = set.getInt("PastingY");
				int rotation = set.getInt("Rotation");

				Util.debugTime("RG SQL parsed");
				//Ginaw
				//hacky solution to randomizing a schematic for overworld village
				if(structureName.equalsIgnoreCase("villages"))
				{
					List<String> schematicsNames = (List<String>) GenerationModule.instance.config.get("Worlds.world.Structures.Village.Schematics");
					String newSchematicName = schematicsNames.get(MantlePlugin.random.nextInt(schematicsNames.size()));
					
					schematicName = newSchematicName;
				}
				//End

				World world = Bukkit.getWorld(worldName);
				final CachedSchematic schematic = new CachedSchematic(schematicName, world);

				if (clearClaims)
				{
					int padding = RegenerationSettings.RESORATION_VILLAGE_CHECK_PADDING.integer();
					GriefPreventionHandler.deleteClaimsInside(world, cornerX, cornerZ, xSize, zSize, padding, false, null);
				}


				final Location pastingLocation = new Location(world, cornerX, pastingY, cornerZ);
				RegStructure structure = RegenerationModule.instance.structures.get(structureName);


				List<RestockableChest> oldRestockableChests = RestockableChest.getChestsInStructure(id);
				for(RestockableChest rc : oldRestockableChests)
					rc.delete(false);

				//Searching for loot chests, removing signs

				if (worldName.equalsIgnoreCase("world_nether"))
				{
					CachedSchematic.isNether = true;
				}
				schematic.findChests();
				if (worldName.equalsIgnoreCase("world_nether"))
				{
					CachedSchematic.isNether = false;
				}

				//find villagers before placing the schematic, so findVillagers can remove the signs from the schematic
				// YAY for side effects in methods~!
				if (structure.shouldRespawnVillagers())
				{
					schematic.findVillagers();
				}

				mapp.put(schematic, structure);
				mappId.put(schematic, id);
				schematic.place(pastingLocation, structure.shouldIgnoreAir());
			}
			statement.close();

			statement = IO.getConnection().prepareStatement("UPDATE regeneration_structures SET LastCheck = ?, LastRestore = ? WHERE ID = ?");
			statement.setInt(1, time);
			statement.setInt(2, time);
			statement.setInt(3, id);

			statement.executeUpdate();
			IO.getConnection().commit();
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

	}

	public static void finishRegenerateStructure(CachedSchematic schematic, final int rotation, Location pastingLocation)
	{
		RegStructure structure = mapp.get(schematic);
		int id = mappId.get(schematic);
		if (structure.shouldRespawnVillagers())
		{
			schematic.clearVillagers(pastingLocation);
			VillagerSpawner villagerSpawner = new VillagerSpawner();
			schematic.spawnVillagers(pastingLocation, villagerSpawner, rotation);
			villagerSpawner.close();
		}

		ChestInfo[] chests = schematic.getChests(pastingLocation, rotation);
		for (ChestInfo chest : chests)
		{
			if (chest.restockable)
				RestockableChest.createChest(chest.loc.getBlock(), chest.lootTable, chest.interval, chest.perPlayer, id, false);
		}

		try
		{
			IO.getConnection().commit();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}

		mapp.remove(schematic);
		mappId.remove(schematic);
		MLog.info("Restored!");
	}


	public static StructureData pickNearestStructure(Location location)
	{
		int x = location.getBlockX();
		int z = location.getBlockZ();
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT ID, ((CornerX - ? + sizeX / 2) * (CornerX - ? + sizeX / 2) + (CornerZ - ? + sizeZ / 2) * (CornerZ - ? + sizeZ / 2)) as dist FROM regeneration_structures WHERE World = ? ORDER BY dist ASC LIMIT 1");
			statement.setInt(1, x);
			statement.setInt(2, x);
			statement.setInt(3, z);
			statement.setInt(4, z);
			statement.setString(5, location.getWorld().getName());

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
