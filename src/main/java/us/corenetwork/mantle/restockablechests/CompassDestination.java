package us.corenetwork.mantle.restockablechests;

import java.util.HashMap;
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
	private Location lastPlayerLocation;
	private int destX;
	private int destZ;
	private VillageInfoHelper vih;
	private Category category;
	public CompassDestination(int x, int z, VillageInfoHelper vih, Category category)
	{
		this.destX = x;
		this.destZ = z;
		this.vih = vih;
		this.category = category;
		lastPlayerLocation = new Location(Bukkit.getWorld("world"), 10000, 4, 10000);
	}
	
	public static void addDestination(HumanEntity player, CompassDestination destination)
	{
		((Player) player).setCompassTarget(new Location(Bukkit.getWorld("world"), destination.destX, 4, destination.destZ));
		destinations.put(player.getUniqueId(), destination);
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
		
		String message = RChestSettings.MESSAGE_COMPASS_NAME.string();
		message = message.replace("<Distance>", distance+"");
		message = message.replace("<Category>", category.getLootTableName());
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', message));
		
		compass.setItemMeta(meta);
		player.getInventory().setItemInHand(null);
		player.getInventory().setItemInHand(compass);
	}
}