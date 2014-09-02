package us.corenetwork.mantle.treasurehunt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import net.minecraft.server.v1_7_R4.EntityItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftItem;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.restockablechests.LootTableNodeParser;
import us.corenetwork.mantle.restockablechests.RChestsModule;



public class THuntManager {
	
	private List<String> huntQueue; 
	private boolean huntRunning;
	private boolean skipDay;
	private String activeHunt;
	
	private List<Location> chestList;
	private List<Player> alreadyClicked;
	
	private List<Player> huntParticipants;
	
	private int wave;
	private int waveCount;
	private int lootTableToUse;
	private int lootTableCount;
	
	public THuntManager()
	{
		huntRunning = false;
		skipDay = false;
		huntQueue = Collections.synchronizedList(new ArrayList<String>());
		chestList = new ArrayList<Location>();
		alreadyClicked = new ArrayList<Player>();
		huntParticipants = new ArrayList<Player>();
		load();
	}
	
	private void load()
	{
		THuntModule.instance.loadStorageYaml();
		huntQueue.clear();
		
		List<String> savedQueue = THuntModule.instance.storageConfig.getStringList("savedQueue");
		
		for(String elem : savedQueue)
		{
			addToQueue(elem);
		}
		boolean running = THuntModule.instance.storageConfig.getBoolean("running");
		
		if(running)
		{
			String savedActiveHunt = THuntModule.instance.storageConfig.getString("activeHunt");
			activeHunt = savedActiveHunt;
			
			for(String serializedLoc : THuntModule.instance.storageConfig.getStringList("chestList"))
			{
				chestList.add(Util.unserializeLocation(serializedLoc));
			}
			endHunt();
			addToQueue(savedActiveHunt);
		}
		
	}
	public void save()
	{
		THuntModule.instance.storageConfig.set("savedQueue", huntQueue);
		THuntModule.instance.storageConfig.set("running", huntRunning);
		THuntModule.instance.storageConfig.set("activeHunt", activeHunt);
		
		List<String> serializedLocations = new ArrayList<String>();
		for(Location loc : chestList)
		{
			serializedLocations.add(Util.serializeLocation(loc));
		}
		THuntModule.instance.storageConfig.set("chestList", serializedLocations);
		THuntModule.instance.saveStorageYaml();
	}
	
	public boolean shouldMessagePlayer(Player player)
	{
		int playTime = player.getStatistic(Statistic.PLAY_ONE_TICK);
		return playTime > 72000;
	}
	
	public List<Player> onlinePlayersToMessage()
	{
		List<Player> onlinePlayersToMessage = new ArrayList<Player>();
		for(Player onlinePlayer : MantlePlugin.instance.getServer().getOnlinePlayers())
		{
			if(shouldMessagePlayer(onlinePlayer))
				onlinePlayersToMessage.add(onlinePlayer);
		}
		return onlinePlayersToMessage;
	}
	
	public boolean isRunning()
	{
		return huntRunning;
	}
	
	public boolean isQueued()
	{
		return !huntQueue.isEmpty();
	}
	
	public int getActiveWave()
	{
		return wave;
	}
	public int getWaveCount()
	{
		return waveCount;
	}
	
	public void addPlayerToHunt(Player player)
	{
		huntParticipants.add(player);
	}
	public void removePlayerFromHunt(Player player, boolean silent)
	{
		if(huntParticipants.contains(player))
		{
			if(silent == false)
			{
				Util.Message(THuntSettings.MESSAGE_LEAVE.string(), player);
			}
			huntParticipants.remove(player);
		}
	}
	public boolean isTakingPart(Player player)
	{
		return huntParticipants.contains(player);
	}
	
	public void addToQueue(String playerName)
	{
		huntQueue.add(playerName);
		
		save();
		
		int timeInMinutes = getTimeToStartTime() + (huntQueue.size() - 1 ) * 20;
		
		//If too close to start time, skip the day
		if(timeInMinutes < THuntSettings.MIN_MINUTES_BEFORE_TO_START.integer())
		{
			skipDay = true;
		}

		//If we are skipping next day, add 20 min to all timeInMinutes to start
		if(skipDay)
		{
			timeInMinutes += 20;
		}
		
		Player player = MantlePlugin.instance.getServer().getPlayer(playerName);
		if(player != null)
		{
			Util.Message(THuntSettings.MESSAGE_ADDED_TO_QUEUE.string().replace("<Time>", timeInMinutes + ""), player);
		}
		
		ArrayList<String> broadcastMessages = (ArrayList<String>) THuntModule.instance.config.getStringList(THuntSettings.MESSAGE_ADDED_TO_QUEUE_BROADCAST.string);
		
		for(String broadcastMessage : broadcastMessages)
		{
			broadcastMessage = broadcastMessage.replace("<Player>", playerName);
			broadcastMessage = broadcastMessage.replace("<Time>", timeInMinutes + "");
			
			List<Player> sendToPlayers = onlinePlayersToMessage();
			if(player != null)
				sendToPlayers.remove(player);
			Util.Multicast(broadcastMessage, sendToPlayers);
		}
	}
		
