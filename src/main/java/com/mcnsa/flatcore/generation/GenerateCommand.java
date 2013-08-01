package com.mcnsa.flatcore.generation;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;
import com.mcnsa.flatcore.flatcorecommands.BaseAdminCommand;

public class GenerateCommand extends BaseAdminCommand {
	public GenerateCommand()
	{
		desc = "Start world generation";
		needPlayer = false;
	}


	public Boolean run(CommandSender sender, String[] args) {
		
		Util.Message(GenerationSettings.MESSAGE_SERVER_FROZEN.string(), sender);

		if (args.length > 0)
		{
			World world = Bukkit.getWorld(args[0]);
			if (world == null)
			{
				sender.sendMessage("World does not exist! Generation aborted.");
				return true;
			}
			
			StructureGenerator.generateWorld(world);
		}
		else
			StructureGenerator.generateAllWorlds();
		
		return true;
	}
}
