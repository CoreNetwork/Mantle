package us.corenetwork.mantle.gametweaks;

import net.minecraft.server.v1_8_R3.Block;

public class SilentPlaceProxy extends Block.StepSound {

	private Block.StepSound parent;
	public SilentPlaceProxy(Block.StepSound parent)
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
