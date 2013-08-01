package com.mcnsa.flatcore.checkpoints.admincommands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.Util;
import com.mcnsa.flatcore.checkpoints.CheckpointsModule;
import com.mcnsa.flatcore.checkpoints.CheckpointsSettings;

public class MoveCommand extends BaseCheckpointCommand {	
	public MoveCommand()
	{
		desc = "Move checkpoint to your position";
		permission = "move";
		needPlayer = true;
	}


	public void run(final CommandSender sender, String[] args) {
		if (args.length < 2 || !Util.isInteger(args[1]))
		{
			Util.Message("Usage: /chp move <list name> <checkpoint position>", sender);
			return;
		}
		
		String checkpointList = args[0];
		int position = Integer.parseInt(args[1]);
		
		String node = "checkpoints."  + checkpointList.toLowerCase();
		
		List<String> stringList = CheckpointsModule.instance.config.getStringList(node);
		if (stringList == null || stringList.size() == 0)
		{
			String message = CheckpointsSettings.MESSAGE_LIST_NOT_EXIST.string();
			message = message.replace("<List>", checkpointList);
			
			Util.Message(message, sender);
			return;

		}

		if (stringList.size() < position)
		{
			String message = CheckpointsSettings.MESSAGE_CHECKPOINT_NOT_EXIST.string();
			message = message.replace("<Position>", Integer.toString(position));
			message = message.replace("<List>", checkpointList);
			
			Util.Message(message, sender);
			return;
		}
		
		
		
		Player player = (Player) sender;
		
		stringList.set(position - 1, Util.serializeLocation(player.getLocation()));
		CheckpointsModule.instance.config.set(node, stringList);
		
		String message = CheckpointsSettings.MESSAGE_CHECKPOINT_MOVED.string();
		message = message.replace("<Position>", Integer.toString(position));
		message = message.replace("<List>", checkpointList);
		
		CheckpointsModule.instance.saveConfig();
		
		Util.Message(message, sender);
		return;
	}	
}
