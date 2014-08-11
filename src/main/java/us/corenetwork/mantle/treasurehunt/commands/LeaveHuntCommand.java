package us.corenetwork.mantle.treasurehunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.treasurehunt.THuntModule;

public class LeaveHuntCommand extends BaseMantleCommand  {

	public LeaveHuntCommand()
	{
		permission = "treasureraid.leave";
		desc = "Callers leaves the treasure raid";
		needPlayer = true;
	}
	
	@Override
	public void run(CommandSender sender, String[] args)
	{
		Player player = (Player) sender;
		THuntModule.manager.removePlayerFromHunt(player);
	}
}