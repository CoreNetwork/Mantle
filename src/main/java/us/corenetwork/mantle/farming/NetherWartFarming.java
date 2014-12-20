package us.corenetwork.mantle.farming;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.util.BlockTraits;

import java.util.Random;


public class NetherWartFarming implements Listener {
    public static final Material WART_BREAK_TOOL = Material.DIAMOND_HOE;
    public static final short TOOL_DAMAGE = 71;
    public static final ItemStack WART_DROP = new ItemStack(Material.NETHER_STALK, 1);
    public static final short DIAMOND_HOE_USES = 1562;
    private static Random random = new Random();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!FarmingModule.instance.active || event.isCancelled()) {
            return;
        }
        if (event.getBlock().getType() == Material.NETHER_WARTS) {
            boolean goodDrop = false;
            ItemStack tool = event.getPlayer().getItemInHand();
            short durability = tool.getDurability();
            if (tool.getType() == WART_BREAK_TOOL) {
                if (durability <= (1562 - TOOL_DAMAGE)) {
                    goodDrop = true;
                }
                tool.setDurability((short) (durability + TOOL_DAMAGE));

                if (tool.getDurability() > DIAMOND_HOE_USES) {
                    event.getPlayer().getInventory().setItemInHand(new ItemStack(Material.AIR));
                    event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ITEM_BREAK, 1f, 1f);
                }
            }


            if (!goodDrop) {
                event.setCancelled(true);

                dropBad(event.getBlock());
            }
        }

        if (event.getBlock().getType() == Material.SOUL_SAND) {
            Block warts = event.getBlock().getRelative(BlockFace.UP);
            if (warts.getType() == Material.NETHER_WARTS) {
                event.setCancelled(true);

                dropBad(warts);

                event.getBlock().breakNaturally();
            }
        }
    }

    @EventHandler
    public void onWaterFlow(BlockFromToEvent event) {
        if (!FarmingModule.instance.active || event.isCancelled()) {
            return;
        }
        if (BlockTraits.FLUID_BLOCKS.contains(event.getBlock().getType())) {
            if (event.getToBlock().getType() == Material.NETHER_WARTS) {
                dropBad(event.getToBlock());
            }
        }
    }

    @EventHandler
    public void onPistonPush(BlockPistonExtendEvent event) {
        if (!FarmingModule.instance.active || event.isCancelled()) {
            return;
        }


        for (Block trace : event.getBlocks()) {
            if (trace.getType() == Material.NETHER_WARTS) {
                dropBad(trace);
            }
            Block warts = trace.getRelative(BlockFace.UP);
            if (trace.getType() == Material.SOUL_SAND && warts.getType() == Material.NETHER_WARTS) {
                dropBad(warts);
            }
            warts = trace.getRelative(event.getDirection());
            if (warts.getType() == Material.NETHER_WARTS) {
                dropBad(warts);
            }
            Block relative = warts.getRelative(BlockFace.UP);
            if (warts.getType() == Material.SOUL_SAND && relative.getType() == Material.NETHER_WARTS) {
                dropBad(relative);
            }

        }
    }

    @EventHandler
    public void onPistonPull(BlockPistonRetractEvent event) {
        if (!FarmingModule.instance.active || event.isCancelled() || !event.isSticky()) {
            return;
        }
        Block pull = event.getBlock().getRelative(event.getDirection()).getRelative(event.getDirection());

        if (pull.getType() == Material.SOUL_SAND) {
            Block warts = pull.getRelative(BlockFace.UP);
            if (warts.getType() == Material.NETHER_WARTS) {
                dropBad(warts);
            }
        }
    }

    private void onBreak(Block block, float yield) {
        if (block.getType() == Material.SOUL_SAND) {
            Block warts = block.getRelative(BlockFace.UP);
            if (warts.getType() == Material.NETHER_WARTS) {
                dropBad(warts, yield);
            }
        }
        if (block.getType() == Material.NETHER_WARTS) {
            dropBad(block, yield);
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        if (!FarmingModule.instance.active || event.isCancelled()) {
            return;
        }
        for (Block block : event.blockList()) {
            onBreak(block, event.getYield());
        }
    }

    private static void dropBad(Block there) {
        dropBad(there, 1f);
    }

    private static void dropBad(Block there, float yield) {
        there.setType(Material.AIR);

        if (yield == 1f || random.nextFloat() <= yield) {
            Location dropLocation = there.getLocation().add(.5, .5, .5);
            dropLocation.getWorld().dropItemNaturally(dropLocation, WART_DROP);
        }
    }
}
