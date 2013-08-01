package com.mcnsa.flatcore.generation;

import java.util.List;

public class PathTileMap {
	public PathTile tileMap[][];
	
	public PathTileMap(List<String> pathTextMap, int rows, int cols)
	{
		tileMap = new PathTile[rows][cols / 3];
		
		for (int x = 0; x < rows; x++)
		{
			String row = pathTextMap.get(x);
			
			for (int z = 0; z < cols; z+=3)
			{
				PathTile tile = new PathTile(this, x, z / 3);
				
				tile.structure = row.charAt(z);
				try
				{
					tile.rotation = Integer.parseInt(Character.toString(row.charAt(z + 1)));
					tile.schematic = Integer.parseInt(Character.toString(row.charAt(z + 2)));
				}
				catch (NumberFormatException e)
				{
				}
				
				tileMap[x][tile.z] = tile;
			}
		}		
	}
}
