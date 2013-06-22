package com.mcnsa.flatcore;

import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.mcnsa.flatcore.flatcorecommands.AdminHelpCommand;
import com.mcnsa.flatcore.flatcorecommands.AnalyzeCommand;
import com.mcnsa.flatcore.flatcorecommands.BaseAdminCommand;
import com.mcnsa.flatcore.flatcorecommands.CreateChestCommand;
import com.mcnsa.flatcore.flatcorecommands.DeleteVillageCommand;
import com.mcnsa.flatcore.flatcorecommands.FindAdminVillageCommand;
import com.mcnsa.flatcore.flatcorecommands.GenerateMoreVillagesCommand;
import com.mcnsa.flatcore.flatcorecommands.InitVillagesCommand;
import com.mcnsa.flatcore.flatcorecommands.ReloadCommand;
import com.mcnsa.flatcore.flatcorecommands.RestockAllCommand;
import com.mcnsa.flatcore.flatcorecommands.TestVillageCommand;
import com.mcnsa.flatcore.rspawncommands.BaseRSpawnCommand;
import com.mcnsa.flatcore.rspawncommands.NoDropCommand;
import com.mcnsa.flatcore.rspawncommands.ProtectCommand;
import com.mcnsa.flatcore.rspawncommands.ProtectCommand.ProtectTimer;
import com.mcnsa.flatcore.rspawncommands.RSpawnCommand;
import com.mcnsa.flatcore.rspawncommands.ToggleCommand;
import com.mcnsa.flatcore.rspawncommands.UnprotectCommand;

public class MCNSAFlatcore extends JavaPlugin {
	public static Logger log = Logger.getLogger("Minecraft");

	private FlatcoreListener listener;

	public static MCNSAFlatcore instance;

	public static Plugin permissions = null;

	public static HashMap<String, BaseAdminCommand> adminCommands = new HashMap<String, BaseAdminCommand>();
	public static HashMap<String, BaseRSpawnCommand> rspawnCommands = new HashMap<String, BaseRSpawnCommand>();

	public static Random random;
	
	@Override
	public void onDisable() {
		IO.freeConnection();
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
		
		adminCommands.put("init", new InitVillagesCommand());
		adminCommands.put("analyze", new AnalyzeCommand());
		adminCommands.put("deletevillage", new DeleteVillageCommand());
		adminCommands.put("findadminclaims", new FindAdminVillageCommand());
		adminCommands.put("testvillage", new TestVillageCommand());
		adminCommands.put("generatemorevillages", new GenerateMoreVillagesCommand());

		adminCommands.put("createchest", new CreateChestCommand());
		adminCommands.put("restockall", new RestockAllCommand());
		
		adminCommands.put("reload", new ReloadCommand());
		
		
		rspawnCommands.put("rspawn", new RSpawnCommand());
		rspawnCommands.put("toggle", new ToggleCommand());
		rspawnCommands.put("protect", new ProtectCommand());
		rspawnCommands.put("unprotect", new UnprotectCommand());
		rspawnCommands.put("nodrop", new NoDropCommand());

		VillageChecker.schedule();
		getServer().getScheduler().runTaskTimer(this, new ProtectTimer(), 20, 20);
		
		log.info("[MCNSAFlatcore] " + getDescription().getFullName() + " loaded!");
	}



	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (command.getName().equals("flatcore"))
		{
			if (args.length < 1 || Util.isInteger(args[0]))
				return adminCommands.get("help").execute(sender, args);

			BaseAdminCommand cmd = adminCommands.get(args[0]);
			if (cmd != null)
				return cmd.execute(sender, args);
			else
				return adminCommands.get("help").execute(sender, args);
		}
		else if (command.getName().equals("togglespawn"))
		{
			return rspawnCommands.get("toggle").execute(sender, args);
		}
		else if (command.getName().equals("unprotect"))
		{
			return rspawnCommands.get("unprotect").execute(sender, args);
		}
		else
		{
			if (args.length < 1 || Util.isInteger(args[0]))
				return rspawnCommands.get("rspawn").execute(sender, args);

			BaseRSpawnCommand cmd = rspawnCommands.get(args[0]);
			if (cmd != null)
				return cmd.execute(sender, args);
			else
				return false;
		}
	}

}
