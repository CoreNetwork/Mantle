package us.corenetwork.mantle.gametweaks;

import net.minecraft.server.v1_7_R2.Block;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;


public class GameTweaksModule extends MantleModule {
	public static GameTweaksModule instance;

	public GameTweaksModule() {
		super("Game Tweaks", null, "gametweaks");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}

	@Override
	protected boolean loadModule() {
		fixSound();
		
		Bukkit.getPluginManager().registerEvents(new GameTweaksListener(), MantlePlugin.instance);
		
		return true;
	}
	@Override
	protected void unloadModule() {
	}
	
	public static void fixSound()
	{
		MLog.info("Enabling sound hack...");

		SilentPlaceProxy.apply(Block.b("wheat"));
		SilentPlaceProxy.apply(Block.b("potatoes"));
		SilentPlaceProxy.apply(Block.b("carrots"));
		SilentPlaceProxy.apply(Block.b("melon_stem"));
		SilentPlaceProxy.apply(Block.b("pumpkin_stem"));
		SilentPlaceProxy.apply(Block.b("nether_wart"));
		
		MLog.info("Sound hack enabled!");
	}	
}
