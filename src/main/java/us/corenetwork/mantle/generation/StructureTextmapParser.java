package us.corenetwork.mantle.generation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.configuration.MemorySection;

import us.corenetwork.mantle.MLog;

public class StructureTextmapParser implements MapIterator {
	private HashMap<Character, StructureData> structures = new HashMap<Character, StructureData>();
	private List<String> textMap;
	
	private int worldMinX;
	private int worldMinZ;
	
	private int rows;
	private int columns;
	
	private int row;
	private int column;
	private int tileSizeX;
	private int tileSizeZ;

	public StructureTextmapParser(File file, MemorySection worldConfig, int worldSizeX, int worldSizeZ, int worldMinX, int worldMinZ)
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
			StructureData structure = new StructureData(e.getKey(), (MemorySection) e.getValue());
			structures.put(structure.getTextAlias(), structure);
		}
		
		textMap = new ArrayList<String>();
		
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			while (true)
			{
				String line = reader.readLine();
				if (line == null)
					break;
				
				line = line.replace(" ", "{SPACE}");
				line = line.trim();
				line = line.replace("{SPACE}", " ");
				
				textMap.add(line);
			}
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		if (textMap.size() == 0)
		{
			MLog.info("Text map is empty. Skipping...");
			return;
		}
		
		rows = textMap.size();
		columns = textMap.get(0).length();

		for (String row : textMap)
		{
			if (row.length() != columns)
			{
				MLog.info("Text map file is not properly aligned. Skipping...");
				return;
			}
		}
		
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
		int percentage = (rowOneBased * colOneBased) * 100 / (rows * columns);
		
		MLog.info("Placing structrue at textmap location " + colOneBased + "x" + rowOneBased + " [" + percentage + "%]...");
		
		return true;
	}

	@Override
	public StructureData getCurStructure() {
		if (column < 0)
			return null;
		
		String rowString = textMap.get(row);
		char structureChar = rowString.charAt(column);
		
		return structures.get(structureChar);
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