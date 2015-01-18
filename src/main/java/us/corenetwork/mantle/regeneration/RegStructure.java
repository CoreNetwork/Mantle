package us.corenetwork.mantle.regeneration;

import org.bukkit.configuration.MemorySection;
import us.corenetwork.mantle.MLog;

public class RegStructure
{
	private MemorySection configNode;
	private String name;

	public RegStructure(String name, MemorySection configNode)
	{
		this.name = name;
		this.configNode = configNode;
	}

	public String getName()
	{
		return name;
	}

	public boolean shouldRespawnVillagers()
	{
		Boolean result = (Boolean) configNode.get("RespawnVillagers");
		if (result == null)
		{
			return false;
		}
		return result;
	}
	
	public boolean shouldWarnAtHighClaimChance()
	{
		Boolean result = (Boolean) configNode.get("WarnHighClaimedPercentage");
		if (result == null)
		{
			return false;
		}
		return result;
	}
	
	public boolean shouldIgnoreAir()
	{
		Boolean result = (Boolean) configNode.get("IgnoreAir");
		if (result == null)
		{
			return false;
		}
		return result;
	}
	
	public int getTimeOffset()
	{
		Integer result = (Integer) configNode.get("TimeOffsetSeconds");
		if (result == null)
		{
			return 0;
		}
		return result;
	}
	
	public int getGenerationInterval()
	{
		Integer result = (Integer) configNode.get("RegenerateIntervalSeconds");
		if (result == null)
		{
			MLog.severe("RegenerateIntervalSeconds is missing!");
			return -1;
		}
		return result;
	}
	
	public int getNumberToGenerateAtOnce()
	{
		Integer result = (Integer) configNode.get("RegenerateAtOnce");
		if (result == null)
		{
			return 1;
		}
		return result;
	}

}


