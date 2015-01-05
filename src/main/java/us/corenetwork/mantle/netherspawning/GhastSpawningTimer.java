package us.corenetwork.mantle.netherspawning;

import net.minecraft.server.v1_8_R1.ChunkProviderServer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.hardmode.HardmodeSettings;

public class GhastSpawningTimer implements Runnable {
    private static World nether;
    private static ChunkProviderServer netherCps;


    public GhastSpawningTimer() {
        if (nether == null)
        {
            nether = Bukkit.getWorld(NetherSpawningSettings.NETHER_WORLD.string());
            netherCps = ((CraftWorld) nether).getHandle().chunkProviderServer;
        }
    }

    @Override
    public void run() {
        int lightCheckY = NetherSpawningSettings.GHAST_LIGHT_CHECK_Y.integer();
        int minSpawnY = NetherSpawningSettings.GHAST_MIN_SPAWN_Y.integer();
        int maxSpawnY = NetherSpawningSettings.GHAST_MAX_SPAWN_Y.integer();

        for (Chunk c : nether.getLoadedChunks()) {
            if (netherCps.unloadQueue.contains(c.getX(), c.getZ())) //Don't spawn on unloading chunk
                continue;

            if (MantlePlugin.random.nextDouble() > NetherSpawningSettings.GHAST_SPAWN_CHANCE.doubleNumber())
                continue;

            int randomX = MantlePlugin.random.nextInt(16);
            int randomY = MantlePlugin.random.nextInt(256);
            int randomZ = MantlePlugin.random.nextInt(16);

            if (randomY < minSpawnY || randomY > maxSpawnY)
                continue;

            Block lightBlock = c.getBlock(randomX, lightCheckY, randomZ);
            if (lightBlock.getLightLevel() > HardmodeSettings.NETHER_MAX_SPAWN_LIGHT_LEVEL.integer())
                continue;

            Block spawnBlock = c.getBlock(randomX, randomY, randomZ);
            int maxDown = NetherSpawningSettings.GHAST_MAX_MOVE_DOWN.integer();
            int minDown = NetherSpawningSettings.GHAST_MIN_MOVE_DOWN.integer();
            spawnBlock = spawnBlock.getRelative(BlockFace.DOWN, MantlePlugin.random.nextInt(maxDown - minDown + 1) + minDown);

            if (!spawnBlock.isEmpty())
                continue;

            if (!Util.isInWorldBorderBounds(spawnBlock))
                continue;


            int[] playerDistances = NetherSpawningTimer.getDistanceToNearestFarthestPlayer(spawnBlock.getLocation());
            if (playerDistances[0] < NetherSpawningSettings.NEAREST_PLAYER_MINIMUM_DISTANCE_SQUARED.integer() || playerDistances[1] > NetherSpawningSettings.FARTHEST_PLAYER_MAXIMUM_DISTANCE_SQUARED.integer())
                continue;

            NetherSpawner.spawnGhast(spawnBlock);
        }
    }
}
