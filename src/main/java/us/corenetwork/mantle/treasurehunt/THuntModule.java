package us.corenetwork.mantle.treasurehunt;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.core_network.cornel.java.NumberUtil;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.slimeballs.commands.SlimeballsHelpCommand;
import us.corenetwork.mantle.treasurehunt.commands.BaseTChaseCommand;
import us.corenetwork.mantle.treasurehunt.commands.BuyHuntCommand;
import us.corenetwork.mantle.treasurehunt.commands.CheckHuntCommand;
import us.corenetwork.mantle.treasurehunt.commands.HelpHuntCommand;
import us.corenetwork.mantle.treasurehunt.commands.InfoHuntCommand;
import us.corenetwork.mantle.treasurehunt.commands.JoinHuntCommand;
import us.corenetwork.mantle.treasurehunt.commands.LeaveHuntCommand;
import us.corenetwork.mantle.treasurehunt.commands.RunHuntCommand;

public class THuntModule extends MantleModule {

	public static THuntModule instance;
	public static THuntManager manager;
	public static THuntPassManager passManager;
	

	public static HashMap<String, BaseTChaseCommand> commands;
	
	public THuntModule()
	{
		super("Treasure Chase", new String[] {"chase"}, "treasurechase");
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
	{
		if (args.length < 1 || NumberUtil.isInteger(args[0]))
			return commands.get("help").execute(sender, args);

		BaseTChaseCommand cmd = commands.get(args[0]);
		if (cmd != null)
			return cmd.execute(sender, args);
		else
			return commands.get("help").execute(sender, args);
		
	}

	@Override
	protected boolean loadModule()
	{

		for (THuntSettings setting : THuntSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();
		commands = new HashMap<String, BaseTChaseCommand>();

		commands.put("help", new HelpHuntCommand());
		commands.put("run", new RunHuntCommand());
		commands.put("buy", new BuyHuntCommand());
		commands.put("check", new CheckHuntCommand());
		commands.put("join", new JoinHuntCommand());
		commands.put("leave", new LeaveHuntCommand());
		commands.put("info", new InfoHuntCommand());

		manager = new THuntManager();
		passManager = new THuntPassManager();
		
		Bukkit.getServer().getPluginManager().registerEvents(new THuntListener(), MantlePlugin.instance);
		
		MantlePlugin.instance.getServer().getScheduler().scheduleSyncRepeatingTask(MantlePlugin.instance, new THuntTimer(), 0, 200);
		
		return true;
	}

	@Override
	protected void unloadModule()
	{
		manager.save();
	}

}
