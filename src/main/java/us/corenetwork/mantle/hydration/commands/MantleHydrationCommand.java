package us.corenetwork.mantle.hydration.commands;

import java.util.HashMap;
import org.bukkit.command.CommandSender;
import us.core_network.cornel.java.NumberUtil;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;


public class MantleHydrationCommand extends BaseMantleCommand {	
	
	public static HashMap<String, BaseHydrationCommand> commands = new HashMap<String, BaseHydrationCommand>();
	
	static
	{
		commands.put("help", new HydrationHelpCommand());
		commands.put("restore", new RestoreCommand());
		commands.put("set", new SetCommand());
		commands.put("load", new LoadCommand());
		commands.put("save", new SaveCommand());
	}
	
	public MantleHydrationCommand()
	{
		permission = "hydration";
		desc = "Hydration commands";
		needPlayer = false;
	}


	public void run(final CommandSender sender, String[] args) {
		if (args.length < 1 || NumberUtil.isInteger(args[0]))
		{
			commands.get("help").execute(sender, args);
			return;
		}

		BaseHydrationCommand cmd = commands.get(args[0]);
		if (cmd != null)
			cmd.execute(sender, args);
		else
			commands.get("help").execute(sender, args);
	}	
}
