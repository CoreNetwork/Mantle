package com.mcnsa.flatcore.checkpoints.usercommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.MCNSAFlatcore;
import com.mcnsa.flatcore.Util;
import com.mcnsa.flatcore.checkpoints.CheckpointsModule;
import com.mcnsa.flatcore.checkpoints.CheckpointsSettings;
import com.mcnsa.flatcore.checkpoints.SavedCheckpoint;
import com.mcnsa.flatcore.checkpoints.ScheduledTeleport;

public class TeleCommand extends BaseCheckpointUserCommand {	
	public TeleCommand()
	{
		permission = "tele";
		needPlayer = true;
	}


	public void run(final CommandSender sender, String[] args) {		
		final Player player = (Player) sender;
		SavedCheckpoint lastCheckpoint = CheckpointsModule.savedCheckpoints.get(player.getName());
		if (lastCheckpoint == null)
		{
			String message = CheckpointsSettings.MESSAGE_NOTHING_SAVED.string();
			message = message.replace("<Player>", player.getName());
			
			Util.Message(message, sender);
			return;
		}
		
		int delay = CheckpointsSettings.TELEPORT_DELAY.integer();
		
		String message = CheckpointsSettings.MESSAGE_TELEPORT_SCHEDULED.string();
		message = message.replace("<Player>", player.getName());
		
		Util.Message(message, sender);

		final ScheduledTeleport schedule = new ScheduledTeleport();
		schedule.player = player;
		schedule.location = lastCheckpoint.location;
		schedule.time = System.currentTimeMillis() + delay * 1000 - 1000;
		
		Bukkit.getScheduler().runTaskLater(MCNSAFlatcore.instance, new Runnable() {
			@Override
			public void run() {
				CheckpointsModule.scheduledTeleports.put(player.getName(), schedule);
			}
		}, 20);
		
		
		
		return;
	}	
}
