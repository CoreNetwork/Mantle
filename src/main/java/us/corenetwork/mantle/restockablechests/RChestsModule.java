package us.corenetwork.mantle.restockablechests;

import com.comphenix.protocol.ProtocolLibrary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.YamlUtils;
import us.corenetwork.mantle.restockablechests.commands.CreateChestCommand;
import us.corenetwork.mantle.restockablechests.commands.RestockAllCommand;


public class RChestsModule extends MantleModule {
	public static RChestsModule instance;
	public static List<Category> basicCategories;
	public static List<Category> rareCategories;
	public static Map<String, Category> categories;

	private CompassProtocolListener compassProtocolListener;

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
			{	
				if (setting.def instanceof ItemStack)
                {
                    YamlUtils.writeItemStack(config, setting.string, (org.bukkit.inventory.ItemStack) setting.def);
                    continue;
                }
				config.set(setting.string, setting.def);
			}
		}
		saveConfig();
		loadStorageYaml();
		loadCategories();

		compassProtocolListener = new CompassProtocolListener();
		ProtocolLibrary.getProtocolManager().addPacketListener(compassProtocolListener);


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

		if (compassProtocolListener != null)
			compassProtocolListener.loadConfig();
	}
	
	private void loadCategories()
	{
		basicCategories = Category.getCategories(RChestsModule.instance.config.getMapList(RChestSettings.BASIC_CATEGORIES.string));
		rareCategories =  Category.getCategories(RChestsModule.instance.config.getMapList(RChestSettings.RARE_CATEGORIES.string));
		categories = new HashMap<String, Category>();
		for(Category c: basicCategories)
		{
			categories.put(c.getLootTableName(), c);
		}
		for(Category c: rareCategories)
		{
			categories.put(c.getLootTableName(), c);
		}
	}
}
