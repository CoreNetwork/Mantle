package us.corenetwork.mantle.perks;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import us.core_network.cornel.common.Messages;
import us.core_network.cornel.custom.PerksUtil;
import us.core_network.cornel.items.ItemStackUtils;
import us.core_network.cornel.items.NbtUtils;
import us.core_network.cornel.player.PlayerUtil;
import us.corenetwork.core.scoreboard.CoreScoreboardManager;
import us.corenetwork.core.util.ScoreboardUtils;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;

import java.util.UUID;

public class PerksListener implements Listener
{
    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event)
    {
        //Color signs - Only people with signs perk can place color signs.
        if (PlayerUtil.hasPermission(event.getPlayer(), "mantle.perks.advsigns"))
        {
            if(!PlayerUtil.hasPermission(event.getPlayer(), "mantle.perks.advsigns.anywhere"))
            {
                Claim claim = GriefPrevention.instance.dataStore.getClaimAt(event.getBlock().getLocation(), true, null);
                if (claim == null)
                    return; //No claim, so nope

                UUID ownerID = claim.ownerID;
                if (claim.parent != null) //is a subdivision
                    ownerID = claim.parent.ownerID;

                if (!event.getPlayer().getUniqueId().equals(ownerID))
                    return; //Only owner of the claim can place color signs.
            }

            for (int i = 0; i < event.getLines().length; i++)
            {
                event.setLine(i, Messages.applyFormattingCodes(event.getLine(i)));
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
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = ItemStackUtils.getInternalNMSStack(stackInHand);
            if (!PerksUtil.isSupposedToBePerkArmorStandItem(nmsStack.getTag())) //Do not perform any checks if item is not subscriber-only
                return;

            if (!canPlaceArmorStand(event.getPlayer(), event.getClickedBlock(), nmsStack))
            {
                event.setCancelled(true);
                event.getPlayer().updateInventory();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event)
    {
        Player player = event.getPlayer();
        EntityType clickedType = event.getRightClicked().getType();
        if(clickedType != EntityType.ARMOR_STAND && clickedType != EntityType.ITEM_FRAME)
        {
            return;
        }

        ItemStack stackInHand = player.getItemInHand();

        if (stackInHand != null)
        {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = ItemStackUtils.getInternalNMSStack(stackInHand);
            if(nmsStack == null)
                return;

            if (!PerksUtil.isSupposedToBePerkBannerItem(nmsStack.getTag()) &&
                    !PerksUtil.isSupposedToBePerkArmorStandItem(nmsStack.getTag()) &&
                    !PerksUtil.iSupposedToBePerkSkullItem(nmsStack.getTag()))
                return;

            Block blockLocation = event.getRightClicked().getLocation().getBlock();


            if (    !canPlaceArmorStand(player, blockLocation, nmsStack) ||
                    !canPlaceSkull(player, blockLocation, nmsStack) ||
                    !canPlaceBanner(player, blockLocation, nmsStack))
            {
                if (clickedType == EntityType.ARMOR_STAND)
                    event.setCancelled(true);
                else
                    returnFromFrame((ItemFrame) event.getRightClicked(), player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        ItemStack stackInHand = event.getItemInHand();
        if (stackInHand != null && stackInHand.getType() == Material.BANNER)
        {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = ItemStackUtils.getInternalNMSStack(stackInHand);
            if (!PerksUtil.isSupposedToBePerkBannerItem(nmsStack.getTag())) //Do not perform any checks if item is not subscriber-only
                return;

            if (!canPlaceBanner(event.getPlayer(), event.getBlockPlaced(), nmsStack))
            {
                event.setCancelled(true);
                event.getPlayer().updateInventory();
            }
        }

        if (stackInHand != null && stackInHand.getType() == Material.SKULL_ITEM)
        {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = ItemStackUtils.getInternalNMSStack(stackInHand);
            if (!PerksUtil.iSupposedToBePerkSkullItem(nmsStack.getTag()))
                return;

            if (!canPlaceSkull(event.getPlayer(), event.getBlockPlaced(), nmsStack))
            {
                event.setCancelled(true);
                event.getPlayer().updateInventory();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event)
    {
        ItemStack item = event.getItem().getItemStack();
        if (item.getType() == Material.BANNER)
        {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = ItemStackUtils.getInternalNMSStack(item);
            if (PerksUtil.isSupposedToBePerkBannerItem(nmsStack.getTag()))
            {
                BannerRecipeProxy.addLoreToBanner(nmsStack);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        //Colored nameplates
        Player player = event.getPlayer();
        applyColoredNameplate(player);
    }

    public static void applyColoredNameplate(Player player)
    {
        if (!PlayerUtil.hasPermission(player, "mantle.perks.nameplate"))
            return;

        String groupName = MantlePlugin.chat.getPrimaryGroup(player);

        String teamName = getTeamName(groupName);

        String prefix = Messages.applyFormattingCodes(MantlePlugin.chat.getGroupPrefix((String) null, groupName));

        if (teamName.length() > 16) //According to some arbitrary limit, team names can't be longer than 16 characters.
            teamName = teamName.substring(0, 16);

        boolean moved = ScoreboardUtils.movePlayerToTeam(player, teamName);
        if (moved)
        {
            Team newTeam = CoreScoreboardManager.getTeamsScoreboard().getTeam(teamName);
            if (!newTeam.getPrefix().equals(prefix)) //Update prefix (usually only done for the first time team has been accessed)
                newTeam.setPrefix(prefix);
        }
    }

    private static String getTeamName(String groupName)
    {
        String prefix = "rankTeam";
        String groupTeamName = PerksModule.instance.config.getString(PerksSettings.GROUP_TEAM_NAMES.getString() + "." + groupName);
        if(groupTeamName == null)
        {
            groupTeamName = PerksModule.instance.config.getString(PerksSettings.GROUP_TEAM_NAMES.getString() + ".Other");
        }
        return prefix + groupTeamName;
    }


    private static void returnFromFrame(final ItemFrame frame, final Player player)
    {
        //Return item to the player after one tick
        Bukkit.getScheduler().runTask(MantlePlugin.instance, new Runnable()
        {
            @Override
            public void run()
            {
                ItemStack stack = frame.getItem();
                frame.setItem(null);

                player.getInventory().addItem(stack);
            }
        });
    }

    private static boolean canPlaceArmorStand(Player player, Block block, net.minecraft.server.v1_8_R3.ItemStack nmsStack)
    {
        if (!PlayerUtil.hasPermission(player, "mantle.perks.advarmorstand"))
        {
            Messages.send(PerksSettings.MESSAGE_ARMOR_STAND_WRONG_PERMISSION.string(), player);
            return false;
        }

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(block.getLocation(), true, null);
        if (claim == null || !player.getUniqueId().equals(claim.ownerID))
        {
            Messages.send(PerksSettings.MESSAGE_ARMOR_STAND_WRONG_CLAIM.string(), player);
            return false;

        }

        return true;
    }

    private static boolean canPlaceBanner(Player player, Block block, net.minecraft.server.v1_8_R3.ItemStack nmsStack)
    {
        if (!PlayerUtil.hasPermission(player, "mantle.perks.advbanners"))
        {
            Messages.send(PerksSettings.MESSAGE_BANNER_WRONG_PERMISSION.string(), player);
            return false;
        }

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(block.getLocation(), true, null);
        if (claim == null || !player.getUniqueId().equals(claim.ownerID))
        {
            Messages.send(PerksSettings.MESSAGE_BANNER_WRONG_CLAIM.string(), player);
            return false;

        }

        return true;
    }

    private static boolean canPlaceSkull(Player player, Block block, net.minecraft.server.v1_8_R3.ItemStack nmsStack)
    {
        if (!PlayerUtil.hasPermission(player, "mantle.perks.advskulls"))
        {
            Messages.send(PerksSettings.MESSAGE_SKULL_WRONG_PERMISSION.string(), player);
            return false;
        }

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(block.getLocation(), true, null);
        if (claim == null || !player.getUniqueId().equals(claim.ownerID))
        {
            Messages.send(PerksSettings.MESSAGE_SKULL_WRONG_CLAIM.string(), player);
            return false;
        }

        return true;
    }
}
