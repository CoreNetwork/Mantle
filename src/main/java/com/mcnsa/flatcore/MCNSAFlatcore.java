package com.mcnsa.flatcore;

import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.mcnsa.flatcore.flatcorecommands.AdminHelpCommand;
import com.mcnsa.flatcore.flatcorecommands.BaseAdminCommand;
import com.mcnsa.flatcore.flatcorecommands.ReloadCommand;

public class MCNSAFlatcore extends JavaPlugin {
	public static Logger log = Logger.getLogger("Minecraft");

	private FlatcoreListener listener;

	public static MCNSAFlatcore instance;

	public static Plugin permissions = null;

	public static HashMap<String, BaseAdminCommand> adminCommands = new HashMap<String, BaseAdminCommand>();

	public static Random random;

	@Override
	public void onDisable() {
		IO.freeConnection();
		FlatcoreModule.unloadAll();
	}

	@Override
	public void onEnable() {
		instance = this;
		listener = new FlatcoreListener();
		random = new Random();

		IO.LoadSettings();
		IO.PrepareDB();

		getServer().getPluginManager().registerEvents(listener, this);

		//Admin commands
		adminCommands.put("help", new AdminHelpCommand());		
		adminCommands.put("reload", new ReloadCommand());

		FlatcoreModule.loadModules();

		log.info("[MCNSAFlatcore] " + getDescription().getFullName() + " loaded!");
	}



	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (command.getName().equals("flatcore"))
			if (args.length < 1 || Util.isInteger(args[0]))
				return adminCommands.get("help").execute(sender, args);

		BaseAdminCommand cmd = adminCommands.get(args[0]);
		if (cmd != null)
			return cmd.execute(sender, args);
		else
			return adminCommands.get("help").execute(sender, args);
	}

}
