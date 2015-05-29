package us.corenetwork.mantle.util;

import java.util.List;
import java.util.Map;
import net.minecraft.server.v1_8_R3.BiomeBase;
import net.minecraft.server.v1_8_R3.EntityTypes;

/**
 * Util for replacing vanilla stuff with our stuff (like entities or blocks)
 */
public class VanillaReplacingUtil
{
    /*
        Replaces vanilla mob class with our custom class.

        @param name Vanilla name of the entity (Savegame ID on the wiki)
        @param id Vanilla ID of the entity
        @param oldClass Vanilla mob class
        @param newClass Our custom mob class
     */
    public static void replaceMob(String name, int id, Class oldClass, Class newClass)
    {
        //Replace  entity type
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "c")).put(name, newClass);
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "d")).put(newClass, name);
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "e")).put(id, newClass);
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "f")).put(newClass, id);

        //Replace all entity types in biomes
        BiomeBase[] biomes = (BiomeBase[]) ReflectionUtils.getStatic(BiomeBase.class, "biomes");
        for (BiomeBase biome : biomes)
        {
            if (biome == null)
                continue;

            replaceMobsInBiomeMeta((List<BiomeBase.BiomeMeta>) ReflectionUtils.get(BiomeBase.class, biome, "aw"), oldClass, newClass);
            replaceMobsInBiomeMeta((List<BiomeBase.BiomeMeta>) ReflectionUtils.get(BiomeBase.class, biome, "at"), oldClass, newClass);
            replaceMobsInBiomeMeta((List<BiomeBase.BiomeMeta>) ReflectionUtils.get(BiomeBase.class, biome, "au"), oldClass, newClass);
            replaceMobsInBiomeMeta((List<BiomeBase.BiomeMeta>) ReflectionUtils.get(BiomeBase.class, biome, "av"), oldClass, newClass);
        }

    }

    private static void replaceMobsInBiomeMeta(List<BiomeBase.BiomeMeta> meta, Class oldClass, Class newClass)
    {
        for (BiomeBase.BiomeMeta m : meta)
        {
            if (m.b.equals(oldClass))
            {
                m.b = newClass;
            }
        }
    }

}

