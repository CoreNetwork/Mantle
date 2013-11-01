package us.corenetwork.mantle.restockablechests;

import java.util.HashMap;

import org.bukkit.block.Block;

public class ChestTimeout {
	private static HashMap<String, HashMap<String, ChestTimer>> timers = new HashMap<String, HashMap<String, ChestTimer>>();
	
	public static void addTimer(String table, String player, int time, Block chest)
	{
		HashMap<String, ChestTimer> tableTimers = timers.get(table);
		if (tableTimers == null)
		{
			tableTimers = new HashMap<String, ChestTimer>();
			timers.put(table, tableTimers);
		}
		
		ChestTimer timer = new ChestTimer();
		timer.lastsUntil = System.currentTimeMillis() + time * 60000;
		timer.ignoredChest = chest;
				
		tableTimers.put(player, timer);
	}
	
	public static boolean isUnderTimer(String table, String player, Block chest)
	{
		HashMap<String, ChestTimer> tableTimers = timers.get(table);
		
		if (tableTimers == null)
			return false;
		
		ChestTimer timer = tableTimers.get(player);
		if (timer == null)
			return false;
		if (chest.getWorld().equals(timer.ignoredChest.getWorld()))
		{
			int chestRange = (int) Math.round(chest.getLocation().distanceSquared(timer.ignoredChest.getLocation()));
			if (chestRange <=  RChestSettings.CHEST_COOLDOWN_EXCLUSION_RANGE.integer())
				return false;
		}
		
		return timer.lastsUntil > System.currentTimeMillis();
	}
	
	private static class ChestTimer
	{
		long lastsUntil;
		Block ignoredChest;
	}
}
