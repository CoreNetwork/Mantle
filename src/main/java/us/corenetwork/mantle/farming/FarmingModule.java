package us.corenetwork.mantle.farming;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;

public class FarmingModule extends MantleModule {
    public static FarmingModule instance;
    FishingConfig fishingConfig = new FishingConfig();

    public FarmingModule() {
        super("Farming", null, "farming");
        instance = this;
    }

    @Override
    protected boolean loadModule() {
        Bukkit.getPluginManager().registerEvents(new PistonNoFarmListener(), MantlePlugin.instance);
        Bukkit.getPluginManager().registerEvents(new NetherWartFarming(), MantlePlugin.instance);
        Bukkit.getPluginManager().registerEvents(fishingConfig, MantlePlugin.instance);

        return true;
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        fishingConfig.loadConfig();
    }

    @Override
    protected void unloadModule() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        return false;
    }
}
