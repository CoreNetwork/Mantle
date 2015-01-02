package us.corenetwork.mantle.portals;

import com.sk89q.worldedit.math.MathUtils;
import javax.print.attribute.standard.NumberUp;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

import java.util.ArrayList;

public class PortalUtil {	
	public static Location processTeleport(Entity entity, Block currentPortalBlock)
	{
		PortalInfo portalInfo = getPortalInfo(currentPortalBlock);
		Block portalBlock = portalInfo.lowestNorthestWestestBlock;
		Block destination = getOtherSide(portalInfo);

		
		destination.getChunk().load();
		
		if (destination.getType() != Material.PORTAL)
		{
			//If portal is in upper part of the rounding zone, use block with higher coordinatest as opposed to lower one
			if (currentPortalBlock.getWorld().getEnvironment() == Environment.NORMAL)
			{
				double ratio = Math.max(Math.ceil(PortalsSettings.PORTAL_RATIO.doubleNumber()), 1);

				double dividedCoordinatesX = (double) portalBlock.getX() / ratio;
				double dividedCoordinatesZ = (double) portalBlock.getX() / ratio;

				if (portalInfo.sizeX > 1 && dividedCoordinatesX > 1 && getFractionPart(dividedCoordinatesX) > 0.5)
				{
					destination = destination.getRelative(-(portalInfo.sizeX - 1), 0, 0);
				}
				else if (portalInfo.sizeZ > 1 && dividedCoordinatesZ > 1 && getFractionPart(dividedCoordinatesZ) > 0.5)
				{
					destination = destination.getRelative(0, 0, -(portalInfo.sizeX - 1));
				}
			}

			buildPortal(destination, portalInfo.orientation);
		}
				
		Location blockLocation = getLocation(destination);
		blockLocation.setPitch(entity.getLocation().getPitch());
		blockLocation.setYaw(entity.getLocation().getYaw());

		return blockLocation;		
	}

	public static Block getOtherSide(Block block)
	{
		PortalInfo dummy = new PortalInfo();
		dummy.lowestNorthestWestestBlock = block;
		dummy.sizeZ = 1;
		dummy.sizeX = 1;

		return getOtherSide(dummy);
	}

	public static Block getOtherSide(PortalInfo info)
	{
		Block portalBlock = info.lowestNorthestWestestBlock;

		double modifier = PortalsSettings.PORTAL_RATIO.doubleNumber();
		Environment destEnvironment;
		int maxY = 0;
		int minY = 0;
		if (portalBlock.getWorld().getEnvironment() == Environment.NETHER)
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

		portalBlock = destWorld.getBlockAt((int) (portalBlock.getX() * modifier), portalBlock.getY(), (int) (portalBlock.getZ() * modifier));

		if (portalBlock.getY() > maxY)
			portalBlock = destWorld.getBlockAt(portalBlock.getX(), maxY, portalBlock.getZ());
		else if (portalBlock.getY() < minY)
			portalBlock = destWorld.getBlockAt(portalBlock.getX(), minY, portalBlock.getZ());

		//Find possible existing portal
		info.lowestNorthestWestestBlock = portalBlock;
		Block existing = getExistingPortal(info);
		if (existing != null)
			return existing;

		//Try to get to the ground
		if (destEnvironment == Environment.NORMAL)
		{
			Block belowBlock = portalBlock.getRelative(BlockFace.DOWN, 2);

			int startingY = belowBlock.getY();
			while (belowBlock != null && belowBlock.getType() == Material.AIR && belowBlock.getY() <= startingY)
			{
				portalBlock = portalBlock.getRelative(BlockFace.DOWN);
				belowBlock = portalBlock.getRelative(BlockFace.DOWN, 2);
			}
		}

		return portalBlock;
	}

