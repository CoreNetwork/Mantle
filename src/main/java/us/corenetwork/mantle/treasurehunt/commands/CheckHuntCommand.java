package us.corenetwork.mantle.treasurehunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.treasurehunt.THuntModule;
import us.corenetwork.mantle.treasurehunt.THuntSettings;

public class CheckHuntCommand extends BaseMantleCommand  {

	public CheckHuntCommand()
	{
		permission = "treasureraid.check";
		desc = "Returns amount of treasure hunts owned by caller.";
		needPlayer = true;
	}
	
	@Override
	public void run(CommandSender sender, String[] args)
	{
		Player player = (Player) sender;
		
		String path = "Amount."+player.getName().toLowerCase();
		int amountLeft = THuntModule.instance.storageConfig.getInt(path);

		Util.Message(THuntSettings.MESSAGE_HUNTS_LEFT.string().replace("<Amount>", amountLeft +""), player);
	}
}
