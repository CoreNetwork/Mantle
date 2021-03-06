package us.corenetwork.mantle.generation;

import us.corenetwork.mantle.MLog;

public class PathTile {
	public PathTileMap map;
	
	public int structure;
	public int rotation;

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
		MLog.info("Tile " + x + " " + z + " " + Integer.toHexString(structure) + " " + rotation);
	}
}
