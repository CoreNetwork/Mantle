package us.corenetwork.mantle.portals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.MLog;

public class PortalUtil {

	public static Location processTeleport(Entity entity, Block entryPortalBlock)
	{
		Block destinationBlock = prepareDestinationBlock(entryPortalBlock, entity);

		if(destinationBlock != null)
		{
			return finalizeTeleportDestination(entity, destinationBlock);
		}
		//null when the passing entity cannot create portal on the other side
		else
		{
			return null;
		}
	}

	/**
	 * Return block to which portal leads. Create new portal on the other side if necessary.
	 * @param entryPortalBlock - portal block that initiaded the teleport.
	 * @return teleportDestination - block where entryPortalBlock leads
	 */
	private static Block prepareDestinationBlock(Block entryPortalBlock, Entity entity)
	{
		PortalInfo portalInfo = PortalInfo.getPortalInfo(entryPortalBlock);
		Block teleportDestination = getOtherSide(portalInfo);

		teleportDestination.getChunk().load();

		//If != PORTAL, create a new portal.
		if (teleportDestination.getType() != Material.PORTAL)
		{
			//Determine if we should in fact create a portal there
			//Dealing with an edge case of already lit portal creating a new one in claimed land

			boolean canBuildAtDestination = true;

			Set<Claim> claims = getClaimsAround(teleportDestination);
			if(entity instanceof Player)
			{
				Player player = (Player) entity;
				for(Claim claim : claims)
				{
					if (claim.allowBuild(player, Material.STONE) != null)
					{
						canBuildAtDestination = false;
						break;
					}
				}
			}
			else
			{
				canBuildAtDestination = false;
			}

			if(!canBuildAtDestination)
			{
				//A player has no rights to create a portal on the other side (coz it would end up in claim)
				//Other entities also cannot do that
				return null;
			}

			Block buildDestination = findBestBuildDestination(teleportDestination, portalInfo);

			//Moving teleportDestination & buildDestination to config limits.
			int minY;
			int maxY;
			if (buildDestination.getWorld().getEnvironment() == Environment.NORMAL)
			{
				maxY = PortalsSettings.OVERWORLD_MOVE_PORTALS_WITH_HIGHER_Y.integer();
				minY = PortalsSettings.OVERWORLD_MOVE_PORTALS_WITH_LOWER_Y.integer();
			}
			else
			{
				maxY = PortalsSettings.NETHER_MOVE_PORTALS_WITH_HIGHER_Y.integer();
				minY = PortalsSettings.NETHER_MOVE_PORTALS_WITH_LOWER_Y.integer();
			}

			teleportDestination = moveToYRange(teleportDestination, minY, maxY);
			buildDestination = moveToYRange(buildDestination, minY, maxY);

			buildPortal(buildDestination, portalInfo.orientation);
		}

		return teleportDestination;
	}

	private static Block findBestBuildDestination(Block teleportDestination, PortalInfo originalPortalInfo)
	{
		Block buildDestination = teleportDestination;
        int destX = buildDestination.getX();
        int destZ = buildDestination.getZ();
		if (buildDestination.getWorld().getEnvironment() == Environment.NETHER)
		{
			List<Integer> occupiedOnX = getOccupiedSquaresInOwAxisX(originalPortalInfo);
			List<Integer> occupiedOnZ = getOccupiedSquaresInOwAxisZ(originalPortalInfo);
            Quadrant quadrant = getQuadrant(buildDestination);

			MLog.debug(quadrant.name());
			MLog.debug("before moving: " + buildDestination.getX() + " " + buildDestination.getY() + " " + buildDestination.getZ());


			//Case 1, original portal fits in one RxR square
			if (occupiedOnX.size() == 1 && occupiedOnZ.size() == 1)
			{
				MLog.debug("original portal fits in one RxR square");
                switch (quadrant)
                {
                    case PLUS_PLUS:
                        // Base on NORTH/WEST
                        if (originalPortalInfo.orientation == 1)
                            buildDestination = buildDestination.getRelative(BlockFace.NORTH);
                        else
                            buildDestination = buildDestination.getRelative(BlockFace.WEST);
                        break;
                    case MINUS_PLUS:
                        // Base on NORTH/EAST
                        if (originalPortalInfo.orientation == 1)
                            buildDestination = buildDestination.getRelative(BlockFace.NORTH);
                        break;
                    case PLUS_MINUS:
                        // Base on SOUTH/WEST
                        if (originalPortalInfo.orientation == 0)
                            buildDestination = buildDestination.getRelative(BlockFace.WEST);
                        break;
                }
			}
            else
            {
				MLog.debug("original portal doesnt fit in one RxR square");
                //portal is looking north/south, move east/west
                if (originalPortalInfo.orientation == 0)
                {
                    switch (quadrant)
                    {
                        case PLUS_MINUS:
                        case PLUS_PLUS:
                            if(occupiedOnX.contains(destX-1))
                                buildDestination = buildDestination.getRelative(BlockFace.WEST);
                            break;

                        case MINUS_PLUS:
                        case MINUS_MINUS:
                            if(!occupiedOnX.contains(destX+1))
                                buildDestination = buildDestination.getRelative(BlockFace.WEST);
                            break;
                    }
                }
                //portal is looking east/west, move north/south
                else
                {
                    switch (quadrant)
                    {
                        case MINUS_PLUS:
                        case PLUS_PLUS:
                            if(occupiedOnZ.contains(destZ-1))
                                buildDestination = buildDestination.getRelative(BlockFace.NORTH);
                            break;

                        case PLUS_MINUS:
                        case MINUS_MINUS:
                            if(!occupiedOnZ.contains(destZ+1))
                                buildDestination = buildDestination.getRelative(BlockFace.NORTH);
                            break;
                    }
                }
            }
			MLog.debug("after moving: " + buildDestination.getX() + " " + buildDestination.getY() + " " + buildDestination.getZ());
		}

		return buildDestination;
	}


