package com.mcnsa.flatcore.rspawncommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
		
		boolean silent = args.length > 0 && args[0].equals("silent");
		
		boolean hasProtection = ProtectCommand.protectedPlayers.containsKey(player.getName());
		if (!hasProtection)
		{
			if (!silent) Util.Message(Settings.getString(Setting.MESSAGE_SPAWN_UNPROTECT_NOT_PROTECTED), sender);
			return true;
		}
		
		ProtectCommand.protectedPlayers.remove(player.getName());
		if (!silent) ProtectCommand.endProtectionMessage(player);
		return true;
	}	
	
}
