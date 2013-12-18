package us.corenetwork.mantle.gametweaks;

import net.minecraft.server.v1_7_R1.Block;

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

		//TODO
//		Block.CROPS.stepSound = new SilentPlaceProxy(Block.CROPS.stepSound);
//		Block.POTATOES.stepSound = new SilentPlaceProxy(Block.POTATOES.stepSound);
//		Block.CARROTS.stepSound = new SilentPlaceProxy(Block.CARROTS.stepSound);
//		Block.MELON_STEM.stepSound = new SilentPlaceProxy(Block.MELON_STEM.stepSound);
//		Block.PUMPKIN_STEM.stepSound = new SilentPlaceProxy(Block.PUMPKIN_STEM.stepSound);
//		Block.NETHER_WART.stepSound = new SilentPlaceProxy(Block.NETHER_WART.stepSound);
		
		MLog.info("Sound hack enabled!");
	}
}
