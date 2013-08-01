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
		MCNSAFlatcore.adminCommands.put("generate", new GenerateCommand());
		MCNSAFlatcore.adminCommands.put("generatePath", new GeneratePathCommand());

		
		return true;
	}	

	@Override
	protected void unloadModule() {
	}
}
