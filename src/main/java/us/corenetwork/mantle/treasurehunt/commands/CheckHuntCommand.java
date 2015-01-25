package us.corenetwork.mantle.treasurehunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.treasurehunt.THuntModule;
import us.corenetwork.mantle.treasurehunt.THuntSettings;

public class CheckHuntCommand extends BaseTChaseCommand  {

	public CheckHuntCommand()
	{
		permission = "check";
		desc = "Returns amount of treasure chases owned by caller.";
		needPlayer = true;
	}
	
	@Override
	public void run(CommandSender sender, String[] args)
	{
		Player player = (Player) sender;
		int amountLeft = THuntModule.passManager.getAmount(player);
		Util.Message(THuntSettings.MESSAGE_BRC_HUNTS_LEFT.string().replace("<Amount>", amountLeft +""), player);
	}
}
