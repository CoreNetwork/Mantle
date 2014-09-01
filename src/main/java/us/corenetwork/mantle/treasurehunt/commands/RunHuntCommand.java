package us.corenetwork.mantle.treasurehunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.treasurehunt.THuntModule;
import us.corenetwork.mantle.treasurehunt.THuntSettings;

public class RunHuntCommand extends BaseMantleCommand {

	public RunHuntCommand()
	{
		permission = "treasureraid.run";
		desc = "Adds a treasure hunt to the queue";
		needPlayer = true;
	}

	@Override
	public void run(CommandSender sender, String[] args)
	{
		Player player = (Player) sender;
		
		String path = "Amount."+player.getName().toLowerCase();
		int amountLeft = THuntModule.instance.storageConfig.getInt(path);
		
		if(amountLeft > 0)
		{
			THuntModule.instance.storageConfig.set(path, amountLeft - 1);

			THuntModule.instance.saveStorageYaml();
			THuntModule.manager.addToQueue(player.getName());
			THuntModule.manager.addPlayerToHunt(player);
		}
		else
		{
			Util.Message(THuntSettings.MESSAGE_NO_HUNT.string(), player);
		}
	}
	
	
}
