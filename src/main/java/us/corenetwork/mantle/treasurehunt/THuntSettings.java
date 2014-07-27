package us.corenetwork.mantle.treasurehunt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public enum THuntSettings {

	
	MIN_X("MinX", -8000),
	MIN_Z("MinZ", -8000),
	MAX_X("MaxX", 8000),
	MAX_Z("MaxZ", 8000),
	
	GROUND_LEVEL("GroundLevel", 4),
	START_PERIOD_BEG("StartPeriodBeginning", 200),
	START_PERIOD_END("StartPeriodEnd", 2000),
	
	WAVES("Waves", new ArrayList<Map<String, Integer>>(){{
		add(new HashMap<String, Integer>(){{
			put("Distance", 250);
			put("TimeLimit", 50);
		}});
		add(new HashMap<String, Integer>(){{
			put("Distance", 500);
			put("TimeLimit", 50);
		}});
		add(new HashMap<String, Integer>(){{
			put("Distance", 750);
			put("TimeLimit", 50);
		}});
	}}),
	
	
	MESSAGE_ADDED_TO_QUEUE("Messages.AddedToQueue", "Your treasure hunt has been added to queue. It will start when the day breaks."),
	MESSAGE_NO_HUNT("Messages.NoHunt", "You have no hunts to run"),
	MESSAGE_HUNT_BOUGHT("Messages.HuntBought","You have bought a Treasure Hunt!"),

	MESSAGE_START_HUNT("Messages.StartHunt", "Treasure Hunt started!"),
	MESSAGE_END_HUNT("Messages.EndHunt", "Treasure Hunt ended!"),
	
	MESSAGE_START_WAVE("Messages.StartWave", "Wave started! Find your chest."),
	MESSAGE_END_WAVE("Messages.EndWave", "Wave ended!"),
	
	
	MESSAGE_IN_LIMBO("Messages.InLimbo", "I'm sorry, you cannot participate in Treasure Hunt while in Limbo"),
	MESSAGE_IN_NETHER("Messages.InNether", "Treasure Hunt takes place in overworld! Travel back to find your chest."),
	
	MESSAGE_RIGHT_CLICK("Messages.RightClick", "You cannot open this chest, left click it to get the rewards."),
	MESSAGE_ONE_PER_WAVE("Messages.OnePerWave", "You can loot only one chest per wave!"),
	
	MESSAGE_DISTANCE("Messages.Distance", "Your chest is <Distance> meters away, at <X>,  <Z>.")
	;
	protected String string;
	protected Object def;
	
	private THuntSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}
	
	public String string()
	{
		return (String) THuntModule.instance.config.get(string, def);
	}
	
	public int integer()
	{
		return (Integer) THuntModule.instance.config.get(string, def);
	}
	
	public List<String> stringList()
	{
		return (List<String>) THuntModule.instance.config.get(string, def);
	}
}
