package us.corenetwork.mantle.hardmode.wither;

import java.util.List;
import java.util.Map;
import net.minecraft.server.v1_8_R3.BiomeBase;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.EntityWither;
import net.minecraft.server.v1_8_R3.NBTReadLimiter;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftWither;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.CreatureSpawnEvent;
import us.corenetwork.mantle.util.ReflectionUtils;
import us.corenetwork.mantle.util.VanillaReplacingUtil;

public class NMSWitherManager {
    public static void register()
    {
        VanillaReplacingUtil.replaceMob("WitherBoss", 64, EntityWither.class, CustomWither.class);
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