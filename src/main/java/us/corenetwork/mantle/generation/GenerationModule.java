package us.corenetwork.mantle.generation;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;


public class GenerationModule extends MantleModule {
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
		
		MantlePlugin.adminCommands.put("generate", new GenerateCommand());
		MantlePlugin.adminCommands.put("generatepath", new GeneratePathCommand());

		MapColors.getColor(1);
		
		return true;
	}	

	@Override
	protected void unloadModule() {
	}
}
