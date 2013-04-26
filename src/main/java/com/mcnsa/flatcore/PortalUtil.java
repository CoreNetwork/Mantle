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
	public static Location processTeleport(final Entity entity)
	{
		final Location destination = getOtherSide(entity.getLocation());
		if (destination.getBlock().getType() != Material.PORTAL)
			buildPortal(destination);

		return destination;		
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

			while (belowBlock != null && belowBlock.getType() == Material.AIR)
			{
				destBlock = destBlock.getRelative(BlockFace.DOWN);
				belowBlock = destBlock.getRelative(BlockFace.DOWN, 2);
			}
		}

		return destBlock.getLocation();
	}

	private static Location getExistingPortal(Location curLocation)
	{
		int radius = curLocation.getWorld().getEnvironment() == Environment.NETHER ? 5 : 20;
		int x = 0;
		int y = 0;
		int dir = 0;
		int trenutniMax = 1;

		int startX = 0;
		int startY = 0;

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

			if (Math.abs(x) + 1 >= radius || Math.abs(y) + 1 >= radius)
			{
				break;
			}

			for (int h = 1; h < 256; h++)
			{
				Block block = curLocation.getWorld().getBlockAt(x + curLocation.getBlockX(), h, y + curLocation.getBlockZ());
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
			}
		}

		return null;
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
