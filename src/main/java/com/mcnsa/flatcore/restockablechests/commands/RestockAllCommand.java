package com.mcnsa.flatcore.restockablechests.commands;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.command.CommandSender;

import com.mcnsa.flatcore.IO;
import com.mcnsa.flatcore.Util;
import com.mcnsa.flatcore.flatcorecommands.BaseAdminCommand;
import com.mcnsa.flatcore.restockablechests.RChestSettings;

public class RestockAllCommand extends BaseAdminCommand {	
	public RestockAllCommand()
	{
		desc = "Immediately restock all chests regardless of their status";
		needPlayer = false;
	}


	public Boolean run(CommandSender sender, String[] args) {
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
		return true;
	}	
}
