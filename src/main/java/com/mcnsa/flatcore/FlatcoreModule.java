package com.mcnsa.flatcore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.mcnsa.flatcore.checkpoints.CheckpointsModule;

public abstract class FlatcoreModule implements CommandExecutor {
	private String moduleName;

	private String configName;
	private String[] commands;

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

			boolean enabled = config.getBoolean("enabled", true);
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
				MCNSAFlatcore.instance.getCommand(command).setExecutor(this);
			}
		}

		return loadModule();
	}

	public void loadConfig()
	{
		File configFile = new File(MCNSAFlatcore.instance.getDataFolder(), configName.concat(".yml"));
		
		
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
		try
		{
			File configFile = new File(MCNSAFlatcore.instance.getDataFolder(), configName.concat(".yml"));

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
			module.saveConfig();
			module.unloadModule();
		}
	}

	public static void loadModules()
	{
		FlatcoreModule module;

		//Checkpoints
		module = new CheckpointsModule();
		if (module.loadModuleInternal())
			modules.add(module);
	}
	
	public static void reloadConfigs()
	{
		for (FlatcoreModule module : modules)
		{
			module.loadConfig();
		}
	}
}
