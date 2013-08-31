package us.corenetwork.mantle;

import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import us.corenetwork.mantle.animalspawning.AnimalSpawningModule;
import us.corenetwork.mantle.generation.GenerationModule;
import us.corenetwork.mantle.hardmode.HardmodeModule;
import us.corenetwork.mantle.hydration.HydrationModule;
import us.corenetwork.mantle.netherspawning.NetherSpawningModule;
import us.corenetwork.mantle.portals.PortalsModule;
import us.corenetwork.mantle.regeneration.RegenerationModule;
import us.corenetwork.mantle.restockablechests.RChestsModule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public abstract class FlatcoreModule implements CommandExecutor {
	private String moduleName;

	private String configName;
	private String[] commands;

	public boolean active = false;

	public YamlConfiguration config;

	protected FlatcoreModule(String name, String[] commands, String configName)
	{
		this.moduleName = name;
		this.configName = configName;
		this.commands = commands;
	}

	protected abstract boolean loadModule();
	protected abstract void unloadModule();

	private boolean loadModuleInternal()
	{
		FCLog.info("Loading module " + moduleName + "....");

		if (configName != null)
		{
			loadConfig();

			Boolean enabled = (Boolean) config.get("enabled");
			if (enabled == null)
			{
				config.set("enabled", true);
				saveConfig();

				enabled = true;
			}

			if (!enabled)
			{
				FCLog.info("Module disabled. Skipping.");
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
				FCLog.severe("Error while loading conifg for module " + moduleName + ".");

				e.printStackTrace();
				return;
			} catch (IOException e) {
				FCLog.severe("Error while loading conifg for module " + moduleName + ".");

				e.printStackTrace();
				return;
			} catch (InvalidConfigurationException e) {
				FCLog.severe("Error while loading conifg for module " + moduleName + ".");

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


	//Module manager

	private static List<FlatcoreModule> modules = new ArrayList<FlatcoreModule>();

	public static void unloadAll()
	{
		for (FlatcoreModule module : modules)
		{
			module.unloadModule();
			module.active = false;
		}
	}

	public static void loadModules()
	{
		FlatcoreModule module;

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

	}

	public static void reloadConfigs()
	{
		for (FlatcoreModule module : modules)
		{
			module.loadConfig();
		}
	}
}
