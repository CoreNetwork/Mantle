package us.corenetwork.mantle.treasurehunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.treasurehunt.THuntModule;

public class LeaveHuntCommand extends BaseMantleCommand  {

	public LeaveHuntCommand()
	{
		permission = "treasurechase.leave";
		desc = "Callers leaves the treasure chase";
		needPlayer = true;
	}
	
	@Override
	public void run(CommandSender sender, String[] args)
	{
		Player player = (Player) sender;
		boolean silent = false;
		if(args.length == 1 && args[0].toLowerCase().equals("silent"))
		{
			silent = true;
		}
		
		THuntModule.manager.removePlayerFromHunt(player, silent);
	}
}