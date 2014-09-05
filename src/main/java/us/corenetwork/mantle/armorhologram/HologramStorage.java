package us.corenetwork.mantle.armorhologram;

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

    public static void load()
    {
        ArmorHologramModule module = ArmorHologramModule.instance;
        module.loadStorageYaml();

        List<Map> hologramList = (List<Map>) module.storageConfig.getList("Holograms");
        if (hologramList == null)
            return;


        for (Hologram hologram : storage)
            hologram.removeForAll();
        storage.clear();

        for (Map map : hologramList)
        {
            Map<String, ?> firstEntry = (Map<String, ?>) map.values().toArray()[0];

            Hologram hologram = new Hologram(firstEntry);
            storage.add(hologram);

            hologram.displayForAll();
        }
    }

    public static void save()
    {
        ArmorHologramModule module = ArmorHologramModule.instance;
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
