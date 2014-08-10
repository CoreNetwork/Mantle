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
	private String activeHunt;
	
	private List<Location> chestList;
	private List<Player> alreadyClicked;
	
	private int wave;
	private int lootTableToUse;
	private int lootTableCount;
	
	public THuntManager()
	{
		huntRunning = false;
		huntQueue = Collections.synchronizedList(new ArrayList<String>());
		chestList = new ArrayList<Location>();
		alreadyClicked = new ArrayList<Player>();
		ArrayList<String> list = (ArrayList<String>) THuntModule.instance.config.getMapList(THuntSettings.WAVES.string).get(0).get("LootTables");
		
		lootTableCount = list.size();
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
	
	public void addToQueue(String playerName)
	{
		huntQueue.add(playerName);
		
		save();
		
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
		
		timeInMinutes += (huntQueue.size() - 1 ) * 20;
		
		Player player = MantlePlugin.instance.getServer().getPlayer(playerName);
		if(player != null)
		{
			Util.Message(THuntSettings.MESSAGE_ADDED_TO_QUEUE.string().replace("<Time>", timeInMinutes + ""), player);
		}
		
		String broadcastMessage = THuntSettings.MESSAGE_ADDED_TO_QUEUE_BROADCAST.string();
		broadcastMessage = broadcastMessage.replace("<Player>", playerName);
		broadcastMessage = broadcastMessage.replace("<Time>", timeInMinutes + "");
		Util.Broadcast(broadcastMessage, playerName);
		
	}
		
	public boolean canStart()
	{
		long time = MantlePlugin.instance.getServer().getWorld("world").getTime();
		boolean timeIsRight = time >= THuntSettings.START_PERIOD_BEG.integer() && time <= THuntSettings.START_PERIOD_END.integer(); 
		
		return timeIsRight && (huntRunning == false) && huntQueue.isEmpty() == false;
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
		
		Util.Broadcast(THuntSettings.MESSAGE_START_HUNT.string());
	}
	
	public void endHunt()
	{
		clearChests();
		huntRunning = false;
		activeHunt = null;
		save();
		
		Util.Broadcast(THuntSettings.MESSAGE_END_HUNT.string());
		messagePlayersInWrongWorlds();
	}
	
	private void runTheHunt()
	{
		List<Map<?, ?>> waveList = THuntModule.instance.config.getMapList(THuntSettings.WAVES.string);
		
		int delay = 0;
		wave = 0;
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
		
		List<Player> playerList = getPlayersWhoCanJoin();
		
		final Map<Player, Location> playerChestsLocations = THuntLocationRandomizer.getPlayerChests(playerList, distance); 
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
	
	private List<Player> getPlayersWhoCanJoin()
	{
		List<Player> playerList = new ArrayList<Player>();
		
		for(Player player : MantlePlugin.instance.getServer().getOnlinePlayers())
		{
			Environment env = player.getWorld().getEnvironment(); 
			if(env == Environment.NORMAL)
			{
				playerList.add(player);
			}
		}
		return playerList;
	}
	
	private void messagePlayersInWrongWorlds()
	{
		for(Player player : MantlePlugin.instance.getServer().getOnlinePlayers())
		{
			Environment env = player.getWorld().getEnvironment(); 
			if(env == Environment.THE_END)
			{
				Util.Message(THuntSettings.MESSAGE_IN_LIMBO.string(), player);
			}
			else if(env == Environment.NETHER)
			{
				Util.Message(THuntSettings.MESSAGE_IN_NETHER.string(), player);
			}
		}
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
			//String message = (String) THuntModule.instance.config.getMapList(THuntSettings.WAVES.string).get(wave - 1).get("Message");
			
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

	public void chestClicked(Player player, Location chestLocation)
	{
		if(huntRunning == false)
			return;
		
		if(alreadyClicked.contains(player))
		{
			Util.Message(THuntSettings.MESSAGE_ONE_PER_WAVE.string(), player);
			return;
		}
		
		alreadyClicked.add(player);
		
		dropLoot(player, chestLocation);
	}
	
	private void dropLoot(Player player, Location chestLocation)
	{
		String lootTable = ((ArrayList<String>) THuntModule.instance.config.getMapList(THuntSettings.WAVES.string).get(wave - 1).get("LootTables")).get(lootTableToUse);
		
		LootTableNodeParser parser = new LootTableNodeParser(lootTable, 1, 0, RChestsModule.instance.config);
		List<ItemStack> items = parser.parse();
		
		for(ItemStack itemToDrop : items)
		{
			Item item = player.getWorld().dropItem(chestLocation.add(0.5, 1, 0.5), itemToDrop);
			EntityItem nmsItem = (EntityItem) ((CraftItem) item).getHandle();
			nmsItem.a(player.getName());
		}
	}
	
}
