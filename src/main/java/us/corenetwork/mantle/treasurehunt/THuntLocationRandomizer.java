package us.corenetwork.mantle.treasurehunt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;

public class THuntLocationRandomizer {

	private static Random r = new Random();
	
	public static Map<Player, Location> getPlayerChests(List<Player> playerList, double distance)
	{
		Map<Player, Location> playerChestLoc = new HashMap<Player, Location>();
		
		
		for(Player player : playerList)
		{
			boolean isValidLocation = false;
			Location origin = getInBoundsLocation(player);
			Location loc = null;
			
			int counter = 0;
			while(isValidLocation == false && counter < 200)
			{
				loc = getRandomLocationAround(origin, distance);
				isValidLocation = isValidLocation(loc);
				counter++;
			}
			
			if(counter == 200)
			{
				MLog.debug("Couldn't find a valid location for treasure hunt after 200 tries! + " + loc.getX() +" "+ loc.getZ());
				loc = null;
			}
			
			playerChestLoc.put(player, loc);
		}
		
		return playerChestLoc;
	}
	
	private static Location getInBoundsLocation(Player player)
	{
		Location origin = player.getLocation();
		
		if(player.getWorld().getEnvironment() == Environment.NETHER)
		{
			origin.setX(origin.getX()*4);
			origin.setZ(origin.getZ()*4);
		}
		
		if(origin.getX() > THuntSettings.MAX_X.integer())
			origin.setX(THuntSettings.MAX_X.integer());
		
		if(origin.getX() < THuntSettings.MIN_X.integer())
			origin.setX(THuntSettings.MIN_X.integer());
		
		if(origin.getZ() > THuntSettings.MAX_Z.integer())
			origin.setZ(THuntSettings.MAX_Z.integer());
		
		if(origin.getZ() < THuntSettings.MIN_Z.integer())
			origin.setZ(THuntSettings.MIN_Z.integer());
		
		return origin;
	}
	
	private static Location getRandomLocationAround(Location origin, double distance)
	{
		Location loc;
		
		double x1 = (r.nextDouble()*2 -1);
		double x2 = (r.nextDouble()*2 -1);
		
		double xV = (x1*x1 - x2*x2) / (x1*x1 + x2*x2);
		double zV = (2*x1*x2) / (x1*x1 + x2*x2);
		
		xV *= distance;
		zV *= distance;
		
		xV = origin.getX() + xV;
		zV = origin.getZ() + zV;
		
		xV = Math.floor(xV);
		zV = Math.floor(zV);
		
		double y = THuntSettings.GROUND_LEVEL.integer();

		loc = new Location(MantlePlugin.instance.getServer().getWorld("world"), xV, y, zV);
		return loc;
	}
		
	private static boolean isValidLocation(Location location)
	{
		boolean hasClaims = GriefPreventionHandler.containsClaim(MantlePlugin.instance.getServer().getWorld("world"), location.getBlockX(), location.getBlockZ(), 0, 0, 5, false, null);

		if(location.getX() > THuntSettings.MAX_X.integer())
			return false;
		
		if(location.getX() < THuntSettings.MIN_X.integer())
			return false;
		
		if(location.getZ() > THuntSettings.MAX_Z.integer())
			return false;
		
		if(location.getZ() < THuntSettings.MIN_Z.integer())
			return false;
		
		if (hasClaims)
			return false;
		
		Block block = location.getBlock();
		
		if (!isEmpty(block))
			return false;
		
		Block belowBlock = block.getRelative(BlockFace.DOWN);
		if (belowBlock == null || belowBlock.getType() != Material.GRASS)
			return false;
		
		Block aboveBlock = block.getRelative(BlockFace.UP);
		if (aboveBlock == null || !aboveBlock.isEmpty())
			return false;
		
		return true;
	}
		
	private static boolean isEmpty(Block block)
	{
		for (int i = 1; i < 13; i++)
		{
			Block aboveBlock = block.getRelative(BlockFace.UP, i);
			if (aboveBlock != null && !aboveBlock.isEmpty())
				return false;
		}
		return true && block.isEmpty();
	}
	
}
