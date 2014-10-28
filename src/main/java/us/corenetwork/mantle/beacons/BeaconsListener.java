package us.corenetwork.mantle.beacons;

import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;


public class BeaconsListener implements Listener
{

      public void onPlayerInventoryClick(InventoryClickEvent event)
      {
      }
//    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
//    public void onPlayerInteract(PlayerInteractEvent event)
//    {
//        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
//            return;
//
//        if (event.getClickedBlock().getType() != Material.BEACON)
//            return;
//
//        CustomBeaconTileEntity tileEntity = (CustomBeaconTileEntity) ((CraftWorld) event.getClickedBlock().getWorld()).getHandle().getTileEntity(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ());
//        if (tileEntity == null)
//        {
//            MLog.warning("Beacon tile entity null at " + event.getClickedBlock());
//            return;
//        }
//
//        tileEntity.clicked(event.getPlayer());
//
//    }
}
