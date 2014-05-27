package us.corenetwork.mantle.restockablechests;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.block.Block;

public class ChestTimeout {
	private static HashMap<String, HashMap<UUID, ChestTimer>> timers = new HashMap<String, HashMap<UUID, ChestTimer>>();
	
	public static void addTimer(String table, UUID player, int time, Block chest)
	{
		HashMap<UUID, ChestTimer> tableTimers = timers.get(table);
		if (tableTimers == null)
		{
			tableTimers = new HashMap<UUID, ChestTimer>();
			timers.put(table, tableTimers);
		}
		
		ChestTimer timer = new ChestTimer();
		timer.lastsUntil = System.currentTimeMillis() + time * 60000;
		timer.ignoredChest = chest;
				
		tableTimers.put(player, timer);
	}
	
	public static int getRemainingTime(String table, UUID player, Block chest)
	{
		HashMap<UUID, ChestTimer> tableTimers = timers.get(table);
		
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
