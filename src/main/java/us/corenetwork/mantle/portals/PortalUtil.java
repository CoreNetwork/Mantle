package us.corenetwork.mantle.portals;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

import us.corenetwork.mantle.MLog;

public class PortalUtil {	
	public static Location processTeleport(final Entity entity)
	{
		final Location destination = getOtherSide(entity.getLocation());
		
		destination.getChunk().load();
		
		if (destination.getBlock().getType() != Material.PORTAL)
			buildPortal(destination);

		MLog.info(destination.getBlock().getType().toString());
		
		return destination;		
	}

	public static Location getOtherSide(Location currentSide)
	{
		//Always pick northest or westest portal block
		Block block = currentSide.getBlock();
		
		if (block.getType() != Material.PORTAL)
		{
			if (block.getRelative(BlockFace.EAST).getType() == Material.PORTAL)
				block = block.getRelative(BlockFace.EAST);
			if (block.getRelative(BlockFace.SOUTH).getType() == Material.PORTAL)
				block = block.getRelative(BlockFace.SOUTH);
		}
		
		while (block.getRelative(BlockFace.NORTH).getType() == Material.PORTAL)
			block = block.getRelative(BlockFace.NORTH);
		while (block.getRelative(BlockFace.WEST).getType() == Material.PORTAL)
			block = block.getRelative(BlockFace.WEST);
		while (block.getRelative(BlockFace.NORTH).getType() == Material.PORTAL)
			block = block.getRelative(BlockFace.NORTH);

		currentSide = block.getLocation();

		double modifier = PortalsSettings.PORTAL_RATIO.doubleNumber();
		Environment destEnvironment;
		int maxY = 0;
		int minY = 0;
		if (currentSide.getWorld().getEnvironment() == Environment.NETHER)
		{
			destEnvironment = Environment.NORMAL;
			maxY = PortalsSettings.OVERWORLD_MOVE_PORTALS_WITH_HIGHER_Y.integer();
			minY = PortalsSettings.OVERWORLD_MOVE_PORTALS_WITH_LOWER_Y.integer();
		}
		else
		{
			modifier = 1 / modifier;
			destEnvironment = Environment.NETHER;
			maxY = PortalsSettings.NETHER_MOVE_PORTALS_WITH_HIGHER_Y.integer();
			minY = PortalsSettings.NETHER_MOVE_PORTALS_WITH_LOWER_Y.integer();
		}
		
		World destWorld = null;
		for (World world : Bukkit.getServer().getWorlds())
		{
			if (world.getEnvironment() == destEnvironment)
			{
				destWorld = world;
				break;
			}
		}

		Location destination = new Location(destWorld, Math.floor(currentSide.getBlockX() * modifier), currentSide.getBlockY(), Math.floor(currentSide.getBlockZ() * modifier));

		if (destination.getY() > maxY)
			destination.setY(maxY);
		else if (destination.getY() < minY)
			destination.setY(minY);

		//Find possible existing portal
		Location existing = getExistingPortal(destination);
		if (existing != null)
			return existing;

		//Increase height if original dest is inside java
		Block destBlock = destination.getBlock();
		if (destBlock.getType() == Material.LAVA || destBlock.getType() == Material.STATIONARY_LAVA)
		{
			destBlock = destination.getWorld().getBlockAt(destination.getBlockX(), 40, destination.getBlockZ());
		}

		//Try to get to the ground
		if (destEnvironment == Environment.NORMAL)
		{
			Block belowBlock = destBlock.getRelative(BlockFace.DOWN, 2);

			int startingY = belowBlock.getY();
			while (belowBlock != null && belowBlock.getType() == Material.AIR && belowBlock.getY() <= startingY)
			{
				destBlock = destBlock.getRelative(BlockFace.DOWN);
				belowBlock = destBlock.getRelative(BlockFace.DOWN, 2);
			}
		}

		return destBlock.getLocation();
	}

