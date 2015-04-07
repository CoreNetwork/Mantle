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

	MIN_MINUTES_BEFORE_TO_START("MinMinutesBeforeToStart", 4),
	START_PERIOD_BEG("StartPeriodBeginning", 200),
	START_PERIOD_END("StartPeriodEnd", 2000),
	
	WAVES("Waves", new ArrayList<Map<String, Object>>(){{
		add(new HashMap<String, Object>(){{
			put("Distance", 250);
			put("TimeLimit", 50);
			put("Messages", new ArrayList<String>(){{
				add("&aWave 1! Treasure crate dropped &6<Distance> blocks away &6(<X>,<Z>)");
				add("&7Walk towards <Direction>");}});
			put("LootTables", new ArrayList<String>(){{add("wave1-1");add("wave1-2");add("wave1-3");}});
			put("NotificationTimes", new ArrayList<Integer>(){{add(10); add(20); add(30);}});
		}});
		add(new HashMap<String, Object>(){{
			put("Distance", 500);
			put("TimeLimit", 50);
			put("Messages", new ArrayList<String>(){{
				add("&aWave 2! Treasure crate dropped &6<Distance> blocks away &6(<X>,<Z>)");
				add("&7Sprint and jump towards <Direction>");}});
			put("LootTables", new ArrayList<String>(){{add("wave2-1");add("wave2-2");add("wave2-3");}});
			put("NotificationTimes", new ArrayList<Integer>(){{add(10); add(20); add(30);}});
		}});
		add(new HashMap<String, Object>(){{
			put("Distance", 750);
			put("TimeLimit", 50);
			put("Messages", new ArrayList<String>(){{
				add("&aWave 3! Treasure crate dropped &6<Distance> blocks away &6(<X>,<Z>)");
				add("&7Ride a horse towards <Direction>");}});
			put("LootTables", new ArrayList<String>(){{add("wave3-1");add("wave3-2");add("wave3-3");}});
			put("NotificationTimes", new ArrayList<Integer>(){{add(10); add(20); add(30);}});
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


	MESSAGE_BRC_NO_HUNT("Messages.BuyRunCheck.NoHunt", "&6You don't have any Treasure Chase passes. Sponsor the server while having fun: store.core-network.us"),
	MESSAGE_BRC_HUNT_BOUGHT("Messages.BuyRunCheck..HuntBought","&6Treasure Chase pass acquired! Type &7/chase run &6to queue it."),
	MESSAGE_BRC_HUNTS_LEFT("Messages.BuyRunCheck..HuntsLeft", "&6Treasure Chase passes left: &7<Amount>"),

	MESSAGE_BRC_ADDED_TO_QUEUE("Messages.BuyRunCheck..AddedToQueue", "&6Treasure Chase pass used. It'll start when the day breaks, in <Time> min."),
	MESSAGE_BRC_ADDED_TO_QUEUE_BROADCAST("Messages.BuyRunCheck..AddedToQueueBroadcast", new ArrayList<String>(){{
							add("&a<Player> sponsored a Treasure Chase! &6It'll begin in <Time>m");
							add("&aTo participate, &6/chase join. &aTo learn more, /help TreasureChase");
							}}),
	MESSAGE_BRC_ADDED_TO_QUEUE_BROADCAST_Q_O_R("Messages.BuyRunCheck..AddedToQueueBroadcastWhileQueuedOrRunning", new ArrayList<String>(){{
							add("&a<Player> sponsored a Treasure Chase! &6It'll begin in <Time>m");
							}}),

	MESSAGE_PROGRESS_START_HUNT("Messages.Progress.StartHunt", "&aTreasure Chase has started!"),
	MESSAGE_PROGRESS_WAVE_NOTIFICATION("Messages.Progress.WaveNotificaton", "&6Crate: &7<X>, <Z> &6| &7<Distance> &6blocks away | &7<TimeLeft>s &6left"),
	MESSAGE_PROGRESS_WAVE_NOTIFICATION_AFTER_LOOTING("Messages.Progress.WaveNotificatonAfterLooting", "&7<TimeLeft>s &6left"),
	MESSAGE_PROGRESS_WAVE_NOTIFICATION_AFTER_LOOTING_LAST_WAVE("Messages.Progress.WaveNotificatonAfterLootingLastWave", "&6Chase ends in &7<TimeLeft>s"),
	MESSAGE_PROGRESS_END_HUNT("Messages.Progress.EndHunt", "&aTreasure Chase sponsored by <Player> has ended. Thanks for supporting our server, hope it was fun! &6Learn more: /help TreasureChase"),
	MESSAGE_PROGRESS_NEXT_HUNT_SCHEDULED("Messages.Progress.NextHuntScheduled", "&aNext Chase will start in <Time> min.  &6/chase join &ato participate!"),

	MESSAGE_JOIN_IN_LIMBO("Messages.Join.InLimbo", "&cYou cannot participate in Treasure Chase while in Limbo."),
	MESSAGE_JOIN_IN_NETHER("Messages.Join.InNether", "&6Travel to Overworld to get directions to your crate."),

	MESSAGE_JOIN_QUEUED("Messages.Join.Queued", "You joined queued Treasure Chase! It will start in <Time>"),
	MESSAGE_JOIN_RUNNING("Messages.Join.Running", "You joined running Treasure Chase!"),
	MESSAGE_JOIN_ALREADY_IN("Messages.Join.AlreadyIn", "You already joined this chase!"),

	MESSAGE_JOIN_LEAVE("Messages.Join.Leave", "&6You left Treasure Chase."),

	MESSAGE_STATUS_TIME_LEFT_TO_START("Messages.Status.TimeLeftToStart", "Next Chase will start in approx. <Time> min."),
	MESSAGE_STATUS_NO_HUNT_SCHEDULED("Messages.Status.NoHuntScheduled", "There is no Chase scheduled. Go buy some~!"),
	MESSAGE_STATUS_HUNT_RUNNING("Messages.Status.HuntRunning", "Treasure Chase is running right now! Wave <Wave> in progress."),


	//On server join while..
	MESSAGE_ENTER_WHILE_RUNNING("Messages.Enter.WhileRunning", "&aTreasure Chase is running right now! &6/chase join &afor next wave."),
	MESSAGE_ENTER_WHILE_QUEUED("Messages.Enter.WhileQueued", "&aTreasure Chase is scheduled to run in <Time> min. &6/chase join"),
	
	MESSAGE_ENTER_WHILE_RUNNING_ALREADY_IN("Messages.Enter.WhileRunningAlreadyIn", "&aTreasure Chase is still running!"),
	MESSAGE_ENTER_WHILE_QUEUED_ALREADY_IN("Messages.Enter.WhileQueuedAlreadyIn", "&aTreasure Chase will begin in <Time> min."),

	//On World change + joining active hunt
	MESSAGE_WAVE_ACTIVE("Messages.WaveActive", "&6Active wave: <Wave>. Please wait for next wave."),
	MESSAGE_WAVE_ACTIVE_LAST("Messages.WaveActiveLast", "&6Active wave: <Wave>. Sorry, there is no next wave."),

	//On chest handling
	MESSAGE_RIGHT_CLICK("Messages.RightClick", "&7Don't right click, &6» punch « the crate &7to open it!"),
	MESSAGE_ONE_PER_WAVE("Messages.OnePerWave", "&6You already got your loot!"),
	
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
