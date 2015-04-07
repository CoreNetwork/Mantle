package us.corenetwork.mantle.slimespawning;

import net.minecraft.server.v1_8_R2.ChunkProviderServer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import us.core_network.cornel.blocks.BlockUtil;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;

public class SlimeSpawningTimer implements Runnable {
    public static SlimeSpawningTimer timerSingleton;

    private static World overworld;
    private static ChunkProviderServer overworldCps;

    public SlimeSpawningTimer() {
        overworld = Bukkit.getWorld(SlimeSpawningSettings.OVERWORLD_NAME.string());
        overworldCps = ((CraftWorld) overworld).getHandle().chunkProviderServer;
    }

    @Override
    public void run() {
        for (Chunk c : overworld.getLoadedChunks()) {
            if (overworldCps.unloadQueue.contains(c.getX(), c.getZ())) //Don't spawn on unloading chunk
                continue;

            int randomX = MantlePlugin.random.nextInt(16);
            int randomZ = MantlePlugin.random.nextInt(16);
            int randomY = MantlePlugin.random.nextInt(40);

            Block block = c.getBlock(randomX, randomY, randomZ);

            if (!BlockUtil.isInWorldBorderBounds(block))
                continue;

            SlimeSpawner.spawn(block);
        }
    }
}
