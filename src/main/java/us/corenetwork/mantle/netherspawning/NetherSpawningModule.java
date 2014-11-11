package us.corenetwork.mantle.netherspawning;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.EntityType;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;


public class NetherSpawningModule extends MantleModule {
	public static NetherSpawningModule instance;
	
	public NetherSpawningModule() {
		super("Nether spawning", null, "netherspawn");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}
	
	@Override
	protected boolean loadModule() {

		for (NetherSpawningSettings setting : NetherSpawningSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
				
		saveConfig();
				
		Bukkit.getServer().getPluginManager().registerEvents(new NetherSpawningHelper(), MantlePlugin.instance);

        if (NetherSpawningSettings.WITHER_SKELETON_SPAWNING_INTERVAL_TICKS.integer() > 0)
		    Bukkit.getScheduler().runTaskTimer(MantlePlugin.instance, new NetherSpawningTimer(EntityType.SKELETON), 20, NetherSpawningSettings.WITHER_SKELETON_SPAWNING_INTERVAL_TICKS.integer());
        if (NetherSpawningSettings.BLAZE_SPAWNING_INTERVAL_TICKS.integer() > 0)
            Bukkit.getScheduler().runTaskTimer(MantlePlugin.instance, new NetherSpawningTimer(EntityType.BLAZE), 20, NetherSpawningSettings.BLAZE_SPAWNING_INTERVAL_TICKS.integer());
        if (NetherSpawningSettings.GHAST_SPAWNING_INTERVAL_TICKS.integer() > 0)
            Bukkit.getScheduler().runTaskTimer(MantlePlugin.instance, new GhastSpawningTimer(), 20, NetherSpawningSettings.GHAST_SPAWNING_INTERVAL_TICKS.integer());

		return true;
	}
	@Override
	protected void unloadModule() {
		saveConfig();
	}
}
