package us.corenetwork.mantle;

import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import us.corenetwork.mantle.beacons.CustomBeaconTileEntity;
import us.corenetwork.mantle.mantlecommands.AdminHelpCommand;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.mantlecommands.ChunkInfoCommand;
import us.corenetwork.mantle.mantlecommands.DumpChunksCommand;
import us.corenetwork.mantle.mantlecommands.ReloadCommand;


public class MantlePlugin extends JavaPlugin {
	public static Logger log = Logger.getLogger("Minecraft");

	private MantleListener listener;

	public static MantlePlugin instance;

	public static Plugin permissions = null;

	public static HashMap<String, BaseMantleCommand> adminCommands = new HashMap<String, BaseMantleCommand>();

	public static Random random;

	@Override
	public void onDisable() {
		IO.freeConnection();
		MantleModule.unloadAll();
	}

    /*
        onEnable now loads BEFORE world loads. Most modules rely on world already lodaded on startup, so we should init them later.
     */
	@Override
	public void onEnable() {
		instance = this;
		listener = new MantleListener();
		random = new Random();

		IO.LoadSettings();
		IO.PrepareDB();

		getServer().getPluginManager().registerEvents(listener, this);

		//Admin commands
		adminCommands.put("help", new AdminHelpCommand());		
		adminCommands.put("reload", new ReloadCommand());
		adminCommands.put("dumpchunks", new DumpChunksCommand());
		adminCommands.put("chunkinfo", new ChunkInfoCommand());

        Bukkit.getScheduler().runTask(this, new Runnable()
        {
            @Override
            public void run()
            {
                onEnablePostWorld();
            }
        });

        CustomBeaconTileEntity.inject();

		log.info("[Mantle] " + getDescription().getFullName() + " initialized!");
	}

    /*
        onEnable event that trigger after worlds has been loaded.
     */
    public void onEnablePostWorld()
    {
        MantleModule.loadModules();
        log.info("[Mantle] " + getDescription().getFullName() + " fully loaded!");
    }



	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (command.getName().equals("title"))
            return adminCommands.get("title").execute(sender, args, false);

        if (args.length < 1 || Util.isInteger(args[0]))
			return adminCommands.get("help").execute(sender, args);

		BaseMantleCommand cmd = adminCommands.get(args[0]);
		if (cmd != null)
			return cmd.execute(sender, args);
		else
			return adminCommands.get("help").execute(sender, args);
	}

}
