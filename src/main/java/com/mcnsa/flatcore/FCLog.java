package com.mcnsa.flatcore;

import org.bukkit.Bukkit;

public class FCLog {
	public static void info(String text)
	{
		Bukkit.getLogger().info("[FlatcorePlugin] " + text);
	}
	
	public static void warning(String text)
	{
		Bukkit.getLogger().warning("[FlatcorePlugin] " + text);
	}
	
	public static void severe(String text)
	{
		Bukkit.getLogger().severe("[FlatcorePlugin] " + text);
	}
}
