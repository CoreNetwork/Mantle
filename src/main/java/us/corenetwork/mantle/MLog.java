package us.corenetwork.mantle;

import org.bukkit.Bukkit;

public class MLog {
	public static void debug(String text)
	{
		if (Settings.getBoolean(Setting.DEBUG))
			info(text);
	}
	
	public static void info(String text)
	{
		Bukkit.getLogger().info("[Mantle] " + text);
	}
	
	public static void warning(String text)
	{
		Bukkit.getLogger().warning("[Mantle] " + text);
	}
	
	public static void severe(String text)
	{
		Bukkit.getLogger().severe("[Mantle] " + text);
	}
}
