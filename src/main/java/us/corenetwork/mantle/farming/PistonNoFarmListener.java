package us.corenetwork.mantle.farming;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;

public class PistonNoFarmListener implements Listener {

    @EventHandler
    public void onPistonPushEvent(BlockPistonExtendEvent event) {
        boolean melonDisable = FarmingModule.instance.config.getBoolean("Piston.Melon", false);
        boolean pumpkinDisable = FarmingModule.instance.config.getBoolean("Piston.Pumpkin", false);
        if (!FarmingModule.instance.active) {
            return;
        }
        boolean pushesAPumpkin = false;

        for (Block trace : event.getBlocks()) {
            Material mat = trace.getType();
            if ((melonDisable && mat == Material.MELON_BLOCK) || (pumpkinDisable && mat == Material.PUMPKIN)) {
                pushesAPumpkin = true;
                break;
            }
        }

        if (pushesAPumpkin) {
            event.setCancelled(true);
        }
    }
}
