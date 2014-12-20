package us.corenetwork.mantle.farming;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import us.corenetwork.mantle.util.BlockTraits;

public class PistonNoFarmListener implements Listener {

    @EventHandler
    public void onPistonPushEvent(BlockPistonExtendEvent event) {
        if (!FarmingModule.instance.active) {
            return;
        }
        boolean pushesAPumpkin = false;

        for (Block trace : event.getBlocks()) {
            Material mat = trace.getType();
            if (mat == Material.MELON_BLOCK || mat == Material.PUMPKIN) {
                pushesAPumpkin = true;
                break;
            }
        }

        if (pushesAPumpkin) {
            event.setCancelled(true);
        }
    }
}
