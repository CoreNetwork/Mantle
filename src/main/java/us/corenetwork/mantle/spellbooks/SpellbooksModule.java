package us.corenetwork.mantle.spellbooks;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.spellbooks.commands.MakeBookCommand;
import us.corenetwork.mantle.spellbooks.commands.ReslimeCommand;


public class SpellbooksModule extends MantleModule {
	public static SpellbooksModule instance;

	public SpellbooksModule() {
		super("Spellbooks", null, "spellbooks");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}

	@Override
	protected boolean loadModule() {

		for (SpellbooksSettings setting : SpellbooksSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();
		
		MantlePlugin.adminCommands.put("makebook", new MakeBookCommand());
		MantlePlugin.adminCommands.put("reslime", new ReslimeCommand())

		Bukkit.getServer().getPluginManager().registerEvents(new SpellbooksListener(), MantlePlugin.instance);
				
		SpellbookManager.init();
		ItemProtocolListener.init();
		
		return true;
	}
	
	@Override
	protected void unloadModule() {
	}
}
