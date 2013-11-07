package us.corenetwork.mantle.nmshacks;

import net.minecraft.server.v1_6_R3.Block;
import net.minecraft.server.v1_6_R3.StepSound;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantleModule;


public class NMSHacksModule extends MantleModule {
	public static NMSHacksModule instance;

	public NMSHacksModule() {
		super("NMS Hacks", null, "NMSHacks");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}

	@Override
	protected boolean loadModule() {
		fixWheatSound();
		
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
