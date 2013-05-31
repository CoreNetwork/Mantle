package com.mcnsa.flatcore.rspawncommands;

import java.util.HashSet;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;

public class ToggleCommand extends BaseRSpawnCommand {	
	public static HashSet<String> ignoredPlayers = new HashSet<String>();
	
	public ToggleCommand()
	{
		desc = "Random teleport";
		needPlayer = true;
		permission = "toggle";
	}

	public Boolean run(final CommandSender sender, String[] args) {
		String playerName = ((Player) sender).getName();
		
		if (ignoredPlayers.contains(playerName))
		{
			ignoredPlayers.remove(playerName);
			Util.Message(Settings.getString(Setting.MESSAGE_SPAWN_UNIGNORED), sender);
		}
		else
		{
			ignoredPlayers.add(playerName);
			Util.Message(Settings.getString(Setting.MESSAGE_SPAWN_IGNORED), sender);
		}
		
		return true;
	}	
}
