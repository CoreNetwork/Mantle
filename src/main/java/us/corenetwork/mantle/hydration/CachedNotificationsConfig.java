package us.corenetwork.mantle.hydration;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.configuration.MemorySection;

public class CachedNotificationsConfig {
	
	public static HashMap<Integer, String> layerMessages = new HashMap<Integer, String>();
	
	public static void load(MemorySection section)
	{
		layerMessages.clear();
		
		for (Entry<String, Object> e : section.getValues(false).entrySet())
		{
			layerMessages.put(Integer.parseInt(e.getKey()), (String) e.getValue());
		}
	}
}
