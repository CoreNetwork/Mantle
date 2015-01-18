package us.corenetwork.mantle.regeneration;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;


public class DeleteRespawnCommand extends BaseMantleCommand {
	private static HashMap<UUID, Integer> pickedStructure = new HashMap<UUID, Integer>();
	
	public DeleteRespawnCommand()
	{
		permission = "deleterespawn";
		desc = "Delete nearby respawnable structure from database";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args) {
		Integer previousDecision = pickedStructure.get(((Player) sender).getUniqueId());
		
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
				return;
			}
			
			pickedStructure.put(((Player) sender).getUniqueId(), nearbyVillage.id);
			
			String message = RegenerationSettings.MESSAGE_DELETE_NEARBY_STRUCTURE.string();
			message = message.replace("<ID>", Integer.toString(nearbyVillage.id));
			message = message.replace("<Distance>", Integer.toString(nearbyVillage.distance));
			Util.Message(message, sender);
		}
	}
}
