package us.corenetwork.mantle.restockablechests.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.restockablechests.CompassDestination;

/**
 * Created by Ginaf on 2015-02-22.
 */
public class UntrackCompassCommand extends BaseMantleCommand {
    public UntrackCompassCommand()
    {
        permission = "untrack";
        desc = "Untrack the compass category";
        needPlayer = true;
    }

    public void run(CommandSender sender, String[] args)
    {
        Player player = (Player) sender;

        CompassDestination.destinations.remove(player.getUniqueId());
        CompassDestination.resetCompassTarget(player);
    }
}
