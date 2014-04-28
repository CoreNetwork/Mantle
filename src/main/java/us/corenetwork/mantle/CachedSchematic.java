package us.corenetwork.mantle;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import us.corenetwork.mantle.generation.ImagePixel;
import us.corenetwork.mantle.generation.MapColors;
import us.corenetwork.mantle.generation.VillagerSpawner;

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
	public int xSize;
	public int zSize;
	public int ySize;
	public String name;
	private Random random;
	private List<ChestInfo> chests;
	private int curRoation;

	private List<VillagerInfo> villagers = new ArrayList<VillagerInfo>();

	public CachedSchematic(String name)
	{
		this.name = name;
		File schematic = new File(new File(MantlePlugin.instance.getDataFolder(), "schematics"), name + ".schematic");

		if (!schematic.exists())
			MLog.severe("Schematic " + schematic.getAbsolutePath() + " does not exist!");

		WorldEditPlugin worldEdit = (WorldEditPlugin) MantlePlugin.instance.getServer().getPluginManager().getPlugin("WorldEdit");
		localSession = new LocalSession(worldEdit.getLocalConfiguration());
		try {
			localSession.setClipboard(SchematicFormat.MCEDIT.load(schematic));
			xSize = localSession.getClipboard().getWidth();
			zSize = localSession.getClipboard().getLength();
			ySize = localSession.getClipboard().getHeight();


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
									try
									{
										SignBlock sign = new SignBlock(baseBlock.getType(), baseBlock.getData());
										sign.setNbtData(baseBlock.getNbtData());

										boolean properSign = true;
										
										if (sign.getText()[0].trim().startsWith("[loot]"))
										{
											ChestInfo info = new ChestInfo();
											info.restockable = true;
											info.lootTable = sign.getText()[1];
											info.interval = Integer.parseInt(sign.getText()[2]);
											info.perPlayer = sign.getText()[3].contains("true");
											info.loc = new Location(null, chest.getBlockX(), chest.getBlockY(), chest.getBlockZ());

											chests.add(info);
										}
										else if (sign.getText()[0].trim().startsWith("[Allow]"))
										{


											ChestInfo info = new ChestInfo();
											info.loc = new Location(null, chest.getBlockX(), chest.getBlockY(), chest.getBlockZ());
											info.restockable = false;

											chests.add(info);
										}
										else
											properSign = false;
										
										if (properSign)
										{
											int replaceID = 0;
											int replaceData = 0;
											
											String prvaSplit[] = sign.getText()[0].split(" ");
											if (prvaSplit.length > 1)
											{
												String idString = prvaSplit[1];
												if (Util.isInteger(idString))
												{
													replaceID = Integer.parseInt(idString);
												}
												else if (idString.contains(":"))
												{
													String[] idSplit = idString.split(":");
													if (Util.isInteger(idSplit[0]))
													{
														replaceID = Integer.parseInt(idSplit[0]);
													}
													if (Util.isInteger(idSplit[1]))
													{
														replaceData = Integer.parseInt(idSplit[1]);
													}
												}
											}

											localSession.getClipboard().setBlock(vector, new BaseBlock(replaceID, replaceData));
										
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

			int x = info.loc.getBlockX() + placementCorner.getBlockX();
			int y = info.loc.getBlockY() + placementCorner.getBlockY();
			int z = info.loc.getBlockZ() + placementCorner.getBlockZ();
			info.loc = new Location(placementCorner.getWorld(), x, y, z);

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
								int amount = 1;

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
								else if (lineS[0].trim().equalsIgnoreCase("greenie"))
									type = 5;
								else
									continue;

								villagerSign = true;

								if (lineS.length >= 2 && Util.isInteger(lineS[1]))
									amount = Integer.parseInt(lineS[1]);
								
								VillagerInfo villager = new VillagerInfo();
								villager.id = type;
								villager.amount = amount;
								villager.loc = new Location(null, x, y, z);	

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
			e.printStackTrace();
		}
	}

	public void spawnVillagers(Location placementCorner, VillagerSpawner spawner)
	{
		for (VillagerInfo villager : villagers)
		{
			int x = villager.loc.getBlockX() + placementCorner.getBlockX();
			int y = villager.loc.getBlockY() + placementCorner.getBlockY();
			int z = villager.loc.getBlockZ() + placementCorner.getBlockZ();
			Location loc = new Location(placementCorner.getWorld(), x, y, z);

			for (int i = 0; i < villager.amount; i++)
			{
				spawner.spawnVillager(loc, villager.id);
			}

		}
	}

	public void clearVillagers(Location corner)
	{
		Chunk cornerChunk = corner.getChunk();
		int sizeChunksX = (int) Math.ceil(xSize / 16.0);
		int sizeChunksZ = (int) Math.ceil(zSize / 16.0);

		int minX = cornerChunk.getX();
		int minZ = cornerChunk.getZ();
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

	public Location place(World world, int x, int y, int z, int randomOff, boolean ignoreAir)
	{
		return place(world, x, y, z, randomOff, ignoreAir, true);
	}
	
	public Location place(World world, int x, int y, int z, int randomOff, boolean ignoreAir, boolean actuallyPlace)
	{
		int randX = randomOff == 0 ? 0 : (random.nextInt(randomOff) - randomOff / 2);
		int randZ = randomOff == 0 ? 0 : (random.nextInt(randomOff) - randomOff / 2);

		Location placement = new Location(world, x - xSize / 2 + randX, y, z - zSize / 2 + randZ);

		if (actuallyPlace)
			place(placement, ignoreAir);

		return placement;
	}

	public void place(Location placement, boolean ignoreAir)
	{

		Vector middle = new Vector(placement.getBlockX(), placement.getBlockY(), placement.getBlockZ());

		try {
			EditSession editSession = new EditSession(new BukkitWorld(placement.getWorld()), -1);

			editSession.enableQueue();
			localSession.getClipboard().place(editSession, middle, ignoreAir);
			editSession.flushQueue();
			
			localSession.clearHistory();

		} catch (MaxChangedBlocksException e) {
			e.printStackTrace();
		} catch (EmptyClipboardException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	public void drawBitmap(BufferedImage image, int centerX, int centerZ)
	{
		int startX = centerX - xSize / 2;
		int startZ = centerZ - zSize / 2;
		
		for (int x = 0; x < xSize; x++)
		{
			int realX = startX + x;
			for (int z = 0; z < zSize; z++)
			{
				int realZ = startZ + z;
				
				int material = getHighestMaterial(x, z);
				int color = MapColors.getColor(material);
				
				image.setRGB(realX, realZ, color);
			}
		}
	}
	
	public void drawBitmap(Collection<ImagePixel> image, int startX, int startZ)
	{		
		for (int x = 0; x < xSize; x++)
		{
			int realX = startX + x;
			for (int z = 0; z < zSize; z++)
			{
				int realZ = startZ + z;
				
				int material = getHighestMaterial(x, z);
				int color = MapColors.getColor(material);
				
				ImagePixel pixel = new ImagePixel();
				pixel.x = realX;
				pixel.z = realZ;
				pixel.color = color;
				
				image.add(pixel);
			}
		}
	}
	
	
	public int getHighestMaterial(int x, int z)
	{
		for (int y = ySize - 1; y >= 0; y--)
		{
			Vector vector = new Vector(x, y, z);
			int material;
			try {
				material = localSession.getClipboard().getPoint(vector).getType();
				if (material != 0)
					return material;
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			} catch (EmptyClipboardException e) {
				e.printStackTrace();
			}
		}
		
		return 0;
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