	public void checkSkipDay()
	{
		if(getTimeToStartTime() >= THuntSettings.MIN_MINUTES_BEFORE_TO_START.integer())
		{
			skipDay = false;
		}
	}
	
	public int getTimeToStartTime()
	{
		int timeInMinutes = 0; 
		long time = MantlePlugin.instance.getServer().getWorld("world").getTime();
		int periodBeg = THuntSettings.START_PERIOD_BEG.integer();
		int periodEnd = THuntSettings.START_PERIOD_END.integer();
		if(time >= periodBeg && time <= periodEnd && huntRunning == false)
		{
			timeInMinutes = 0;
		}
		else if (time < periodBeg)
		{
			timeInMinutes = (int) (Math.floor((periodBeg - time) / 20 / 60));
		}
		else if (time > periodBeg)
		{
			int tickTillStart = (int) (24000 - time + periodBeg);
			timeInMinutes = (int) (Math.floor(tickTillStart / 20 / 60));
		}
		return timeInMinutes;
	}
	
	public boolean canStart()
	{
		long time = MantlePlugin.instance.getServer().getWorld("world").getTime();
		boolean timeIsRight = time >= THuntSettings.START_PERIOD_BEG.integer() && time <= THuntSettings.START_PERIOD_END.integer(); 
		
		return skipDay == false && timeIsRight && (huntRunning == false) && huntQueue.isEmpty() == false;
	}
	
	public void startHunt()
	{
		if(huntRunning)
		{
			return;
		}
		
		String elem = huntQueue.get(0);
		huntQueue.remove(0);
		activeHunt = elem;
		huntRunning = true;
		save();
		runTheHunt();
		
		Util.Multicast(THuntSettings.MESSAGE_START_HUNT.string(), onlinePlayersToMessage());
	}
	
	public void endHunt()
	{
		clearChests();
		huntParticipants.clear();
		huntRunning = false;
		activeHunt = null;
		save();
		
		Util.Multicast(THuntSettings.MESSAGE_END_HUNT.string(), onlinePlayersToMessage());
		
		if(huntQueue.isEmpty() == false)
		{
			MantlePlugin.instance.getServer().getScheduler().scheduleSyncDelayedTask(MantlePlugin.instance, new Runnable() {
				
				@Override
				public void run()
				{
					Util.Broadcast(THuntSettings.MESSAGE_NEXT_HUNT_SCHEDULED.string().replace("<Time>", getTimeToStartTime()+""));
				}
			}, 200);
		}
	}
	
	private void runTheHunt()
	{
		List<Map<?, ?>> waveList = THuntModule.instance.config.getMapList(THuntSettings.WAVES.string);
		
		int delay = 0;
		wave = 0;
		waveCount = waveList.size();
		
		ArrayList<String> list = (ArrayList<String>) waveList.get(0).get("LootTables");
		lootTableCount = list.size();
		
		lootTableToUse = (new Random()).nextInt(lootTableCount);
		for(Map<?, ?> waveMap : waveList)
		{
			final int distance = (Integer) waveMap.get("Distance");
			final int timeLimit = (Integer) waveMap.get("TimeLimit");
			
			delay += 10;			
			
			//Starting wave task
			MantlePlugin.instance.getServer().getScheduler().scheduleSyncDelayedTask(MantlePlugin.instance, new Runnable() {
				@Override
				public void run() {startWave(distance);}
			}, delay);
						
			delay += timeLimit * 20;
			
			//Finishing wave task
			MantlePlugin.instance.getServer().getScheduler().scheduleSyncDelayedTask(MantlePlugin.instance, new Runnable() {
				
				@Override
				public void run()
				{
					endWave();
				}
			}, delay);
		}
		
		delay += 10;	
		
		//Finishing hunt task
		MantlePlugin.instance.getServer().getScheduler().scheduleSyncDelayedTask(MantlePlugin.instance, new Runnable() {
			@Override
			public void run() {
				endHunt();
			}
		}, delay);
		
	}

