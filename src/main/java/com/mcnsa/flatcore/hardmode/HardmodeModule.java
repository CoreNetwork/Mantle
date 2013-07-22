package com.mcnsa.flatcore.hardmode;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.mcnsa.flatcore.FlatcoreModule;
import com.mcnsa.flatcore.MCNSAFlatcore;

public class HardmodeModule extends FlatcoreModule {
	public static HardmodeModule instance;

	public HardmodeModule() {
		super("Hard mode", null, "hardmode");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}

	@Override
	protected boolean loadModule() {

		for (HardmodeSettings setting : HardmodeSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();
				
		Bukkit.getServer().getPluginManager().registerEvents(new HardmodeListener(), MCNSAFlatcore.instance);
		
		return true;
	}

	@Override
	protected void unloadModule() {
	}
}
