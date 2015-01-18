package us.corenetwork.mantle.perks;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.nanobot.NanobotUtil;

public class PerksListener implements Listener
{
    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event)
    {
        //Color signs - Only people with signs perk can place color signs.
        if (Util.hasPermission(event.getPlayer(), "mantle.perks.advsigns"))
        {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(event.getBlock().getLocation(), true, null);
            if (claim == null || !event.getPlayer().getUniqueId().equals(claim.ownerID))
                return; //Only owner of the claim can place color signs.

            for (int i = 0; i < event.getLines().length; i++)
            {
                event.setLine(i, Util.applyColors(event.getLine(i)));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        ItemStack stackInHand = event.getItem();
        if (stackInHand != null && stackInHand.getType() == Material.ARMOR_STAND)
        {
            net.minecraft.server.v1_8_R1.ItemStack nmsStack = NanobotUtil.getInternalNMSStack(stackInHand);
            if (!PerksUtil.isPerkItem(nmsStack))
                return;

            if (!canPlaceArmorStand(event.getPlayer(), event.getClickedBlock(), nmsStack))
            {
                event.setCancelled(true);
                event.getPlayer().updateInventory();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        ItemStack stackInHand = event.getItemInHand();
        if (stackInHand != null && stackInHand.getType() == Material.BANNER)
        {
            net.minecraft.server.v1_8_R1.ItemStack nmsStack = NanobotUtil.getInternalNMSStack(stackInHand);
            if (!PerksUtil.isPerkItem(nmsStack))
                return;

            if (!canPlaceBanner(event.getPlayer(), event.getBlockPlaced(), nmsStack))
            {
                event.setCancelled(true);
                event.getPlayer().updateInventory();
            }
        }


        if (stackInHand != null && stackInHand.getType() == Material.SKULL_ITEM)
        {
            net.minecraft.server.v1_8_R1.ItemStack nmsStack = NanobotUtil.getInternalNMSStack(stackInHand);
            if (!PerksUtil.isPerkItem(nmsStack))
                return;

            if (!canPlaceSkull(event.getPlayer(), event.getBlockPlaced(), nmsStack))
            {
                event.setCancelled(true);
                event.getPlayer().updateInventory();
            }
        }
    }

//    public void onBlockBreak(BlockBreakEvent event)
//    {
//        event.
//        Block block = event.getBlock();
//        if ()
//    }

    private static boolean canPlaceArmorStand(Player player, Block block, net.minecraft.server.v1_8_R1.ItemStack nmsStack)
    {
        if (!Util.hasPermission(player, "mantle.perks.advarmorstand"))
        {
            Util.Message(PerksSettings.MESSAGE_ARMOR_STAND_WRONG_PERMISSION.string(), player);
            return false;
        }

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(block.getLocation(), true, null);
        if (claim == null || !player.getUniqueId().equals(claim.ownerID))
        {
            Util.Message(PerksSettings.MESSAGE_ARMOR_STAND_WRONG_CLAIM.string(), player);
            return false;

        }

        return true;
    }

    private static boolean canPlaceBanner(Player player, Block block, net.minecraft.server.v1_8_R1.ItemStack nmsStack)
    {
        if (!Util.hasPermission(player, "mantle.perks.advbanners"))
        {
            Util.Message(PerksSettings.MESSAGE_BANNER_WRONG_PERMISSION.string(), player);
            return false;
        }

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(block.getLocation(), true, null);
        if (claim == null || !player.getUniqueId().equals(claim.ownerID))
        {
            Util.Message(PerksSettings.MESSAGE_BANNER_WRONG_CLAIM.string(), player);
            return false;

        }

        return true;
    }

    private static boolean canPlaceSkull(Player player, Block block, net.minecraft.server.v1_8_R1.ItemStack nmsStack)
    {
        if (!Util.hasPermission(player, "mantle.perks.advskulls"))
        {
            Util.Message(PerksSettings.MESSAGE_SKULL_WRONG_PERMISSION.string(), player);
            return false;
        }

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(block.getLocation(), true, null);
        if (claim == null || !player.getUniqueId().equals(claim.ownerID))
        {
            Util.Message(PerksSettings.MESSAGE_SKULL_WRONG_CLAIM.string(), player);
            return false;
        }

        return true;
    }

    private static boolean canPlaceBanner()
    {
        return false;
    }

}