	private static Location getExistingPortal(Location curLocation)
	{
		int minY;
		int maxY;
		if (curLocation.getWorld().getEnvironment() == Environment.NETHER)
		{
			minY = PortalsSettings.NETHER_MIN_Y.integer();
			maxY = PortalsSettings.NETHER_MAX_Y.integer();

		}
		else
		{
			minY = PortalsSettings.OVERWORLD_MIN_Y.integer();
			maxY = PortalsSettings.OVERWORLD_MAX_Y.integer();
		}
		
		int radius = Math.max(curLocation.getWorld().getEnvironment() == Environment.NETHER ? 1 : (int) Math.ceil(PortalsSettings.PORTAL_RATIO.doubleNumber()), 1);
		
		int x = 0;
		int y = 0;
		int dir = 0;
		int trenutniMax = 1;

		int startX = 0;
		int startY = 0;

		int closestPortalDistance = Integer.MAX_VALUE;
		Location closestPortal = null;
		
		for (int h = minY; h < maxY; h++)
		{
			Block block = curLocation.getWorld().getBlockAt(x + curLocation.getBlockX(), h, y + curLocation.getBlockZ());
			if (block != null)
			{
				if (block.getType() == Material.PORTAL)
				{
					Location portalLoc = block.getLocation();
					int distance = (int) Math.round(portalLoc.distanceSquared(curLocation));
					if (distance < closestPortalDistance)
					{
						closestPortalDistance = distance;
						closestPortal = portalLoc;
					}
				}
			}			
		}
		
		while (true)
		{
			switch (dir)
			{
			case 0:
				y--;
				break;
			case 1:
				x++;
				break;
			case 2:
				y++;
				break;
			case 3:
				x--;
				break;
			}

			if (Math.abs(x - startX) >= trenutniMax && dir % 2 == 1)
			{
				trenutniMax++;
				dir++;
				if (dir > 3)
				{
					dir = 0;
				}

				startY = y;					
			}
			else if (Math.abs(y - startY)  >= trenutniMax && dir % 2 == 0)
			{
				dir++;
				if (dir > 3)
				{
					dir = 0;
				}

				startX = x;
			}
			
			if (Math.abs(x) > radius || Math.abs(y) > radius)
			{
				break;
			}
			
			for (int h = minY; h < maxY; h++)
			{
				Block block = curLocation.getWorld().getBlockAt(x + curLocation.getBlockX(), h, y + curLocation.getBlockZ());
				if (block != null)
				{
					if (block.getType() == Material.PORTAL)
					{
						Location portalLoc = block.getLocation();
						int distance = (int) Math.round(portalLoc.distanceSquared(curLocation));
						if (distance < closestPortalDistance)
						{
							closestPortalDistance = distance;
							closestPortal = portalLoc;
						}
					}
				}			
			}			
		}

		return closestPortal;
	}

	public static void buildPortal(Location location)
	{
		Block baseBlock = location.getBlock();
		
		//Build top and bottom frame
		for (int x = -1; x < 3; x++)
		{
			baseBlock.getRelative(x, -1, 0).setType(Material.OBSIDIAN);
			baseBlock.getRelative(x, 3, 0).setType(Material.OBSIDIAN);
		}

		//Build side frame
		for (int y = -1; y < 3; y++)
		{
			baseBlock.getRelative(-1, y, 0).setType(Material.OBSIDIAN);
			baseBlock.getRelative(2, y, 0).setType(Material.OBSIDIAN);

		}


		//Place portals and clear space
		for (int x = 0; x < 2; x++)
		{
			for (int y = 0; y < 3; y++)
			{
				baseBlock.getRelative(x, y, -1).setType(Material.AIR);
				baseBlock.getRelative(x, y, 1).setType(Material.AIR);
				baseBlock.getRelative(x, y, 0).setTypeId(Material.PORTAL.getId(), false);
			}
		}

		if (!baseBlock.getRelative(0, -2, 0).getType().isSolid())
		{
			//Overhang
			for (int x = 0; x < 2; x++)
			{
				baseBlock.getRelative(x, -1, -1).setType(Material.OBSIDIAN);
				baseBlock.getRelative(x, -1, 1).setType(Material.OBSIDIAN);

			}
		}
		
		//Clear out nearby lava
		
		for (int x = -2; x < 4; x++)
		{
			for (int y = 0; y < 5; y++)
			{
				for (int z = -2; z < 3; z++)
				{
					Block block = baseBlock.getRelative(x, y, z);
					
					if (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA)
						block.setType(Material.AIR);

				}
			}
		}

	}

}
