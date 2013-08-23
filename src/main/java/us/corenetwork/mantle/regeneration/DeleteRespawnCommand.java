package us.corenetwork.mantle.regeneration;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.Setting;
import us.corenetwork.mantle.Settings;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseAdminCommand;


public class DeleteRespawnCommand extends BaseAdminCommand {
	private static HashMap<String, Integer> pickedStructure = new HashMap<String, Integer>();
	
	public DeleteRespawnCommand()
	{
		desc = "Delete nearby respawnable structure from database";
		needPlayer = true;
	}


	public Boolean run(CommandSender sender, String[] args) {
		Integer previousDecision = pickedStructure.get(((Player) sender).getName());
		
		if (previousDecision != null)
		{
			StructureData nearbyStructure = RegenerationUtil.pickNearestStructure(((Player) sender).getLocation());
			if (nearbyStructure.id != previousDecision)
				previousDecision = null;
			else
			{
				try
				{
					PreparedStatement statement = IO.getConnection().prepareStatement("DELETE FROM regeneration_structures WHERE ID = ?");
					statement.setInt(1, previousDecision);

					statement.executeUpdate();
					IO.getConnection().commit();
					
					Util.Message(RegenerationSettings.MESSAGE_STRUCTURE_DELETED.string(), sender);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		if (previousDecision == null)
		{
			StructureData nearbyVillage = RegenerationUtil.pickNearestStructure(((Player) sender).getLocation());
			if (nearbyVillage == null)
			{
				Util.Message(RegenerationSettings.MESSAGE_NO_STRUCTURES.string(), sender);
				return true;
			}
			
			pickedStructure.put(((Player) sender).getName(), nearbyVillage.id);
			
			String message = RegenerationSettings.MESSAGE_DELETE_NEARBY_STRUCTURE.string();
			message = message.replace("<ID>", Integer.toString(nearbyVillage.id));
			message = message.replace("<Distance>", Integer.toString(nearbyVillage.distance));
			Util.Message(message, sender);
		}
		
		return true;

	}
}
