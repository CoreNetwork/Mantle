package us.corenetwork.mantle.treasurehunt.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import us.core_network.cornel.common.Messages;
import us.core_network.cornel.java.NumberUtil;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.treasurehunt.THuntModule;
import us.corenetwork.mantle.treasurehunt.THuntSettings;

public class BuyHuntCommand extends BaseTChaseCommand {

	public BuyHuntCommand()
	{
		permission = "buy";
		desc = "Adds one chase to callers pool, run later with /chase run";
		needPlayer = false;
	}
	
	@Override
	public void run(CommandSender sender, String[] args)
	{
		if(args.length != 1 && args.length != 2)
		{
            Messages.send("Usage: /chase buy <playerName> [<amount>]", sender);
			return;
		}
		
		int amount = 1;
		
		if(args.length == 2)
		{
			if(NumberUtil.isInteger(args[1]))
			{
				amount = Integer.parseInt(args[1]);
			}
			else
			{
                Messages.send("[<amount>] must be an integer.", sender);
				return;
			}
		}

		String playerName = args[0];
		OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(playerName);

		THuntModule.passManager.addPass(offlinePlayer, amount);
		if(offlinePlayer.isOnline())
		{
            Messages.send(THuntSettings.MESSAGE_BRC_HUNT_BOUGHT.string(), offlinePlayer.getPlayer());
		}
	}

}
