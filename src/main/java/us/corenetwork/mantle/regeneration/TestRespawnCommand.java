package us.corenetwork.mantle.regeneration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;


public class TestRespawnCommand extends BaseMantleCommand {
	
	public TestRespawnCommand()
	{
		permission = "testrespawn";
		desc = "Test nearby respawning structure for deletion";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		Location location = player.getLocation();
		World firstWorld = Bukkit.getWorlds().get(0);
		
		int x = location.getBlockX();
		int z = location.getBlockZ();
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT ID, CornerX, CornerZ, SizeX, SizeZ, ((CornerX - ? + sizeX / 2) * (CornerX - ? + sizeX / 2) + (CornerZ - ? + sizeZ / 2) * (CornerZ - ? + sizeZ / 2)) as dist FROM regeneration_structures ORDER BY dist ASC LIMIT 1");
			statement.setInt(1, x);
			statement.setInt(2, x);
			statement.setInt(3, z);
			statement.setInt(4, z);

			ResultSet set = statement.executeQuery();
			if (!set.next())
				Util.Message(RegenerationSettings.MESSAGE_NO_STRUCTURES.string(), sender);
			else
			{
				final int villageX = set.getInt("CornerX");
				final int villageZ = set.getInt("CornerZ");
				final int xSize = set.getInt("SizeX");
				final int zSize = set.getInt("SizeZ");
				int id = set.getInt("ID");
				int distance = (int) Math.sqrt(set.getInt("dist"));
							
				int padding = RegenerationSettings.RESORATION_VILLAGE_CHECK_PADDING.integer();
				
				String message;
				if (GriefPreventionHandler.containsClaim(firstWorld, villageX, villageZ, xSize, zSize, padding, false, null))
				{
					message = RegenerationSettings.MESSAGE_STRUCTURE_WILL_NOT_BE_RESTORED.string();
				}		
				else
				{
					message = RegenerationSettings.MESSAGE_STRUCTURE_WILL_BE_RESTORED.string();
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
	}
	
}
