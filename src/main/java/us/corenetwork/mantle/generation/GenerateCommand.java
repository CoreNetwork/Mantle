package us.corenetwork.mantle.generation;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import us.core_network.cornel.common.Messages;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;


public class GenerateCommand extends BaseMantleCommand {
	public GenerateCommand()
	{
		permission = "generate";
		desc = "Start world generation";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args) {

        Messages.send(GenerationSettings.MESSAGE_SERVER_FROZEN.string(), sender);

		if (args.length > 0)
		{
			World world = Bukkit.getWorld(args[0]);
			if (world == null)
			{
				sender.sendMessage("World does not exist! Generation aborted.");
				return;
			}
			
			StructureGenerator.generateWorld(world);
		}
		else
			StructureGenerator.generateAllWorlds();
	}
}