	private static Block moveToYRange(Block block, int minY, int maxY)
	{
		int targetY = block.getY();
		if (block.getY() > maxY)
			targetY = maxY;
		else if (block.getY() < minY)
			targetY = minY;
		return block.getWorld().getBlockAt(block.getX(), targetY, block.getZ());
	}

	private static Location finalizeTeleportDestination(Entity entity, Block teleportDestinationBlock)
	{
		Location teleportDestinationLocation = new Location(teleportDestinationBlock.getWorld(), teleportDestinationBlock.getX() + 0.5, teleportDestinationBlock.getY(), teleportDestinationBlock.getZ() + 0.5);
		teleportDestinationLocation.setPitch(entity.getLocation().getPitch());
		teleportDestinationLocation.setYaw(entity.getLocation().getYaw());

		return teleportDestinationLocation;
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
		Block portalBlock = getOtherSideExact(info.entryBlock);

		//Find possible existing portal
		Block existing = getExistingPortal(info, portalBlock);
		if (existing != null)
			return existing;

		return portalBlock;
	}

	public static Block getOtherSideExact(Block entryBlock)
	{
		double modifier = PortalsSettings.PORTAL_RATIO.doubleNumber();
		Environment destEnvironment;
		if (entryBlock.getWorld().getEnvironment() == Environment.NETHER)
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

		return destWorld.getBlockAt((int) Math.floor(entryBlock.getX() * modifier), entryBlock.getY(), (int) Math.floor(entryBlock.getZ() * modifier));

	}

	private static Block getExistingPortal(PortalInfo sourcePortalInfo, Block targetBlock)
	{
		Block sourcePortalBlock = sourcePortalInfo.entryBlock;
		World targetWorld = targetBlock.getWorld();

		//Init stuff based on world
		int ratio = targetWorld.getEnvironment() == Environment.NETHER ? 1 : PortalsSettings.PORTAL_RATIO.integer();
		//Search only where portals can exist
		int minY = 0;
		int maxY = 256;
		if(targetWorld.getEnvironment() == Environment.NORMAL)
		{
			minY = PortalsSettings.OVERWORLD_MIN_Y.integer();
			maxY = PortalsSettings.OVERWORLD_MAX_Y.integer();
		}
		else if(targetWorld.getEnvironment() == Environment.NETHER)
		{
			minY = PortalsSettings.NETHER_MIN_Y.integer();
			maxY = PortalsSettings.NETHER_MAX_Y.integer();
		}
		List<Integer> yRange = getIncrementingNumbersInRange(targetBlock.getY(), minY, maxY);

		//We search in RxR square in overworld where R = ratio of the portal
		int originalSquareStartX = targetBlock.getX();
		int originalSquareStartZ = targetBlock.getZ();

				//Remove after removing debug:
				int originalSquareEndX = originalSquareStartX + (ratio - 1);
				int originalSquareEndZ = originalSquareStartZ + (ratio - 1);

		MLog.debug("PBL:" + sourcePortalInfo.portalBlocksLeftNorth + " " + sourcePortalInfo.portalBlocksLeftSouth + " " + sourcePortalInfo.portalBlocksLeftEast + " " + sourcePortalInfo.portalBlocksLeftWest);
		MLog.debug("Source coordinates:" + sourcePortalBlock.getX() + " " + sourcePortalBlock.getY() + " " + sourcePortalBlock.getZ());
		MLog.debug("Direct coordinates:" + targetBlock.getX() + " " + targetBlock.getY() + " " + targetBlock.getZ());
		MLog.debug("Original square:" + originalSquareStartX + "-" + originalSquareEndX + " Z:" + originalSquareStartZ + "-" + originalSquareEndZ);

		//Additional squares we search based on number of portal blocks
		List<Integer> squaresX;
		List<Integer> squaresZ;
		if(targetWorld.getEnvironment() == Environment.NORMAL)
		{
			squaresX = getIncrementingNumbersInRange(0, -sourcePortalInfo.portalBlocksLeftWest, sourcePortalInfo.portalBlocksLeftEast);
			squaresZ = getIncrementingNumbersInRange(0, -sourcePortalInfo.portalBlocksLeftNorth, sourcePortalInfo.portalBlocksLeftSouth);
		}
		else
		{

			//Ugly way of getting min/max
			List<Integer> rawSquaresInNX = getOccupiedSquaresInOwAxisX(sourcePortalInfo);
			int minXSquare = Collections.min(rawSquaresInNX) - originalSquareStartX;
			int maxXSquare = Collections.max(rawSquaresInNX) - originalSquareStartX;

			List<Integer> rawSquaresInNZ = getOccupiedSquaresInOwAxisZ(sourcePortalInfo);
			int minZSquare = Collections.min(rawSquaresInNZ) - originalSquareStartZ;
			int maxZSquare = Collections.max(rawSquaresInNZ) - originalSquareStartZ;

			squaresX = getIncrementingNumbersInRange(0, minXSquare, maxXSquare);
			squaresZ = getIncrementingNumbersInRange(0, minZSquare, maxZSquare);
		}
		MLog.debug("Square offsets to search X:" + squaresX);
		MLog.debug("Square offsets to search Z:" + squaresZ);


		// Loop through all RxR squares in X range
		for (Integer squareOffsetX : squaresX)
		{
			// Loop through all RxR squares in Z range
			for (Integer squareOffsetZ : squaresZ)
			{
				int startX = originalSquareStartX + squareOffsetX * ratio;
				int endX = startX + ratio;

				int startZ = originalSquareStartZ + squareOffsetZ * ratio;
				int endZ = startZ + ratio;

				// Loop through all RxR squares in Y range
				for (Integer y : yRange)
				{
					// Loop through all block in square
					for (int x = startX; x < endX; x++)
					{
						for (int z = startZ; z < endZ; z++)
						{
							Block block = targetWorld.getBlockAt(x, y, z);
							if (block != null && block.getType() == Material.PORTAL)
							{
								block = getFarthestPortalBlock(block, BlockFace.DOWN);
								return block;
							}
						}
					}
				}
			}
		}

		return null;
	}

