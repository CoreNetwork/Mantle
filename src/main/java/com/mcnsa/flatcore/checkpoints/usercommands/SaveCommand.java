package com.mcnsa.flatcore.checkpoints.usercommands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.Util;
import com.mcnsa.flatcore.checkpoints.CheckpointsModule;
import com.mcnsa.flatcore.checkpoints.CheckpointsSettings;
import com.mcnsa.flatcore.checkpoints.SavedCheckpoint;

public class SaveCommand extends BaseCheckpointUserCommand {	
	public SaveCommand()
	{
		permission = "save";
		needPlayer = true;
	}


	public void run(final CommandSender sender, String[] args) {
		if (args.length < 2 || !Util.isInteger(args[1]))
		{
			Util.Message("Usage: /chp save <list name> <checkpoint position>", sender);
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
		
		SavedCheckpoint lastCheckpoint = CheckpointsModule.savedCheckpoints.get(player.getName());
		if (lastCheckpoint != null && lastCheckpoint.list == checkpointList && lastCheckpoint.position < position)
		{
			String message = CheckpointsSettings.MESSAGE_CHECKPOINT_NO_BACKWARDS.string();
			message = message.replace("<Player>", player.getName());
			message = message.replace("<Position>", Integer.toString(position));
			message = message.replace("<List>", checkpointList);
			
			Util.Message(message, sender);
			return;
		}
		
		lastCheckpoint = new SavedCheckpoint();
		lastCheckpoint.list = checkpointList;
		lastCheckpoint.position = position;
		lastCheckpoint.location = Util.unserializeLocation(stringList.get(position - 1));
		
		CheckpointsModule.savedCheckpoints.put(player.getName(), lastCheckpoint);
		
		
		String message = CheckpointsSettings.MESSAGE_CHECKPOINT_SAVED.string();
		message = message.replace("<Player>", player.getName());
		message = message.replace("<Position>", Integer.toString(position));
		message = message.replace("<List>", checkpointList);
		
		Util.Message(message, sender);
		
		return;
	}	
}
