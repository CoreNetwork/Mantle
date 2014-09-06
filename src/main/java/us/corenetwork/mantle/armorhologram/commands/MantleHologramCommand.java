package us.corenetwork.mantle.armorhologram.commands;

import java.util.HashMap;
import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;


public class MantleHologramCommand extends BaseMantleCommand {
	
	public static HashMap<String, BaseHologramCommand> commands = new HashMap<String, BaseHologramCommand>();

	static
	{
		commands.put("help", new HologramHelpCommand());
        commands.put("reload", new HologramReloadCommand());
        commands.put("set", new HologramSetCommand());
        commands.put("remove", new HologramRemoveCommand());
        commands.put("update", new HologramUpdateCommand());
        commands.put("updateline", new HologramUpdateLineCommand());

    }
	
	public MantleHologramCommand()
	{
		permission = "armorhologram";
		desc = "Hologram commands";
		needPlayer = false;
	}


	public void run(final CommandSender sender, String[] args) {
		if (args.length < 1 || Util.isInteger(args[0]))
		{
			commands.get("help").execute(sender, args);
			return;
		}

		BaseHologramCommand cmd = commands.get(args[0]);
		if (cmd != null)
			cmd.execute(sender, args);
		else
			commands.get("help").execute(sender, args);
	}	
}
