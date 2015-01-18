package us.corenetwork.mantle.generation;

import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;


public class GeneratePathCommand extends BaseMantleCommand {
	public GeneratePathCommand()
	{
		permission = "generatepath";
		desc = "Start path generation";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args) {
		
		Util.Message(GenerationSettings.MESSAGE_SERVER_FROZEN.string(), sender);
		
		if (args.length < 1)
		{
			sender.sendMessage("Usage: /flatcore generatepath [Path Name]");
			return;
		}
		
		new PathGenerator().generatePath(args[0]);
	}
}
