package us.corenetwork.mantle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class MLog {

	public static void debug(String text)
	{
		if (Settings.getBoolean(Setting.DEBUG))
			sendLog("&f[&3Mantle&f]&f "+text);
	}

	public static void info(String text)
	{
		sendLog("&f[&fMantle&f]&f "+text);
	}

	public static void warning(String text)
	{
		sendLog("&f[&eMantle&f]&f " + text);
	}

	public static void severe(String text)
	{
		sendLog("&f[&cMantle&f]&f " + text);
	}

	public static void sendLog(String text)
	{
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', text));
	}

}
