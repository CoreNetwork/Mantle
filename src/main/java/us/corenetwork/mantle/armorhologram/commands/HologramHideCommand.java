package us.corenetwork.mantle.armorhologram.commands;

import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.armorhologram.HologramsSettings;
import us.corenetwork.mantle.armorhologram.Hologram;
import us.corenetwork.mantle.armorhologram.HologramStorage;


public class HologramHideCommand extends BaseHologramCommand
{

	public HologramHideCommand()
	{
		permission = "hide";
		desc = "Hide hologram";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args) {
        if (args.length < 1)
        {
            sender.sendMessage("/holo hide <id> [p - persistent]");
            return;
        }

        Hologram hologram = HologramStorage.namedHolograms.get(args[0]);
        if (hologram == null)
        {
            Util.Message(HologramsSettings.MESSAGE_NO_HOLOGRAM_WITH_THAT_NAME.string(), sender);
            return;
        }

        boolean persistent = args.length > 1;
        hologram.setHidden(true, persistent);
	}
	

}
