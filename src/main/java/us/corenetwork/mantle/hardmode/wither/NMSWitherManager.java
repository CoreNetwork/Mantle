package us.corenetwork.mantle.hardmode.wither;

import java.util.List;
import java.util.Map;
import net.minecraft.server.v1_8_R1.BiomeBase;
import net.minecraft.server.v1_8_R1.BiomeMeta;
import net.minecraft.server.v1_8_R1.EntityTypes;
import net.minecraft.server.v1_8_R1.EntityWither;
import net.minecraft.server.v1_8_R1.NBTReadLimiter;
import net.minecraft.server.v1_8_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftWither;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.CreatureSpawnEvent;
import us.corenetwork.mantle.util.ReflectionUtils;

public class NMSWitherManager {
    public static void register()
    {
        //Replace "Wither" entity type
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "c")).put("WitherBoss", CustomWither.class);
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "d")).put(CustomWither.class, "WitherBoss");
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "e")).put(64, CustomWither.class);
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "f")).put(CustomWither.class, 64);
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "g")).put("WitherBoss", 64);

        //Replace all villagers in biomes
        BiomeBase[] biomes = (BiomeBase[]) ReflectionUtils.getStatic(BiomeBase.class, "biomes");
        for (BiomeBase biome : biomes)
        {
            if (biome == null)
                continue;

            fixBiomeMeta((List<BiomeMeta>) ReflectionUtils.get(BiomeBase.class, biome, "aw"));
            fixBiomeMeta((List<BiomeMeta>) ReflectionUtils.get(BiomeBase.class, biome, "at"));
            fixBiomeMeta((List<BiomeMeta>) ReflectionUtils.get(BiomeBase.class, biome, "au"));
            fixBiomeMeta((List<BiomeMeta>) ReflectionUtils.get(BiomeBase.class, biome, "av"));

        }
    }

    private static void fixBiomeMeta(List<BiomeMeta> meta)
    {
        for (BiomeMeta m : meta)
        {
            if (m.b.equals(EntityWither.class))
            {
                m.b = CustomWither.class;
            }
        }
    }

    /***
     * Convert regular wither into custom villager.
     */
    public static CustomWither convert(Wither entity)
    {
        if (entity.getType() != EntityType.WITHER)
            return null;
        
        EntityWither nmsWither = ((CraftWither) entity).getHandle();

        if(nmsWither instanceof CustomWither)
        	return (CustomWither) nmsWither;
        
        World world = nmsWither.world;
        Location location = entity.getLocation();

        CustomWither newWither = new CustomWither(world);
        newWither.setPosition(location.getX(), location.getY(), location.getZ());
        world.removeEntity(nmsWither);
        world.addEntity(newWither, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return newWither;
    }

    public static boolean isCustomWither(Entity entity)
    {
        return ((CraftEntity) entity).getHandle() instanceof CustomWither;
    }

    public static NBTReadLimiter UNLIMTED_NBT_READER_INSTANCE = new UnlimitedNBTLimiter();
    private static class UnlimitedNBTLimiter extends NBTReadLimiter
    {
        public UnlimitedNBTLimiter()
        {
            super(0);
        }

        @Override
        public void a(long l)
        {
        }
    }
}