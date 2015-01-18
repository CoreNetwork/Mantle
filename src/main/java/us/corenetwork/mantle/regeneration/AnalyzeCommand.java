package us.corenetwork.mantle.regeneration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;


public class AnalyzeCommand extends BaseMantleCommand {	
	public AnalyzeCommand()
	{
		permission = "analyze";
		desc = "Analyze all structures for claim status";
		needPlayer = false;
	}


	public void run(final CommandSender sender, String[] args) {
		Util.Message(RegenerationSettings.MESSAGE_ANALYZING.string(), sender);

		Bukkit.getServer().getScheduler().runTaskAsynchronously(MantlePlugin.instance, new Runnable() {

			@Override
			public void run() {
				Util.Message(RegenerationSettings.MESSAGE_ANALYZE_HEADER.string(), sender);

				for (RegStructure structure : RegenerationModule.instance.structures.values())
				{
					int claimed = 0;
					int empty = 0;
					int approved = 0;
					int postponed = 0;


					try
					{
						PreparedStatement statement = IO.getConnection().prepareStatement("SELECT CornerX,CornerZ,SizeX,SizeZ,World,InspectionStatus FROM regeneration_structures WHERE StructureName = ?");
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
							
							int inspectionStatus = set.getInt("InspectionStatus");
							if (inspectionStatus < 0)
								postponed++;
							else if (inspectionStatus == 1)
								approved++;
							
							int padding = RegenerationSettings.RESORATION_VILLAGE_CHECK_PADDING.integer();
							
							if (inspectionStatus <= 0)
							{
								if (GriefPreventionHandler.containsClaim(world, cornerX, cornerZ, xSize, zSize, padding, false, null))
								{
									claimed++;
								}		
								else
								{
									empty++;
								}
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
					
					String postponedApproved = "";
					if (approved > 0)
						postponedApproved += RegenerationSettings.MESSAGE_APPROVED_INSERT.string().replace("<Approved>", Integer.toString(approved));
					
					if (postponed > 0)
						postponedApproved += RegenerationSettings.MESSAGE_POSTPONED_INSERT.string().replace("<Postponed>", Integer.toString(postponed));
					
					String message = RegenerationSettings.MESSAGE_ANALYZE_LINE.string();
					message = message.replace("<Structure>", structure.getName().replace('_', ' '));
					message = message.replace("<Total>", Integer.toString(total));
					message = message.replace("<Claimed>", Integer.toString(claimed));
					message = message.replace("<ClaimedPercent>", Integer.toString(claimedPercent));
					message = message.replace("<Empty>", Integer.toString(empty));
					message = message.replace("<EmptyPercent>", Integer.toString(emptyPercent));
					message = message.replace("<PostponedApproved>", postponedApproved);
					Util.Message(message, sender);
				}
			}

		});
	}	
}
