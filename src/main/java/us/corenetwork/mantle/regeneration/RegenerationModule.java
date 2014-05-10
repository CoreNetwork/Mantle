package us.corenetwork.mantle.regeneration;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;

import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;


public class RegenerationModule extends MantleModule {
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
				
		Bukkit.getServer().getPluginManager().registerEvents(new RegenerationListener(), MantlePlugin.instance);
		
		MantlePlugin.adminCommands.put("analyze", new AnalyzeCommand());
		MantlePlugin.adminCommands.put("deleterespawn", new DeleteRespawnCommand());
		MantlePlugin.adminCommands.put("testrespawn", new TestRespawnCommand());
		MantlePlugin.adminCommands.put("respawn", new RespawnCommand());

		MemorySection structuresConfig = (MemorySection) config.get("Structures");
		if (structuresConfig != null)
		{
			for (Entry<String,Object> e : structuresConfig.getValues(false).entrySet())
			{
				RegStructure structure = new RegStructure(e.getKey(), (MemorySection) e.getValue());
				structures.put(e.getKey(), structure);
			}
		}
		
		StructureChecker.schedule();
		
		return true;
	}
	@Override
	protected void unloadModule() {
	}	
}
