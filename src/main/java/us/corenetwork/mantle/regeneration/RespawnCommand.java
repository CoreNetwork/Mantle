package us.corenetwork.mantle.regeneration;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;


public class RespawnCommand extends BaseMantleCommand {

	public RespawnCommand()
	{
		permission = "respawn";
		desc = "Respawn nearest respawning structure";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args) {
		StructureData nearbyVillage = RegenerationUtil.pickNearestStructure(((Player) sender).getLocation());
		if (nearbyVillage == null)
		{
			Util.Message(RegenerationSettings.MESSAGE_NO_STRUCTURES.string(), sender);
			return;
		}

		RegenerationUtil.regenerateStructure(nearbyVillage.id);

		String message = RegenerationSettings.MESSAGE_RESPAWNED.string();
		message = message.replace("<Distance>", Integer.toString(nearbyVillage.distance));
		Util.Message(message, sender);
	}
}
