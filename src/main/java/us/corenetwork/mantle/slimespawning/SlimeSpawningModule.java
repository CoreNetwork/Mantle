package us.corenetwork.mantle.slimespawning;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;


public class SlimeSpawningModule extends MantleModule {
	public static SlimeSpawningModule instance;
	
	public SlimeSpawningModule() {
		super("Slime spawning", null, "slimespawn");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}
	
	@Override
	protected boolean loadModule() {

		for (SlimeSpawningSettings setting : SlimeSpawningSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
				
		saveConfig();
				
		Bukkit.getServer().getPluginManager().registerEvents(new SlimeSpawningHelper(), MantlePlugin.instance);
		
		SlimeSpawningTimer.timerSingleton = new SlimeSpawningTimer();
		SlimeKillTimer.timerSingleton = new SlimeKillTimer();

		IgnoredSlimeChunks.load();
		
		Bukkit.getScheduler().runTaskTimer(MantlePlugin.instance, SlimeSpawningTimer.timerSingleton, 20, SlimeSpawningSettings.SPAWNING_INTERVAL_TICKS.integer());
		Bukkit.getScheduler().runTaskTimer(MantlePlugin.instance, SlimeKillTimer.timerSingleton, 20, 5 * 20);

		return true;
	}
	@Override
	protected void unloadModule() {
		saveConfig();
	}
}
