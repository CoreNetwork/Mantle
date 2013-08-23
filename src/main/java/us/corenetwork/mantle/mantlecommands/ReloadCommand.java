package us.corenetwork.mantle.mantlecommands;

import org.bukkit.command.CommandSender;

import us.corenetwork.mantle.FlatcoreModule;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.Setting;
import us.corenetwork.mantle.Settings;
import us.corenetwork.mantle.Util;


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
