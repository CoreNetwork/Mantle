package us.corenetwork.mantle.netherspawning;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import us.corenetwork.mantle.MantlePlugin;

public class NetherSpawningTimer implements Runnable {
    public static NetherSpawningTimer timerSingleton;

    private static World nether;

    public NetherSpawningTimer() {
        nether = Bukkit.getWorld(NetherSpawningSettings.NETHER_WORLD.string());
    }

    @Override
    public void run() {
        long start = System.nanoTime();
        
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

            NetherSpawner.spawnMob(block);
        }

        long end = System.nanoTime();
    }

}
