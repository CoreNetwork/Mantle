package us.corenetwork.mantle.nanobot.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.core_network.cornel.player.PlayerUtil;
import us.corenetwork.mantle.Util;

public abstract class NanobotBaseCommand {
	public Boolean needPlayer;
	public Boolean adminCommand;
	public String desc;
	public String permission;
	
	public abstract void run(CommandSender sender, String[] args);
	
	public Boolean execute(CommandSender sender, String[] args)
	{
		String[] newargs = new String[args.length - 1];
		for (int i = 1; i < args.length; i++)
		{
			newargs[i - 1] = args[i];
		}
		args = newargs;
		
		if (needPlayer && !(sender instanceof Player)) 
		{
			sender.sendMessage("This cannot be run from console!");
			return true;
		}
				
		if (adminCommand && sender instanceof Player && !PlayerUtil.hasPermission(sender, "mantle.nanobot.command." + permission))
		{
			((Player)sender).sendMessage("You do not have permission to do that!");
			return true;
		}
						
		run(sender, args);
		return true;
	}	
}