package com.mcnsa.flatcore.generation;

import org.bukkit.command.CommandSender;

import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;
import com.mcnsa.flatcore.flatcorecommands.BaseAdminCommand;

public class GeneratePathCommand extends BaseAdminCommand {
	public GeneratePathCommand()
	{
		desc = "Start path generation";
		needPlayer = false;
	}


	public Boolean run(CommandSender sender, String[] args) {
		
		Util.Message(Settings.getString(Setting.MESSAGE_SERVER_FROZEN), sender);
		
		if (args.length < 1)
		{
			sender.sendMessage("Usage: /flatcore generatepath [Path Name]");
			return true;
		}
		
		new PathGenerator().generatePath(args[0]);
		
		return true;
	}
}
