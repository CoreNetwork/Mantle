package us.corenetwork.mantle.spellbooks;

import net.minecraft.server.v1_7_R2.ItemPotion;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;


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
		
		MantlePlugin.adminCommands.put("bindbook", new BindBookCommand());
		
		Bukkit.getServer().getPluginManager().registerEvents(new SpellbooksListener(), MantlePlugin.instance);
		
		Bukkit.getScheduler().runTaskTimer(MantlePlugin.instance, new SpellbooksTimer(), 20, 20);
		
		SpellbookManager.init();
		ItemProtocolListener.init();
		
		return true;
	}
	
	@Override
	protected void unloadModule() {
	}
}
