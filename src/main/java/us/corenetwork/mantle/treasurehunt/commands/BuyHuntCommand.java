package us.corenetwork.mantle.treasurehunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.treasurehunt.THuntModule;
import us.corenetwork.mantle.treasurehunt.THuntSettings;

public class BuyHuntCommand extends BaseMantleCommand {

	public BuyHuntCommand()
	{
		permission = "treasurechase.buy";
		desc = "Adds one chase to callers pool, run later with /chase run";
		needPlayer = false;
	}
	
	@Override
	public void run(CommandSender sender, String[] args)
	{
		if(args.length != 1 && args.length != 2)
		{
			Util.Message("Usage: /chase buy <playerName> [<amount>]", sender);
			return;
		}
		
		int amount = 1;
		
		if(args.length == 2)
		{
			if(Util.isInteger(args[1]))
			{
				amount = Integer.parseInt(args[1]);
			}
			else
			{
				Util.Message("[<amount>] must be an integer.", sender);
				return;
			}
		}
		
		String playerName = args[0]; 
		
		String path = "Amount."+playerName.toLowerCase();
		int amountLeft = THuntModule.instance.storageConfig.getInt(path);
		
		THuntModule.instance.storageConfig.set(path, amountLeft + amount);
		THuntModule.instance.saveStorageYaml();
		
		Player player = MantlePlugin.instance.getServer().getPlayer(playerName);
		if(player != null)
		{
			Util.Message(THuntSettings.MESSAGE_HUNT_BOUGHT.string(), player);
		}
	}

}
