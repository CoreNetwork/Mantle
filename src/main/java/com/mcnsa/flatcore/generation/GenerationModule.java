package com.mcnsa.flatcore.generation;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.mcnsa.flatcore.FlatcoreModule;
import com.mcnsa.flatcore.MCNSAFlatcore;

public class GenerationModule extends FlatcoreModule {
	public static GenerationModule instance;

	public GenerationModule() {
		super("Generation", null, "generation");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}

	@Override
	protected boolean loadModule() {	
		for (GenerationSettings setting : GenerationSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();
		
		MCNSAFlatcore.adminCommands.put("generate", new GenerateCommand());
		MCNSAFlatcore.adminCommands.put("generatePath", new GeneratePathCommand());

		MapColors.getColor(1);
		
		return true;
	}	

	@Override
	protected void unloadModule() {
	}
}
