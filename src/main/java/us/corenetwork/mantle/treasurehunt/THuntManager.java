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
		
		if(canStart())
		{
			startHunt();
		}
		else
		{
			Player player = MantlePlugin.instance.getServer().getPlayer(playerName);
			if(player != null)
				Util.Message(THuntSettings.MESSAGE_ADDED_TO_QUEUE.string(), player);
		}
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
		Util.Broadcast(THuntSettings.MESSAGE_START_WAVE.string());
		wave++;
		
		List<Player> playerList = getPlayersWhoCanJoin();
		
		Map<Player, Location> playerChestsLocations = THuntLocationRandomizer.getPlayerChests(playerList, distance); 
		chestList = new ArrayList<Location>(playerChestsLocations.values());
		
		announceChest(playerChestsLocations);
		placeChests();
		
		save();
	}
	
	private void endWave()
	{
		Util.Broadcast(THuntSettings.MESSAGE_END_WAVE.string());
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
			if(env == Environment.THE_END)
			{
				Util.Message(THuntSettings.MESSAGE_IN_LIMBO.string(), player);
			}
			else if(env == Environment.NETHER)
			{
				Util.Message(THuntSettings.MESSAGE_IN_NETHER.string(), player);
			}
			else
			{
				playerList.add(player);
			}
		}
		return playerList;
	}
	
	
	private void announceChest(Map<Player, Location> playerChestsLocations)	
	{
		//tan (22.5 degrees)
		double magicValue = 0.41421;
		double magicValue2 = 2.41421;
		for(Entry<Player, Location> entry : playerChestsLocations.entrySet())
		{
			Location playerLoc = entry.getKey().getLocation();
			Location chestLoc = entry.getValue();
			/* One day, not today
			double aaa = (chestLoc.getBlockX() - playerLoc.getBlockX()) / (chestLoc.getBlockZ() - playerLoc.getBlockZ());
			
			String direction = "woo";
			if(aaa >= magicValue && aaa < magicValue2)
				direction = "SouthEast";
			if(aaa >= magicValue2 && aaa < -magicValue2)
				direction = "East";
			if(aaa < -magicValue && aaa >= -magicValue2)
				direction = "NorthEast";
			if(aaa >= -magicValue && aaa < magicValue)
				direction = "North";
			if(aaa >= magicValue && aaa < magicValue2)
				direction = "NorthWest";
			if(aaa >= magicValue2 && aaa < -magicValue2)
				direction = "West";
			if(aaa <= -magicValue2 && aaa > -magicValue)
				direction = "SouthWest";
			if(aaa >= -magicValue && aaa < magicValue)
				direction = "South";
			*/
			String message = (String) THuntModule.instance.config.getMapList(THuntSettings.WAVES.string).get(wave - 1).get("Message");
			int distance = (int) Math.floor(Math.sqrt(Util.flatDistanceSquared(chestLoc, playerLoc)));
			message = message.replace("<Distance>", distance+"").replace("<X>", chestLoc.getBlockX() + "").replace("<Z>", chestLoc.getBlockZ() + "");
					//.replace("<Direction>", direction);
			Util.Message(message, entry.getKey());
		}
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

	public void chestClicked(Player player)
	{
		if(huntRunning == false)
			return;
		
		if(alreadyClicked.contains(player))
		{
			Util.Message(THuntSettings.MESSAGE_ONE_PER_WAVE.string(), player);
			return;
		}
		
		alreadyClicked.add(player);
		
		dropLoot(player);
	}
	
	private void dropLoot(Player player)
	{
		String lootTable = ((ArrayList<String>) THuntModule.instance.config.getMapList(THuntSettings.WAVES.string).get(wave - 1).get("LootTables")).get(lootTableToUse);
		
		LootTableNodeParser parser = new LootTableNodeParser(lootTable, 1, 0, RChestsModule.instance.config);
		List<ItemStack> items = parser.parse();
		
		for(ItemStack itemToDrop : items)
		{
			Item item = player.getWorld().dropItem(player.getLocation(), itemToDrop);
			EntityItem nmsItem = (EntityItem) ((CraftItem) item).getHandle();
			nmsItem.a(player.getName());
		}
	}
	
}
