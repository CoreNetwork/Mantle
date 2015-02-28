package us.corenetwork.mantle.hardmode;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.EntityZombie;
import net.minecraft.server.v1_8_R1.MathHelper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class BabyZombieBurner implements Listener, Runnable{
    private Set<WeakReference<Zombie>> babyZombies = new HashSet<>();
    private Field nmsEntityField;

    public BabyZombieBurner() {
        try {
            nmsEntityField = CraftEntity.class.getDeclaredField("entity");
            nmsEntityField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        for (World world : Bukkit.getWorlds()) {
            for (Entity zombie : world.getEntitiesByClasses(Zombie.class)) {
                Zombie z = (Zombie) zombie;
                if (z.isBaby()) {
                    babyZombies.add(new WeakReference<>(z));
                }
            }
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Zombie && ((Zombie) event.getEntity()).isBaby()) {
            babyZombies.add(new WeakReference<>((Zombie) event.getEntity()));
        }
    }

    @Override
    public void run() {
        LinkedList<WeakReference<Zombie>> toRemove = new LinkedList<>();
        for (WeakReference<Zombie> ref : babyZombies) {
            try {
                Zombie zombie = ref.get();
                if (zombie == null) {
                    toRemove.add(ref);
                    continue;
                }
                EntityZombie nmsZombie = (EntityZombie) nmsEntityField.get(zombie);

                //begin copied code from EntityZombie.e
                float f = nmsZombie.c(1.0F);

                if ((f > 0.5F) && (nmsZombie.world.i(new BlockPosition(MathHelper.floor(nmsZombie.locX), MathHelper.floor(nmsZombie.locY), MathHelper.floor(nmsZombie.locZ))))) {
                    long time = zombie.getWorld().getTime();
                    if (zombie.getLocation().getBlock().getLightFromSky() >= 15 && time <= 12000) {
                        zombie.setFireTicks(30); // TODO config
                    }
                }
                //end
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        babyZombies.removeAll(toRemove);
    }
}
