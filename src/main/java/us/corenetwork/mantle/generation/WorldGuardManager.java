package us.corenetwork.mantle.generation;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import java.awt.*;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import us.corenetwork.mantle.util.RegionSet;

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
		} catch (StorageException e)
		{
			e.printStackTrace();
		}
	}

    public static RegionSet getRegionsInsideRectangle(World world, Rectangle rectangle)
    {
        WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        RegionManager manager = wg.getRegionManager(world);

        Map<String, ProtectedRegion> allRegions = manager.getRegions();


        RegionSet regionSet = new RegionSet();
        for (ProtectedRegion region : allRegions.values())
        {
            int width = region.getMaximumPoint().getBlockX() - region.getMinimumPoint().getBlockX() + 1;
            int height = region.getMaximumPoint().getBlockZ() - region.getMinimumPoint().getBlockZ() + 1;
            Rectangle regionRectangle = new Rectangle(region.getMinimumPoint().getBlockX(), region.getMaximumPoint().getBlockZ(), width, height);

            if (regionRectangle.intersects(rectangle))
            {
                regionSet.add(region);
            }
        }

        return regionSet;
    }
}
