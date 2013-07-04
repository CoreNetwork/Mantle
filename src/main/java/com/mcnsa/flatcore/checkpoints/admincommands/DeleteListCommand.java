package com.mcnsa.flatcore.checkpoints.admincommands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.Util;
import com.mcnsa.flatcore.checkpoints.CheckpointsModule;
import com.mcnsa.flatcore.checkpoints.CheckpointsSettings;

public class DeleteListCommand extends BaseCheckpointCommand {	
	public DeleteListCommand()
	{
		desc = "Delete list of checkpoints";
		permission = "deleteList";
		needPlayer = true;
	}


	public void run(final CommandSender sender, String[] args) {
		if (args.length < 1)
		{
			Util.Message("Usage: /chp deleteList <list name>", sender);
			return;
		}
		
		String checkpointList = args[0];
		String node = "checkpoints."  + checkpointList.toLowerCase();
		
		List<String> stringList = CheckpointsModule.instance.config.getStringList(node);
		if (stringList == null || stringList.size() == 0)
		{
			String message = CheckpointsSettings.MESSAGE_LIST_NOT_EXIST.string();
			message = message.replace("<List>", checkpointList);
			
			Util.Message(message, sender);
			return;
		}
		
		CheckpointsModule.instance.config.set(node, null);
		
		String message = CheckpointsSettings.MESSAGE_LIST_DELETED.string();
		message = message.replace("<List>", checkpointList);
		
		CheckpointsModule.instance.saveConfig();
		
		Util.Message(message, sender);
		return;
	}	
}
