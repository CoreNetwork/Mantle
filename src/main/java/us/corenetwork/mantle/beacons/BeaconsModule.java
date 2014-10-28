package us.corenetwork.mantle.beacons;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;


public class BeaconsModule extends MantleModule {
	public static BeaconsModule instance;

	public BeaconsModule() {
		super("Beacons", null, "beacons");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}

	@Override
	protected boolean loadModule() {

		for (BeaconsSettings setting : BeaconsSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();
				
		Bukkit.getServer().getPluginManager().registerEvents(new BeaconsListener(), MantlePlugin.instance);
		
		return true;
	}
	@Override
	protected void unloadModule() {
	}
}
