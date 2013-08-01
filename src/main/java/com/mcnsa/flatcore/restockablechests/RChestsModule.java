package com.mcnsa.flatcore.restockablechests;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.mcnsa.flatcore.FlatcoreModule;
import com.mcnsa.flatcore.MCNSAFlatcore;
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
		Bukkit.getServer().getPluginManager().registerEvents(new RChestsListener(), MCNSAFlatcore.instance);
		
		MCNSAFlatcore.adminCommands.put("createchest", new CreateChestCommand());
		MCNSAFlatcore.adminCommands.put("restockall", new RestockAllCommand());

		for (RChestSettings setting : RChestSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();
		
		return true;
	}

	@Override
	protected void unloadModule() {
	}
}
