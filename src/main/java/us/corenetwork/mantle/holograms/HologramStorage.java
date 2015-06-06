package us.corenetwork.mantle.holograms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Created by Matej on 5.9.2014.
 */
public class HologramStorage
{
    private static Map<String, Map<Long, HologramsChunk>> worlds = new HashMap<String, Map<Long, HologramsChunk>>();
    public static HashMap<String, Hologram> namedHolograms = new HashMap<String, Hologram>();

    public static void load()
    {
        HologramsModule module = HologramsModule.instance;
        module.loadStorageYaml();

        List<Map> hologramList = (List<Map>) module.storageConfig.getList("Holograms");
        if (hologramList == null)
            return;


        worlds.clear();
        namedHolograms.clear();
        for (Map map : hologramList)
        {
            Map<String, ?> firstEntry = (Map<String, ?>) map.values().toArray()[0];

            Hologram hologram = new Hologram(firstEntry);
            add(hologram);
        }
    }

    public static void save()
    {
        HologramsModule module = HologramsModule.instance;
        YamlConfiguration configuration = module.storageConfig;

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        for (Map<Long, HologramsChunk> world : worlds.values())
        {
            for (HologramsChunk chunk : world.values())
            {
                for (Hologram hologram : chunk.holograms)
                {
                    if (hologram.getShouldNotSave())
                        continue;

                    HashMap<String, Object> firstMap = new HashMap<String, Object>();
                    firstMap.put("Hologram", hologram.serialize());
                    list.add(firstMap);
                }
            }
        }

        configuration.set("Holograms", list);

        module.saveStorageYaml();
    }

    public static void add(Hologram hologram)
    {
        if (hologram.getName() != null)
            namedHolograms.put(hologram.getName(), hologram);

        HologramsChunk chunk = getOrCreateChunkData(hologram.getWorld().getName(), hologram.getChunkX(), hologram.getChunkZ());
        hologram.setParentChunk(chunk);
        chunk.holograms.add(hologram);
    }

    public static Hologram getNearest(Location location)
    {
        Map<Long, HologramsChunk> chunkMap = worlds.get(location.getWorld().getName());
        if (chunkMap == null)
            return null;

        double nearestDistance = Double.MAX_VALUE;
        Hologram nearest = null;

        for (HologramsChunk chunk : chunkMap.values())
        {
            for (Hologram hologram : chunk.holograms)
            {
                Location hologramLocation = new Location(location.getWorld(), hologram.getX(), hologram.getY(), hologram.getZ());
                double distance = hologramLocation.distanceSquared(location);
                if (distance < nearestDistance)
                {
                    nearestDistance = distance;
                    nearest = hologram;
                }
            }
        }


        return nearest;
    }

    private static HologramsChunk getOrCreateChunkData(String world, int x, int z)
    {
        Map<Long, HologramsChunk> chunkMap = worlds.get(world);

        if (chunkMap == null)
        {
            chunkMap = new HashMap<Long, HologramsChunk>();
            worlds.put(world, chunkMap);
        }

        Long id = ChunkCoordIntPair.a(x, z);
        HologramsChunk chunk = chunkMap.get(id);

        if (chunk == null)
        {
            chunk = new HologramsChunk();
            chunkMap.put(id, chunk);
        }

        return chunk;

    }

    public static HologramsChunk getChunkData(String world, int x, int z)
    {
        Map<Long, HologramsChunk> chunkMap = worlds.get(world);
        if (chunkMap == null)
            return null;

        Long id = ChunkCoordIntPair.a(x, z);
        return chunkMap.get(id);
    }
}
