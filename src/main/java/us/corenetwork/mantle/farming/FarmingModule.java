package us.corenetwork.mantle.farming;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.restockablechests.CompassProtocolListener;

public class FarmingModule extends MantleModule {
    public static FarmingModule instance;
    FishingConfig fishingConfig = new FishingConfig();
    private NetherwartProtocolListener netherwartProtocolListener;
    public FarmingModule() {
        super("Farming", null, "farming");
        instance = this;
    }

    @Override
    protected boolean loadModule() {
        Bukkit.getPluginManager().registerEvents(new PistonNoFarmListener(), MantlePlugin.instance);
        Bukkit.getPluginManager().registerEvents(new NetherWartFarming(), MantlePlugin.instance);
        Bukkit.getPluginManager().registerEvents(fishingConfig, MantlePlugin.instance);

        netherwartProtocolListener = new NetherwartProtocolListener();
        ProtocolLibrary.getProtocolManager().addPacketListener(netherwartProtocolListener);

        return true;
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        fishingConfig.loadConfig();

        if (netherwartProtocolListener != null)
            netherwartProtocolListener.loadConfig();
    }

    @Override
    protected void unloadModule() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        return false;
    }
}
