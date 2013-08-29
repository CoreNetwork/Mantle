package us.corenetwork.mantle.netherspawning;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.FCLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;

public class NetherSpawningTimer implements Runnable {
    public static NetherSpawningTimer timerSingleton;

    private static World nether;

    public NetherSpawningTimer() {
        nether = Bukkit.getWorld(NetherSpawningSettings.NETHER_WORLD.string());
    }

    @Override
    public void run() {
        FCLog.debug("Starting nether spawning!");

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

//            while (!belowBlock.getType().isSolid())
//            {
//                block = belowBlock;
//            	belowBlock = belowBlock.getRelative(BlockFace.DOWN);
//            }
                        
            Block aboveBlock = block.getRelative(BlockFace.UP);
            if (aboveBlock == null)
                continue;
            if (aboveBlock.getType().isSolid())
                continue;

            boolean tooSmallDistance = false;
            int minDistance = Integer.MAX_VALUE;
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                int distance = (int) Math.round(player.getLocation().distanceSquared(block.getLocation()));
                if (distance < NetherSpawningSettings.MIN_DISTANCE_PLAYER.integer()) {
                    tooSmallDistance = true;
                    break;
                }

                if (distance < minDistance)
                    minDistance = distance;
            }
            
            if (tooSmallDistance)
                continue;

            if (minDistance > NetherSpawningSettings.MAX_DISTANCE_PLAYER.integer())
                continue;

            NetherSpawner.spawnMob(block);
        }

        long end = System.nanoTime();

        FCLog.debug("Nether spawning ended!");
        FCLog.debug("Nether spawning time: " + (end - start) / 1000000.0);
    }

}
