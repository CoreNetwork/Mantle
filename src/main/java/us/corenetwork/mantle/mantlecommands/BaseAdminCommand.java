package us.corenetwork.mantle.mantlecommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.Setting;
import us.corenetwork.mantle.Settings;
import us.corenetwork.mantle.Util;


public abstract class BaseAdminCommand {
	public Boolean needPlayer;
	public String desc;

	public abstract Boolean run(CommandSender sender, String[] args);
	
	public Boolean execute(CommandSender sender, String[] args)
	{
		if (args.length > 0 && !Util.isInteger(args[0]))
		{
			String[] newargs = new String[args.length - 1];
			for (int i = 1; i < args.length; i++)
			{
				newargs[i - 1] = args[i];
			}
			args = newargs;			
		}

		if (!(sender instanceof Player) && needPlayer) 
		{
		Util.Message("Sorry, but you need to execute this command as player.", sender);
			return false;
		}
		if (sender instanceof Player && !Util.hasPermission(sender,"mcnsaflatcore.command.cha")) 
		{
			Util.Message(Settings.getString(Setting.MESSAGE_NO_PERMISSION), sender);
			return false;
		}
		
		return run(sender, args);
	}

}