	private static Block getExistingPortal(PortalInfo portalInfo)
	{
		Block portalBlock = portalInfo.lowestNorthestWestestBlock;
		World world = portalBlock.getWorld();

		int minY;
		int maxY;

		if (world.getEnvironment() == Environment.NETHER)
		{
			minY = PortalsSettings.NETHER_MIN_Y.integer();
			maxY = PortalsSettings.NETHER_MAX_Y.integer();

		}
		else
		{
			minY = PortalsSettings.OVERWORLD_MIN_Y.integer();
			maxY = PortalsSettings.OVERWORLD_MAX_Y.integer();
		}

		int closestPortalDistance = Integer.MAX_VALUE;
		Block closestPortal = null;

		//Find existing portal in 2D spiral
		int ratio = Math.max(world.getEnvironment() == Environment.NETHER ? 1 : (int) Math.ceil(PortalsSettings.PORTAL_RATIO.doubleNumber()), 1);

		//Spiral size depends on the size of origin portal

		int width = 1;
		int height = 1;
		int centerX = portalBlock.getX();
		int centerZ = portalBlock.getZ();

		if (portalInfo.sizeX > 1 || ratio > 1)
		{
			width = ratio * (portalInfo.sizeX + 1);

			//Make sure height and width of the spiral are even
			if (width % 2 == 0)
				width++;

			centerX = centerX - ratio + width / 2;
		}

		if (portalInfo.sizeZ > 1 || ratio > 1)
		{
			height = ratio * (portalInfo.sizeZ + 1);

			if (height % 2 == 0)
				height++;

			centerZ = portalBlock.getZ() - ratio + height / 2;
		}

		int x=0, z=0, dx = 0, dz = -1;
		int t = Math.max(width,height);
		int maxI = t*t;

		for (int i=0; i < maxI; i++){


			if ((-width/2 <= x) && (x <= width/2) && (-height/2 <= z) && (z <= height/2)) {
				Bukkit.broadcastMessage((centerX + x) + " " + (centerZ + z));

				for (int y = minY; y < maxY; y++)
				{
					Block block = world.getBlockAt(centerX + x, y, centerZ + z);
					if (block != null)
					{
						if (block.getType() == Material.PORTAL)
						{
							Location portalLoc = block.getLocation();
							int distance = Math.abs(y - portalBlock.getY());
							if (distance < closestPortalDistance)
							{
								closestPortalDistance = distance;
								closestPortal = block;
							}
						}
					}
				}

				if (closestPortal != null)
					break;
			}

			if( (x == z) || ((x < 0) && (x == -z)) || ((x > 0) && (x == 1-z))) {
				t=dx;
				dx=-dz;
				dz=t;
			}
			x+=dx;
			z+=dz;
		}

		if (closestPortal == null)
			return null;

		while (true)
		{
			Block bottomBlock = closestPortal.getRelative(BlockFace.DOWN);
			if (bottomBlock.getType() != Material.PORTAL || bottomBlock.getY() > closestPortal.getY())
				break;
			
			closestPortal = bottomBlock;
		}
		
		
		return closestPortal;
	}

	private static final SchematicBlock[] portal = new SchematicBlock[] {
		//Front view:
		// OOOO
		// OPPO
		// OPPO
		// OXPO
		// OOOO
		// O = Obsidian
		// P = Portal
		// X = Origin portal block
	
		//Side view:
		// AOA
		// AOA
		// AOA
		// AOA
		// OOO
		// A = Air
		
		//Top frame
		new SchematicBlock(-1, 3, 0, Material.OBSIDIAN),
		new SchematicBlock(0, 3, 0, Material.OBSIDIAN),
		new SchematicBlock(1, 3, 0, Material.OBSIDIAN),
		new SchematicBlock(2, 3, 0, Material.OBSIDIAN),
		
		//Bottom frame
		new SchematicBlock(-1, -1, 0, Material.OBSIDIAN),
		new SchematicBlock(0, -1, 0, Material.OBSIDIAN),
		new SchematicBlock(1, -1, 0, Material.OBSIDIAN),
		new SchematicBlock(2, -1, 0, Material.OBSIDIAN),
		
		//Left side
		new SchematicBlock(-1, -1, 0, Material.OBSIDIAN),
		new SchematicBlock(-1, 0, 0, Material.OBSIDIAN),
		new SchematicBlock(-1, 1, 0, Material.OBSIDIAN),
		new SchematicBlock(-1, 2, 0, Material.OBSIDIAN),

		//Right side
		new SchematicBlock(2, -1, 0, Material.OBSIDIAN),
		new SchematicBlock(2, 0, 0, Material.OBSIDIAN),
		new SchematicBlock(2, 1, 0, Material.OBSIDIAN),
		new SchematicBlock(2, 2, 0, Material.OBSIDIAN),
		
		//Obsidian ledge
		new SchematicBlock(0, -1, 1, Material.OBSIDIAN, true),
		new SchematicBlock(1, -1, 1, Material.OBSIDIAN, true),
		new SchematicBlock(0, -1, -1, Material.OBSIDIAN, true),
		new SchematicBlock(1, -1, -1, Material.OBSIDIAN, true),
		
		//Portal blocks
		new SchematicBlock(0, 0, 0, Material.PORTAL),
		new SchematicBlock(0, 1, 0, Material.PORTAL),
		new SchematicBlock(0, 2, 0, Material.PORTAL),
		new SchematicBlock(1, 0, 0, Material.PORTAL),
		new SchematicBlock(1, 1, 0, Material.PORTAL),
		new SchematicBlock(1, 2, 0, Material.PORTAL),
		
		//Air pockets around portal
		new SchematicBlock(0, 0, 1, Material.AIR),
		new SchematicBlock(0, 1, 1, Material.AIR),
		new SchematicBlock(0, 2, 1, Material.AIR),
		new SchematicBlock(1, 0, 1, Material.AIR),
		new SchematicBlock(1, 1, 1, Material.AIR),
		new SchematicBlock(1, 2, 1, Material.AIR),
		new SchematicBlock(0, 0, -1, Material.AIR),
		new SchematicBlock(0, 1, -1, Material.AIR),
		new SchematicBlock(0, 2, -1, Material.AIR),
		new SchematicBlock(1, 0, -1, Material.AIR),
		new SchematicBlock(1, 1, -1, Material.AIR),
		new SchematicBlock(1, 2, -1, Material.AIR)

	};
	public static void buildPortal(Block startingBlock, int rotation)
	{
		SchematicBlock[] rotatedPortal = SchematicBlock.getRotatedSchematic(portal, rotation);
		SchematicBlock.placeSchematic(rotatedPortal, startingBlock);
 		
		//Clear out nearby lava
		for (int x = -2; x < 4; x++)
		{
			for (int y = 0; y < 5; y++)
			{
				for (int z = -2; z < 4; z++)
				{
					Block block = startingBlock.getRelative(x, y, z);
					
					if (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA)
						block.setType(Material.AIR);
				}
			}
		}

	}
	
