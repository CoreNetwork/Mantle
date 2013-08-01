package com.mcnsa.flatcore;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager.Profession;

import com.mcnsa.flatcore.generation.VillagerSpawner;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.DoubleArrayList;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

public class CachedSchematic {
	private LocalSession localSession;
	private EditSession editSession;
	public int xSize;
	public int zSize;
	public String name;
	private Random random;
	private List<ChestInfo> chests;
	private int curRoation;
	
	private List<VillagerInfo> villagers = new ArrayList<VillagerInfo>();
	
	public CachedSchematic(String name)
	{
		this.name = name;
    	File schematic = new File(new File(MCNSAFlatcore.instance.getDataFolder(), "schematics"), name + ".schematic");
    	
    	if (!schematic.exists())
    		FCLog.severe("Schematic " + schematic.getAbsolutePath() + " does not exist!");
    	
		World firstWorld = Bukkit.getWorlds().get(0);
    	
    	WorldEditPlugin worldEdit = (WorldEditPlugin) MCNSAFlatcore.instance.getServer().getPluginManager().getPlugin("WorldEdit");
		editSession = new EditSession(new BukkitWorld(firstWorld), -1);
		localSession = new LocalSession(worldEdit.getLocalConfiguration());
		editSession.enableQueue();
		try {
			localSession.setClipboard(SchematicFormat.MCEDIT.load(schematic));
			xSize = localSession.getClipboard().getWidth();
			zSize = localSession.getClipboard().getLength();

									
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		random = new Random();
		curRoation = 0;
	}
	
	public void rotateTo(int rotation)
	{
		rotation = rotation % 5;
		int rotateBy = rotation - curRoation;
		if (rotateBy < 0)
			rotateBy = 4 + rotateBy;
		
		try {
			localSession.getClipboard().rotate2D(rotateBy * 90);
									
			xSize = localSession.getClipboard().getWidth();
			zSize = localSession.getClipboard().getLength();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		curRoation = rotation;
	}
	
	public void findChests()
	{
		chests = new ArrayList<ChestInfo>();
		try
		{
			for (int x = 0; x < xSize; x++)
			{
				for (int z = 0; z < zSize; z++)
				{
					for (int y = 0; y < localSession.getClipboard().getHeight(); y++)
					{
						Vector vector = new Vector(x, y, z);
						BaseBlock baseBlock = localSession.getClipboard().getPoint(vector);
						if (Util.isInventoryContainer(baseBlock.getType()))
						{
							FCLog.info("Found chest! " + baseBlock.getType());
							Vector chest = vector;
							for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH})
							{
								vector = new Vector(x + face.getModX(), y, z + face.getModZ());
								try
								{
									baseBlock = localSession.getClipboard().getPoint(vector);
								}
								catch (ArrayIndexOutOfBoundsException e)
								{
									continue;
								}
									
										
								if (baseBlock.getType() == Material.SIGN_POST.getId() || baseBlock.getType() == Material.WALL_SIGN.getId())
								{
									FCLog.info("Found sign!");

									try
									{
										SignBlock sign = new SignBlock(baseBlock.getType(), baseBlock.getData());
										sign.setNbtData(baseBlock.getNbtData());
										
										if (sign.getText()[0].trim().startsWith("[loot]"))
										{
											FCLog.info("Found loot sign!");

											int replaceID = 0;
											String prvaSplit[] = sign.getText()[0].split(" ");
											if (prvaSplit.length > 1 && Util.isInteger(prvaSplit[1]))
												replaceID = Integer.parseInt(prvaSplit[1]);
											
											ChestInfo info = new ChestInfo();
											info.restockable = true;
											info.lootTable = sign.getText()[1];
											info.interval = Integer.parseInt(sign.getText()[2]);
											info.perPlayer = sign.getText()[3].contains("true");
											info.loc = new Location(Bukkit.getWorlds().get(0), chest.getBlockX(), chest.getBlockY(), chest.getBlockZ());
											
											localSession.getClipboard().setBlock(vector, new BaseBlock(replaceID));
											
											chests.add(info);

											break;
										}
										else if (sign.getText()[0].trim().startsWith("[Allow]"))
										{
											int replaceID = 0;
											String prvaSplit[] = sign.getText()[0].split(" ");
											if (prvaSplit.length > 1 && Util.isInteger(prvaSplit[1]))
												replaceID = Integer.parseInt(prvaSplit[1]);

											
											ChestInfo info = new ChestInfo();
											info.loc = new Location(Bukkit.getWorlds().get(0), chest.getBlockX(), chest.getBlockY(), chest.getBlockZ());
											info.restockable = false;
											
											localSession.getClipboard().setBlock(vector, new BaseBlock(replaceID));
											
											chests.add(info);
											
											break;
										}
									}
									catch (DataException e)
									{
										e.printStackTrace();
									}
									
								}
							}
						}
					}
				}
			}
		}
		catch (EmptyClipboardException e)
		{
			e.printStackTrace();
		}
	}
	
	public ChestInfo[] getChests(Location placementCorner)
	{			
		ChestInfo[] infos = new ChestInfo[chests.size()];
		
		for (int i = 0; i < chests.size(); i++)
		{
			ChestInfo info = new ChestInfo(chests.get(i));
			info.loc = info.loc.clone();
			info.loc.setX(info.loc.getBlockX() + placementCorner.getBlockX());
			info.loc.setY(info.loc.getBlockY() + placementCorner.getBlockY());
			info.loc.setZ(info.loc.getBlockZ() + placementCorner.getBlockZ());
			infos[i] = info;
		}
	
		return infos;
	}
	
