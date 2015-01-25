package us.corenetwork.mantle.treasurehunt.commands;

import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.treasurehunt.THuntModule;
import us.corenetwork.mantle.treasurehunt.THuntSettings;

public class JoinHuntCommand extends BaseTChaseCommand  {

	public JoinHuntCommand()
	{
		permission = "join";
		desc = "Callers joins the treasure chase";
		needPlayer = true;
	}
	
	@Override
	public void run(CommandSender sender, String[] args)
	{
		Player player = (Player) sender;

		if(!THuntModule.manager.isQueued() && !THuntModule.manager.isRunning())
			{
				Util.Message(THuntSettings.MESSAGE_STATUS_NO_HUNT_SCHEDULED.string(), player);
				return;
		}

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
			if(THuntModule.manager.isTakingPart(player))
			{
				Util.Message(THuntSettings.MESSAGE_JOIN_ALREADY_IN.string(), player);
			}
			else
			{
				if (THuntModule.manager.isQueued())
				{
					Util.Message(THuntSettings.MESSAGE_JOIN_QUEUED.string().replace("<Time>", THuntModule.manager.getTimeToStartNextHunt() + ""), player);
				}
				else
				{
					Util.Message(THuntSettings.MESSAGE_JOIN_RUNNING.string(), player);
					THuntModule.manager.messageAboutCurrentWave(player);
				}
				THuntModule.manager.addPlayerToHunt(player);
			}
		}
	}
}
