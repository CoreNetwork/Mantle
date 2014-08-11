package us.corenetwork.mantle.treasurehunt.commands;

import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.treasurehunt.THuntModule;
import us.corenetwork.mantle.treasurehunt.THuntSettings;

public class JoinHuntCommand extends BaseMantleCommand  {

	public JoinHuntCommand()
	{
		permission = "treasureraid.join";
		desc = "Callers joins the treasure raid";
		needPlayer = true;
	}
	
	@Override
	public void run(CommandSender sender, String[] args)
	{
		Player player = (Player) sender;

		Environment env = player.getWorld().getEnvironment(); 
		if(env == Environment.THE_END)
		{
			Util.Message(THuntSettings.MESSAGE_JOIN_IN_LIMBO.string(), player);
		}
		else if(env == Environment.NETHER)
		{
			Util.Message(THuntSettings.MESSAGE_JOIN_IN_NETHER.string(), player);
		}
		else
		{
			Util.Message(THuntSettings.MESSAGE_JOIN.string(), player);
		}
		THuntModule.manager.addPlayerToHunt(player);
	}
}
