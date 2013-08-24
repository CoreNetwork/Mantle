package us.corenetwork.mantle.hydration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerData {
	private static HashMap<String, PlayerData> players = new HashMap<String, PlayerData>();
	
	private String playerName;
	
	public int fatigueLevel;
	public long fatigueEffectStart;
	public double hydrationLevel;
	public double saturationLevel;
	public List<Integer> deliveredMessages;
	
	public static PlayerData getPlayer(String name)
	{
		name = name.toLowerCase();
		
		PlayerData data = players.get(name);
		if (data != null)
			return data;
		
		data = new PlayerData();
		
		data.playerName = name;
		data.fatigueLevel = 0;
		data.fatigueEffectStart = 0;
		data.hydrationLevel = 100;
		data.saturationLevel = 0;
		
		players.put(name, data);
		return data;
	}
	
	public static void load()
	{
		players.clear();
		
		YamlConfiguration config = HydrationModule.instance.config;

		MemorySection section = (MemorySection) config.get("Players");
		if (section == null)
			return;
	
		for (Entry<String, Object> e : section.getValues(false).entrySet())
		{
			players.put(e.getKey(), new PlayerData(e.getKey(), (MemorySection) e.getValue()));
		}
	}
		
	public void save()
	{
		YamlConfiguration config = HydrationModule.instance.config;
		String prefix = "Players." + playerName + ".";
		config.set(prefix + "hydration", hydrationLevel);
		config.set(prefix + "saturation", saturationLevel);
		config.set(prefix + "fatigueLevel", fatigueLevel);
		config.set(prefix + "fatigueStart", fatigueEffectStart);
		config.set(prefix + "deliveredMessages", deliveredMessages);
	}
	
	private PlayerData()
	{
		deliveredMessages = new ArrayList<Integer>();
	}
	
	private PlayerData(String name, MemorySection section)
	{
		playerName = name;
		hydrationLevel = section.getDouble("hydration", 100);
		saturationLevel = section.getDouble("saturation", 0);
		fatigueLevel = section.getInt("fatigueLevel", 0);
		fatigueEffectStart = section.getLong("fatigueLevelStart", 0);
		deliveredMessages = section.getIntegerList("deliveredMessages");
	}
}
