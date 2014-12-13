package us.corenetwork.mantle.holograms.commands;

import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.holograms.HologramsSettings;
import us.corenetwork.mantle.holograms.HologramStorage;


public class HologramReloadCommand extends BaseHologramCommand
{

	public HologramReloadCommand()
	{
		permission = "reload";
		desc = "Reload all holograms";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args) {
        HologramStorage.load();
        Util.Message(HologramsSettings.MESSAGE_RELOADED.string(), sender);
	}
	

}
