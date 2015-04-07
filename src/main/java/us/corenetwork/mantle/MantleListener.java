package us.corenetwork.mantle;

import java.util.HashSet;
import net.minecraft.server.v1_8_R2.IInventory;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftInventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import us.core_network.cornel.blocks.SignUtil;

public class MantleListener implements Listener {

    public static boolean disablePhysics = false;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event)
    {
        if (disablePhysics)
        {
            event.setCancelled(true);
            return;
        }

        //Do not drop colored sign
        if (event.getBlock().getState() instanceof Sign)
        {
            Sign sign = (Sign) event.getBlock().getState();

            if (SignUtil.isSignFormatted(sign))
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    //Dirty solution until god module is made in core
    public static HashSet<Integer> godEntities = new HashSet<Integer>();

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(final EntityDamageEvent event)
    {
        if (godEntities.contains(event.getEntity().getEntityId()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onChunkUnload(ChunkUnloadEvent event)
    {
        if (event.isCancelled())
        {
            MLog.info("Something cancelled chunk unload!");
        }
    }
}
