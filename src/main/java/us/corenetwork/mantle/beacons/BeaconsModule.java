package us.corenetwork.mantle.beacons;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.YamlUtils;


public class BeaconsModule extends MantleModule {
	public static BeaconsModule instance;

	public BeaconsModule() {
		super("Beacons", null, "beacons");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}

	@Override
	protected boolean loadModule() {

		for (BeaconsSettings setting : BeaconsSettings.values())
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

		Bukkit.getServer().getPluginManager().registerEvents(new BeaconsListener(), MantlePlugin.instance);

		return true;
	}

    @Override
    public void loadConfig()
    {
        super.loadConfig();
        BeaconEffect.STORAGE.load(config);
    }

    @Override
	protected void unloadModule() {
	}
}
