package us.corenetwork.mantle.portals;

import net.minecraft.server.v1_6_R3.AxisAlignedBB;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

import us.corenetwork.mantle.MLog;

public class PortalUtil {	
	public static Location processTeleport(final Entity entity)
	{
		Block portalBlock = getPortalBlock(entity);
		Block destination = getOtherSide(portalBlock);
				
		
		destination.getChunk().load();
		
		if (destination.getType() != Material.PORTAL)
		{
			int orientation = 0;
			BlockFace[] faces = new BlockFace[] { BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH };
			for (int i = 0; i < 4 ; i++)
			{
				if (portalBlock.getRelative(faces[i]).getType() == Material.PORTAL)
				{
					orientation = i;
					break;
				}
			}
			
			buildPortal(destination, orientation);
		}
				
		return getLocation(destination);		
	}

	public static Block getOtherSide(Block block)
	{
		Location currentSide = block.getLocation();

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
		Block existing = getExistingPortal(destination);
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

		return destBlock;
	}

	private static Block getExistingPortal(Location curLocation)
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
		Block closestPortal = null;
		
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
						closestPortal = block;
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
							closestPortal = block;
						}
					}
				}			
			}			
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

	public static Block getPortalBlock(Entity entity)
	{
		net.minecraft.server.v1_6_R3.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		AxisAlignedBB boundingBox = nmsEntity.boundingBox;
				
		// Need to check this after every NMS update, letters might change.
		int minX = (int) Math.floor(boundingBox.a);
		int minY = (int) Math.floor(boundingBox.b);
		int minZ = (int) Math.floor(boundingBox.c);
		int maxX = (int) Math.floor(boundingBox.d);
		int maxY = (int) Math.floor(boundingBox.e);
		int maxZ = (int) Math.floor(boundingBox.f);

		for (int x = minX; x <= maxX; x++)
		{
			for (int y = minY; y <= maxY; y++)
			{
				for (int z = minZ; z <= maxZ; z++)
				{
					int blockType = entity.getWorld().getBlockTypeIdAt(x, y, z);
					if (blockType == Material.PORTAL.getId())
					{
						return entity.getWorld().getBlockAt(x, y, z);
					}
				}
			}
		}

		Block block = entity.getLocation().getBlock();
		MLog.severe("Unable to find portal block at " + block.toString());
		return block;

	}
	
//	public static Block getPortalBlock(Location location)
//	{
//		Block block = location.getBlock();
//		if (block.getType() != Material.PORTAL)
//		{
//			double diffX = location.getX() - Math.floor(location.getX());
//			
//			if (diffX < 0.5)
//			{
//				Block newBlock = block.getRelative(-1, 0, 0);
//				if (newBlock.getType() == Material.PORTAL)
//					block = newBlock;
//			}
//			else
//			{
//				Block newBlock = block.getRelative(1, 0, 0);
//				if (newBlock.getType() == Material.PORTAL)
//					block = newBlock;
//			}
//			
//			if (block.getType() != Material.PORTAL)
//			{
//				double diffZ = location.getZ() - Math.floor(location.getZ());
//				
//				if (diffZ < 0.5)
//				{
//					Block newBlock = block.getRelative(0, 0, -1);
//					if (newBlock.getType() == Material.PORTAL)
//						block = newBlock;
//				}
//				else
//				{
//					Block newBlock = block.getRelative(0, 0, 1);
//					if (newBlock.getType() == Material.PORTAL)
//						block = newBlock;				
//				}
//
//			}
//
//		}		
//		
//		if (block.getType()  != Material.PORTAL)
//		{
//			MLog.severe("Unable to find portal block at " + block.toString());
//			return block;
//		}
//		
//		//Always pick northest, westest,lowest portal block
//		while (block.getRelative(BlockFace.DOWN).getType() == Material.PORTAL)
//			block = block.getRelative(BlockFace.DOWN);
//		while (block.getRelative(BlockFace.NORTH).getType() == Material.PORTAL)
//			block = block.getRelative(BlockFace.NORTH);
//		while (block.getRelative(BlockFace.WEST).getType() == Material.PORTAL)
//			block = block.getRelative(BlockFace.WEST);
//				
//		return block;
//	}
}
