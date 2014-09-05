package us.corenetwork.mantle.armorhologram.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.armorhologram.Hologram;
import us.corenetwork.mantle.armorhologram.HologramStorage;


public class HologramAddCommand extends BaseHologramCommand
{

	public HologramAddCommand()
	{
		permission = "add";
		desc = "Add hologram";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args) {
        if (args.length < 1)
        {
            sender.sendMessage("Usage: /mantle hologram add <text>");
            return;
        }

        String text = "";
        for (int i = 0; i < args.length; i++)
        {
            text += args[i] + " ";
        }
        text = text.substring(0, text.length() - 1);

        Player player = (Player) sender;
        Location location = player.getLocation();

        Hologram hologram = new Hologram(location.getWorld(), location.getX(), location.getY(), location.getZ(), text);
        HologramStorage.storage.add(hologram);
        hologram.displayForAll();

        HologramStorage.save();

        sender.sendMessage("Hologram added.");
	}
	

}
