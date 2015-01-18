package us.corenetwork.mantle.animalspawning;

import java.util.ArrayList;
import java.util.Random;
import us.corenetwork.mantle.MLog;


public class AnimalRange {

	private static int lastWeightedChunk = 0;
	private static ArrayList<AnimalRange> ranges = new ArrayList<AnimalRange>();
	private static Random random = new Random();
	
	private int startChunk;
	private int endChunk;
	private int width;
	private int weight;
	
	private int chunkCount;
	private int weightedChunkCount;
	
	private int startWeightedChunk;
	private int endWeightedChunk;
	
	private AnimalRange(int startChunk, int endChunk, int weight)
	{
		this.startChunk = startChunk;
		this.endChunk = endChunk;
		this.width = Math.abs(endChunk - startChunk) + 1;
		this.weight = weight;
		
		chunkCount = 4 * (endChunk + startChunk - 1) * (endChunk - startChunk + 1);
		weightedChunkCount = chunkCount * weight;
		
		startWeightedChunk = lastWeightedChunk + 1;
		endWeightedChunk = lastWeightedChunk + weightedChunkCount;
		lastWeightedChunk = endWeightedChunk;
	}
	
	public static void initializeRanges()
	{
		lastWeightedChunk = 0;
		ranges = new ArrayList<AnimalRange>();
	}
	
	public static void addRange(int startChunk, int endChunk, int weight)
	{
		AnimalRange animalRange = new AnimalRange(startChunk, endChunk, weight);
		ranges.add(animalRange);
	}
	
	public static ChunkCoordinates getRandomChunk()
	{
		AnimalRange randRange = getRandomRange();
		
		return randRange.getChunk();
	}
	
	private static AnimalRange getRandomRange()
	{
		int randomWeightedChunk = random.nextInt(lastWeightedChunk) + 1;
		MLog.debug("Random weighted chunk : " + randomWeightedChunk);
		
		for(AnimalRange range : ranges)
		{
			if (range.isInRange(randomWeightedChunk))
				return range;
		}
		
		return ranges.get(ranges.size() - 1);
	}
	
	public static void printAllRanges()
	{
		for(AnimalRange r : ranges)
			MLog.debug(r.toString());
	}
	
	@Override
	public String toString()
	{
		return "StartChunk : " + startChunk + System.lineSeparator()
			+  "EndChunk : " + endChunk + System.lineSeparator()
			+  "Weight : " + weight + System.lineSeparator()
			+  "ChunkCount : " + chunkCount + System.lineSeparator()
			+  "WeightedChunkCount : " + weightedChunkCount + System.lineSeparator()
			+  "StartWeightedChunk : " + startWeightedChunk + System.lineSeparator()
			+  "EndWeightedChunk : " + endWeightedChunk + System.lineSeparator();
	}

	private boolean isInRange(int weightedChunk)
	{
		return startWeightedChunk <= weightedChunk && weightedChunk <= endWeightedChunk;
	}
	
	private ChunkCoordinates getChunk()
	{
		int x;
		int z;
		
		
		int dim1 = width;
		int dim2 = 2 * endChunk - width;
		
		//We choose which rectangle chunk will be in
		// -------------
		// |____1____|  |
		// |  |      | 2|
		// | 0|______|__|
		// |__|____3____|
		//
	    // The flat rectangle is dim2 long, and dim1 high
	    // The non-flat is dim1 long, and dim2 high
		
		int section = random.nextInt(4);
		switch (section)
		{
		case 0:
			x = -endChunk + random.nextInt(dim1);
			z = -endChunk + random.nextInt(dim2);
			break;
		case 1:
			x = -endChunk + random.nextInt(dim2);
			z = startChunk - 1 + random.nextInt(dim1);
			break;
		case 2:
			x = startChunk -1 + random.nextInt(dim1);
			z = (- startChunk + 1) + random.nextInt(dim2);
			break;
		case 3:
			x = (-startChunk + 1) + random.nextInt(dim2);
			z = -endChunk + random.nextInt(dim1);
			break;
		default:
			x = 0;
			z = 0;
			break;
		}
		
		
		return new ChunkCoordinates(x, z); 
	}
	
}
