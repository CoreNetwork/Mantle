package us.corenetwork.mantle.treasurehunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.treasurehunt.THuntModule;
import us.corenetwork.mantle.treasurehunt.THuntSettings;

public class RunHuntCommand extends BaseTChaseCommand {

	public RunHuntCommand()
	{
		permission = "run";
		desc = "Adds a treasure chase to the queue";
		needPlayer = true;
	}

	@Override
	public void run(CommandSender sender, String[] args)
	{
		Player player = (Player) sender;

		if(THuntModule.passManager.getAmount(player) <= 0)
		{
			Util.Message(THuntSettings.MESSAGE_BRC_NO_HUNT.string(), player);
		}
		else
		{
			THuntModule.passManager.removePass(player);
			THuntModule.passManager.runPass(player);
		}
	}
	
	
}
