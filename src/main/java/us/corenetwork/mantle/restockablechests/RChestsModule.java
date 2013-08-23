package us.corenetwork.mantle.restockablechests;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import us.corenetwork.mantle.FlatcoreModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.restockablechests.commands.CreateChestCommand;
import us.corenetwork.mantle.restockablechests.commands.RestockAllCommand;


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
		Bukkit.getServer().getPluginManager().registerEvents(new RChestsListener(), MantlePlugin.instance);
		
		MantlePlugin.adminCommands.put("createchest", new CreateChestCommand());
		MantlePlugin.adminCommands.put("restockall", new RestockAllCommand());

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
