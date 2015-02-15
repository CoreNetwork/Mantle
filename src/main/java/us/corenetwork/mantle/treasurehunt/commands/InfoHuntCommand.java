package us.corenetwork.mantle.treasurehunt.commands;

import org.bukkit.command.CommandSender;
import us.core_network.cornel.common.Messages;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.treasurehunt.THuntModule;
import us.corenetwork.mantle.treasurehunt.THuntSettings;

/**
 * Created by Ginaf on 2015-01-25.
 */
public class InfoHuntCommand  extends BaseTChaseCommand {

    public InfoHuntCommand()
    {
        permission = "info";
        desc = "Returns info about chase status.";
        needPlayer = false;
    }

    @Override
    public void run(CommandSender sender, String[] args)
    {
        if(THuntModule.manager.isRunning())
        {
            Messages.send(THuntSettings.MESSAGE_STATUS_HUNT_RUNNING.string().replace("<Wave>", THuntModule.manager.getActiveWave() + ""), sender);
        }
        else if(THuntModule.manager.isQueued())
        {
            Messages.send(THuntSettings.MESSAGE_STATUS_TIME_LEFT_TO_START.string().replace("<Time>", THuntModule.manager.getTimeToStartNextHunt() + ""), sender);
        }
        else
        {
            Messages.send(THuntSettings.MESSAGE_STATUS_NO_HUNT_SCHEDULED.string(), sender);
        }
    }
}
