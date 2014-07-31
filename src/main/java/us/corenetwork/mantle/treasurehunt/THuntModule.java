package us.corenetwork.mantle.treasurehunt;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.treasurehunt.commands.BuyHuntCommand;
import us.corenetwork.mantle.treasurehunt.commands.RunHuntCommand;

public class THuntModule extends MantleModule {

	public static THuntModule instance;
	public static THuntManager manager;
	
	public THuntModule()
	{
		super("Treasure Hunt", null, "treasureraid");
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3)
	{
		return false;
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
		
		MantlePlugin.adminCommands.put("runhunt", new RunHuntCommand());
		MantlePlugin.adminCommands.put("buyhunt", new BuyHuntCommand());

		manager = new THuntManager();
		
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
