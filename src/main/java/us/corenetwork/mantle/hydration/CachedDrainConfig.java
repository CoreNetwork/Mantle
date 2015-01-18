package us.corenetwork.mantle.hydration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;

public class CachedDrainConfig {
	
	private List<Double> layerPositions = new ArrayList<Double>();
	private List<WorldLayer> worldLayers = new ArrayList<WorldLayer>();
	
	public CachedDrainConfig(MemorySection configSection)
	{
		for (Entry<String, Object> e : configSection.getValues(false).entrySet())
		{
			layerPositions.add(Double.parseDouble(e.getKey()));
			
			MemorySection section = (MemorySection) e.getValue();
			WorldLayer layer = new WorldLayer();
			
			layer.regularDrain = section.getDouble("Drain", 0);
			layer.lavaDrain = section.getDouble("LavaDrain", 0);
			layer.fireDrain = section.getDouble("FireDrain", 0);

			layer.startingMF = section.getInt("StartingMF", 0);
			
			worldLayers.add(layer);
		}		
	}
	
	public static WorldLayer getWoldLayer(String world, int y)
	{
		CachedDrainConfig config = HydrationModule.drainConfigs.get(world);
		if (config == null)
			return null;
		
		int lastIndex = -1;
		for (int i = 0; i < config.layerPositions.size(); i++)
		{
			if (y < config.layerPositions.get(i))
				lastIndex = i;
		}
		
		if (lastIndex < 0)
			return null;
		
		return config.worldLayers.get(lastIndex);
	}
	
	public static class WorldLayer
	{
		public double getDrain(Player player)
		{
			Long lastLavaDamage = HydrationListener.lavaPlayer.get(player.getUniqueId());
			if (lastLavaDamage != null && lastLavaDamage > System.currentTimeMillis() - 2000)
			{
				HydrationListener.lavaPlayer.remove(player.getUniqueId());
				return lavaDrain;
			}
			
			if (player.getFireTicks() > 0)
				return fireDrain;
			
			return regularDrain;
		}
		
		double regularDrain;
		double lavaDrain;
		double fireDrain;
		
		int startingMF;		
	}

}
