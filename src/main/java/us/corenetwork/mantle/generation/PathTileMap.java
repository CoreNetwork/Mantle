package us.corenetwork.mantle.generation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class PathTileMap {
	public int rows;
    public int cols;
    public PathTile tileMap[][];
	
	public PathTileMap(BufferedImage image)
	{
        rows = image.getHeight() / 10;
        cols = image.getWidth() / 10;
		tileMap = new PathTile[rows][cols];

		for (int x = 0; x < rows; x++)
		{
			for (int z = 0; z < cols; z++)
			{
				PathTile tile = new PathTile(this, x, z);

                int imageTileX = x * 10;
                int imageTileZ = z * 10;

				tile.structure = image.getRGB(imageTileX, imageTileZ) & 0x00FFFFFF;

                if (new Color(image.getRGB(imageTileX + 9, imageTileZ)).equals(Color.BLACK)) // Is top right corner equal to black
                    tile.rotation = 1;
                else if (new Color(image.getRGB(imageTileX + 9, imageTileZ + 9)).equals(Color.BLACK)) // Is bottom right corner equal to black
                    tile.rotation = 2;
                else if (new Color(image.getRGB(imageTileX, imageTileZ + 9)).equals(Color.BLACK)) // Is bottom left corner equal to black
                    tile.rotation = 3;
                else
                    tile.rotation = 0;

				tileMap[x][tile.z] = tile;
			}
		}		
	}
}
