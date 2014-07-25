package us.corenetwork.mantle.treasurehunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.treasurehunt.THuntModule;

public class BuyHuntCommand extends BaseMantleCommand {

	public BuyHuntCommand()
	{
		permission = "buyhunt";
		desc = "Adds one hunt to callers pool, run later with /mantle runhunt";
		needPlayer = true;
	}
	
	@Override
	public void run(CommandSender sender, String[] args)
	{
		Player player = (Player) sender;
		
		String path = "Amount."+player.getUniqueId().toString();
		
		int amountLeft = THuntModule.instance.storageConfig.getInt(path);
		
		THuntModule.instance.storageConfig.set(path, amountLeft + 1);
		THuntModule.instance.saveStorageYaml();
		//TODO: Message?
		Util.Message("You bought one treasure hunt", player);
	}

}
