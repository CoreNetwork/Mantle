package us.corenetwork.mantle.spellbooks;

import java.lang.reflect.Field;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class SpellbookUtil {
	public static Location getPointInFrontOfPlayer(Location playerEyeLoc, double distance)
	{
		Vector direction = playerEyeLoc.getDirection();
		
		return playerEyeLoc.clone().add(direction.getX() * distance, direction.getY() * distance, direction.getZ() * distance);
	}

	public static net.minecraft.server.v1_7_R3.ItemStack getNMSInnerItem(ItemStack bukkitStack)
	{
		try
		{
			Field handleField = CraftItemStack.class.getDeclaredField("handle");
			handleField.setAccessible(true);
			
			return (net.minecraft.server.v1_7_R3.ItemStack) handleField.get(bukkitStack);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static boolean compareItemTypes(net.minecraft.server.v1_7_R3.ItemStack a, net.minecraft.server.v1_7_R3.ItemStack b)
	{
		return 	 (a.getItem() == b.getItem() && a.getData() == b.getData() && 
				  ((a.tag != null && a.tag.equals(b.tag)) || (a.tag == null && b.tag == null)));

	}
}