	private void startWave(int distance)
	{
		wave++;
		
		List<Player> waveParticipants = getWaveParticipants();
		
		final Map<Player, Location> playerChestsLocations = THuntLocationRandomizer.getPlayerChests(waveParticipants, distance); 
		chestList = new ArrayList<Location>(playerChestsLocations.values());
		
		announceChest(playerChestsLocations);
		
		placeChests();
		save();
		
		final int totalTime = (Integer) THuntModule.instance.config.getMapList(THuntSettings.WAVES.string).get(wave - 1).get("TimeLimit");
		List<Integer> notificationTimes = (List<Integer>) THuntModule.instance.config.getMapList(THuntSettings.WAVES.string).get(wave - 1).get("NotificationTimes");
		for(int time : notificationTimes)
		{
			final int notificationTime = time;
			MantlePlugin.instance.getServer().getScheduler().scheduleSyncDelayedTask(MantlePlugin.instance, new Runnable() {
				
				@Override
				public void run()
				{
					for(Entry<Player, Location> entry : playerChestsLocations.entrySet())
					{
						if(alreadyClicked.contains(entry.getKey()))
						{
							continue;
						}
						
						if(entry.getKey().getWorld().getEnvironment() != Environment.NORMAL)
						{
							continue;
						}
						
						Location playerLoc = entry.getKey().getLocation();
						Location chestLoc = entry.getValue();
						List<String> messageList = (List<String>) THuntModule.instance.config.getMapList(THuntSettings.WAVES.string).get(wave - 1).get("Messages");

						String message = THuntSettings.MESSAGE_WAVE_NOTIFICATION.string();
						int distance = (int) Math.floor(Math.sqrt(Util.flatDistanceSquared(chestLoc, playerLoc)));
						message = message.replace("<Distance>", distance+"").replace("<X>", chestLoc.getBlockX() + "").replace("<Z>", chestLoc.getBlockZ() + "")
								.replace("<TimeLeft>", (totalTime - notificationTime) + "");
						Util.Message(message, entry.getKey());
					}
				}
			}, time * 20);
		}
	}
	
	private void endWave()
	{
		clearChests();
		alreadyClicked.clear();
		save();
	}
	
	private List<Player> getWaveParticipants()
	{
		List<Player> playerList = new ArrayList<Player>();
		
		for(Player player : huntParticipants)
		{
			Environment env = player.getWorld().getEnvironment(); 
			if(env == Environment.NORMAL)
			{
				playerList.add(player);
			}
		}
		return playerList;
	}
	
	
	
	
	private void announceChest(Map<Player, Location> playerChestsLocations)	
	{
		for(Entry<Player, Location> entry : playerChestsLocations.entrySet())
		{
			Location playerLoc = entry.getKey().getLocation();
			Location chestLoc = entry.getValue();
			String direction = getDirection(chestLoc, playerLoc);
			List<String> messageList = (List<String>) THuntModule.instance.config.getMapList(THuntSettings.WAVES.string).get(wave - 1).get("Messages");

			for(String message : messageList)
			{
				int distance = (int) Math.floor(Math.sqrt(Util.flatDistanceSquared(chestLoc, playerLoc)));
				message = message.replace("<Distance>", distance+"").replace("<X>", chestLoc.getBlockX() + "").replace("<Z>", chestLoc.getBlockZ() + "")
						.replace("<Direction>", direction);
				Util.Message(message, entry.getKey());
			}
		}
	}
		
	private String getDirection(Location chestLoc, Location playerLoc)
	{
		double dx = chestLoc.getBlockX() - playerLoc.getBlockX();
		double dz = chestLoc.getBlockZ() - playerLoc.getBlockZ();
		
		List<String> headings =  (List<String>) THuntModule.instance.config.getList(THuntSettings.DIRECTIONS.string);
		double angle = Math.atan2(-dz, dx);
		//{ "E", "NE", "N", "NW", "W", "SW", "S", "SE" };
		int octant = (int) (Math.round( 8 * angle / (2*Math.PI) + 8 ) % 8);
		return headings.get(octant);
	}
	private void placeChests()
	{
		for(Location loc : chestList)
		{
			loc.getBlock().setType(Material.CHEST);
		}
	}
	
	private void clearChests()
	{
		for(Location loc : chestList)
		{
			loc.getBlock().setType(Material.AIR);
		}
		chestList.clear();
	}

	public boolean isHuntChest(Location loc)
	{
		return chestList.contains(loc);
	}

	public void chestClicked(Player player, Location chestLocation, boolean leftClick)
	{
		if(huntRunning == false)
			return;
		
		if(alreadyClicked.contains(player))
		{
			Util.Message(THuntSettings.MESSAGE_ONE_PER_WAVE.string(), player);
			return;
		}
		
		if(leftClick)
		{
			alreadyClicked.add(player);
			dropLoot(player, chestLocation);
		}
		else
		{
			Util.Message(THuntSettings.MESSAGE_RIGHT_CLICK.string(), player);
		}
	}
	
	private void dropLoot(Player player, Location chestLocation)
	{
		String lootTable = ((ArrayList<String>) THuntModule.instance.config.getMapList(THuntSettings.WAVES.string).get(wave - 1).get("LootTables")).get(lootTableToUse);
		
		LootTableNodeParser parser = new LootTableNodeParser(lootTable, 1, 0, RChestsModule.instance.config);
		List<ItemStack> items = parser.parse();
		Location dropLocation = chestLocation.add(0.5, 1, 0.5);
		for(ItemStack itemToDrop : items)
		{
			Item item = player.getWorld().dropItem(dropLocation, itemToDrop);
			item.teleport(dropLocation);
			EntityItem nmsItem = (EntityItem) ((CraftItem) item).getHandle();
			nmsItem.a(player.getName());
		}
	}

	
}
