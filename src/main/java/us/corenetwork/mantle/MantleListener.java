package us.corenetwork.mantle;

import java.util.HashSet;
import net.minecraft.server.v1_7_R4.IInventory;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftInventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class MantleListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event)
    {
        //Do not drop colored sign
        if (event.getBlock().getState() instanceof Sign)
        {
            Sign sign = (Sign) event.getBlock().getState();

            String colorSymbol = "\u00A7";
            for (String line : sign.getLines())
            {
                if (line.contains(colorSymbol))
                {
                    event.setCancelled(true);
                    return;
                }
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

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event)
    {
        CraftInventory inventory = (CraftInventory) event.getInventory();
        IInventory nmsInventory = inventory.getInventory();
        if (nmsInventory instanceof InventoryGUI.GUIVanillaInventory)
        {
            ((InventoryGUI.GUIVanillaInventory) nmsInventory).click(event);
            event.setCancelled(true);
        }
    }
}
