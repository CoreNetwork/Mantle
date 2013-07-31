package com.mcnsa.flatcore.flatcorecommands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.IO;
import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;

public class DeleteVillageCommand extends BaseAdminCommand {
	private static HashMap<String, Integer> pickedVillage = new HashMap<String, Integer>();
	
	public DeleteVillageCommand()
	{
		desc = "Delete nearby village from database";
		needPlayer = true;
	}


	public Boolean run(CommandSender sender, String[] args) {
		Integer previousDecision = pickedVillage.get(((Player) sender).getName());
		
		if (previousDecision != null)
		{
			VillageData nearbyVillage = pickNearestVillage(((Player) sender).getLocation());
			if (nearbyVillage.id != previousDecision)
				previousDecision = null;
			else
			{
				try
				{
					PreparedStatement statement = IO.getConnection().prepareStatement("DELETE FROM Villages WHERE ID = ?");
					statement.setInt(1, previousDecision);

					statement.executeUpdate();
					IO.getConnection().commit();
					
					Util.Message(Settings.getString(Setting.MESSAGE_VILLAGE_DELETED), sender);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		if (previousDecision == null)
		{
			VillageData nearbyVillage = pickNearestVillage(((Player) sender).getLocation());
			if (nearbyVillage == null)
			{
				Util.Message(Settings.getString(Setting.MESSAGE_NO_VILLAGES), sender);
				return true;
			}
			
			pickedVillage.put(((Player) sender).getName(), nearbyVillage.id);
			
			String message = Settings.getString(Setting.MESSAGE_DELETE_NEARBY_VILLAGE);
			message = message.replace("<ID>", Integer.toString(nearbyVillage.id));
			message = message.replace("<Distance>", Integer.toString(nearbyVillage.distance));
			Util.Message(message, sender);
		}
		
		return true;

	}
	
	public static VillageData pickNearestVillage(Location location)
	{
		int x = location.getBlockX();
		int z = location.getBlockZ();
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT ID, ((CornerX - ? + sizeX / 2) * (CornerX - ? + sizeX / 2) + (CornerZ - ? + sizeZ / 2) * (CornerZ - ? + sizeZ / 2)) as dist FROM villages ORDER BY dist ASC LIMIT 1");
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
				
				VillageData data = new VillageData();
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

	private static class VillageData
	{
		public int id;
		public int distance;
	}
}
