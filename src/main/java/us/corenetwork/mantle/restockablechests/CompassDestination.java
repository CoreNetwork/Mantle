package us.corenetwork.mantle.restockablechests;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CompassDestination {
	public static HashMap<UUID, CompassDestination> destinations = new HashMap<UUID, CompassDestination>();	
	private Location lastPlayerLocation;
	private int destX;
	private int destZ;
	
	public CompassDestination(int x, int z)
	{
		this.destX = x;
		this.destZ = z;
	}
	
	
	public void playerMoved(PlayerMoveEvent event)
	{
		Location to = event.getTo();
		
		if (lastPlayerLocation == null || 
		   ((to.getWorld() == lastPlayerLocation.getWorld()) && 
				   (to.distanceSquared(lastPlayerLocation)) > 25 || Math.abs(to.getYaw() - lastPlayerLocation.getYaw()) > 36))
		{
			updateCompass(event.getPlayer());
			lastPlayerLocation = to;
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
		int targetAngle = (int) Math.toDegrees(Math.atan2(diffX, diffZ));
		int angleDiff = (int) (targetAngle  - playerLoc.getYaw());
		if (angleDiff < -180)
			angleDiff += 360;
		int tick = (angleDiff + 180) / 20;
		
		
		StringBuilder textCompass = new StringBuilder();
		for (int i = 0; i <= 17; i++)
		{
			if (i == tick)
				textCompass.append('|');
			else
				textCompass.append('-');
		}
		
		meta.setDisplayName(textCompass.toString());
		//meta.setDisplayName(targetAngle + " " + " " + playerLoc.getYaw() + " " + " " + angleDiff + " " + distance + " " + tick);

		
		compass.setItemMeta(meta);
		//player.getInventory().setItemInHand(null);
		player.getInventory().setItemInHand(compass);
		//player.sendMessage(String.valueOf(System.currentTimeMillis()));
	}
}