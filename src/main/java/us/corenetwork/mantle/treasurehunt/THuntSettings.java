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
	
	WAVES("Waves", new ArrayList<Map<String, Object>>(){{
		add(new HashMap<String, Object>(){{
			put("Distance", 250);
			put("TimeLimit", 50);
			put("Message", new ArrayList<String>(){{
				add("&aWave 1! Treasure crate dropped &6<Distance> blocks away &6(<X>,<Z>)");
				add("&7Walk towards <Direction>");}});
			put("LootTables", new ArrayList<String>(){{add("wave1-1");add("wave1-2");add("wave1-3");}});
		}});
		add(new HashMap<String, Object>(){{
			put("Distance", 500);
			put("TimeLimit", 50);
			put("Message", new ArrayList<String>(){{
				add("&aWave 2! Treasure crate dropped &6<Distance> blocks away &6(<X>,<Z>)");
				add("&7Sprint and jump towards <Direction>");}});
			put("LootTables", new ArrayList<String>(){{add("wave2-1");add("wave2-2");add("wave2-3");}});
		}});
		add(new HashMap<String, Object>(){{
			put("Distance", 750);
			put("TimeLimit", 50);
			put("Message", new ArrayList<String>(){{
				add("&aWave 3! Treasure crate dropped &6<Distance> blocks away &6(<X>,<Z>)");
				add("&7Ride a horse towards <Direction>");}});
			put("LootTables", new ArrayList<String>(){{add("wave3-1");add("wave3-2");add("wave3-3");}});
		}});
	}}),
	//{ "E", "NE", "N", "NW", "W", "SW", "S", "SE" };
	DIRECTIONS("Directions", new ArrayList<String>(){{
		add("east");
		add("north east");
		add("north");
		add("north west");
		add("west");
		add("south west");
		add("south");
		add("south east");
		}}),
	MESSAGE_ADDED_TO_QUEUE("Messages.AddedToQueue", "Your treasure hunt has been added to queue. It will start when the day breaks."),
	MESSAGE_ADDED_TO_QUEUE_BROADCAST("Messages.AddedToQueueBroadcast", "<Player> scheduled a Treasure Hunt! It will start in <Time> min"),
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
