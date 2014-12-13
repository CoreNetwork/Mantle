package us.corenetwork.mantle.holograms.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.holograms.HologramsSettings;
import us.corenetwork.mantle.holograms.Hologram;
import us.corenetwork.mantle.holograms.HologramStorage;


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
        double nearestDistance = Double.MAX_VALUE;

        if (args.length < 1)
        {
            for (Hologram hologram : HologramStorage.storage)
            {
                Location hologramLocation = new Location(location.getWorld(), hologram.getX(), hologram.getY(), hologram.getZ());
                double distance = hologramLocation.distanceSquared(location);
                if (distance < nearestDistance)
                {
                    nearestDistance = distance;
                    nearest = hologram;
                }
            }
        }
        else
        {
            nearest = HologramStorage.namedHolograms.get(args[0]);
        }

        if (nearest == null)
        {
            Util.Message(HologramsSettings.MESSAGE_NO_HOLOGRAM_WITH_THAT_NAME.string(), sender);
        }
        else
        {
            nearest.removeForAll();

            HologramStorage.storage.remove(nearest);
            if (nearest.getName() != null)
                HologramStorage.namedHolograms.remove(nearest.getName());

            HologramStorage.save();

            sender.sendMessage("Hologram removed. If it did not disappear, try reconnecting.");
        }


	}
	

}
