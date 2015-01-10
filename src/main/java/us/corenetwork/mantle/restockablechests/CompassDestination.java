package us.corenetwork.mantle.restockablechests;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.regeneration.RegenerationSettings;

public class CompassDestination {
	public static HashMap<UUID, CompassDestination> destinations = new HashMap<UUID, CompassDestination>();
	public static List<CompassDestinationHelper> oldDestinations = new LinkedList<CompassDestinationHelper>();
	private Location lastPlayerLocation;
	private int destX;
	private int destZ;
	private VillageInfoHelper vih;
	private Category category;
	public String compassName;

	public CompassDestination(int x, int z, VillageInfoHelper vih, Category category)
	{
		this.destX = x;
		this.destZ = z;
		this.vih = vih;
		this.category = category;
		lastPlayerLocation = new Location(Bukkit.getWorld("world"), 10000, 4, 10000);
		setCompassName(RChestSettings.MESSAGE_COMPASS_NAME_WITHOUT_DISTANCE.string().replace("<Category>", category.getDisplayName()));
	}

	public void setCompassName(String value)
	{
		compassName = ChatColor.translateAlternateColorCodes('&', value);
	}


	public static void addDestination(HumanEntity player, CompassDestination destination)
	{
		destination.refreshCompassTarget((Player) player);
		destinations.put(player.getUniqueId(), destination);
	}

	public void refreshCompassTarget(Player player)
	{
		player.setCompassTarget(new Location(Bukkit.getWorld("world"), this.destX, 4, this.destZ));
	}
	
	public VillageInfoHelper getVih()
	{
		return vih;
	}
	
	public void playerMoved(PlayerMoveEvent event)
	{
		Location to = event.getTo();
		
		if (lastPlayerLocation.distance(to) > 15)
		{
			updateCompass(event.getPlayer());
			lastPlayerLocation = to;
			
			//add checking if village is claimed, repick a village
			int padding = RegenerationSettings.RESORATION_VILLAGE_CHECK_PADDING.integer();
			if (GriefPreventionHandler.containsClaim(vih.world, vih.villageX, vih.villageZ, vih.xSize, vih.zSize, padding, false, null))
			{
				Util.Message(RChestSettings.MESSAGE_COMPASS_VILLAGE_CLAIMED.string(), event.getPlayer());
				destinations.remove(event.getPlayer().getUniqueId());
				GUICategoryPicker.selectChestForPlayerCategory(event.getPlayer(), category);
				
			}
		}
	}
	
	private void updateCompass(Player player)
	{
		Location playerLoc = player.getLocation();
		ItemStack compass = player.getItemInHand();

		ItemMeta meta = compass.getItemMeta();
		
		int diffX = playerLoc.getBlockX() - destX;
		int diffZ = playerLoc.getBlockZ() - destZ;
		int distance = (int) Math.sqrt(diffX * diffX + diffZ * diffZ);
		
		String message = RChestSettings.MESSAGE_COMPASS_NAME_WITH_DISTANCE.string();
		message = message.replace("<Distance>", distance+"");
		message = message.replace("<Category>", category.getDisplayName());
		setCompassName(message);
		meta.setDisplayName(compassName);
		compass.setItemMeta(meta);

        
	}

	//Removed destination from 'destinations' collection and adds it to 'oldDestinations' with timestamp
	//The purpouse is to not regenerate village that was set as a destination of a player who recently went offline
	public static void removeDestinationSlow(UUID uuid)
	{
		CompassDestination oldDest = destinations.get(uuid);
		if(oldDest != null)
		{
			destinations.remove(uuid);
			oldDestinations.add(new CompassDestinationHelper(oldDest));
		}
	}

	public static List<CompassDestination> getOldDestinations()
	{
		clearExpiredOldDestinations();
		List<CompassDestination> oldDests = new LinkedList<>();
		for(CompassDestinationHelper cdh : oldDestinations)
		{
			oldDests.add(cdh.destination);
		}
		return oldDests;
	}

	private static void clearExpiredOldDestinations()
	{
		Iterator<CompassDestinationHelper> it = oldDestinations.iterator();
		while(it.hasNext())
		{
			if(System.currentTimeMillis() - it.next().timestamp > RChestSettings.COMPASS_OLD_DESTINATION_EXPIRATION_TIME_SECONDS.integer() * 1000)
			{
				it.remove();
			}
		}
	}
}