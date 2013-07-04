package com.mcnsa.flatcore.checkpoints;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.mcnsa.flatcore.FlatcoreModule;
import com.mcnsa.flatcore.MCNSAFlatcore;
import com.mcnsa.flatcore.checkpoints.admincommands.BaseCheckpointCommand;
import com.mcnsa.flatcore.checkpoints.admincommands.CheckpointHelpCommand;
import com.mcnsa.flatcore.checkpoints.admincommands.CreateCommand;
import com.mcnsa.flatcore.checkpoints.admincommands.DeleteListCommand;
import com.mcnsa.flatcore.checkpoints.admincommands.MoveCommand;
import com.mcnsa.flatcore.checkpoints.usercommands.BaseCheckpointUserCommand;
import com.mcnsa.flatcore.checkpoints.usercommands.ClearCommand;
import com.mcnsa.flatcore.checkpoints.usercommands.SaveCommand;
import com.mcnsa.flatcore.checkpoints.usercommands.TeleCommand;

public class CheckpointsModule extends FlatcoreModule {
	public static CheckpointsModule instance;
	
	public static HashMap<String, BaseCheckpointCommand> admincommands;
	public static HashMap<String, BaseCheckpointUserCommand> usercommands;

	public static HashMap<String, SavedCheckpoint> savedCheckpoints;
	public static HashMap<String, ScheduledTeleport> scheduledTeleports;

	public CheckpointsModule() {
		super("Checkpoints", new String[] {"chp", "checkpoint"}, "checkpoints");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

		if (command.getName().equals("checkpoint"))
		{
			BaseCheckpointUserCommand baseCommand = null;
			if (args.length > 0) 
				baseCommand = usercommands.get(args[0]);
			
			if (baseCommand != null)
				return baseCommand.execute(sender, args);
			
			usercommands.get("tele").execute(sender, args);
		}
		else
		{
			
			BaseCheckpointCommand baseCommand = null;
			if (args.length > 0) 
				baseCommand = admincommands.get(args[0]);
			
			if (baseCommand != null)
				return baseCommand.execute(sender, args);
			
			admincommands.get("help").execute(sender, args);
		}
		
		return false;
	}

	@Override
	protected boolean loadModule() {

		for (CheckpointsSettings setting : CheckpointsSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();
		
		admincommands = new HashMap<String, BaseCheckpointCommand>();
		admincommands.put("create", new CreateCommand());
		admincommands.put("help", new CheckpointHelpCommand());
		admincommands.put("deletelist", new DeleteListCommand());
		admincommands.put("move", new MoveCommand());

		usercommands = new HashMap<String, BaseCheckpointUserCommand>();
		usercommands.put("save", new SaveCommand());
		usercommands.put("tele", new TeleCommand());
		usercommands.put("clear", new ClearCommand());

		savedCheckpoints = new HashMap<String, SavedCheckpoint>();
		scheduledTeleports = new HashMap<String, ScheduledTeleport>();
		
		Bukkit.getScheduler().runTaskTimer(MCNSAFlatcore.instance, new Teleporter(), 20, 20);
		
		Bukkit.getServer().getPluginManager().registerEvents(new CheckpointsListener(), MCNSAFlatcore.instance);
		
		return true;
	}

	@Override
	protected void unloadModule() {
	}

	private static class Teleporter implements Runnable
	{
		@Override
		public void run() {
			if (scheduledTeleports.size() == 0)
				return;
			
			Iterator<Entry<String, ScheduledTeleport>> i = scheduledTeleports.entrySet().iterator();
			
			while (i.hasNext())
			{
				ScheduledTeleport teleport = i.next().getValue();
				if (System.currentTimeMillis() >= teleport.time)
				{
					teleport.player.teleport(teleport.location);
					i.remove();
				}
			}
		}
		
	}
}
