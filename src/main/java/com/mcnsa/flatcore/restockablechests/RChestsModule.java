package com.mcnsa.flatcore.restockablechests;

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
import com.mcnsa.flatcore.restockablechests.commands.CreateChestCommand;
import com.mcnsa.flatcore.restockablechests.commands.RestockAllCommand;

public class RChestsModule extends FlatcoreModule {
	public static RChestsModule instance;
	
	public RChestsModule() {
		super("Restockable chests", null, "rchests");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

		
		return false;
	}

	@Override
	protected boolean loadModule() {

		for (RChestsSettings setting : RChestsSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();
				
		
		Bukkit.getServer().getPluginManager().registerEvents(new RChestsListener(), MCNSAFlatcore.instance);
		
		MCNSAFlatcore.adminCommands.put("createchest", new CreateChestCommand());
		MCNSAFlatcore.adminCommands.put("restockall", new RestockAllCommand());

		
		return true;
	}

	@Override
	protected void unloadModule() {
	}
}
