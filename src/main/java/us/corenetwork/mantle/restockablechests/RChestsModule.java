package us.corenetwork.mantle.restockablechests;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.restockablechests.commands.CreateChestCommand;
import us.corenetwork.mantle.restockablechests.commands.RestockAllCommand;


public class RChestsModule extends MantleModule {
	public static RChestsModule instance;
	public static List<Category> basicCategories;
	public static List<Category> rareCategories;
	
	
	public RChestsModule() {
		super("Restockable chests", null, "rchests");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

		
		return false;
	}

	@Override
	protected boolean loadModule() {				
		Bukkit.getServer().getPluginManager().registerEvents(new RChestsListener(), MantlePlugin.instance);
		
		MantlePlugin.adminCommands.put("createchest", new CreateChestCommand());
		MantlePlugin.adminCommands.put("restockall", new RestockAllCommand());

		for (RChestSettings setting : RChestSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();
		loadStorageYaml();
		loadCategories();
		NBTStorage.cleanStorage();
		
		MantlePlugin.instance.getServer().getScheduler().scheduleSyncRepeatingTask(MantlePlugin.instance, new DiminishTimerChecker(), RChestSettings.DIMINISH_CHECKER_INTERNVAL.integer()*20, RChestSettings.DIMINISH_CHECKER_INTERNVAL.integer()*20);
		return true;
	}

	@Override
	protected void unloadModule() {
	}
	
	@Override
	public void loadConfig()
	{
		super.loadConfig();
		loadStorageYaml();
		loadCategories();
	}
	
	private void loadCategories()
	{
		basicCategories = Category.getCategories(RChestsModule.instance.config.getMapList(RChestSettings.BASIC_CATEGORIES.string));
		rareCategories =  Category.getCategories(RChestsModule.instance.config.getMapList(RChestSettings.RARE_CATEGORIES.string));
	}
}
