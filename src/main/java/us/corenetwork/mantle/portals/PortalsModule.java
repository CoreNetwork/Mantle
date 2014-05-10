package us.corenetwork.mantle.portals;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;


public class PortalsModule extends MantleModule {
	public static PortalsModule instance;

	public PortalsModule() {
		super("Portals", null, "portals");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}

	@Override
	protected boolean loadModule() {

		for (PortalsSettings setting : PortalsSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();
				
		Bukkit.getServer().getPluginManager().registerEvents(new PortalsListener(), MantlePlugin.instance);
		
		return true;
	}
	@Override
	protected void unloadModule() {
	}
}
