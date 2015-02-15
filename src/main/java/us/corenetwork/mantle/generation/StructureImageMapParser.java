package us.corenetwork.mantle.generation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import us.corenetwork.mantle.MLog;

public class StructureImageMapParser implements MapIterator {
	private HashMap<Integer, StructureData> structures = new HashMap<Integer, StructureData>();
	private BufferedImage image;
	
	private int worldMinX;
	private int worldMinZ;
	
	private int rows;
	private int columns;
	
	private int row;
	private int column;
	private int tileSizeX;
	private int tileSizeZ;

	public StructureImageMapParser(World world, File file, MemorySection worldConfig, int worldSizeX, int worldSizeZ, int worldMinX, int worldMinZ)
	{
		this.worldMinX = worldMinX;
		this.worldMinZ = worldMinZ;
		row = 0;
		column = -1;
		rows = 0;
		columns = 0;
		
		MLog.info("Preparing structures...");

		MemorySection structuresConfig = (MemorySection) worldConfig.get("Structures");
		for (Entry<String,Object> e : structuresConfig.getValues(false).entrySet())
		{
			StructureData structure = new StructureData(e.getKey(), (MemorySection) e.getValue(), world);
			structures.put(structure.getImageColor(), structure);
		}
		
		try
		{
			image = ImageIO.read(file);

		}
		catch (IOException e)
		{
			MLog.info("Error while loading " + file.getName() + " ...");
			return;
		}
		
		int width = image.getWidth();
		int height = image.getHeight();
		if (width % 20 != 0 || height % 20 != 0)
		{
			MLog.info("Image's dimensions must be divisable by 20! Aborting...");
			return;
		}
				
		rows = height / 20;
		columns = width / 20;
		
		tileSizeX = worldSizeX / columns;
		tileSizeZ = worldSizeZ / rows;
		
		MLog.info("Size of every letter's tile on the map is " + tileSizeX + "x" + tileSizeZ + ". Structures will be placed into center of each area.");
	}


	@Override
	public boolean advance() {	
		do
		{
			column++;
			if (column >= columns)
			{
				column = 0;
				row++;
				if (row >= rows)
				{
					return false;
				}
			}
		}
		while (getCurStructure() == null);
		
		int rowOneBased = row + 1;
		int colOneBased = column + 1;
		int percentage = (row * columns + column) * 100 / (rows * columns);
		
		MLog.info("Image map generation, location " + colOneBased + "x" + rowOneBased + " [" + percentage + "%]...");
		
		return true;
	}

	@Override
	public StructureData getCurStructure() {
		if (column < 0)
			return null;
		
		int pixelX = column * 20;
		int pixelZ = row * 20;
		
		int color = image.getRGB(pixelX, pixelZ) & 0x00FFFFFF;
						
		return structures.get(color);
	}

	@Override
	public int getCurX() {
		return column * tileSizeX + tileSizeX / 2 + worldMinX;
	}

	@Override
	public int getCurZ() {
		return row * tileSizeZ + tileSizeZ / 2 + worldMinZ;

	}
}
