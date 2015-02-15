package us.corenetwork.mantle.animalspawning.commands;

import java.util.Map.Entry;
import org.bukkit.command.CommandSender;
import us.core_network.cornel.common.Messages;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.animalspawning.AnimalSpawningModule;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;

public class CountersCommand extends BaseMantleCommand {

	public CountersCommand()
	{
		permission = "counters";
		desc = "Print animal counters";
		needPlayer = false;
	}




	@Override
	public void run(CommandSender sender, String[] args)
	{
        Messages.send("", sender);
        Messages.send("&7Animals on the server:", sender);
        Messages.send("&8----------------------", sender);
		for(Entry<String, Integer> entry : AnimalSpawningModule.animalCounts.entrySet())
		{
			String pretty = (entry.getKey().charAt(0) +"").toUpperCase()+ entry.getKey().substring(1).toLowerCase();
            Messages.send("&6" + pretty + ": " + "&7" + entry.getValue(), sender);
		}
	}

	
}
