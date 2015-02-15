package us.corenetwork.mantle.holograms.commands;

import org.bukkit.command.CommandSender;
import us.core_network.cornel.common.Messages;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.holograms.HologramStorage;
import us.corenetwork.mantle.holograms.HologramsSettings;


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
        Messages.send(HologramsSettings.MESSAGE_RELOADED.string(), sender);
	}
	

}
