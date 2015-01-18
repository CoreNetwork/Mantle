package us.corenetwork.mantle.holograms;


import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;


public class HologramsListener implements Listener
{
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event)
    {
        HologramsChunk hologramsChunk = HologramStorage.getChunkData(event.getWorld().getName(), event.getChunk().getX(), event.getChunk().getZ());
        if (hologramsChunk != null)
            hologramsChunk.update();
    }

    //WorldGuard workaround
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event)
    {
        if (!event.isCancelled() || event.getEntityType() != EntityType.ARMOR_STAND)
            return;

        event.setCancelled(false);
    }
}
