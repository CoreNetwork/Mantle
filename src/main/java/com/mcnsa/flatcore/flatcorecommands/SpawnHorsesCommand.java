package com.mcnsa.flatcore.flatcorecommands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Style;

import com.mcnsa.flatcore.FCLog;
import com.mcnsa.flatcore.IO;
import com.mcnsa.flatcore.MCNSAFlatcore;
import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;

public class SpawnHorsesCommand extends BaseAdminCommand {
	public SpawnHorsesCommand()
	{
		desc = "Spawn horses";
		needPlayer = false;
	}


	public Boolean run(CommandSender sender, String[] args) {

		Util.Message(Settings.getString(Setting.MESSAGE_SERVER_FROZEN), sender);
		
		World world = Bukkit.getWorlds().get(0);
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT CenterX,CenterZ,SizeX,SizeZ FROM villages");
			ResultSet set = statement.executeQuery();

			while (set.next())
			{
				final int villageX = set.getInt("centerX");
				final int villageZ = set.getInt("centerZ");
				final int xSize = set.getInt("SizeX");
				final int zSize = set.getInt("SizeZ");
				FCLog.info("Spawning horses near village at " + villageX + " " + villageZ);
				int horses = MCNSAFlatcore.random.nextInt(3) + 1;
				FCLog.info("Amount of horses picked: " + horses);
				
				
				
				for (int i = 0; i < horses; i++)
				{
					int counter = 0;
					while (true)
					{
						counter++;
						if (counter > 1000)
						{
							FCLog.info("Unable to find horse position after 1000 tries. Skipping...");
							break;
						}
						
						int randomX = villageX + MCNSAFlatcore.random.nextInt(xSize);
						int randomZ = villageZ + MCNSAFlatcore.random.nextInt(zSize);

						Chunk chunk = world.getChunkAt(new Location(world, randomX, 0, randomZ));
						if (!chunk.isLoaded())
							chunk.load();
						
						Block pickedBlock = world.getHighestBlockAt(randomX, randomZ).getRelative(BlockFace.DOWN);
						
						boolean canSpawn = hasEnoughSpace(pickedBlock);
						if (!canSpawn)
							continue;
						
						Location spawnLocation = pickedBlock.getRelative(BlockFace.UP).getLocation();
						
						Horse horse = (Horse) world.spawnEntity(spawnLocation, EntityType.HORSE);
						
						//Randomize looks
						horse.setVariant(Horse.Variant.values()[MCNSAFlatcore.random.nextInt(2)]);
						horse.setStyle(Style.values()[MCNSAFlatcore.random.nextInt(Style.values().length)]);
						horse.setColor(Horse.Color.values()[MCNSAFlatcore.random.nextInt(Horse.Color.values().length)]);
						
						break;
					}
				}
			}

			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		FCLog.info("Spawning complete!");

		
		return true;
	}	
	
	private boolean hasEnoughSpace(Block pickedBlock)
	{		
		
		//There must be 3x3 space around picked block	
		for (int x = -1; x <= 1; x++)
		{
			for (int z = -1; z<= 1; z++ )
			{
				for (int y = 0; y < 4; y++)
				{
					Block neighbourBlock = pickedBlock.getRelative(x, y, z);
					
					if (neighbourBlock == null)
						return false;
					
					if (!neighbourBlock.getChunk().isLoaded())
						neighbourBlock.getChunk().load();
										
					if ((y == 0 && !neighbourBlock.getType().isSolid()) || (y > 0 && !neighbourBlock.isEmpty()))
					{
						return false;
					}
				}
			}
		}
		
		return true;
	}
}
