package us.corenetwork.mantle.animalspawning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.animalspawning.commands.CountersCommand;


public class AnimalSpawningModule extends MantleModule {
	public static AnimalSpawningModule instance;
	
	
	public static Map<UUID, String> spawnedAnimals = new HashMap<UUID, String>();
	public static List<UUID> killedAnimals = new ArrayList<UUID>();
	
	public static Map<String, Integer> animalCounts = new HashMap<String, Integer>();
	
	public AnimalSpawningModule() {
		super("Animal spawning", null, "animalspawn");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}
	
	@Override
	protected boolean loadModule() {

		for (AnimalSpawningSettings setting : AnimalSpawningSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
			
		}
				
		saveConfig();
				
		Bukkit.getServer().getPluginManager().registerEvents(new AnimalSpawningListener(), MantlePlugin.instance);
		
		AnimalSpawningTimer.timerSingleton = new AnimalSpawningTimer();
		
		MantlePlugin.adminCommands.put("counters", new CountersCommand());
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(MantlePlugin.instance, AnimalSpawningTimer.timerSingleton, 20);
		AnimalSpawningIO.PrepareDB();
		init();
		animalCounts = AnimalSpawningIO.getAnimalCounts();
		return true;
	}
	@Override
	protected void unloadModule() {
		saveConfig();
	}	
	
	@Override
	public void loadConfig()
	{
		super.loadConfig();
		init();
	}
	
	private void init()
	{
		AnimalRange.initializeRanges();
		List<Map<?, ?>> ranges = config.getMapList(AnimalSpawningSettings.RANGES.string);
		
		for(Map<?, ?> range : ranges)
		{
			int startChunk = (Integer) range.get("StartChunk");
			int endChunk = (Integer) range.get("EndChunk");
			int weight = (Integer) range.get("Weight");
			
			AnimalRange.addRange(startChunk, endChunk, weight);
		}
	}

}
