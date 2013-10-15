package us.corenetwork.mantle.generation;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

public class WorldGuardManager {
	public static void createRegion(Location firstPoint, Location secondPoint, String name, String exampleRegion)
	{
		name = name.toLowerCase();
		
		WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
		RegionManager manager = wg.getRegionManager(firstPoint.getWorld());
		
		BlockVector firstVector = new BlockVector(firstPoint.getX(), firstPoint.getY(), firstPoint.getZ());
		BlockVector secondVector = new BlockVector(secondPoint.getX(), secondPoint.getY(), secondPoint.getZ());
	
		ProtectedRegion region = new ProtectedCuboidRegion(name, firstVector, secondVector);
		
		if (exampleRegion != null)
		{
			ProtectedRegion eRegion = manager.getRegion(exampleRegion);
			
			region.setFlags(eRegion.getFlags());
			region.setMembers(eRegion.getMembers());
			region.setOwners(eRegion.getOwners());
			try {
				region.setParent(eRegion.getParent());
			} catch (CircularInheritanceException e1) {
				e1.printStackTrace();
			}
		}
		
		manager.addRegion(region);
		try {
			manager.save();
		} catch (ProtectionDatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
