package us.corenetwork.mantle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import us.corenetwork.mantle.animalspawning.AnimalSpawningModule;
import us.corenetwork.mantle.armorhologram.HologramsModule;
import us.corenetwork.mantle.beacons.BeaconsModule;
import us.corenetwork.mantle.gametweaks.GameTweaksModule;
import us.corenetwork.mantle.generation.GenerationModule;
import us.corenetwork.mantle.hardmode.HardmodeModule;
import us.corenetwork.mantle.hydration.HydrationModule;
import us.corenetwork.mantle.inspector.InspectorModule;
import us.corenetwork.mantle.nanobot.NanobotModule;
import us.corenetwork.mantle.netherspawning.NetherSpawningModule;
import us.corenetwork.mantle.portals.PortalsModule;
import us.corenetwork.mantle.regeneration.RegenerationModule;
import us.corenetwork.mantle.restockablechests.RChestsModule;
import us.corenetwork.mantle.slimespawning.SlimeSpawningModule;
import us.corenetwork.mantle.spellbooks.SpellbooksModule;
import us.corenetwork.mantle.treasurehunt.THuntModule;


public abstract class MantleModule implements CommandExecutor {
	private String moduleName;

	private String configName;
	private String[] commands;

	public boolean active = false;

	public YamlConfiguration config;
	public YamlConfiguration storageConfig;

	protected MantleModule(String name, String[] commands, String configName)
	{
		this.moduleName = name;
		this.configName = configName;
		this.commands = commands;
	}

	protected abstract boolean loadModule();
	protected abstract void unloadModule();

	private boolean loadModuleInternal()
	{
		MLog.info("Loading module " + moduleName + "....");

		if (configName != null)
		{
			loadConfig();

			Boolean enabled = (Boolean) config.get("Enabled");
			if (enabled == null)
			{
				config.set("Enabled", true);
				saveConfig();

				enabled = true;
			}

			if (!enabled)
			{
				MLog.info("Module disabled. Skipping.");
				return false;
			}

		}

		if (commands != null)
		{
			for (String command : commands)
			{
				MantlePlugin.instance.getCommand(command).setExecutor(this);
			}
		}

		return loadModule();
	}

	public void loadConfig()
	{
		File configFile = new File(MantlePlugin.instance.getDataFolder(), configName.concat(".yml"));


		config = new YamlConfiguration();

		if (configFile.exists())
		{
			try {
				config.load(configFile);
			} catch (FileNotFoundException e) {
				MLog.severe("Error while loading conifg for module " + moduleName + ".");

				e.printStackTrace();
				return;
			} catch (IOException e) {
				MLog.severe("Error while loading conifg for module " + moduleName + ".");

				e.printStackTrace();
				return;
			} catch (InvalidConfigurationException e) {
				MLog.severe("Error while loading conifg for module " + moduleName + ".");

				e.printStackTrace();
				return;
			}
		}
	}

	public void saveConfig()
	{
		if (config == null)
			return;

		try
		{
			File configFile = new File(MantlePlugin.instance.getDataFolder(), configName.concat(".yml"));

			config.save(configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void loadStorageYaml()
	{
		File storageFolder = new File(MantlePlugin.instance.getDataFolder(), "storage");
		if (!storageFolder.exists())
			storageFolder.mkdir();

		File configFile = new File(storageFolder, configName.concat(".yml"));

		storageConfig = new YamlConfiguration();

		if (configFile.exists())
		{
			try {
				storageConfig.load(configFile);
			} catch (FileNotFoundException e) {
				MLog.severe("Error while loading storage for module " + moduleName + ".");

				e.printStackTrace();
				return;
			} catch (IOException e) {
				MLog.severe("Error while loading storage for module " + moduleName + ".");

				e.printStackTrace();
				return;
			} catch (InvalidConfigurationException e) {
				MLog.severe("Error while loading storage for module " + moduleName + ".");

				e.printStackTrace();
				return;
			}
		}
	}

	public void saveStorageYaml()
	{
		if (storageConfig == null)
			return;

		try
		{
			File storageFolder = new File(MantlePlugin.instance.getDataFolder(), "storage");			
			File configFile = new File(storageFolder, configName.concat(".yml"));

			storageConfig.save(configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	//Module manager

	private static List<MantleModule> modules = new ArrayList<MantleModule>();

	public static void unloadAll()
	{
		for (MantleModule module : modules)
		{
			module.unloadModule();
			module.active = false;
		}
	}

	public static void loadModules()
	{
		MantleModule module;

		//Hard mode
		module = new HardmodeModule();
		if (module.loadModuleInternal())
		{
			module.active = true;
			modules.add(module);
		}

		//Generation
		module = new GenerationModule();
		if (module.loadModuleInternal())
		{
			module.active = true;
			modules.add(module);
		}

		//Restockable chests
		module = new RChestsModule();
		if (module.loadModuleInternal())
		{
			module.active = true;
			modules.add(module);
		}

		//Portals
		module = new PortalsModule();
		if (module.loadModuleInternal())
		{
			module.active = true;
			modules.add(module);
		}

		//Regeneration
		module = new RegenerationModule();
		if (module.loadModuleInternal())
		{
			module.active = true;
			modules.add(module);
		}	

		//Regeneration
		module = new HydrationModule();
		if (module.loadModuleInternal())
		{
			module.active = true;
			modules.add(module);
		}		

		//Animal spawning
		module = new AnimalSpawningModule();
		if (module.loadModuleInternal())
		{
			module.active = true;
			modules.add(module);
		}

		//Nether spawning
		module = new NetherSpawningModule();
		if (module.loadModuleInternal())
		{
			module.active = true;
			modules.add(module);
		}

		//Spellbooks
		module = new SpellbooksModule();
		if (module.loadModuleInternal())
		{
			module.active = true;
			modules.add(module);
		}

		//Nanobot
		module = new NanobotModule();
		if (module.loadModuleInternal())
		{
			module.active = true;
			modules.add(module);
		}        

		//Game Tweaks
		module = new GameTweaksModule();
		if (module.loadModuleInternal())
		{
			module.active = true;
			modules.add(module);
		}        

		//Slime spawning
		module = new SlimeSpawningModule();
		if (module.loadModuleInternal())
		{
			module.active = true;
			modules.add(module);
		}

		//Inspector
		module = new InspectorModule();
		if (module.loadModuleInternal())
		{
			module.active = true;
			modules.add(module);
		}        

		//Treasure hunt
		module = new THuntModule();
		if (module.loadModuleInternal())
		{
			module.active = true;
			modules.add(module);
		}

        //Armor Stand Holograms
        module = new HologramsModule();
        if (module.loadModuleInternal())
        {
            module.active = true;
            modules.add(module);
        }

        //Beacons
        module = new BeaconsModule();
        if (module.loadModuleInternal())
        {
            module.active = true;
            modules.add(module);
        }
    }

	public static void reloadConfigs()
	{
		for (MantleModule module : modules)
		{
			module.loadConfig();
		}
	}
}
