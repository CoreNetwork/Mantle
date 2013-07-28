package com.mcnsa.flatcore.flatcorecommands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import com.mcnsa.flatcore.CachedSchematic;
import com.mcnsa.flatcore.CachedSchematic.ChestInfo;
import com.mcnsa.flatcore.FCLog;
import com.mcnsa.flatcore.GriefPreventionHandler;
import com.mcnsa.flatcore.IO;
import com.mcnsa.flatcore.RestockableChest;
import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;

public class InitVillagesCommand extends BaseAdminCommand {
	public InitVillagesCommand()
	{
		desc = "Init villages";
		needPlayer = false;
	}


	public Boolean run(CommandSender sender, String[] args) {

		try {
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT ID FROM villages LIMIT 1");
			ResultSet set = statement.executeQuery();
			if (set.next())
			{
				Util.Message(Settings.getString(Setting.MESSAGE_DELETE_DB_TO_IMPORT), sender);
				statement.close();
				return true;
			}

			Util.Message(Settings.getString(Setting.MESSAGE_SERVER_FROZEN), sender);
			FCLog.info("Initializing village generation...");
			
			statement.close();
			statement = IO.getConnection().prepareStatement("INSERT INTO villages (Type, CenterX, CenterZ, SizeX, SizeZ) VALUES (?,?,?,?,?)");

			int num = Settings.getInt(Setting.NUMBER_OF_VILLAGES);
			CachedSchematic[] villages = new CachedSchematic[num];
			for (int i = 0; i < num; i++)
			{				
				String villageIdString = i >= 9 ? Integer.toString(i + 1) : ("0" + (i + 1));
				String fileName = "village-" + villageIdString + ".schematic";
				
				villages[i] = new CachedSchematic(fileName);
				villages[i].findVillagers();
				
				FCLog.info("Loading schematic for village #" + (i + 1) + " (Size: " + villages[i].xSize + " " + villages[i].zSize + ") ...");
			}
				

			int minX = Settings.getInt(Setting.GENERATION_MIN_X);
			int minZ = Settings.getInt(Setting.GENERATION_MIN_Z);
			int maxX = Settings.getInt(Setting.GENERATION_MAX_X);
			int maxZ = Settings.getInt(Setting.GENERATION_MAX_Z);
			int step = Settings.getInt(Setting.VILLAGE_GRID_SPACE);
			int offX = Settings.getInt(Setting.VILLAGE_OFFSET_X);
			int offZ = Settings.getInt(Setting.VILLAGE_OFFSET_Z);
			int randomOffset = Settings.getInt(Setting.VILLAGE_RANDOM_OFFSET);
			int y = Settings.getInt(Setting.VILLAGE_PASTING_Y);
			
			int numOfVillages = (Math.abs(maxX - minX) / step) * (Math.abs(maxZ - minZ) / step);
			int done = 0;

			Random random = new Random();
			for (int x = minX; x < maxX; x+= step)
			{
				for (int z = minZ; z < maxZ; z+= step)
				{
					int villageId = random.nextInt(num);
					int percentage = done * 100 / numOfVillages;
					Location center;
					center = villages[villageId].place(x + offX, y, z + offZ, randomOffset);
					villages[villageId].spawnVillagers(center);
					
					statement.setInt(1, villageId);
					statement.setInt(2, center.getBlockX());
					statement.setInt(3, center.getBlockZ());
					statement.setInt(4, villages[villageId].xSize);
					statement.setInt(5, villages[villageId].zSize);
					statement.addBatch();
					
					done++;
					FCLog.info("Placed village #" + (villageId + 1) + " at " + center.getBlockX() + " " + center.getBlockZ() + " [" + percentage + "% completed]");
				
					if (percentage % 5 == 0)
						for (Chunk c : Bukkit.getServer().getWorlds().get(0).getLoadedChunks())
							c.unload(true, true);
				}
			}

			FCLog.info("Updating village database...");
			statement.executeBatch();
			IO.getConnection().commit();
			
			FCLog.info("Initializing campfire generation...");

			villages = null;
			int normalNum = Settings.getInt(Setting.NUMBER_OF_NORMAL_CAMPFIRES);
			int trappedNum = Settings.getInt(Setting.NUMBER_OF_TRAPPED_CAMPFIRES);
			
			CachedSchematic[] normalCampfires = new CachedSchematic[normalNum];
			for (int i = 0; i < normalNum; i++)
			{
				FCLog.info("Loading schematic for normal campfire #" + i + " ...");
				
				String fileName = "campfire" + (i + 1) + "-safe.schematic";
				
				normalCampfires[i] = new CachedSchematic(fileName);
				normalCampfires[i].findChests();
			}
			
			CachedSchematic[] trappedCampfires = new CachedSchematic[trappedNum];
			for (int i = 0; i < trappedNum; i++)
			{
				FCLog.info("Loading schematic for trapped campfire #" + i + " ...");
				
				String fileName = "campfire" + (i + 1) + "-trapped.schematic";
				
				trappedCampfires[i] = new CachedSchematic(fileName);
				trappedCampfires[i].findChests();
			}
				

			step = Settings.getInt(Setting.CAMPFIRE_GRID_SPACE);
			offX = Settings.getInt(Setting.CAMPFIRE_OFFSET_X);
			offZ = Settings.getInt(Setting.CAMPFIRE_OFFSET_Z);
			randomOffset = Settings.getInt(Setting.CAMPFIRE_RANDOM_OFFSET);
			y = Settings.getInt(Setting.CAMPFIRE_PASTING_Y);
			int trappedChance = Settings.getInt(Setting.CAMPFIRE_TRAP_CHANCE);
			
			int numOfCampfires = (Math.abs(maxX - minX) / step) * (Math.abs(maxZ - minZ) / step);
			done = 0;

			for (int x = minX; x < maxX; x+= step)
			{
				for (int z = minZ; z < maxZ; z+= step)
				{
					boolean trap = random.nextInt(100) < trappedChance;
					int campFire = random.nextInt(trap ? trappedNum : normalNum);
					int percentage = done * 100 / numOfCampfires;
					
					Location center;
					if (trap)
						center = trappedCampfires[campFire].place(x + offX, y, z + offZ, randomOffset);
					else
						center = normalCampfires[campFire].place(x + offX, y, z + offZ, randomOffset);
					
					ChestInfo chest;
					if (trap)
						chest = trappedCampfires[campFire].getChests(center)[0];
					else
						chest = normalCampfires[campFire].getChests(center)[0];

					RestockableChest.createChest(chest.loc.getBlock(), chest.lootTable, chest.interval, chest.perPlayer);
					
					GriefPreventionHandler.secureCampfire(chest.loc);
					
					//GriefPreventionHandler.secureCampfire(center)
					
					FCLog.info("Placed " + (trap ? "trapped " : "") + "campfire #" + (campFire + 1) + " at " + center.getBlockX() + " " + center.getBlockZ() + " [" + percentage + "% completed]");
					done++;
					
					if (percentage % 5 == 0)
						for (Chunk c : Bukkit.getServer().getWorlds().get(0).getLoadedChunks())
							c.unload(true, true);
				}
			}
			
			FCLog.info("Initializing outpost generation...");

			int outpostNum = Settings.getInt(Setting.NUMBER_OF_OUTPOSTS);
			
			CachedSchematic[] outposts = new CachedSchematic[outpostNum];
			for (int i = 0; i < trappedNum; i++)
			{
				FCLog.info("Loading schematic for outpost #" + i + " ...");
				
				String fileName = "outpost" + (i + 1) + ".schematic";
				
				outposts[i] = new CachedSchematic(fileName);
				outposts[i].findChests();
			}
				

			step = Settings.getInt(Setting.OUTPOST_GRID_SPACE);
			offX = Settings.getInt(Setting.OUTPOST_OFFSET_X);
			offZ = Settings.getInt(Setting.OUTPOST_OFFSET_Z);
			randomOffset = Settings.getInt(Setting.OUTPOST_RANDOM_OFFSET);
			y = Settings.getInt(Setting.OUTPOST_PASTING_Y);
			
			int numOfOutposts = (Math.abs(maxX - minX) / step) * (Math.abs(maxZ - minZ) / step);
			done = 0;

			for (int x = minX; x < maxX; x+= step)
			{
				for (int z = minZ; z < maxZ; z+= step)
				{
					int outpost = random.nextInt(outpostNum);
					int percentage = done * 100 / numOfOutposts;
					
					Location center = outposts[outpost].place(x + offX, y, z + offZ, randomOffset);
					
					ChestInfo[] chests = outposts[outpost].getChests(center);

					for (ChestInfo chest : chests)
						RestockableChest.createChest(chest.loc.getBlock(), chest.lootTable, chest.interval, chest.perPlayer);
					
					center = outposts[outpost].getCenter(center);
					GriefPreventionHandler.secureOutpost(center);
					
					FCLog.info("Placed " + "outpost #" + (outpost + 1) + " at " + center.getBlockX() + " " + center.getBlockZ() + " [" + percentage + "% completed]");
					done++;
					
					if (percentage % 5 == 0)
						for (Chunk c : Bukkit.getServer().getWorlds().get(0).getLoadedChunks())
							c.unload(true, true);
				}
			}
			
			System.gc();
						
			FCLog.info("DONE");
			
			Util.Message(Settings.getString(Setting.MESSAGE_GENERATION_COMPLETED), sender);
			
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;

	}

}
