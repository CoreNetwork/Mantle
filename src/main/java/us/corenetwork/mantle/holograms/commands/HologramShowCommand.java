package us.corenetwork.mantle.holograms.commands;

import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.holograms.HologramsSettings;
import us.corenetwork.mantle.holograms.Hologram;
import us.corenetwork.mantle.holograms.HologramStorage;


public class HologramShowCommand extends BaseHologramCommand
{

	public HologramShowCommand()
	{
		permission = "show";
		desc = "Show hologram";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args) {
        if (args.length < 1)
        {
            sender.sendMessage("/holo show <id> [p - persistent]");
            return;
        }

        Hologram hologram = HologramStorage.namedHolograms.get(args[0]);
        if (hologram == null)
        {
            Util.Message(HologramsSettings.MESSAGE_NO_HOLOGRAM_WITH_THAT_NAME.string(), sender);
            return;
        }

        boolean persistent = args.length > 1;
        hologram.setHidden(false, persistent);
	}
	

}
