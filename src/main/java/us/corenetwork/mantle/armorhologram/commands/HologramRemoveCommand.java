package us.corenetwork.mantle.armorhologram.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.armorhologram.Hologram;
import us.corenetwork.mantle.armorhologram.HologramStorage;


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
            sender.sendMessage("No hologram found!");
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
