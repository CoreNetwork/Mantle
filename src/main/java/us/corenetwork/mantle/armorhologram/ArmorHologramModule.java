package us.corenetwork.mantle.armorhologram;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.armorhologram.commands.BaseHologramCommand;
import us.corenetwork.mantle.armorhologram.commands.HologramHelpCommand;
import us.corenetwork.mantle.armorhologram.commands.HologramHideCommand;
import us.corenetwork.mantle.armorhologram.commands.HologramReloadCommand;
import us.corenetwork.mantle.armorhologram.commands.HologramRemoveCommand;
import us.corenetwork.mantle.armorhologram.commands.HologramSetCommand;
import us.corenetwork.mantle.armorhologram.commands.HologramShowCommand;
import us.corenetwork.mantle.armorhologram.commands.HologramUpdateCommand;
import us.corenetwork.mantle.armorhologram.commands.HologramUpdateLineCommand;


public class ArmorHologramModule extends MantleModule {
	public static ArmorHologramModule instance;

    public static HashMap<String, BaseHologramCommand> commands = new HashMap<String, BaseHologramCommand>();

    public ArmorHologramModule() {
		super("Armor Hologram", new String[] {"holo"}, "armorhologram");

		instance = this;
	}


	@Override
	protected boolean loadModule() {

		for (ArmorHologramSettings setting : ArmorHologramSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();
				
		Bukkit.getServer().getPluginManager().registerEvents(new ArmorHologramListener(), MantlePlugin.instance);

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
	}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (args.length < 1 || Util.isInteger(args[0]))
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
