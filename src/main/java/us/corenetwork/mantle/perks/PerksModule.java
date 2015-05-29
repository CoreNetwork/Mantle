package us.corenetwork.mantle.perks;

import java.util.HashMap;
import net.minecraft.server.v1_8_R3.CraftingManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.perks.commands.BasePerksCommand;
import us.corenetwork.mantle.perks.commands.SkullCommand;


public class PerksModule extends MantleModule {
	public static PerksModule instance;

	public static HashMap<String, BasePerksCommand> commands = new HashMap<String, BasePerksCommand>();


	public PerksModule() {
		super("Perks", new String[] {"perks"}, "perks");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (args.length < 1)
		{
			return true;
		}

		BasePerksCommand cmd = commands.get(args[0]);
		if (cmd != null)
			cmd.execute(sender, args);

		return true;
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
		commands.put("skull", new SkullCommand());
		Bukkit.getPluginManager().registerEvents(new PerksListener(), MantlePlugin.instance);

		//Add our custom recipe
		CraftingManager.getInstance().a(new CustomArmorStandRecipe());

		BannerRecipeProxy.inject();

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
