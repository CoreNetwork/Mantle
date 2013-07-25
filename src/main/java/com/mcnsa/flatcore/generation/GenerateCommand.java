package com.mcnsa.flatcore.generation;

import org.bukkit.command.CommandSender;

import com.mcnsa.flatcore.flatcorecommands.BaseAdminCommand;

public class GenerateCommand extends BaseAdminCommand {
	public GenerateCommand()
	{
		desc = "Start world generation";
		needPlayer = false;
	}


	public Boolean run(CommandSender sender, String[] args) {
		return true;
	}
}
