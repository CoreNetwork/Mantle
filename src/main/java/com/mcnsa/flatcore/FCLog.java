package com.mcnsa.flatcore;

import org.bukkit.Bukkit;

public class FCLog {
	public static void debug(String text)
	{
		if (Settings.getBoolean(Setting.DEBUG))
			info(text);
	}
	
	public static void info(String text)
	{
		Bukkit.getLogger().info("[MCNSAFlatcore] " + text);
	}
	
	public static void warning(String text)
	{
		Bukkit.getLogger().warning("[MCNSAFlatcore] " + text);
	}
	
	public static void severe(String text)
	{
		Bukkit.getLogger().severe("[MCNSAFlatcore] " + text);
	}
}
