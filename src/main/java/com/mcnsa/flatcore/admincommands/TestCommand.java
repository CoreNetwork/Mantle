package com.mcnsa.flatcore.admincommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.Util;

public class TestCommand extends BaseAdminCommand {	
	public TestCommand()
	{
		desc = "Analyze all villages for claim status";
		needPlayer = true;
	}


	public Boolean run(final CommandSender sender, String[] args) {
		Player player = (Player) sender;
		player.sendMessage(Boolean.toString(Util.isNetherFortress(player.getLocation())));
		
		return true;
	}	
}
