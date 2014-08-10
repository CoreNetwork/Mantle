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
		permission = "buyhunt";
		desc = "Adds one hunt to callers pool, run later with /mantle runhunt";
		needPlayer = false;
	}
	
	@Override
	public void run(CommandSender sender, String[] args)
	{
		if(args.length != 1)
		{
			Util.Message("Usage: /mantle buyhunt <playerName>", sender);
			return;
		}
		String playerName = args[0]; 
		
		String path = "Amount."+playerName.toLowerCase();
		int amountLeft = THuntModule.instance.storageConfig.getInt(path);
		
		THuntModule.instance.storageConfig.set(path, amountLeft + 1);
		THuntModule.instance.saveStorageYaml();
		
		Player player = MantlePlugin.instance.getServer().getPlayer(playerName);
		if(player != null)
		{
			Util.Message(THuntSettings.MESSAGE_HUNT_BOUGHT.string(), player);
		}
	}

}
