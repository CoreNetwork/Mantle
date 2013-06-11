package com.mcnsa.flatcore.rspawncommands;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.GriefPreventionHandler;
import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;

public class UnprotectCommand extends BaseRSpawnCommand {	

	public UnprotectCommand()
	{
		needPlayer = true;
		permission = "unprotect";
	}

	public Boolean run(final CommandSender sender, String[] args) {
		Player player = (Player) sender;
		
		boolean hasProtection = ProtectCommand.protectedPlayers.containsKey(player.getName());
		if (!hasProtection)
		{
			Util.Message(Settings.getString(Setting.MESSAGE_SPAWN_UNPROTECT_NOT_PROTECTED), sender);
			return true;
		}
		
		ProtectCommand.protectedPlayers.remove(player.getName());
		ProtectCommand.endProtection(player);
		return true;
	}	
	
}
