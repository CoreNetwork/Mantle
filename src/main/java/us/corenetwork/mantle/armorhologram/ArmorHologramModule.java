package us.corenetwork.mantle.armorhologram;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.armorhologram.commands.MantleHologramCommand;
import us.corenetwork.mantle.hydration.CachedDrainConfig;


public class ArmorHologramModule extends MantleModule {
	public static ArmorHologramModule instance;
	public static HashMap<String, CachedDrainConfig> drainConfigs;

	public ArmorHologramModule() {
		super("Armor Hologram", null, "armorhologram");

		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}


	@Override
	protected boolean loadModule() {

		for (ArmorHologramSettings setting : ArmorHologramSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();
				
		Bukkit.getServer().getPluginManager().registerEvents(new ArmorHologramListener(), MantlePlugin.instance);
		
		MantlePlugin.adminCommands.put("hologram", new MantleHologramCommand());

        HologramStorage.load();

		return true;
	}
	@Override
	protected void unloadModule() {
		saveConfig();
	}	
}
