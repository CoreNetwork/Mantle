package us.corenetwork.mantle;

import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import net.minecraft.server.v1_8_R3.EntityChicken;
import net.minecraft.server.v1_8_R3.EntityCow;
import net.minecraft.server.v1_8_R3.EntityPig;
import net.minecraft.server.v1_8_R3.EntityPigZombie;
import net.minecraft.server.v1_8_R3.EntityRabbit;
import net.minecraft.server.v1_8_R3.EntitySheep;
import net.minecraft.server.v1_8_R3.EntitySkeleton;
import net.minecraft.server.v1_8_R3.EntityWitch;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import us.corenetwork.mantle.beacons.CustomBeaconTileEntity;
import us.corenetwork.mantle.hardmode.CustomPigman;
import us.corenetwork.mantle.hardmode.CustomSkeleton;
import us.corenetwork.mantle.hardmode.CustomWitch;
import us.corenetwork.mantle.hardmode.animals.CustomChicken;
import us.corenetwork.mantle.hardmode.animals.CustomCow;
import us.corenetwork.mantle.hardmode.animals.CustomPig;
import us.corenetwork.mantle.hardmode.animals.CustomRabbit;
import us.corenetwork.mantle.hardmode.animals.CustomSheep;
import us.corenetwork.mantle.mantlecommands.AdminHelpCommand;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.mantlecommands.ChunkInfoCommand;
import us.corenetwork.mantle.mantlecommands.DebugCommand;
import us.corenetwork.mantle.mantlecommands.TabReloadCommand;
import us.corenetwork.mantle.mantlecommands.ReloadCommand;
import us.corenetwork.mantle.util.VanillaReplacingUtil;


public class MantlePlugin extends JavaPlugin {
	public static Logger log = Logger.getLogger("Minecraft");

	private MantleListener listener;

	public static MantlePlugin instance;

	public static Permission permission;
	public static Chat chat;

	public static HashMap<String, BaseMantleCommand> adminCommands = new HashMap<String, BaseMantleCommand>();

	public static Random random;

	@Override
	public void onDisable() {
		MantleModule.unloadAll();
        IO.freeConnection();
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
		adminCommands.put("chunkinfo", new ChunkInfoCommand());
		adminCommands.put("tabreload", new TabReloadCommand());
		adminCommands.put("debug", new DebugCommand());

        Bukkit.getScheduler().runTask(this, new Runnable()
        {
            @Override
            public void run()
            {
                onEnablePostWorld();
            }
        });

        CustomBeaconTileEntity.inject();
		VanillaReplacingUtil.replaceMob("PigZombie", 57, EntityPigZombie.class, CustomPigman.class);
        VanillaReplacingUtil.replaceMob("Skeleton", 51, EntitySkeleton.class, CustomSkeleton.class);
        VanillaReplacingUtil.replaceMob("Witch", 66, EntityWitch.class, CustomWitch.class);

        VanillaReplacingUtil.replaceMob("Chicken", 93, EntityChicken.class, CustomChicken.class);
        VanillaReplacingUtil.replaceMob("Cow", 92, EntityCow.class, CustomCow.class);
        VanillaReplacingUtil.replaceMob("Pig", 90, EntityPig.class, CustomPig.class);
        VanillaReplacingUtil.replaceMob("Sheep", 91, EntitySheep.class, CustomSheep.class);
        VanillaReplacingUtil.replaceMob("Rabbit", 101, EntityRabbit.class, CustomRabbit.class);

        if (!setupPermissions())
		{
			getLogger().warning("could not load Vault permissions - did you forget to install Vault?");
		}
		if (!setupChat())
		{
			getLogger().warning("could not load Vault chat - did you forget to install Vault?");
		}

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

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return (permission != null);
	}

	private boolean setupChat()
	{
		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
		if (chatProvider != null) {
			chat = chatProvider.getProvider();
		}

		return (chat != null);
	}


}
