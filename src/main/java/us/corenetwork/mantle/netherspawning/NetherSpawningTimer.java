package us.corenetwork.mantle.netherspawning;

import javax.swing.text.html.parser.Entity;
import net.minecraft.server.v1_7_R4.ChunkProviderServer;

import net.minecraft.server.v1_7_R4.EntitySlime;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.hardmode.HardmodeSettings;

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
    public void run() {
        for (EntityType entityType : new EntityType[] { EntityType.SKELETON, EntityType.BLAZE, EntityType.SLIME})
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

        //Separate loop for ghasts
        int ghastY = NetherSpawningSettings.GHAST_Y.integer();
        for (Chunk c : nether.getLoadedChunks()) {
            if (netherCps.unloadQueue.contains(c.getX(), c.getZ())) //Don't spawn on unloading chunk
                continue;

            int randomX = MantlePlugin.random.nextInt(16);
            int randomZ = MantlePlugin.random.nextInt(16);

            Block block = c.getBlock(randomX, ghastY, randomZ);

            if (block.getLightLevel() > HardmodeSettings.NETHER_MAX_SPAWN_LIGHT_LEVEL.integer())
                continue;

            block = block.getRelative(BlockFace.DOWN, MantlePlugin.random.nextInt(10) + 5);

            if (!block.isEmpty())
                continue;

            int[] playerDistances = getDistanceToNearestFarthestPlayer(block.getLocation());
            if (playerDistances[0] < NetherSpawningSettings.NEAREST_PLAYER_MINIMUM_DISTANCE_SQUARED.integer() || playerDistances[1] > NetherSpawningSettings.FARTHEST_PLAYER_MAXIMUM_DISTANCE_SQUARED.integer())
                continue;

            NetherSpawner.spawnGhast(block);
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
