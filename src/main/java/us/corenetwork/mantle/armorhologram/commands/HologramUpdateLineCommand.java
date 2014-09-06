package us.corenetwork.mantle.armorhologram.commands;

import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.armorhologram.HologramsSettings;
import us.corenetwork.mantle.armorhologram.Hologram;
import us.corenetwork.mantle.armorhologram.HologramStorage;


public class HologramUpdateLineCommand extends BaseHologramCommand
{

	public HologramUpdateLineCommand()
	{
		permission = "update";
		desc = "Update single line of the hologram - NOT PERSISTENT";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args) {
        if (args.length < 3 || !Util.isInteger(args[1]))
        {
            sender.sendMessage("/holo update <id> <line> <text>");
            return;
        }

        Hologram hologram = HologramStorage.namedHolograms.get(args[0]);
        if (hologram == null)
        {
            Util.Message(HologramsSettings.MESSAGE_NO_HOLOGRAM_WITH_THAT_NAME.string(), sender);
            return;
        }

        int line = Integer.parseInt(args[1]);

        String text = "";
        for (int i = 2; i < args.length; i++)
        {
            text += args[i] + " ";
        }
        text = text.substring(0, text.length() - 1);

        //Removing quotes
        if (text.startsWith("\""))
            text = text.substring(1);
        if (text.endsWith("\""))
            text = text.substring(0, text.length() - 1);

        hologram.updateLine(line - 1, text);
	}
	

}
