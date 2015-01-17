package us.corenetwork.mantle.perks;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import us.corenetwork.mantle.Util;

public class PerksListener implements Listener
{
    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event)
    {
        //Color signs - check permission
        if (Util.hasPermission(event.getPlayer(), "mantle.perks.advsigns"))
        {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(event.getBlock().getLocation(), true, null);
            if (!event.getPlayer().getUniqueId().equals(claim.ownerID))
                return; //Only owner of the claim can place color signs.

            for (int i = 0; i < event.getLines().length; i++)
            {
                event.setLine(i, Util.applyColors(event.getLine(i)));
            }
        }
    }
}
