package com.mcnsa.flatcore.admincommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.IO;
import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;

public class ReloadCommand extends BaseAdminCommand {	
	public ReloadCommand()
	{
		desc = "Reload config";
		needPlayer = true;
	}


	public Boolean run(final CommandSender sender, String[] args) {
		IO.LoadSettings();
		Util.Message(Settings.getString(Setting.MESSAGE_CONFIGURATION_RELOADED), sender);
		return true;
	}	
}
