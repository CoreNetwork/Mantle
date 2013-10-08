package us.corenetwork.mantle.restockablechests;

import java.util.Arrays;
import java.util.HashMap;

public class ChestTimeout {
	private static HashMap<String, HashMap<String, Long>> timers = new HashMap<String, HashMap<String, Long>>();
	
	public static void addTimer(String table, String player, int time)
	{
		HashMap<String, Long> tableTimers = timers.get(table);
		if (tableTimers == null)
		{
			tableTimers = new HashMap<String, Long>();
			timers.put(table, tableTimers);
		}
		
		tableTimers.put(player, System.currentTimeMillis() + time * 60000);
	}
	
	public static boolean isUnderTimer(String table, String player)
	{
		HashMap<String, Long> tableTimers = timers.get(table);
		
		if (tableTimers == null)
			return false;
		
		Long time = tableTimers.get(player);
		if (time == null)
			return false;
		
		return time > System.currentTimeMillis();
	}
}
