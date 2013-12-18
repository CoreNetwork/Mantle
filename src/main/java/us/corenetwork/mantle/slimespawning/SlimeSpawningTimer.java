package us.corenetwork.mantle.slimespawning;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

import us.corenetwork.mantle.MantlePlugin;

public class SlimeSpawningTimer implements Runnable {
    public static SlimeSpawningTimer timerSingleton;

    private static World overworld;

    public SlimeSpawningTimer() {
        overworld = Bukkit.getWorld(SlimeSpawningSettings.OVERWORLD_NAME.string());
    }

    @Override
    public void run() {
        for (Chunk c : overworld.getLoadedChunks()) {
            int randomX = MantlePlugin.random.nextInt(16);
            int randomZ = MantlePlugin.random.nextInt(16);
            int randomY = MantlePlugin.random.nextInt(128);

            Block block = c.getBlock(randomX, randomY, randomZ);
            
            SlimeSpawner.spawn(block);
        }
    }
}
