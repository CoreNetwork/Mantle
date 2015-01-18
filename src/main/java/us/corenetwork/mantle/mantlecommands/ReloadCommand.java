package us.corenetwork.mantle.mantlecommands;

import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.Setting;
import us.corenetwork.mantle.Settings;
import us.corenetwork.mantle.Util;


public class ReloadCommand extends BaseMantleCommand {	
	public ReloadCommand()
	{
		permission = "reload";
		desc = "Reload config";
		needPlayer = false;
	}


	public void run(final CommandSender sender, String[] args) {
		IO.LoadSettings();
		MantleModule.reloadConfigs();
		Util.Message(Settings.getString(Setting.MESSAGE_CONFIGURATION_RELOADED), sender);
	}	
}
