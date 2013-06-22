package com.mcnsa.flatcore.rspawncommands;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.mcnsa.flatcore.MCNSAFlatcore;
import com.mcnsa.flatcore.Util;

public class NoDropCommand extends BaseRSpawnCommand {	
	public static HashSet<String> blockedPlayers = new HashSet<String>();

	public NoDropCommand()
	{
		needPlayer = false;
		permission = "nodrop";
	}

	public Boolean run(final CommandSender sender, String[] args) {
		if (args.length < 2 || !Util.isInteger(args[1]))
			return true;

		final String playerName = args[0];
		int protectionLength = Integer.parseInt(args[1]);

		blockedPlayers.add(playerName);

		Bukkit.getScheduler().runTaskLater(MCNSAFlatcore.instance, new Runnable() {

			@Override
			public void run() {
				blockedPlayers.remove(playerName);
			}

		}, protectionLength);

		return true;
	}	

}
