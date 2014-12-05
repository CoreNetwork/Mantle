package us.corenetwork.mantle.animalspawning.commands;

import java.util.Map.Entry;
import org.bukkit.command.CommandSender;
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
		Util.Message("", sender);
		Util.Message("&7Animals on the server:", sender);
		Util.Message("&8----------------------", sender);
		for(Entry<String, Integer> entry : AnimalSpawningModule.animalCounts.entrySet())
		{
			String pretty = (entry.getKey().charAt(0) +"").toUpperCase()+ entry.getKey().substring(1).toLowerCase();
			Util.Message("&6"+pretty+": " + "&7"+entry.getValue(), sender);
		}
	}

	
}
