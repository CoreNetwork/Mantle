package us.corenetwork.mantle.portals;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.server.v1_8_R1.BlockDoor;
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
		Block portalBlock = portalInfo.entryBlock;
		Block destination = getOtherSide(portalInfo);

		
		destination.getChunk().load();
		
		if (destination.getType() != Material.PORTAL)
		{
			//Portal placement in nether needs some extra care
			if (destination.getWorld().getEnvironment() == Environment.NETHER)
			{
				// Base portal placing on different blocks in different world quadrants to make sure portals do not cut corners
				// Initially we base it on westest, southest block so to rebase, we just need to move base

				if (destination.getX() > 0 && destination.getZ() > 0) // ++ quadrant
				{
					Bukkit.broadcastMessage("++ quadrant");
					Bukkit.broadcastMessage("before moving: " + destination.getX() + " " + destination.getY() + " " + destination.getZ());

					// Base on NORTH/WEST
					if (portalInfo.orientation == 1)
						destination = destination.getRelative(BlockFace.NORTH, portalInfo.sizeZ - 1);
					else
						destination = destination.getRelative(BlockFace.WEST, portalInfo.sizeX - 1);

					Bukkit.broadcastMessage("after moving: " + destination.getX() + " " + destination.getY() + " " + destination.getZ());

				}
				else if (destination.getX() < 0 && destination.getZ() < 0) // -- quadrant
				{
					// Base on SOUTH/EAST
					if (portalInfo.orientation == 1)
						destination = destination.getRelative(BlockFace.SOUTH, portalInfo.sizeZ - 1);
					else
						destination = destination.getRelative(BlockFace.EAST, portalInfo.sizeX - 1);
				}
				else if (destination.getX() < 0 && destination.getZ() > 0) // +- quadrant
				{
					// Base on NORTH/EAST
					if (portalInfo.orientation == 1)
						destination = destination.getRelative(BlockFace.NORTH, portalInfo.sizeZ - 1);
					else
						destination = destination.getRelative(BlockFace.EAST, portalInfo.sizeX - 1);
				}
				else // +- quadrant and (0,0)
				{
					// Base on SOUTH/WEST
					if (portalInfo.orientation == 1)
						destination = destination.getRelative(BlockFace.SOUTH, portalInfo.sizeZ - 1);
					else
						destination = destination.getRelative(BlockFace.WEST, portalInfo.sizeX - 1);

				}
			}


			int maxY = 0;
			int minY = 0;
			if (portalBlock.getWorld().getEnvironment() == Environment.NETHER)
			{
				maxY = PortalsSettings.OVERWORLD_MOVE_PORTALS_WITH_HIGHER_Y.integer();
				minY = PortalsSettings.OVERWORLD_MOVE_PORTALS_WITH_LOWER_Y.integer();
			}
			else
			{
				maxY = PortalsSettings.NETHER_MOVE_PORTALS_WITH_HIGHER_Y.integer();
				minY = PortalsSettings.NETHER_MOVE_PORTALS_WITH_LOWER_Y.integer();
			}

			if (destination.getY() > maxY)
				destination = destination.getWorld().getBlockAt(destination.getX(), maxY, destination.getZ());
			else if (destination.getY() < minY)
				destination = destination.getWorld().getBlockAt(destination.getX(), minY, destination.getZ());


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
		dummy.entryBlock = block;
		dummy.sizeZ = 1;
		dummy.sizeX = 1;

		return getOtherSide(dummy);
	}

	public static Block getOtherSide(PortalInfo info)
	{
		Block portalBlock = info.entryBlock;

		double modifier = PortalsSettings.PORTAL_RATIO.doubleNumber();
		Environment destEnvironment;
		if (portalBlock.getWorld().getEnvironment() == Environment.NETHER)
		{
			destEnvironment = Environment.NORMAL;
		}
		else
		{
			modifier = 1 / modifier;
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

		portalBlock = destWorld.getBlockAt((int) Math.floor(portalBlock.getX() * modifier), portalBlock.getY(), (int) Math.floor(portalBlock.getZ() * modifier));

		//Find possible existing portal
		Block existing = getExistingPortal(info, portalBlock);
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

	private static Block getExistingPortal(PortalInfo sourcePortalInfo, Block targetBlock)
	{
		Block sourcePortalBlock = sourcePortalInfo.entryBlock;
		World targetWorld = targetBlock.getWorld();

		// Nether -> Overworld

		int minY = PortalsSettings.OVERWORLD_MIN_Y.integer();
		int maxY = PortalsSettings.OVERWORLD_MAX_Y.integer();
		int ratio = targetWorld.getEnvironment() == Environment.NETHER ? 1 : PortalsSettings.PORTAL_RATIO.integer();

		//We search in RxR square in overworld where R = ratio of the portal
		int overworldSquareStartX = targetBlock.getX();
		int overworldSquareEndX = overworldSquareStartX + (ratio - 1);
		int overworldSquareStartZ = targetBlock.getZ();
		int overworldSquareEndZ = overworldSquareStartZ + (ratio - 1);
		int centerX = overworldSquareStartX + ratio / 2;
		int centerZ = overworldSquareStartZ + ratio / 2;

		//Increase search range for additional portal blocks
		overworldSquareStartX -= sourcePortalInfo.portalBlocksLeftWest * ratio;
		overworldSquareEndX += sourcePortalInfo.portalBlocksLeftEast * ratio;
		overworldSquareStartZ -= sourcePortalInfo.portalBlocksLeftNorth * ratio;
		overworldSquareEndZ += sourcePortalInfo.portalBlocksLeftSouth * ratio;

		Bukkit.broadcastMessage("PBL:" + sourcePortalInfo.portalBlocksLeftNorth + " " + sourcePortalInfo.portalBlocksLeftSouth + " " + sourcePortalInfo.portalBlocksLeftEast + " " + sourcePortalInfo.portalBlocksLeftWest);
		Bukkit.broadcastMessage("Source coordinates:" + sourcePortalBlock.getX() + " " + sourcePortalBlock.getY() + " " + sourcePortalBlock.getZ());
		Bukkit.broadcastMessage("Direct coordinates:" + targetBlock.getX() + " " + targetBlock.getY() + " " + targetBlock.getZ());
		Bukkit.broadcastMessage("Searching X:" + overworldSquareStartX + "-" + overworldSquareEndX + " Z:" + overworldSquareStartZ + "-" + overworldSquareEndZ + " Center:" + centerX + "," + centerZ);

		List<Integer> xCoordinates = getIncrementingNumbersInRange(centerX, overworldSquareStartX, overworldSquareEndX);
		List<Integer> zCoordinates = getIncrementingNumbersInRange(centerZ, overworldSquareStartZ, overworldSquareEndZ);

		for (Integer y : getIncrementingNumbersInRange(targetBlock.getY(), minY, maxY))
		{
			//int y = targetBlock.getY();
			for (Integer x : xCoordinates)
			{
				for (Integer z : zCoordinates)
				{
					//System.out.println("searching " + x + " " + z);

					Block block = targetWorld.getBlockAt(x, y, z);
					if (block != null && block.getType() == Material.PORTAL)
					{
						block = getFarthestPortalBlock(block, BlockFace.DOWN);
						return block;
					}
				}
			}
		}
		
		return null;
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
		block = getFarthestPortalBlock(block, BlockFace.DOWN);

		PortalInfo info = new PortalInfo();
		info.sizeX = 1;
		info.sizeZ = 1;
		info.orientation = getPortalBlockOrientation(block);

		Block secondBlock = block;
		if (info.orientation == 1)
		{
			//Find size in Z-
			while (true)
			{
				Block neighbour = secondBlock.getRelative(BlockFace.SOUTH);
				if (neighbour.getType() != Material.PORTAL)
					break;

				info.portalBlocksLeftSouth++;
				info.sizeZ++;

				secondBlock = neighbour;
			}

			//Find size in Z+
			secondBlock = block;
			while (true)
			{
				Block neighbour = secondBlock.getRelative(BlockFace.NORTH);
				if (neighbour.getType() != Material.PORTAL)
					break;

				info.portalBlocksLeftNorth++;
				info.sizeZ++;

				secondBlock = neighbour;
			}
		}
		else
		{
			//Find size in X+
			secondBlock = block;
			while (true)
			{
				Block neighbour = secondBlock.getRelative(BlockFace.EAST);
				if (neighbour.getType() != Material.PORTAL)
					break;

				info.portalBlocksLeftEast++;
				info.sizeX++;

				secondBlock = neighbour;
			}

			secondBlock = block;
			//Find size in X-
			while (true)
			{
				Block neighbour = secondBlock.getRelative(BlockFace.WEST);
				if (neighbour.getType() != Material.PORTAL)
					break;

				info.portalBlocksLeftWest++;
				info.sizeX++;

				secondBlock = neighbour;
			}
		}

		info.entryBlock = block;

		return info;
	}

	public static Block getPortalBlockWithLowestCoordinates(Block block, int portalOrientation)
	{
		BlockFace direction = portalOrientation == 0 ? BlockFace.WEST : BlockFace.SOUTH;
		return getFarthestPortalBlock(block, direction);
	}

	public static Block getFarthestPortalBlock(Block block, BlockFace direction)
	{
		while (true)
		{
			Block neighbour = block.getRelative(direction);
			if (neighbour.getType() != Material.PORTAL)
				break;

			block = neighbour;
		}

		return block;
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

	public static int getPortalBlockOrientation(Block block)
	{
		byte data = block.getData();
		if ((data & 3) == 2) //Portal block is rotated Z way
		{
			return 1;
		}
		else
		{
			return 0;
		}

	}

	// Return list of numbers that will move away equally from center
	// For example for parameters 0,-2,2: 0, 1, -1, 2, -2
	// Can anyone come up with better name please?
	public static List<Integer> getIncrementingNumbersInRange(int start, int min, int max)
	{
		LinkedList list = new LinkedList<Integer>();


		int stop = Math.max(Math.abs(min), Math.abs(max));
		max-= start;
		min -= start;

		int a = 0;
		while (true)
		{
			if (a <= max && a >= min)
				list.add(a + start);

			if (a > 0)
				a = -a;
			else
			{
				a = -a + 1;
				if (a > stop)
					break;
			}
		}

		return list;
	}
}
