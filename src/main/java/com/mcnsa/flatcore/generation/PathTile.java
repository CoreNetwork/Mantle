package com.mcnsa.flatcore.generation;

import com.mcnsa.flatcore.FCLog;

public class PathTile {
	public PathTileMap map;
	
	public char structure;
	public int rotation;
	public int schematic;
	
	public int x;
	public int z;
		
	public PathTile(PathTileMap map, int x, int z)
	{
		this.map = map;
		this.x = x;
		this.z = z;
	}
	
	public PathTile getNeighbour(int dir)
	{
		int xMod = 0;
		int zMod = 0;
		
		switch (dir)
		{
		case 0:
			zMod--;
			break;
		case 1:
			xMod++;
			break;
		case 2:
			zMod++;
			break;
		case 3:
			xMod--;
		}
		
		int newX = x + xMod;
		int newZ = z + zMod;
		
		try
		{
			return map.tileMap[newX][newZ];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			return null;
		}
	}
	
	public void printTile()
	{
		FCLog.info("Tile " + x + " " + z + " " + structure + " " + rotation + " " + schematic);
	}
}
