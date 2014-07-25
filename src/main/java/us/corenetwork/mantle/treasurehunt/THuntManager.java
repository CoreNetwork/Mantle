package us.corenetwork.mantle.treasurehunt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;

public class THuntManager {

	private List<String> huntQueue; 
	private boolean huntRunning;
	private String activeHunt;
	
	private static Random r = new Random();
	
	public THuntManager()
	{
		huntQueue = Collections.synchronizedList(new ArrayList<String>());
		huntRunning = false;
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
			addToQueue(savedActiveHunt);
			
			//TODO message? or is this even required?
		}
	}
	public void save()
	{
		THuntModule.instance.storageConfig.set("savedQueue", huntQueue);
		THuntModule.instance.storageConfig.set("running", huntRunning);
		THuntModule.instance.storageConfig.set("activeHunt", activeHunt);
		THuntModule.instance.saveStorageYaml();
	}
	
	public void addToQueue(String uuid)
	{
		huntQueue.add(uuid);
		save();
		
		if(timeIsRight())
		{
			start();
		}
	}
	
	public void start()
	{
		if(huntRunning)
		{
			stop();
		}
		
		huntRunning = true;
		String elem = huntQueue.get(0);
		huntQueue.remove(0);
		activeHunt = elem;
		save();
		
		runTheHunt();
	}

	private void runTheHunt()
	{
		int secondsLimitFirst = 60;
		double speedFirst = 4.3;
		
		List<Location> chestLocations = getRandomLocations(secondsLimitFirst * speedFirst);
		
		
	}
	
	private List<Location> getRandomLocations(double distance)
	{
		List<Location> randomLocs = new ArrayList<Location>();
		
		
		for(Player player : MantlePlugin.instance.getServer().getOnlinePlayers())
		{
			if(player.getWorld().getEnvironment() == Environment.THE_END)
			{
				//TODO message about bad luck, being in limbo and all
			}
			Location playerLoc = player.getLocation();
			
			if(player.getWorld().getEnvironment() == Environment.NETHER)
			{
				playerLoc.setX(playerLoc.getX()*4);
				playerLoc.setZ(playerLoc.getZ()*4);
			}
		
			boolean isValidLocation = false;
			Location loc = playerLoc;
			while(isValidLocation == false)
			{
				loc = getRandomLocationAround(playerLoc, distance);
				isValidLocation = isValidLocation(loc);
			}
			
			//TODO add a message to the player about the location, distance and so on.
			Util.Message("Chest - " + loc.getX() + " "+ loc.getZ(), player);
			randomLocs.add(loc);
		}
		
		return randomLocs;
	}
	
	private Location getRandomLocationAround(Location origin, double distance)
	{
		Location loc;
		
		double x1 = (r.nextDouble()*2 -1);
		double x2 = (r.nextDouble()*2 -1);
		
		double xV = (x1*x1 - x2*x2) / (x1*x1 + x2*x2);
		double zV = (2*x1*x2) / (x1*x1 + x2*x2);
		
		xV *= distance;
		zV *= distance;
		
		loc = new Location(MantlePlugin.instance.getServer().getWorld("world"), origin.getX() + xV, origin.getY(), origin.getZ() + zV);
		return loc;
	}
	
	private boolean isValidLocation(Location location)
	{
		return true;
	}
	
	public void stop()
	{
		clearTheHunt();
		huntRunning = false;
		activeHunt = null;
		save();
	}
	
	private void clearTheHunt()
	{
		
	}
	
	private boolean timeIsRight()
	{
		return true;
	}
}
