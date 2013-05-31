package com.mcnsa.flatcore.flatcorecommands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.GriefPreventionHandler;
import com.mcnsa.flatcore.IO;
import com.mcnsa.flatcore.MCNSAFlatcore;
import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;

public class FindAdminVillageCommand extends BaseAdminCommand {
	private volatile static HashMap<String, Location> pickedVillage = new HashMap<String, Location>();
	
	public FindAdminVillageCommand()
	{
		desc = "Find village with admin claim";
		needPlayer = true;
	}


	public Boolean run(CommandSender sender, String[] args) {
		Location previousDecision = pickedVillage.get(((Player) sender).getName());
		
		final Player player = (Player) sender;
		
		if (previousDecision == null)
		{
			Util.Message(Settings.getString(Setting.MESSAGE_ANALYZING), sender);

			Bukkit.getScheduler().runTaskAsynchronously(MCNSAFlatcore.instance, new Runnable() {
				@Override
				public void run() {
					World overworld = Bukkit.getServer().getWorlds().get(0);
					boolean found = false;

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
							
							if (GriefPreventionHandler.containsClaim(villageX, villageZ, xSize, zSize, true))
							{
								Location location = new Location(overworld, villageX + xSize / 2, 15, villageZ + zSize / 2);
								if (pickedVillage.containsValue(location))
									continue;
								
								pickedVillage.put(player.getName(), location);
								
								String message = Settings.getString(Setting.MESSAGE_FOUND_ADMIN_VILLAGE);
								message = message.replace("<X>", Integer.toString(location.getBlockX()));
								message = message.replace("<Z>", Integer.toString(location.getBlockZ()));
								Util.Message(message, player);
								found = true;
								
								break;
							}		
						}
						
						statement.close();
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
					
					if (!found)
					{
						Util.Message(Settings.getString(Setting.MESSAGE_NO_ADMIN_VILLAGE), player);
					}
				}
			});			
		}
		else
		{
			player.teleport(previousDecision);
			pickedVillage.remove(player.getName());
			Util.Message(Settings.getString(Setting.MESSAGE_TELEPORTED), sender);
		}
		
		return true;
	}	
}
