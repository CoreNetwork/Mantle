package com.mcnsa.flatcore.admincommands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.mcnsa.flatcore.CachedSchematic;
import com.mcnsa.flatcore.FCLog;
import com.mcnsa.flatcore.GriefPreventionHandler;
import com.mcnsa.flatcore.IO;
import com.mcnsa.flatcore.MCNSAFlatcore;
import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;

public class AnalyzeCommand extends BaseAdminCommand {	
	public AnalyzeCommand()
	{
		desc = "Analyze all villages for claim status";
		needPlayer = false;
	}


	public Boolean run(final CommandSender sender, String[] args) {
		Util.Message(Settings.getString(Setting.MESSAGE_ANALYZING), sender);
		
		Bukkit.getServer().getScheduler().runTaskAsynchronously(MCNSAFlatcore.instance, new Runnable() {

			@Override
			public void run() {
				int claimed = 0;
				int empty = 0;
				
				
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
												
						if (GriefPreventionHandler.containsClaim(villageX, villageZ, xSize, zSize, false))
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
				int claimedPercent = claimed * 100 / total;
				int emptyPercent = empty * 100 / total;
				
				String message = Settings.getString(Setting.MESSAGE_VILLAGE_STATUS);
				message = message.replace("<Total>", Integer.toString(total));
				message = message.replace("<Claimed>", Integer.toString(claimed));
				message = message.replace("<ClaimedPercent>", Integer.toString(claimedPercent));
				message = message.replace("<Empty>", Integer.toString(empty));
				message = message.replace("<EmptyPercent>", Integer.toString(emptyPercent));

				Util.Message(message, sender);
			}
			
		});
		
		return true;
	}	
}
