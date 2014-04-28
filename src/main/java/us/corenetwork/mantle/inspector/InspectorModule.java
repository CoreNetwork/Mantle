package us.corenetwork.mantle.inspector;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.regeneration.RegenerationModule;


public class InspectorModule extends MantleModule {
	public static InspectorModule instance;
	
	public InspectorModule() {
		super("Structure Inspector", new String[] { "inspect" }, "inspector");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		InspectCommand.command(sender, command, commandLabel, args);
		return true;
	}

	@Override
	protected boolean loadModule() {

		if (!RegenerationModule.instance.active)
		{
			MLog.warning("Cannot use Inspector module without regeneration module! Skipping...");
			return false;
		}
		
		for (InspectorSettings setting : InspectorSettings.values())
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
