package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_7_R4.EntityZombie;
import net.minecraft.server.v1_7_R4.MathHelper;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class BabyZombieBurner implements Listener, Runnable{
    private Set<Zombie> babyZombies = new HashSet<>();
    private Field nmsEntityField;

    public BabyZombieBurner() {
        try {
            nmsEntityField = CraftEntity.class.getDeclaredField("entity");
            nmsEntityField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Zombie && ((Zombie) event.getEntity()).isBaby()) {
            babyZombies.add((Zombie) event.getEntity());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCreatureDeath(EntityDeathEvent event) {
        babyZombies.remove(event.getEntity());
    }

    @Override
    public void run() {
        for (Zombie zombie : babyZombies) {
            try {
                EntityZombie nmsZombie = (EntityZombie) nmsEntityField.get(zombie);

                //begin copied code from EntityZombie.e
                float f = nmsZombie.d(1.0F);

                if ((f > 0.5F) && (nmsZombie.world.i(MathHelper.floor(nmsZombie.locX), MathHelper.floor(nmsZombie.locY), MathHelper.floor(nmsZombie.locZ)))) {
                    zombie.setFireTicks(30); // TODO config
                }
                //end
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }
}
