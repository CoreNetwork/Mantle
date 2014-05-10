package us.corenetwork.mantle.hydration;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;

import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.hydration.commands.BaseHydrationCommand;
import us.corenetwork.mantle.hydration.commands.MantleHydrationCommand;
import us.corenetwork.mantle.hydration.commands.RestoreCommand;


public class HydrationModule extends MantleModule {
	public static HydrationModule instance;
	public static HashMap<String, CachedDrainConfig> drainConfigs;	
	
	public HydrationModule() {
		super("Hydration", null, "hydration");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		cacheConfigs();
	}

	private void cacheConfigs()
	{
		drainConfigs = new HashMap<String, CachedDrainConfig>();
		
		MemorySection worlds = (MemorySection) config.get("Worlds");
		if (worlds != null)
		{
			for (Entry<String, Object> e : worlds.getValues(false).entrySet())
			{
				drainConfigs.put(e.getKey(), new CachedDrainConfig((MemorySection) e.getValue()));
			}

		}
		
		MemorySection potions = (MemorySection) config.get("Potions");
		if (potions != null)
		{
			CachedPotionConfig.loadPotions(potions);
		}
		
		MemorySection notifications = (MemorySection) config.get("Notifications");
		if (potions != null)
		{
			CachedNotificationsConfig.load(notifications);
		}
	}
	
	@Override
	protected boolean loadModule() {

		for (HydrationSettings setting : HydrationSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();
				
		Bukkit.getServer().getPluginManager().registerEvents(new HydrationListener(), MantlePlugin.instance);
		
		MantlePlugin.adminCommands.put("hydration", new MantleHydrationCommand());

		cacheConfigs();
		
		Bukkit.getScheduler().runTaskTimer(MantlePlugin.instance, new HydrationTimer(), 20, 20);
	    
		return true;
	}
	@Override
	protected void unloadModule() {
		saveConfig();
	}	
}