	//Get a list of X coords of occupied squares. This will assume you are asking with OW portal, wont check on its own
	private static List<Integer> getOccupiedSquaresInOwAxisX(PortalInfo portalInfo)
	{
		return getOccupiedSquaresInOwInRange(portalInfo.entryBlock.getX() - portalInfo.portalBlocksLeftWest, portalInfo.entryBlock.getX() + portalInfo.portalBlocksLeftEast);
	}

	//Get a list of Z coords of occupied squares. This will assume you are asking with OW portal, wont check on its own
	private static List<Integer> getOccupiedSquaresInOwAxisZ(PortalInfo portalInfo)
	{
		return getOccupiedSquaresInOwInRange(portalInfo.entryBlock.getZ() - portalInfo.portalBlocksLeftNorth, portalInfo.entryBlock.getZ() + portalInfo.portalBlocksLeftSouth);
	}

	private static List<Integer> getOccupiedSquaresInOwInRange(int start, int end)
	{
		List<Integer> occupiedSquares = new LinkedList<>();
		int ratio = PortalsSettings.PORTAL_RATIO.integer();

		for(int i = start; i <= end; i++)
		{
			int sqaureCoord;

            if (i < 0)
                //sqaureCoord = -((Math.abs(i)-1) / ratio + 1);
                sqaureCoord = (int) Math.floor(i * (1.0/ratio));
            else
                sqaureCoord = i / ratio;

			if(!occupiedSquares.contains(sqaureCoord))
			{
				occupiedSquares.add(sqaureCoord);
			}
		}
		return occupiedSquares;
	}

	public static void buildPortal(Block startingBlock, int rotation)
	{
		SchematicBlock[] rotatedPortal = SchematicBlock.getRotatedSchematic(SchematicBlock.portal, rotation);
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

	// Return list of numbers that will move away equally from center
	// For example for parameters 0,-2,2: 0, 1, -1, 2, -2
	public static List<Integer> getIncrementingNumbersInRange(int start, int min, int max)
	{
		LinkedList<Integer> list = new LinkedList<Integer>();

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

	public static Set<Claim> getClaimsAround(Block block)
	{
		Set<Claim> claims = new HashSet<Claim>();

		//Search for 5x3x5 area around block
		for (int x = -2; x <= 2; x++)
		{
			for (int y = -1; y <= 2; y++)
			{
				for (int z = -2; z <= 2; z++)
				{
					Claim claim = GriefPrevention.instance.dataStore.getClaimAt(block.getRelative(x,y,z).getLocation(), true, null);
					if (claim != null)
						claims.add(claim);
				}
			}
		}
		return claims;
	}

    enum Quadrant{
        PLUS_PLUS,
        MINUS_MINUS,
        MINUS_PLUS,
        PLUS_MINUS
    }

    private static Quadrant getQuadrant(Block block)
    {
        if (block.getX() >= 0 && block.getZ() >= 0)
        {
            return Quadrant.PLUS_PLUS;
        }
        else if (block.getX() < 0 && block.getZ() >= 0)
        {
            return Quadrant.MINUS_PLUS;
        }
        else if (block.getX() >= 0 && block.getZ() < 0)
        {
            return  Quadrant.PLUS_MINUS;
        }
        else
        {
            return Quadrant.MINUS_MINUS;
        }
    }


}
