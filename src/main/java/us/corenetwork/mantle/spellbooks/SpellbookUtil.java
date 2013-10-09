package us.corenetwork.mantle.spellbooks;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class SpellbookUtil {
	public static Location getPointInFrontOfPlayer(Location playerEyeLoc, double distance)
	{
		Vector direction = playerEyeLoc.getDirection();
		
		return playerEyeLoc.clone().add(direction.getX() * distance, direction.getY() * distance, direction.getZ() * distance);
	}
}