	private static Location getLocation(Block block)
	{
		return new Location(block.getWorld(), block.getX() + 0.5, block.getY(), block.getZ() + 0.5);
	}

	public static PortalInfo getPortalInfo(Block block)
	{
		while (block.getRelative(BlockFace.DOWN).getType() == Material.PORTAL)
			block = block.getRelative(BlockFace.DOWN);

		PortalInfo info = new PortalInfo();
		info.sizeX = 1;
		info.sizeZ = 1;

		//Find size in Z+
		Block secondBlock = block;
		while (secondBlock.getRelative(BlockFace.SOUTH).getType() == Material.PORTAL)
		{
			info.sizeZ++;
			secondBlock = secondBlock.getRelative(BlockFace.SOUTH);
		}

		//Find size in X+
		secondBlock = block;
		while (secondBlock.getRelative(BlockFace.EAST).getType() == Material.PORTAL)
		{
			info.sizeX++;
			secondBlock = secondBlock.getRelative(BlockFace.EAST);
		}

		//Find size in Z-
		secondBlock = block;
		while (secondBlock.getRelative(BlockFace.NORTH).getType() == Material.PORTAL)
		{
			info.sizeZ++;
			secondBlock = secondBlock.getRelative(BlockFace.NORTH);
		}

		//Find size in X+
		secondBlock = block;
		while (secondBlock.getRelative(BlockFace.WEST).getType() == Material.PORTAL)
		{
			info.sizeX++;
			secondBlock = secondBlock.getRelative(BlockFace.WEST);
		}

		byte data = block.getData();
		if ((data & 3) == 2) //Portal block is rotated Z way
		{
			info.orientation = 1;

			//Find northest portal block
			while (block.getRelative(BlockFace.NORTH).getType() == Material.PORTAL)
			{
				block = block.getRelative(BlockFace.NORTH);
			}

		}
		else
		{
			info.orientation = 0;

			//Find westest portal block
			while (block.getRelative(BlockFace.WEST).getType() == Material.PORTAL)
			{
				block = block.getRelative(BlockFace.WEST);
			}
		}

		info.lowestNorthestWestestBlock = block;

		return info;
	}
	
	public static Block findBestSignLocation(ArrayList<Block> blocks)
	{
		//Try to find block that has something on the opposite side (for example two sides of portal frame). 
		// That ensures sign will be inside portal
		for (Block b : blocks)
		{
			if (!b.getType().isSolid())
				continue;

			for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH})
			{
				Block neighbour = b.getRelative(face);
				if (!neighbour.isEmpty())
					continue;

				for (Block potentialFacingBlock : blocks)
				{
					if (potentialFacingBlock == b)
						continue;

					if (b.getY() == potentialFacingBlock.getY() &&
							(face.getModX() != 0 || potentialFacingBlock.getX() == b.getX()) &&
							(face.getModZ() != 0 || potentialFacingBlock.getZ() == b.getZ()) &&
							face.getModX() < 0 == potentialFacingBlock.getX() < b.getX() &&
							face.getModZ() < 0 == potentialFacingBlock.getZ() < b.getZ())
					{
						return neighbour;
					}
				}
			}
		}

		for (Block b : blocks)
		{
			if (!b.getType().isSolid())
				continue;

			Block upperBlock = b.getRelative(BlockFace.UP);

			if (upperBlock != null && upperBlock.isEmpty())
				return upperBlock;
		}

		for (Block b : blocks)
		{
			if (b.isEmpty())
				return b;
		}

		return blocks.get(0);
	}

	public static double getFractionPart(double a)
	{
		return a - (int) a;
	}
}
