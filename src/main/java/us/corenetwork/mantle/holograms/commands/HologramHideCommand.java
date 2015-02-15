package us.corenetwork.mantle.holograms.commands;

import org.bukkit.command.CommandSender;
import us.core_network.cornel.common.Messages;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.holograms.Hologram;
import us.corenetwork.mantle.holograms.HologramStorage;
import us.corenetwork.mantle.holograms.HologramsSettings;


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
            sender.sendMessage("/holo hide <id>");
            return;
        }

        Hologram hologram = HologramStorage.namedHolograms.get(args[0]);
        if (hologram == null)
        {
            Messages.send(HologramsSettings.MESSAGE_NO_HOLOGRAM_WITH_THAT_NAME.string(), sender);
            return;
        }

        boolean persistent = args.length > 1;
        hologram.setHidden(true);
	}
	

}
