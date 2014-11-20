package us.corenetwork.mantle.slimeballs;


import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.Util;


public class SlimeballsListener implements Listener
{
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        // AIR interact events are always cancelled in advance on Bukkit for some reason...
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.isCancelled())))
            return;

        if (!event.hasItem())
            return;

        ItemStack playerItemInHand = event.getItem();
        if (!SlimeballItem.isSlimeball(playerItemInHand))
            return;

        if (SlimeballsStorage.getSlimeballs(event.getPlayer().getUniqueId()) < 1)
        {
            Util.Message(SlimeballsSettings.MESSAGE_SLIMEBALLS_RELEASE_EMPTY_ACCOUNT.string(), event.getPlayer());
            return;
        }

         new FancyMessage(Util.applyColors(SlimeballsSettings.MESSAGE_SLIMEBALL_CLICK_TEXT_PREFIX.string()))
                .then(Util.applyColors(SlimeballsSettings.MESSAGE_SLIMEBALL_CLICK_TEXT_BUTTON.string()))
                    .command("/slimeballs pay")
                 .then(Util.applyColors(SlimeballsSettings.MESSAGE_SLIMEBALL_CLICK_TEXT_SUFFIX.string()))
             .send(event.getPlayer());


    }
}
