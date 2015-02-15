package us.corenetwork.mantle.holograms;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.core_network.cornel.java.NumberUtil;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.holograms.commands.BaseHologramCommand;
import us.corenetwork.mantle.holograms.commands.HologramHelpCommand;
import us.corenetwork.mantle.holograms.commands.HologramHideCommand;
import us.corenetwork.mantle.holograms.commands.HologramReloadCommand;
import us.corenetwork.mantle.holograms.commands.HologramRemoveCommand;
import us.corenetwork.mantle.holograms.commands.HologramSetCommand;
import us.corenetwork.mantle.holograms.commands.HologramShowCommand;
import us.corenetwork.mantle.holograms.commands.HologramUpdateCommand;
import us.corenetwork.mantle.holograms.commands.HologramUpdateLineCommand;


public class HologramsModule extends MantleModule {
	public static HologramsModule instance;

    public static HashMap<String, BaseHologramCommand> commands = new HashMap<String, BaseHologramCommand>();

    public HologramsModule() {
		super("Holograms", new String[] {"holo"}, "holograms");

		instance = this;
	}


	@Override
	protected boolean loadModule() {

		for (HologramsSettings setting : HologramsSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();
				
		Bukkit.getServer().getPluginManager().registerEvents(new HologramsListener(), MantlePlugin.instance);

        commands.put("help", new HologramHelpCommand());
        commands.put("reload", new HologramReloadCommand());
        commands.put("set", new HologramSetCommand());
        commands.put("remove", new HologramRemoveCommand());
        commands.put("update", new HologramUpdateCommand());
        commands.put("updateline", new HologramUpdateLineCommand());
        commands.put("show", new HologramShowCommand());
        commands.put("hide", new HologramHideCommand());

        HologramStorage.load();

		return true;
	}

	@Override
	protected void unloadModule() {
		saveConfig();
        HologramStorage.save();
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (args.length < 1 || NumberUtil.isInteger(args[0]))
        {
            commands.get("help").execute(sender, args);
            return true;
        }

        BaseHologramCommand cmd = commands.get(args[0]);
        if (cmd != null)
            cmd.execute(sender, args);
        else
            commands.get("help").execute(sender, args);

        return true;
    }
}
