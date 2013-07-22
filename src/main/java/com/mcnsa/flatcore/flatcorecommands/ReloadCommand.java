package com.mcnsa.flatcore.flatcorecommands;

import org.bukkit.command.CommandSender;

import com.mcnsa.flatcore.FlatcoreModule;
import com.mcnsa.flatcore.IO;
import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;

public class ReloadCommand extends BaseAdminCommand {	
	public ReloadCommand()
	{
		desc = "Reload config";
		needPlayer = false;
	}


	public Boolean run(final CommandSender sender, String[] args) {
		IO.LoadSettings();
		FlatcoreModule.reloadConfigs();
		Util.Message(Settings.getString(Setting.MESSAGE_CONFIGURATION_RELOADED), sender);
		return true;
	}	
}