	public void findVillagers()
	{
		try
		{
			for (int x = 0; x < xSize; x++)
			{
				for (int z = 0; z < zSize; z++)
				{
					for (int y = 0; y < localSession.getClipboard().getHeight(); y++)
					{
						Vector vector = new Vector(x, y, z);
						BaseBlock baseBlock = localSession.getClipboard().getPoint(vector);
						if (baseBlock.getType() == Material.SIGN_POST.getId() || baseBlock.getType() == Material.WALL_SIGN.getId())
						{
							SignBlock sign = new SignBlock(baseBlock.getType(), baseBlock.getData());
							sign.setNbtData(baseBlock.getNbtData());
							
							boolean villagerSign = false;
							for (int i = 0; i < 4; i++)
							{
								String line = sign.getText()[i];
								String lineS[] = line.split(" ");
								if (lineS.length < 2 || !Util.isInteger(lineS[1]))
									continue;
								
								int type = -1;
								if (lineS[0].trim().equalsIgnoreCase("farmer"))
									type = 0;
								else if (lineS[0].trim().equalsIgnoreCase("librarian"))
									type = 1;
								else if (lineS[0].trim().equalsIgnoreCase("priest"))
									type = 2;
								else if (lineS[0].trim().equalsIgnoreCase("blacksmith"))
									type = 3;
								else if (lineS[0].trim().equalsIgnoreCase("butcher"))
									type = 4;
								else
									continue;
								
								villagerSign = true;
								
								int amount = Integer.parseInt(lineS[1]);
								
								VillagerInfo villager = new VillagerInfo();
								villager.id = type;
								villager.amount = amount;
								villager.loc = new Location(Bukkit.getWorlds().get(0), x, y, z);	
																
								villagers.add(villager);
							}
							
							if (villagerSign)
								localSession.getClipboard().setBlock(vector, new BaseBlock(Material.AIR.getId()));
						}
					}
				}
			}
		}
		catch (EmptyClipboardException e)
		{
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void spawnVillagers(Location placementCorner, VillagerSpawner spawner)
	{
		for (VillagerInfo villager : villagers)
		{
			Location loc = villager.loc.clone();
			loc.setX(loc.getBlockX() + placementCorner.getBlockX());
			loc.setY(loc.getBlockY() + placementCorner.getBlockY());
			loc.setZ(loc.getBlockZ() + placementCorner.getBlockZ());
			
			for (int i = 0; i < villager.amount; i++)
			{
				spawner.spawnVillager(loc, Profession.getProfession(villager.id));
			}

		}
	}
	
	public void clearVillagers(Location corner)
	{
		Chunk cornerChunk = corner.getChunk();
		int sizeChunksX = (int) Math.ceil(xSize / 16.0);
		int sizeChunksZ = (int) Math.ceil(zSize / 16.0);
		
		int minX = cornerChunk.getX();;
		int minZ = cornerChunk.getZ();;
		int maxX = minX + sizeChunksX;
		int maxZ = minZ + sizeChunksZ;
		
		for (int x = minX; x <= maxX; x++)
		{
			for (int z = minZ; z <= maxZ; z++)
			{
				Chunk chunk = corner.getWorld().getChunkAt(x,z);
				
				if (!chunk.isLoaded())
					chunk.load();
				
				for (Entity e : chunk.getEntities())
				{
					if (e.getType() == EntityType.VILLAGER)
						e.remove();
				}
			}
		}

	}
	
	public Location getCenter(Location corner)
	{
		corner = corner.clone();
		corner.setX(corner.getX() + xSize / 2);
		corner.setZ(corner.getZ() + zSize / 2);
		
		return corner;
	}
	
	public Location placeAtCorner(int x, int y, int z)
	{
		Location placement = new Location(Bukkit.getWorlds().get(0), x, y, z);
		place(placement);
		return placement;
	}
	
	public Location place(World world, int x, int y, int z, int randomOff)
	{
		int randX = randomOff == 0 ? 0 : (random.nextInt(randomOff) - randomOff / 2);
		int randZ = randomOff == 0 ? 0 : (random.nextInt(randomOff) - randomOff / 2);

		Location placement = new Location(Bukkit.getWorlds().get(0), x - xSize / 2 + randX, y, z - zSize / 2 + randZ);

		place(placement);
		
		return placement;
	}
	
	public void place(Location placement)
	{
		
		Vector middle = new Vector(placement.getBlockX(), placement.getBlockY(), placement.getBlockZ());
		
		try {
			localSession.getClipboard().place(editSession, middle, false);
			
			localSession.clearHistory();
			
			Field f = editSession.getClass().getDeclaredField("original");
			f.setAccessible(true);
			DoubleArrayList<BlockVector, BaseBlock> originalBlocks = (DoubleArrayList<BlockVector, BaseBlock>) f.get(editSession);
			originalBlocks.clear();
		} catch (MaxChangedBlocksException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EmptyClipboardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		editSession.flushQueue();
	}
	
	private static class VillagerInfo
	{
		public Location loc;
		public int id;
		public int amount;
	}
	
	public static class ChestInfo
	{
		public ChestInfo()
		{
			
		}
		
		public ChestInfo(ChestInfo i)
		{
			restockable = i.restockable;
			loc = i.loc;
			lootTable = i.lootTable;
			interval = i.interval;
			perPlayer = i.perPlayer;
		}
		
		public Location loc;
		public boolean restockable;
		public String lootTable;
		public int interval;
		public boolean perPlayer;
	}
}
