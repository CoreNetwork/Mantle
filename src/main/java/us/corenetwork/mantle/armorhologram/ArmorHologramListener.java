package us.corenetwork.mantle.armorhologram;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;


public class ArmorHologramListener implements Listener
{
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        HologramPlayerData data = HologramPlayerData.get(event.getPlayer().getUniqueId());
        data.clearDisplayedHolograms();

        processPlayerMoving(event.getPlayer(), event.getPlayer().getLocation(), true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (!event.getFrom().getChunk().equals(event.getTo().getChunk())) //We only need to update it every chunk, better performance
            processPlayerMoving(event.getPlayer(), event.getPlayer().getLocation(), false);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        processPlayerMoving(event.getPlayer(), event.getTo(), true);
    }

    public void processPlayerMoving(Player player, Location location, boolean teleport)
    {
        if (!HologramPlayerData.isPlayer18(player))
            return;

        HologramPlayerData playerData = HologramPlayerData.get(player.getUniqueId());
        for (Hologram hologram : HologramStorage.storage)
        {
            boolean displayed = playerData.isHologramDisplayed(hologram.getId());
            boolean inViewDistance = hologram.isInViewDistance(player);
            if ((displayed != inViewDistance) || (displayed && teleport))
            {
                if (displayed && !teleport)
                {
                    playerData.setHologramAsNotDisplayed(hologram.getId());
                }
                else
                {
                    playerData.addHologram(hologram.getId(), hologram.display(player));
                }
            }
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event)
    {
        for (Hologram hologram : HologramStorage.storage)
        {
            if (hologram.getChunkX() != event.getChunk().getX() || hologram.getChunkZ() != event.getChunk().getZ())
                return;

            for (Player player : Bukkit.getOnlinePlayers())
            {
                if (!HologramPlayerData.isPlayer18(player))
                    continue;

                HologramPlayerData playerData = HologramPlayerData.get(player.getUniqueId());

                if (!playerData.isHologramDisplayed(hologram.getId()) &&  hologram.isInViewDistance(player))
                {
                    playerData.addHologram(hologram.getId(), hologram.display(player));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event)
    {
        for (Hologram hologram : HologramStorage.storage)
        {
            if (hologram.getChunkX() != event.getChunk().getX() || hologram.getChunkZ() != event.getChunk().getZ())
                return;

            for (Player player : Bukkit.getOnlinePlayers())
            {
                if (!HologramPlayerData.isPlayer18(player))
                    continue;

                HologramPlayerData playerData = HologramPlayerData.get(player.getUniqueId());

                if (playerData.isHologramDisplayed(hologram.getId()))
                {
                    playerData.setHologramAsNotDisplayed(hologram.getId());
                }
            }
        }
    }
}
