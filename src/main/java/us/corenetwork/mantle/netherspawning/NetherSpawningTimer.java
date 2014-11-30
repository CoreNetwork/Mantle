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

public class NetherSpawningTimer implements Runnable {
    private static World nether;
    private static ChunkProviderServer netherCps;

    private EntityType entityType;

    public NetherSpawningTimer(EntityType entityType) {
        if (nether == null)
        {
            nether = Bukkit.getWorld(NetherSpawningSettings.NETHER_WORLD.string());
            netherCps = ((CraftWorld) nether).getHandle().chunkProviderServer;
        }

        this.entityType = entityType;
    }

    @Override
    public void run()
    {
        for (Chunk c : nether.getLoadedChunks())
        {
            if (netherCps.unloadQueue.contains(c.getX(), c.getZ())) //Don't spawn on unloading chunk
                continue;

            int randomX = MantlePlugin.random.nextInt(16);
            int randomZ = MantlePlugin.random.nextInt(16);
            int randomY = MantlePlugin.random.nextInt(256);

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

            int[] playerDistances = getDistanceToNearestFarthestPlayer(block.getLocation());
            if (playerDistances[0] < NetherSpawningSettings.NEAREST_PLAYER_MINIMUM_DISTANCE_SQUARED.integer() || playerDistances[1] > NetherSpawningSettings.FARTHEST_PLAYER_MAXIMUM_DISTANCE_SQUARED.integer())
                continue;

            NetherSpawner.startSpawning(block, entityType);
        }
    }
    /*
        @return array where first element is distance to nearest player and second element is distance to farthest player.
     */
    public static int[] getDistanceToNearestFarthestPlayer(Location location)
    {
        int minDistance = Integer.MAX_VALUE;
        int maxDistance = Integer.MIN_VALUE;

        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (player.getWorld() != location.getWorld())
                continue;

            int distance = (int) player.getLocation().distanceSquared(location);
            if (distance < minDistance)
                minDistance = distance;
            if (distance > maxDistance)
                maxDistance = distance;
        }

        return new int[] { minDistance, maxDistance };
    }
}
