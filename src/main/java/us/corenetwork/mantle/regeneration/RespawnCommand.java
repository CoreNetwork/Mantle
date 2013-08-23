package us.corenetwork.mantle.regeneration;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseAdminCommand;


public class RespawnCommand extends BaseAdminCommand {

	public RespawnCommand()
	{
		desc = "Respawn nearest respawning structure";
		needPlayer = true;
	}


	public Boolean run(CommandSender sender, String[] args) {
		StructureData nearbyVillage = RegenerationUtil.pickNearestStructure(((Player) sender).getLocation());
		if (nearbyVillage == null)
		{
			Util.Message(RegenerationSettings.MESSAGE_NO_STRUCTURES.string(), sender);
			return true;
		}

		RegenerationUtil.regenerateStructure(nearbyVillage.id);

		String message = RegenerationSettings.MESSAGE_RESPAWNED.string();
		message = message.replace("<Distance>", Integer.toString(nearbyVillage.distance));
		Util.Message(message, sender);
		return true;

	}
}
