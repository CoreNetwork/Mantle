package com.mcnsa.flatcore.checkpoints.usercommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.checkpoints.CheckpointsModule;

public class ClearCommand extends BaseCheckpointUserCommand {	
	public ClearCommand()
	{
		permission = "clear";
		needPlayer = true;
	}


	public void run(final CommandSender sender, String[] args) {		
		final Player player = (Player) sender;
		CheckpointsModule.savedCheckpoints.remove(player.getName());
		return;
	}	
}
