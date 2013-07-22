package com.mcnsa.flatcore.checkpoints.usercommands;

import java.util.List;

import org.bukkit.Bukkit;
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
		needPlayer = false;
	}


	public void run(final CommandSender sender, String[] args) {
		if (args.length < 3 || !Util.isInteger(args[2]))
		{
			Util.Message("Usage: /chp save <player name> <list name> <checkpoint position>", sender);
			return;
		}
		
		String playerName = args[0];
		String checkpointList = args[1];
		int position = Integer.parseInt(args[2]);
		
		Player player = Bukkit.getServer().getPlayerExact(playerName);
		if (player == null)
		{
			return; //Don't save anything if player is not online
		}
		
		String node = "checkpoints."  + checkpointList.toLowerCase();
		
		List<String> stringList = CheckpointsModule.instance.config.getStringList(node);
		if (stringList == null || stringList.size() == 0)
		{
			String message = CheckpointsSettings.MESSAGE_LIST_NOT_EXIST.string();
			message = message.replace("<List>", checkpointList);
			
			Util.Message(message, player);
			return;

		}

		if (stringList.size() < position)
		{
			String message = CheckpointsSettings.MESSAGE_CHECKPOINT_NOT_EXIST.string();
			message = message.replace("<Position>", Integer.toString(position));
			message = message.replace("<List>", checkpointList);
			
			Util.Message(message, player);
			return;
		}

		
		SavedCheckpoint lastCheckpoint = CheckpointsModule.savedCheckpoints.get(player.getName());
		if (lastCheckpoint != null && lastCheckpoint.list.equals(checkpointList) && lastCheckpoint.position < position)
		{
			String message = CheckpointsSettings.MESSAGE_CHECKPOINT_NO_BACKWARDS.string();
			message = message.replace("<Player>", player.getName());
			message = message.replace("<Position>", Integer.toString(position));
			message = message.replace("<List>", checkpointList);
			
			Util.Message(message, player);
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
		
		Util.Message(message, player);
		
		return;
	}	
}
