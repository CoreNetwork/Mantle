package com.mcnsa.flatcore.regeneration;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;

import com.mcnsa.flatcore.FlatcoreModule;
import com.mcnsa.flatcore.MCNSAFlatcore;

public class RegenerationModule extends FlatcoreModule {
	public static RegenerationModule instance;
	public HashMap<String, RegStructure> structures = new HashMap<String, RegStructure>();
	
	public RegenerationModule() {
		super("Structure Regeneration", null, "regeneration");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}

	@Override
	protected boolean loadModule() {

		for (RegenerationSettings setting : RegenerationSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();
				
		Bukkit.getServer().getPluginManager().registerEvents(new RegenerationListener(), MCNSAFlatcore.instance);
		
		MCNSAFlatcore.adminCommands.put("analyze", new AnalyzeCommand());
		MCNSAFlatcore.adminCommands.put("deleteRespawn", new DeleteRespawnCommand());
		MCNSAFlatcore.adminCommands.put("testRespawn", new TestRespawnCommand());
		MCNSAFlatcore.adminCommands.put("respawn", new RespawnCommand());

		MemorySection structuresConfig = (MemorySection) config.get("Structures");
		for (Entry<String,Object> e : structuresConfig.getValues(false).entrySet())
		{
			RegStructure structure = new RegStructure(e.getKey(), (MemorySection) e.getValue());
			structures.put(e.getKey(), structure);
		}
		
		StructureChecker.schedule();
		
		return true;
	}
	@Override
	protected void unloadModule() {
	}	
}
