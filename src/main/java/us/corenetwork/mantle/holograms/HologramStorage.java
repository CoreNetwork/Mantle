package us.corenetwork.mantle.holograms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Created by Matej on 5.9.2014.
 */
public class HologramStorage
{
    public static ArrayList<Hologram> storage = new ArrayList<Hologram>();
    public static HashMap<String, Hologram> namedHolograms = new HashMap<String, Hologram>();

    public static void load()
    {
        HologramsModule module = HologramsModule.instance;
        module.loadStorageYaml();

        List<Map> hologramList = (List<Map>) module.storageConfig.getList("Holograms");
        if (hologramList == null)
            return;


        for (Hologram hologram : storage)
            hologram.removeForAll();
        storage.clear();
        namedHolograms.clear();
        for (Map map : hologramList)
        {
            Map<String, ?> firstEntry = (Map<String, ?>) map.values().toArray()[0];

            Hologram hologram = new Hologram(firstEntry);
            storage.add(hologram);
            if (hologram.getName() != null)
                namedHolograms.put(hologram.getName(), hologram);

            hologram.displayForAll();
        }
    }

    public static void save()
    {
        HologramsModule module = HologramsModule.instance;
        YamlConfiguration configuration = module.storageConfig;

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        for (Hologram hologram : storage)
        {
            HashMap<String, Object> firstMap = new HashMap<String, Object>();
            firstMap.put("Hologram", hologram.serialize());
            list.add(firstMap);

        }

        configuration.set("Holograms", list);

        module.saveStorageYaml();
    }
}
