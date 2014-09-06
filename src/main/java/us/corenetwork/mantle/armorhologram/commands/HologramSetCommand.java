package us.corenetwork.mantle.armorhologram.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.armorhologram.HologramsSettings;
import us.corenetwork.mantle.armorhologram.Hologram;
import us.corenetwork.mantle.armorhologram.HologramStorage;


public class HologramSetCommand extends BaseHologramCommand
{

	public HologramSetCommand()
	{
		permission = "set";
		desc = "Add/set hologram - PERSISTENT";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args) {
        if (args.length < 1)
        {
            sender.sendMessage("Usage: /holo id [<name>] \"<text>\"");
            return;
        }

        String name = null;
        int textStart = 0;
        if (!args[0].startsWith("\""))
        {
            textStart = 1;
            name = args[0];
        }

        String text = "";
        for (int i = textStart; i < args.length; i++)
        {
            text += args[i] + " ";
        }
        text = text.substring(0, text.length() - 1);

        //Removing quotes
        if (text.startsWith("\""))
            text = text.substring(1);
        if (text.endsWith("\""))
            text = text.substring(0, text.length() - 1);

        text = text.replace("<EMPTY>", "");

        Player player = (Player) sender;
        Location location = player.getLocation();

        Hologram hologram = null;
        if (name != null)
            hologram = HologramStorage.namedHolograms.get(name);

        if (hologram == null)
        {
            hologram = new Hologram(name, location.getWorld(), location.getX(), location.getY(), location.getZ(), text);
            HologramStorage.storage.add(hologram);
            if (name != null)
                HologramStorage.namedHolograms.put(name, hologram);
            hologram.displayForAll();

            Util.Message(HologramsSettings.MESSAGE_HOLOGRAM_ADDED.string(), sender);
        }
        else
        {
            hologram.update(text);
            Util.Message(HologramsSettings.MESSAGE_HOLOGRAM_REMOVED.string(), sender);
        }

        HologramStorage.save();
	}
	

}
