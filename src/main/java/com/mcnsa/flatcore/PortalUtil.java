package com.mcnsa.flatcore;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

public class PortalUtil {

	public static void processTeleport(final Entity entity)
	{
		final Location destination = getOtherSide(entity.getLocation());
		if (destination.getBlock().getType() != Material.PORTAL)
			buildPortal(destination);
		
		entity.teleport(destination);
		Bukkit.getScheduler().scheduleSyncDelayedTask(MCNSAFlatcore.instance, new Runnable() {
			public void run() {
				entity.teleport(destination);
			}
		});
	}
	
	public static Location getOtherSide(Location currentSide)
	{
		//Always pick northest or westest portal block
		Block block = currentSide.getBlock();
		while (block.getRelative(BlockFace.NORTH).getType() == Material.PORTAL)
			block = block.getRelative(BlockFace.NORTH);
		while (block.getRelative(BlockFace.WEST).getType() == Material.PORTAL)
			block = block.getRelative(BlockFace.WEST);
		while (block.getRelative(BlockFace.NORTH).getType() == Material.PORTAL)
			block = block.getRelative(BlockFace.NORTH);

		currentSide = block.getLocation();
		
		double modifier;
		Environment destEnvironment;
		if (currentSide.getWorld().getEnvironment() == Environment.NETHER)
		{
			modifier = 8;
			destEnvironment = Environment.NORMAL;
		}
		else
		{
			modifier = 0.125;
			destEnvironment = Environment.NETHER;
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
		
		Block destBlock = destination.getBlock();
		if (destBlock.getType() == Material.LAVA || destBlock.getType() == Material.STATIONARY_LAVA)
		{
			destination.setY(40);
		}
		
		for (BlockFace face : new BlockFace[] {BlockFace.SELF, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH})
		{
			Block relativeBlock = destBlock.getRelative((int) (face.getModX() * modifier), 0, (int) (face.getModZ() * modifier));
			
			Location existing = getExistingPortal(relativeBlock.getLocation());
			if (existing != null)
				return existing;
		}
		
		while (destBlock.getRelative(BlockFace.DOWN, 2).getType() == Material.AIR)
		{
			destBlock = destBlock.getRelative(BlockFace.DOWN, 2);
		}
		
		return destBlock.getLocation();
	}
	
	private static Location getExistingPortal(Location curLocation)
	{
		int x = curLocation.getBlockX();
		int y = curLocation.getBlockY();
		int z = curLocation.getBlockZ();
		
		int minX = curLocation.getBlockX() - 10;
		int maxX = curLocation.getBlockX() + 10;
		int minZ = curLocation.getBlockZ() - 10;
		int maxZ = curLocation.getBlockZ() + 10;
		
		boolean xDir = false;
		boolean yDir = false;
		boolean zDir = false;
		
		while (true)
		{
			
			yDir = false;
			y = curLocation.getBlockY();
			
			while (true)
			{
				zDir = false;
				z = curLocation.getBlockZ();
				
				while (true)
				{
					Block block = curLocation.getWorld().getBlockAt(x, y, z);
					if (block != null)
					{
						if (block.getType() == Material.PORTAL)
						{
							Block below = block.getRelative(BlockFace.DOWN);
							if (below != null && below.getType() == Material.PORTAL)
							{
								return below.getLocation();
							}
							
							return block.getLocation();
						}
					}
					
					
					if (zDir) z++; else z--;
					if (z > maxZ)
					{
						z = curLocation.getBlockZ();
						zDir = false;
					}
					else if (z < minZ)
					{
						break;
					}
				}
				
				if (yDir) y++; else y--;
				if (y > 256)
				{
					y = curLocation.getBlockY();
					yDir = false;
				}
				else if (y < 1)
				{
					break;
				}
			}
			
			if (xDir) x++; else x--;
			if (x > maxX)
			{
				x = curLocation.getBlockX();
				xDir = false;
			}
			else if (x < minX)
			{
				break;
			}
		}
		
		return curLocation;
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
		
				
	}

}
