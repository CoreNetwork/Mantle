package us.corenetwork.mantle.holograms.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.core_network.cornel.common.Messages;
import us.core_network.cornel.strings.NumberParsing;
import us.corenetwork.mantle.Settings;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.holograms.HologramsModule;


public class HologramHelpCommand extends BaseHologramCommand
{
	
	public HologramHelpCommand()
	{
		permission = "help";
		desc = "List all possible commands";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args) {		
		int page = 1;
		if (args.length > 0 && NumberParsing.isInteger(args[0])) page = Integer.parseInt(args[0]);
		List<String> komandes = new ArrayList<String>();

		for (Entry<String, BaseHologramCommand> e : HologramsModule.commands.entrySet())
		{
			komandes.add(Settings.getCommandDescription(e.getKey(), "holo", e.getValue().desc));
		}  		
		String[] komande = komandes.toArray(new String[0]);
		Arrays.sort(komande);
		
		int maxpage = (int) Math.ceil((double) komande.length / (sender instanceof Player ? 15.0 : 30.0));
		
		if (page > maxpage)
			page = maxpage;

        Messages.send("List of all commands:", sender);
        Messages.send("&8Page " + String.valueOf(page) + " of " + String.valueOf(maxpage), sender);

		for (int i = (page - 1) * 15; i < page * 15; i++)
		{
			if (komande.length < i + 1 || i < 0) break;
            Messages.send(komande[i], sender);
		}   		
	}
	

}
