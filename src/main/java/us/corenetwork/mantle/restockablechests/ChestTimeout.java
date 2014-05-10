package us.corenetwork.mantle.restockablechests;

import java.util.HashMap;

import org.bukkit.block.Block;

import us.corenetwork.mantle.MLog;

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
	
	public static int getRemainingTime(String table, String player, Block chest)
	{
		HashMap<String, ChestTimer> tableTimers = timers.get(table);
		
		if (tableTimers == null)
			return -1;

		ChestTimer timer = tableTimers.get(player);
		if (timer == null)
			return -1;
		
		if (chest.getWorld().equals(timer.ignoredChest.getWorld()))
		{
			int chestRange = (int) Math.round(chest.getLocation().distanceSquared(timer.ignoredChest.getLocation()));
			if (chestRange <= RChestsModule.instance.config.getInt("LootTables." + table + ".PlayerControl.MultiChestTimeout.ExclusionRangeSquared"))
				return -1;
		}

		return (int) ((timer.lastsUntil - System.currentTimeMillis()) / 60000);
	}
	
	private static class ChestTimer
	{
		long lastsUntil;
		Block ignoredChest;
	}
}
