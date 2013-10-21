package us.corenetwork.mantle.netherspawning;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.MantlePlugin;

public class NetherSpawningTimer implements Runnable {
    public static NetherSpawningTimer timerSingleton;

    private static World nether;

    public NetherSpawningTimer() {
        nether = Bukkit.getWorld(NetherSpawningSettings.NETHER_WORLD.string());
    }

    @Override
    public void run() {
        for (Chunk c : nether.getLoadedChunks()) {
            int randomX = MantlePlugin.random.nextInt(16);
            int randomZ = MantlePlugin.random.nextInt(16);
            int randomY = MantlePlugin.random.nextInt(256);

            Block block = c.getBlock(randomX, randomY, randomZ);

            if (!block.isEmpty())
                continue;

            Block belowBlock = block.getRelative(BlockFace.DOWN);
            
            if (belowBlock == null)
                continue;
            if (!belowBlock.getType().isSolid())
                continue;
                        
            Block aboveBlock = block.getRelative(BlockFace.UP);
            if (aboveBlock == null)
                continue;
            if (aboveBlock.getType().isSolid())
                continue;

            if (getDistanceToNearestPlayer(block.getLocation()) < NetherSpawningSettings.NEAREST_PLAYER_MINIMUM_DISTANCE_SQUARED.integer())
            	continue;
            
            NetherSpawner.spawnMob(block);
        }
    }

    public static int getDistanceToNearestPlayer(Location location)
    {
    	int minDistance = Integer.MAX_VALUE;
    	for (Player player : Bukkit.getOnlinePlayers())
    	{
    		if (player.getWorld() != location.getWorld())
    			continue;
    		
    		int distance = (int) player.getLocation().distanceSquared(location);
    		if (distance < minDistance)
    			minDistance = distance;
    	}
    	
    	return minDistance;
    }    
}
