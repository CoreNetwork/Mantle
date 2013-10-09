package us.corenetwork.mantle.hardmode;

public class HorseSpeed {
	public static double getOriginalHorseSpeed(String id)
	{
		return HardmodeModule.instance.config.getDouble("OriginalHorseSpeeds.".concat(id), -1);
	}
	
	public static void setOriginalHorseSpeed(String id, double speed)
	{
		HardmodeModule.instance.config.set("OriginalHorseSpeeds.".concat(id), speed);
		HardmodeModule.instance.saveConfig();
	}
}
