package us.corenetwork.mantle.armorhologram.commands;

import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.armorhologram.HologramStorage;


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
        sender.sendMessage("Reloaded. If some old holograms did not disappear, try reconnecting.");
	}
	

}
