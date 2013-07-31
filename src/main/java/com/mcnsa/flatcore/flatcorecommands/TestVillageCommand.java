package com.mcnsa.flatcore.flatcorecommands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.GriefPreventionHandler;
import com.mcnsa.flatcore.IO;
import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;

public class TestVillageCommand extends BaseAdminCommand {
	
	public TestVillageCommand()
	{
		desc = "Test nearby village for deletion";
		needPlayer = true;
	}


	public Boolean run(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		Location location = player.getLocation();
		World firstWorld = Bukkit.getWorlds().get(0);
		
		int x = location.getBlockX();
		int z = location.getBlockZ();
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT ID, CornerX, CornerZ, SizeX, SizeZ, ((CornerX - ? + sizeX / 2) * (CornerX - ? + sizeX / 2) + (CornerZ - ? + sizeZ / 2) * (CornerZ - ? + sizeZ / 2)) as dist FROM villages ORDER BY dist ASC LIMIT 1");
			statement.setInt(1, x);
			statement.setInt(2, x);
			statement.setInt(3, z);
			statement.setInt(4, z);

			ResultSet set = statement.executeQuery();
			if (!set.next())
				Util.Message(Settings.getString(Setting.MESSAGE_NO_VILLAGES), sender);
			else
			{
				final int villageX = set.getInt("CornerX");
				final int villageZ = set.getInt("CornerZ");
				final int xSize = set.getInt("SizeX");
				final int zSize = set.getInt("SizeZ");
				int id = set.getInt("ID");
				int distance = (int) Math.sqrt(set.getInt("dist"));
							
				String message;
				if (GriefPreventionHandler.containsClaim(firstWorld, villageX, villageZ, xSize, zSize, false))
				{
					message = Settings.getString(Setting.MESSAGE_VILLAGE_WILL_NOT_BE_RESTORED);
				}		
				else
				{
					message = Settings.getString(Setting.MESSAGE_VILLAGE_WILL_BE_RESTORED);
				}
				
				message = message.replace("<ID>", Integer.toString(id));
				message = message.replace("<Distance>", Integer.toString(distance));
				Util.Message(message, sender);


			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return true;

	}
	
}
