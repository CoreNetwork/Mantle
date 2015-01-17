package us.corenetwork.mantle.perks;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.YamlUtils;


public class PerksModule extends MantleModule {
	public static PerksModule instance;

	public PerksModule() {
		super("Perks", null, "perks");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}

	@Override
	protected boolean loadModule() {

		for (PerksSettings setting : PerksSettings.values())
		{
			if (config.get(setting.string) == null)
            {
                config.set(setting.string, setting.def);

            }
		}
		saveConfig();

		Bukkit.getPluginManager().registerEvents(new PerksListener(), MantlePlugin.instance);

		return true;
	}

    @Override
    public void loadConfig()
    {
        super.loadConfig();
    }

    @Override
	protected void unloadModule() {
	}
}
