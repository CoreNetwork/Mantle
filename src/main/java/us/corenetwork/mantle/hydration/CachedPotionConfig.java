package us.corenetwork.mantle.hydration;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;

public class CachedPotionConfig {
	
	private static HashMap<String, CachedPotionConfig> potions;
	
	public double normal;
	public double saturation;
	
	public static void loadPotions(MemorySection section)
	{
		potions = new HashMap<String, CachedPotionConfig>();
				
		for (Entry<String, Object> e : section.getValues(false).entrySet())
		{
			potions.put(e.getKey(), new CachedPotionConfig((MemorySection) e.getValue()));
		}
	}
	
	private CachedPotionConfig(MemorySection configSection)
	{
		normal = configSection.getDouble("normal", 0);
		saturation = configSection.getDouble("saturation", 0);
	}
	
	public static CachedPotionConfig getPotionConfig(ItemStack stack)
	{
		String idString = stack.getTypeId() + ":" + stack.getDurability();
		CachedPotionConfig config = potions.get(idString);
		if (config != null)
			return config;
		
		idString = Integer.toString(stack.getTypeId());
		config = potions.get(idString);
		return config;		
	}
}
