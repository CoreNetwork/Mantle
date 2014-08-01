package us.corenetwork.mantle.netherspawning;

import net.minecraft.server.v1_7_R3.ChunkProviderServer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.MantlePlugin;

public class NetherSpawningTimer implements Runnable {
    public static NetherSpawningTimer timerSingleton;

    private static World nether;
    private static ChunkProviderServer netherCps;
    public NetherSpawningTimer() {
        nether = Bukkit.getWorld(NetherSpawningSettings.NETHER_WORLD.string());
        netherCps = ((CraftWorld) nether).getHandle().chunkProviderServer;
    }

    @Override
    public void run() {
        for (Chunk c : nether.getLoadedChunks()) {
            if (netherCps.unloadQueue.contains(c.getX(), c.getZ())) //Don't spawn on unloading chunk
                continue;

            int randomX = MantlePlugin.random.nextInt(16);
            int randomZ = MantlePlugin.random.nextInt(16);
            int randomY = MantlePlugin.random.nextInt(128);

            Block block = c.getBlock(randomX, randomY, randomZ);

            if (!block.isEmpty())
                continue;

            Block belowBlock = block.getRelative(BlockFace.DOWN);
            
            if (belowBlock == null)
                continue;
            if (!NetherSpawner.canSpawnOnThisBlock(belowBlock))
                continue;
                        
            Block aboveBlock = block.getRelative(BlockFace.UP);
            if (aboveBlock == null || aboveBlock.getY() < block.getY())
                continue;
            if (!(aboveBlock.getType().isTransparent() && aboveBlock.getType() != Material.CARPET))
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
