package us.corenetwork.mantle.armorhologram.commands;

import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.armorhologram.Hologram;
import us.corenetwork.mantle.armorhologram.HologramStorage;


public class HologramUpdateCommand extends BaseHologramCommand
{

	public HologramUpdateCommand()
	{
		permission = "update";
		desc = "Update text of the hologram - not persistent and thus faster than set";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args) {
        if (args.length < 2)
        {
            sender.sendMessage("/holo update <id> <text>");
            return;
        }

        Hologram hologram = HologramStorage.namedHolograms.get(args[0]);
        if (hologram == null)
        {
            sender.sendMessage("No hologram with such name exist!");
            return;
        }

        String text = "";
        for (int i = 1; i < args.length; i++)
        {
            text += args[i] + " ";
        }
        text = text.substring(0, text.length() - 1);

        //Removing quotes
        if (text.startsWith("\""))
            text = text.substring(1);
        if (text.endsWith("\""))
            text = text.substring(0, text.length() - 1);

        hologram.update(text);
	}
	

}
