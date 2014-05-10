package us.corenetwork.mantle.restockablechests.commands;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.command.CommandSender;

import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.restockablechests.RChestSettings;


public class RestockAllCommand extends BaseMantleCommand {	
	public RestockAllCommand()
	{
		permission = "restockall";
		desc = "Immediately restock all chests regardless of their status";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args) {
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("UPDATE PlayerChests SET LastAccess=0");
			statement.executeUpdate();
			statement.close();
			
			IO.getConnection().commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		Util.Message(RChestSettings.MESSAGE_CHESTS_RESTOCKED.string(), sender);
	}	
}
