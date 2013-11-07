package us.corenetwork.mantle.gametweaks;

import net.minecraft.server.v1_6_R3.Block;
import net.minecraft.server.v1_6_R3.StepSound;

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
		fixWheatSound();
		
		Bukkit.getPluginManager().registerEvents(new GameTweaksListener(), MantlePlugin.instance);
		
		return true;
	}
	@Override
	protected void unloadModule() {
	}
	
	public static void fixWheatSound()
	{
		MLog.info("Enabling wheat sound hack...");

		Block.CROPS.stepSound = new StepSound("grass", 1.0F, 1.0F) {
			@Override
			public String getPlaceSound() {
				return "silent";
			}
		};
		
		MLog.info("Wheat sound hack enabled!");
	}
}
