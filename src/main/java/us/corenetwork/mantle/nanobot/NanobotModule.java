package us.corenetwork.mantle.nanobot;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GrassSpecies;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.TexturedMaterial;
import us.core_network.cornel.items.ItemStackUtils;
import us.core_network.cornel.strings.NumberParsing;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.nanobot.commands.HeadCommand;
import us.corenetwork.mantle.nanobot.commands.InfiniteCommand;
import us.corenetwork.mantle.nanobot.commands.LoadCommand;
import us.corenetwork.mantle.nanobot.commands.MakeCommand;
import us.corenetwork.mantle.nanobot.commands.NanobotBaseCommand;
import us.corenetwork.mantle.nanobot.commands.SaveCommand;


public class NanobotModule extends MantleModule {
	public static NanobotModule instance;

	private HashMap<String, NanobotBaseCommand> commands = new HashMap<String, NanobotBaseCommand>();

	public static File folder;
	
	public NanobotModule() {
		super("Nanobot", new String[] {"nbt"}, "nanobot");

		instance = this;
	}

	@Override
	protected boolean loadModule() {
		commands.put("save", new SaveCommand());
		commands.put("load", new LoadCommand());
		commands.put("make", new MakeCommand());
		commands.put("head", new HeadCommand());
		commands.put("infinite", new InfiniteCommand());

		folder = new File(MantlePlugin.instance.getDataFolder(), "nanobot");
		if (!folder.exists())
			folder.mkdir();

		return true;
	}

	@Override
	protected void unloadModule() {
	}

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (args.length < 1 || !commands.containsKey(args[0])) 
		{
			int page = 1;
			if (args.length > 0 && NumberParsing.isInteger(args[0])) page = Integer.parseInt(args[0]);
			List<String> komandes = new ArrayList<String>();
			for (Entry<String, NanobotBaseCommand> e : commands.entrySet())
			{
				if (!e.getValue().adminCommand || !(sender instanceof Player) || ((Player)sender).isOp())
				{
					komandes.add(ChatColor.COLOR_CHAR + "a/nbt " + e.getKey() + ChatColor.COLOR_CHAR + "8 - " + ChatColor.COLOR_CHAR + "f" + e.getValue().desc);
				}
			}  		
			String[] komande = komandes.toArray(new String[0]);
			Arrays.sort(komande);

			int maxpage = (int) Math.ceil((double) komande.length / (sender instanceof Player ? 15.0 : 30.0));

			if (page > maxpage)
			{
				sender.sendMessage("Nothing to see here!");
				return true;
			}

			sender.sendMessage("List of commands:");
			sender.sendMessage(ChatColor.COLOR_CHAR + "8Page " + String.valueOf(page) + " of " + String.valueOf(maxpage));

			for (int i = (page - 1) * 15; i < page * 15; i++)
			{
				if (komande.length < i + 1 || i < 0) break;	
				sender.sendMessage(komande[i]);
			}   		
			return true;
		}
		NanobotBaseCommand cmd = commands.get(args[0]);
		if (cmd != null) return cmd.execute(sender, args);
		return false;
	}
}
