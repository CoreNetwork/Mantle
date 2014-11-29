package us.corenetwork.mantle.generation;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;

import us.corenetwork.mantle.CachedSchematic;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;


public class PathGenerator {
	private static Runtime runtime = Runtime.getRuntime();

	private int startZ;
	private int startX;
	public HashSet<PathTile> visitedTiles = new HashSet<PathTile>();
	private World world;
	private HashMap<Integer, StructureData> structures;
	private ArrayDeque<ImagePixel> pixels;
	
	public void generatePath(String path)
	{
		MemorySection pathConfig = (MemorySection) GenerationModule.instance.config.get("Paths." + path);

		if (pathConfig == null)
		{
			MLog.info("Path " + path + " does not have generation config node. Aborting...");
			return;
		}

		MLog.info("Starting generation for path " + path);

		world = Bukkit.getWorld((String) pathConfig.get("World"));

		if (world == null)
		{
			MLog.info("Path " + path + " has invalid world set. Aborting...");
			return;
		}

		pixels = new ArrayDeque<ImagePixel>();
		
		String[] startPos = ((String) pathConfig.get("Start")).split(" ");

		startX = Integer.parseInt(startPos[0]);
		startZ = Integer.parseInt(startPos[1]);

		MLog.info("Preparing structures...");

		structures = new HashMap<Integer, StructureData>();

		MemorySection structuresConfig = (MemorySection) pathConfig.get("Structures");
		for (Entry<String,Object> e : structuresConfig.getValues(false).entrySet())
		{
			StructureData structure = new StructureData(e.getKey(), (MemorySection) e.getValue(), world);
			structures.put(structure.getImageColor(), structure);
		}

		MLog.info("Loading imagemap...");

		File imageMapFile = new File(MantlePlugin.instance.getDataFolder(), (String) pathConfig.get("ImageMapFileName"));

		if (!imageMapFile.exists())
		{
			MLog.info("Image map file for path " + path + " does not exist. Aborting...");
			return;
		}


        BufferedImage imageMap = null;
		try
		{
            imageMap = ImageIO.read(imageMapFile);
			}
		catch (IOException e)
		{
			e.printStackTrace();
		}


		PathTileMap tileMap = new PathTileMap(imageMap);

		PathTile firstTile = tileMap.tileMap[0][0];

		processTile(firstTile, startX, startZ, 1, -1);

		MLog.info("Generating image");
		
		int highestX = 0;
		int highestZ = 0;
		int lowestX = Integer.MAX_VALUE;
		int lowestZ = Integer.MAX_VALUE;
		for (ImagePixel pixel : pixels)
		{
			if (highestX < pixel.x)
				highestX = pixel.x;
			
			if (highestZ < pixel.z)
				highestZ = pixel.z;
			
			if (lowestX > pixel.x)
				lowestX = pixel.x;
			
			if (lowestZ > pixel.z)
				lowestZ = pixel.z;
		}
		
		int xSize = highestX - lowestX + 1;
		int zSize = highestZ - lowestZ + 1;
		
		
		BufferedImage pathImage = new BufferedImage(xSize, zSize, BufferedImage.TYPE_INT_RGB);
				
		for (int x = 0; x < xSize; x++)
		{
			for (int z = 0; z < zSize; z++)
			{
				pathImage.setRGB(x, z, 0xffffff);
			}
		}
		
		for (ImagePixel pixel : pixels)
		{
			pathImage.setRGB(pixel.x - lowestX, pixel.z - lowestZ, pixel.color);
		}
		
		File imageFile = new File(MantlePlugin.instance.getDataFolder(), "path_" + path + ".png");
		
		try {
			ImageIO.write(pathImage, "png", imageFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		MLog.info("Generation finished");
	}

	private void processTile(PathTile tile, int x, int z, int coordCorner, int cornerSize)
	{
		if (visitedTiles.contains(tile))
			return;

		visitedTiles.add(tile);

		StructureData structure = structures.get(tile.structure);
		if (structure == null)
			return;

		tile.printTile();

		CachedSchematic schematic = structure.getRandomSchematic();
		schematic.rotateTo(tile.rotation);

		switch (coordCorner)
		{
		case 3:
			x-= schematic.xSize;
			break;
		case 0:
			z-= schematic.zSize;
			break;
		}

		//Attempt to center tile
		if (cornerSize > 0)
		{
			int myCornerSize = (coordCorner == 0 || coordCorner == 2) ? schematic.xSize : schematic.zSize;
			int diff = cornerSize - myCornerSize;
			MLog.info("Corned diff " + diff);
			diff /= 2;

			if (coordCorner == 0 || coordCorner == 2)
				x += diff;
			else 
				z += diff;
		}

		Location placingLocation = new Location(world, x, structure.getPasteHeight(), z);
		
		schematic.place(placingLocation, structure.shouldIgnoreAir());
		schematic.drawBitmap(pixels, x, z);
		
		StructureData.WorldGuard region = structure.getWorldGuardData();
		if (region != null)
		{
			Location firstBlock = placingLocation.clone().add(region.firstBlock.getX(), region.firstBlock.getY(), region.firstBlock.getZ());
			Location secondBlock = placingLocation.clone().add(region.secondBlock.getX(), region.secondBlock.getY(), region.secondBlock.getZ());

			String name = structure.getName() + "-" + tile.x + "x" + tile.z;
			WorldGuardManager.createRegion(firstBlock, secondBlock, name, region.exampleRegion);
		}
		
		float memLeft = (float) ((float) runtime.freeMemory() / runtime.totalMemory());
		MLog.debug("MemLeft: " + memLeft);
		if (memLeft < 0.3)
		{
			for (Chunk c : world.getLoadedChunks())
				c.unload(true, true);

			System.gc();
		}

		int xSize = schematic.xSize;
		int zSize = schematic.zSize;

		for (int i = 0; i < 4; i++)
		{
			PathTile neighbour = tile.getNeighbour(i);
			if (neighbour == null)
				continue;

			int newX = x;
			int newZ = z;
			int sideSize = (i == 0 || i == 2) ? xSize : zSize;

			switch (i)
			{
			case 1:
				newX+= schematic.xSize;
				break;
			case 2:
				newZ+= schematic.zSize;
				break;
			}

			processTile(neighbour, newX, newZ, i, sideSize);
			
		}
	}


}
