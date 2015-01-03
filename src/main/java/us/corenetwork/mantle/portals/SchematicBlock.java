package us.corenetwork.mantle.portals;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import us.corenetwork.mantle.MLog;

public class SchematicBlock {
	public int modX;
	public int modY;
	public int modZ;
	public int rotation;
	public Material material;
	public boolean onlyInAir;

	//Moved here for clarity of PortalUtil
	public static final SchematicBlock[] portal = new SchematicBlock[] {
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



	public SchematicBlock(int modX, int modY, int modZ, Material material)
	{
		this.modX = modX;
		this.modY = modY;
		this.modZ = modZ;
		this.material = material;
		this.onlyInAir = false;
		rotation = 0;
	}
	
	public SchematicBlock(int modX, int modY, int modZ, Material material, boolean onlyInAir)
	{
		this.modX = modX;
		this.modY = modY;
		this.modZ = modZ;
		this.material = material;
		this.onlyInAir = onlyInAir;
		rotation = 0;
	}
	
	public static void placeSchematic(SchematicBlock[] schematic, Block origin)
	{
		for (SchematicBlock sBlock : schematic)
		{
			
			Block block = origin.getRelative(sBlock.modX, sBlock.modY, sBlock.modZ);
			if (block.getType() == Material.BEDROCK)
			{
				if (sBlock.material != Material.AIR && !sBlock.onlyInAir)
					MLog.severe("Portal is breaking bedrock at " + block);
				
				continue;
			}
			
			if (sBlock.onlyInAir && block.getRelative(BlockFace.DOWN).getType().isSolid())
			{
				continue;
			}
			
			if (sBlock.material == Material.PORTAL)
			{
				byte data;
				if (sBlock.rotation == 1 || sBlock.rotation == 3)
					data = 2;
				else
					data = 1;

				block.setTypeIdAndData(Material.PORTAL.getId(), data, false);
				continue;
			}
			
			block.setType(sBlock.material);
		}
	}
	
	public static SchematicBlock[] getRotatedSchematic(SchematicBlock[] original, int rotation)
	{
		if (rotation == 0)
		{
			return original;
		}
		else if (rotation == 1)
		{
			SchematicBlock[] schematic = new SchematicBlock[original.length];
			for (int i = 0; i < original.length; i++)
			{
				schematic[i] = new SchematicBlock(-original[i].modZ, original[i].modY, original[i].modX, original[i].material, original[i].onlyInAir);
				schematic[i].rotation = rotation;
			}
			return schematic;
		}
		else if (rotation == 2)
		{
			SchematicBlock[] schematic = new SchematicBlock[original.length];
			for (int i = 0; i < original.length; i++)
			{
				schematic[i] = new SchematicBlock(original[i].modZ, original[i].modY, -original[i].modX, original[i].material, original[i].onlyInAir);
				schematic[i].rotation = rotation;
			}
			return schematic;
		}
		else
		{
			SchematicBlock[] schematic = new SchematicBlock[original.length];
			for (int i = 0; i < original.length; i++)
			{
				schematic[i] = new SchematicBlock(-original[i].modZ, original[i].modY, original[i].modX, original[i].material, original[i].onlyInAir);
				schematic[i].rotation = rotation;
			}
			return schematic;
		}
	}
}
