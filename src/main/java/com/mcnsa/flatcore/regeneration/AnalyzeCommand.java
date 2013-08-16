package com.mcnsa.flatcore.regeneration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.mcnsa.flatcore.GriefPreventionHandler;
import com.mcnsa.flatcore.IO;
import com.mcnsa.flatcore.MCNSAFlatcore;
import com.mcnsa.flatcore.Util;
import com.mcnsa.flatcore.flatcorecommands.BaseAdminCommand;

public class AnalyzeCommand extends BaseAdminCommand {	
	public AnalyzeCommand()
	{
		desc = "Analyze all structures for claim status";
		needPlayer = false;
	}


	public Boolean run(final CommandSender sender, String[] args) {
		Util.Message(RegenerationSettings.MESSAGE_ANALYZING.string(), sender);

		Bukkit.getServer().getScheduler().runTaskAsynchronously(MCNSAFlatcore.instance, new Runnable() {

			@Override
			public void run() {
				Util.Message(RegenerationSettings.MESSAGE_ANALYZE_HEADER.string(), sender);

				for (RegStructure structure : RegenerationModule.instance.structures.values())
				{
					int claimed = 0;
					int empty = 0;


					try
					{
						PreparedStatement statement = IO.getConnection().prepareStatement("SELECT CornerX,CornerZ,SizeX,SizeZ,World FROM regeneration_structures WHERE StructureName = ?");
						statement.setString(1, structure.getName());

						ResultSet set = statement.executeQuery();

						while (set.next())
						{
							int cornerX = set.getInt("CornerX");
							int cornerZ = set.getInt("CornerZ");
							int xSize = set.getInt("SizeX");
							int zSize = set.getInt("SizeZ");
							String worldName = set.getString("World");
							World world = Bukkit.getWorld(worldName);
							
							int padding = RegenerationSettings.RESORATION_VILLAGE_CHECK_PADDING.integer();
							
							if (GriefPreventionHandler.containsClaim(world, cornerX, cornerZ, xSize, zSize, padding, false))
							{
								claimed++;
							}		
							else
							{
								empty++;
							}
						}

						statement.close();
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}

					int total = claimed + empty;
					int claimedPercent = total == 0 ? 0 : (claimed * 100 / total);
					int emptyPercent = total == 0 ? 0 : (empty * 100 / total);

					String message = RegenerationSettings.MESSAGE_ANALYZE_LINE.string();
					message = message.replace("<Structure>", structure.getName());
					message = message.replace("<Total>", Integer.toString(total));
					message = message.replace("<Claimed>", Integer.toString(claimed));
					message = message.replace("<ClaimedPercent>", Integer.toString(claimedPercent));
					message = message.replace("<Empty>", Integer.toString(empty));
					message = message.replace("<EmptyPercent>", Integer.toString(emptyPercent));

					Util.Message(message, sender);
				}
			}

		});

		return true;
	}	
}
