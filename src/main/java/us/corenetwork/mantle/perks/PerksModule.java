package us.corenetwork.mantle.perks;

import net.minecraft.server.v1_8_R1.Blocks;
import net.minecraft.server.v1_8_R1.CraftingManager;
import net.minecraft.server.v1_8_R1.EnumBannerPatternType;
import net.minecraft.server.v1_8_R1.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.util.ReflectionUtils;


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
