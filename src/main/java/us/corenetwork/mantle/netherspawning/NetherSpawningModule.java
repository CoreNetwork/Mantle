package us.corenetwork.mantle.netherspawning;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;


public class NetherSpawningModule extends MantleModule {
	public static NetherSpawningModule instance;
	
	public NetherSpawningModule() {
		super("Nether spawning", null, "netherspawn");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}
	
	@Override
	protected boolean loadModule() {

		for (NetherSpawningSettings setting : NetherSpawningSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
				
		saveConfig();
				
		Bukkit.getServer().getPluginManager().registerEvents(new NetherSpawningListener(), MantlePlugin.instance);
		return true;
	}
	@Override
	protected void unloadModule() {
		saveConfig();
	}
}
