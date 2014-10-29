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

        Block trace = event.getBlock();
        for (int i = 0; i < 12; i++) {
            trace = trace.getRelative(event.getDirection());
            Material mat = trace.getType();
            if (mat == Material.MELON_BLOCK || mat == Material.PUMPKIN) {
                pushesAPumpkin = true;
                break;
            }
            if (mat == Material.AIR || !mat.isSolid() || BlockTraits.NO_PISTON_PUSH_BLOCKS.contains(mat)) {
                break;
            }
        }

        if (pushesAPumpkin) {
            event.setCancelled(true);
        }
    }
}
