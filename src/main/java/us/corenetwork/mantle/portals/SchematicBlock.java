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
	public Material material;
	public boolean onlyInAir;
	
	public SchematicBlock(int modX, int modY, int modZ, Material material)
	{
		this.modX = modX;
		this.modY = modY;
		this.modZ = modZ;
		this.material = material;
		this.onlyInAir = false;
	}
	
	public SchematicBlock(int modX, int modY, int modZ, Material material, boolean onlyInAir)
	{
		this.modX = modX;
		this.modY = modY;
		this.modZ = modZ;
		this.material = material;
		this.onlyInAir = onlyInAir;
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
				block.setTypeId(Material.PORTAL.getId(), false);
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
			}
			return schematic;
		}
		else if (rotation == 2)
		{
			SchematicBlock[] schematic = new SchematicBlock[original.length];
			for (int i = 0; i < original.length; i++)
			{
				schematic[i] = new SchematicBlock(original[i].modZ, original[i].modY, -original[i].modX, original[i].material, original[i].onlyInAir);
			}
			return schematic;
		}
		else
		{
			SchematicBlock[] schematic = new SchematicBlock[original.length];
			for (int i = 0; i < original.length; i++)
			{
				schematic[i] = new SchematicBlock(-original[i].modZ, original[i].modY, original[i].modX, original[i].material, original[i].onlyInAir);
			}
			return schematic;
		}
	}
}
