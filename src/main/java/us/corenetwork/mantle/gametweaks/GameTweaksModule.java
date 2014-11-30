package us.corenetwork.mantle.gametweaks;

import net.minecraft.server.v1_8_R1.Block;

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

		SilentPlaceProxy.apply(Block.getByName("wheat"));
		SilentPlaceProxy.apply(Block.getByName("potatoes"));
		SilentPlaceProxy.apply(Block.getByName("carrots"));
		SilentPlaceProxy.apply(Block.getByName("melon_stem"));
		SilentPlaceProxy.apply(Block.getByName("pumpkin_stem"));
		SilentPlaceProxy.apply(Block.getByName("nether_wart"));
		
		MLog.info("Sound hack enabled!");
	}	
}
