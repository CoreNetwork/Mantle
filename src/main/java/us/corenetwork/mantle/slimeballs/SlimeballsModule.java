package us.corenetwork.mantle.slimeballs;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.slimeballs.commands.BaseSlimeballsCommand;
import us.corenetwork.mantle.slimeballs.commands.SlimeballsCheckCommand;
import us.corenetwork.mantle.slimeballs.commands.SlimeballsHelpCommand;
import us.corenetwork.mantle.slimeballs.commands.SlimeballsImportCommand;
import us.corenetwork.mantle.slimeballs.commands.SlimeballsPayCommand;


public class SlimeballsModule extends MantleModule {
	public static SlimeballsModule instance;

    public static HashMap<String, BaseSlimeballsCommand> commands = new HashMap<String, BaseSlimeballsCommand>();

    public SlimeballsModule() {
        super("Slimeballs", new String[] {"slimeballs"}, "slimeballs");

		instance = this;
	}

	@Override
	protected boolean loadModule() {

		for (SlimeballsSettings setting : SlimeballsSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();

        if (!SlimeballItem.init())
            return false;

		Bukkit.getServer().getPluginManager().registerEvents(new SlimeballsListener(), MantlePlugin.instance);

        commands.put("help", new SlimeballsHelpCommand());
        commands.put("import", new SlimeballsImportCommand());
        commands.put("check", new SlimeballsCheckCommand());
        commands.put("pay", new SlimeballsPayCommand());

		return true;
	}

	@Override
	protected void unloadModule() {
	}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (args.length < 1)
        {
            commands.get("check").execute(sender, args);
            return true;
        }

        BaseSlimeballsCommand cmd = commands.get(args[0]);
        if (cmd != null)
            cmd.execute(sender, args);
        else
            commands.get("help").execute(sender, args);

        return true;
    }
}
