package us.corenetwork.mantle.holograms.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.core_network.cornel.common.Messages;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.holograms.Hologram;
import us.corenetwork.mantle.holograms.HologramStorage;
import us.corenetwork.mantle.holograms.HologramsSettings;


public class HologramRemoveCommand extends BaseHologramCommand
{

	public HologramRemoveCommand()
	{
		permission = "remove";
		desc = "Remove nearest hologram";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args) {

        Player player = (Player) sender;
        Location location = player.getLocation();

        Hologram nearest = null;

        if (args.length < 1)
        {
            nearest = HologramStorage.getNearest(location);
        }
        else
        {
            nearest = HologramStorage.namedHolograms.get(args[0]);
        }

        if (nearest == null)
        {
            Messages.send
                    (HologramsSettings.MESSAGE_NO_HOLOGRAM_WITH_THAT_NAME.string(), sender);
        }
        else
        {
            nearest.delete();
            if (nearest.getName() != null)
                HologramStorage.namedHolograms.remove(nearest.getName());

            HologramStorage.save();

            sender.sendMessage("Hologram removed. If it did not disappear, try reconnecting.");
        }


	}
	

}
