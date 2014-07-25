package us.corenetwork.mantle.treasurehunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.corenetwork.core.PlayerUtils;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.treasurehunt.THuntModule;

public class RunHuntCommand extends BaseMantleCommand {

	public RunHuntCommand()
	{
		permission = "runhunt";
		desc = "Adds a treasure hunt to the queue";
		needPlayer = true;
	}

	@Override
	public void run(CommandSender sender, String[] args)
	{
		Player player = (Player) sender;
		
		String path = "Amount."+player.getUniqueId().toString();
		int amountLeft = THuntModule.instance.storageConfig.getInt(path);
		
		if(amountLeft > 0)
		{
			THuntModule.instance.storageConfig.set(path, amountLeft - 1);

			THuntModule.instance.saveStorageYaml();
			THuntModule.manager.addToQueue(player.getUniqueId().toString());
		}
		else
		{
			Util.Message("You have no hunt to run!", player);
		}
	}
	
	
}
