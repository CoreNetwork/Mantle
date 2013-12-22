package us.corenetwork.mantle.gametweaks;

import net.minecraft.server.v1_7_R1.Block;
import net.minecraft.server.v1_7_R1.StepSound;

public class SilentPlaceProxy extends StepSound {

	private StepSound parent;
	public SilentPlaceProxy(StepSound parent) 
	{
		
		super("", 0f, 0f);
		this.parent = parent;
	}
	@Override
	public String getBreakSound() {
		return parent.getBreakSound();
	}
	@Override
	public String getPlaceSound() {
		return "silent";
	}
	@Override
	public String getStepSound() {
		return parent.getStepSound();
	}
	@Override
	public float getVolume1() {
		return parent.getVolume1();
	}
	@Override
	public float getVolume2() {
		return parent.getVolume2();
	}
	
	public static void apply(Block block)
	{
		block.stepSound = new SilentPlaceProxy(block.stepSound);
	}

}
