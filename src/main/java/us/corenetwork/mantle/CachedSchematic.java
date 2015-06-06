package us.corenetwork.mantle;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.registry.WorldData;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import net.minecraft.server.v1_8_R3.EntityVillager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import us.core_network.cornel.strings.NumberParsing;
import us.corenetwork.mantle.generation.ImagePixel;
import us.corenetwork.mantle.generation.MapColors;
import us.corenetwork.mantle.generation.VillagerSpawner;
import us.corenetwork.mantle.regeneration.RegenerationModule;
import us.corenetwork.mantle.regeneration.RegenerationSettings;
import us.corenetwork.mantle.regeneration.RegenerationUtil;

public class CachedSchematic {
	private LocalSession localSession;
	public int xSize;
	public int zSize;
	public int ySize;
	public String name;
	private Random random;
	private List<ChestInfo> chests;
	private int rotation;

	private List<VillagerInfo> villagers = new ArrayList<VillagerInfo>();


	//HACK to get nether schematics to work during generation.
	public static boolean isNether = false;

	public CachedSchematic(String name, World world)
	{
		this.name = name;
		File schematic = new File(new File(MantlePlugin.instance.getDataFolder(), "schematics"), name + ".schematic");

		if (!schematic.exists())
			MLog.severe("Schematic " + schematic.getAbsolutePath() + " does not exist!");

		WorldEditPlugin worldEdit = (WorldEditPlugin) MantlePlugin.instance.getServer().getPluginManager().getPlugin("WorldEdit");
		localSession = new LocalSession(worldEdit.getLocalConfiguration());

		WorldData worldData = new BukkitWorld(world).getWorldData();
		try {

			localSession.setClipboard(readSchematic(schematic, worldData));
			calculateSize();

		} catch (Exception e) {
			e.printStackTrace();
		}

		random = new Random();
	}

	public void rotateTo(int rotateBy)
	{
		rotation = rotateBy;
		rotateBy = (rotateBy % 5) * 90;

		try {
			ClipboardHolder holder = localSession.getClipboard();
			AffineTransform transform = new AffineTransform();

			transform = transform.rotateY(rotateBy);
			holder.setTransform(transform);

			calculateSize();

			if (rotateBy == 90 || rotateBy == 270)
			{
				int tmp = xSize;
				xSize = zSize;
				zSize = tmp;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void findChests()
	{
		String path = "Containers."+name;
		
		List<String> stringVectorList = new ArrayList<String>();
		List<Vector> vectorList = new ArrayList<Vector>();



		//if regeneration module is enabled, basically
		if(RegenerationModule.instance.storageConfig != null)
		{
			Object oList = RegenerationModule.instance.storageConfig.get(path);

			//if no value in config
			if(oList == null)
			{
				vectorList = findChestsLoopThrough();

				stringVectorList = vectorToStringVectorList(vectorList);
				RegenerationModule.instance.storageConfig.set(path, stringVectorList);
				RegenerationModule.instance.saveStorageYaml();
			}
			else
			{
				stringVectorList = RegenerationModule.instance.storageConfig.getStringList(path);
				vectorList = stringVectorToVectorList(stringVectorList);
			}
		}
		else
		{
			vectorList = findChestsLoopThrough();
		}
		handleChests(vectorList);
	}

	private List<Vector> findChestsLoopThrough()
	{
		List<Vector> vectorList = new ArrayList<Vector>();
		try
		{
			Clipboard cc =  localSession.getClipboard().getClipboard();
			for (int y = 0; y < ySize; y++)
			{
				for (int z = 0; z < zSize; z++)
				{
					for (int x = 0; x < xSize; x++)
					{

						Vector vector = new Vector(x,y,z);
						if(isNether)
							vector = vector.add(cc.getMinimumPoint());
						BaseBlock baseBlock = cc.getBlock(vector);
						if (Util.isInventoryContainer(baseBlock.getType()))
						{
							if(isNether)
								vectorList.add(vector.subtract(cc.getMinimumPoint()));
							else
								vectorList.add(vector);
						}
					}
				}
			}
		}
		catch (EmptyClipboardException e)
		{
			e.printStackTrace();
		}
		return vectorList;
	}

	private void handleChests(List<Vector> chestLocations)
	{
		chests = new ArrayList<ChestInfo>();
		for(Vector v : chestLocations)
		{
			try
			{
				Vector minPoint = localSession.getClipboard().getClipboard().getRegion().getMinimumPoint();
				Vector chest = v;
						
				BaseBlock baseBlock = null;
				for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH})
				{
					Vector vector = new Vector(chest.getBlockX() + face.getModX(), chest.getBlockY(), chest.getBlockZ() + face.getModZ());

					vector = vector.add(minPoint);
					try
					{
						baseBlock = localSession.getClipboard().getClipboard().getBlock(vector);
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
							String[] signText = sign.getText();

							if(isNether)
							{
								for (int i = 0; i < signText.length; i++)
								{
									signText[i] = signText[i].substring(11, signText[i].indexOf(',')-2);
								}
							}
							else
							{
								for (int i = 0; i < signText.length; i++)
								{
									signText[i] = signText[i].substring(1, signText[i].length() - 1);
								}
							}
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
									if (NumberParsing.isInteger(idString))
									{
										replaceID = Integer.parseInt(idString);
									}
									else if (idString.contains(":"))
									{
										String[] idSplit = idString.split(":");
										if (NumberParsing.isInteger(idSplit[0]))
										{
											replaceID = Integer.parseInt(idSplit[0]);
										}
										if (NumberParsing.isInteger(idSplit[1]))
										{
											replaceData = Integer.parseInt(idSplit[1]);
										}
									}
								}
		
								localSession.getClipboard().getClipboard().setBlock(vector, new BaseBlock(replaceID, replaceData));
							
								break;
							}
						} catch (WorldEditException e)
						{
							e.printStackTrace();
						}

					}
				}
			
			}
			catch (EmptyClipboardException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public ChestInfo[] getChests(Location placementCorner, int rotation)
	{			
		ChestInfo[] infos = new ChestInfo[chests.size()];

		for (int i = 0; i < chests.size(); i++)
		{
			ChestInfo info = new ChestInfo(chests.get(i));

			int x = placementCorner.getBlockX();
			int y = info.loc.getBlockY() + placementCorner.getBlockY();
			int z = placementCorner.getBlockZ();

			switch (rotation)
			{
				case 0:
					x += info.loc.getBlockX();
					z += info.loc.getBlockZ();
					break;
				case 1:
					x += info.loc.getBlockZ();
					z += this.xSize - info.loc.getBlockX() - 1;
					break;
				case 2:
					x += this.xSize - info.loc.getBlockX() - 1;
					z += this.zSize - info.loc.getBlockZ() - 1;
					break;
				case 3:
					x += this.zSize - info.loc.getBlockZ() - 1;
					z += info.loc.getBlockX();
					break;
				default :
					x += info.loc.getBlockX();
					z += info.loc.getBlockZ();
					break;
			}
			info.loc = new Location(placementCorner.getWorld(), x, y, z);

			infos[i] = info;
		}

		return infos;
	}

	public void findVillagers()
	{
		String path = "Villagers."+name;
		
		List<String> stringVectorList = new ArrayList<String>();
		List<Vector> vectorList;
		

		//if regeneration module is enabled, basically
		if(RegenerationModule.instance.storageConfig != null)
		{
			Object oList = RegenerationModule.instance.storageConfig.get(path);

			//if no value in config
			if(oList == null)
			{
				vectorList = findVillagersLoopThrough();

				stringVectorList = vectorToStringVectorList(vectorList);
				RegenerationModule.instance.storageConfig.set(path, stringVectorList);
				RegenerationModule.instance.saveStorageYaml();
			}
			else
			{
				stringVectorList = RegenerationModule.instance.storageConfig.getStringList(path);
				vectorList = stringVectorToVectorList(stringVectorList);
			}
		}
		else
		{
			vectorList = findVillagersLoopThrough();
		}
		handleVillagerSigns(vectorList);
	}

	private List<Vector> findVillagersLoopThrough()
	{
		List<Vector> vectorList = new ArrayList<Vector>();
		try
		{
			Clipboard cc =  localSession.getClipboard().getClipboard();
			for (int x = 0; x < xSize; x++) {
				for (int z = 0; z < zSize; z++)	{
					for (int y = 0; y < ySize; y++)
					{
						Vector vector = new Vector(x,y,z);
						BaseBlock baseBlock = cc.getBlock(vector);
						if (baseBlock.getType() == Material.SIGN_POST.getId() || baseBlock.getType() == Material.WALL_SIGN.getId())
						{
							vectorList.add(vector);
						}
					}
				}
			}
		}
		catch (EmptyClipboardException e)
		{
			e.printStackTrace();
		}
		return vectorList;
	}

	private List<String> vectorToStringVectorList(List<Vector> list)
	{
		List<String> returnList = new ArrayList<String>();
		
		for(Vector v : list)
		{
			String s = v.getBlockX()+":"+v.getBlockY()+":"+v.getBlockZ();
			returnList.add(s);
		}
		
		return returnList;
	}
	
	private List<Vector> stringVectorToVectorList(List<String> list)
	{
		List<Vector> returnList = new ArrayList<Vector>();
		
		for(String s : list)
		{
			String arr[] = s.split(":");
			
			Vector v = new Vector(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2])); 
			returnList.add(v);
		}
		
		return returnList;
	}
	
	private void handleVillagerSigns(List<Vector> signLocations)
	{
		try
		{

			Vector minPoint = localSession.getClipboard().getClipboard().getRegion().getMinimumPoint();

			for(Vector vector : signLocations)
			{
				BaseBlock baseBlock = localSession.getClipboard().getClipboard().getBlock(vector.add(minPoint));
				SignBlock sign = new SignBlock(baseBlock.getType(), baseBlock.getData());
				sign.setNbtData(baseBlock.getNbtData());
		
				boolean villagerSign = false;
				for (int i = 0; i < 4; i++)
				{
					String line = sign.getText()[i];
					if(line.length() <= 2)
						continue;
					line = line.substring(1,line.length()-1);
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
		
					if (lineS.length >= 2 && NumberParsing.isInteger(lineS[1]))
						amount = Integer.parseInt(lineS[1]);
					
					VillagerInfo villager = new VillagerInfo();
					villager.id = type;
					villager.amount = amount;
					villager.loc = new Location(null, vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());	
		
					villagers.add(villager);
				}
		
				if (villagerSign)	
				{
					localSession.getClipboard().getClipboard().setBlock(vector, new BaseBlock(Material.AIR.getId()));
				}
			}
		
		}
		catch (EmptyClipboardException e)
		{
			e.printStackTrace();
		} catch (WorldEditException e)
		{
			e.printStackTrace();
		}
	}
	
	public void spawnVillagers(Location placementCorner, VillagerSpawner spawner, int rotation)
	{
		for (VillagerInfo villager : villagers)
		{


			double x = placementCorner.getBlockX() + 0.5;
			double y = villager.loc.getBlockY() + placementCorner.getBlockY()+0.5;
			double z = placementCorner.getBlockZ()+0.5;

			switch (rotation)
			{
				case 0:
					x += villager.loc.getBlockX();
					z += villager.loc.getBlockZ();
					break;
				case 1:
					x += villager.loc.getBlockZ();
					z += this.xSize - villager.loc.getBlockX() - 1;
					break;
				case 2:
					x += this.xSize - villager.loc.getBlockX() - 1;
					z += this.zSize - villager.loc.getBlockZ() - 1;
					break;
				case 3:
					x += this.zSize - villager.loc.getBlockZ() - 1;
					z += villager.loc.getBlockX();
					break;
				default :
					x += villager.loc.getBlockX();
					z += villager.loc.getBlockZ();
					break;
			}

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
					{
						EntityVillager nmsVillager = ((CraftVillager) e).getHandle();
						nmsVillager.die();
					}
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
		try {
			Vector to = new Vector(placement.getBlockX(), placement.getBlockY(), placement.getBlockZ());
			Vector origin = localSession.getClipboard().getClipboard().getOrigin();
			Vector min = localSession.getClipboard().getClipboard().getMinimumPoint();
			Vector max = localSession.getClipboard().getClipboard().getMaximumPoint();
			to = origin.subtract(max).add(to).add(max.subtract(min));

			if(rotation % 2 == 1)
			{
				if(xSize % 2 == 1)
				{
					to = to.add(0, 0, 0.5);
				}
				if(zSize % 2 == 1)
				{
					to = to.add(0.5, 0, 0);
				}
			}

			final EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(placement.getWorld()), -1);
			editSession.enableQueue();

			final List<Operation> listOfOperation = new ArrayList<Operation>();

			ClipboardHolder holder = localSession.getClipboard();
			BlockTransformExtent extent = new BlockTransformExtent(holder.getClipboard(), holder.getTransform(), holder.getWorldData().getBlockRegistry() );

			Region rr = holder.getClipboard().getRegion();
			int height = rr.getHeight();
			ExistingBlockMask ebm = new ExistingBlockMask(holder.getClipboard());

			for(int i = 0; i<height;i++)
			{
				int j = -height + i + 1;
				rr = holder.getClipboard().getRegion();
				rr.contract(new Vector(0,i,0), new Vector(0,j,0));
				ForwardExtentCopy copy = new ForwardExtentCopy(extent, rr, holder.getClipboard().getOrigin(), editSession, to);
				if(ignoreAir)
					copy.setSourceMask(ebm);
				listOfOperation.add(copy);
			}

			int delayInTickBetweenEach = RegenerationSettings.REGENERATE_LAYER_EVERY_X_TICKS.integer();
			int numOfOp = listOfOperation.size();

			for(int i = 0;i<numOfOp;i++)
			{
				final int id = i;
				Bukkit.getScheduler().scheduleSyncDelayedTask(MantlePlugin.instance, new Runnable() {
					@Override
					public void run()
					{
						MantleListener.disablePhysics = true;

						Operation op = listOfOperation.get(id);
						try
						{
							Operations.complete(op);
						} catch (WorldEditException e)
						{
							e.printStackTrace();
						}
						editSession.flushQueue();
						MantleListener.disablePhysics = false;
					}
				}, delayInTickBetweenEach*i);

			}

			final Location toFinal = new Location(placement.getWorld(), placement.getX(), placement.getY(), placement.getZ());
			final CachedSchematic handle = this;
			Bukkit.getScheduler().scheduleSyncDelayedTask(MantlePlugin.instance, new Runnable() {
				@Override
				public void run()
				{
					localSession.clearHistory();
					RegenerationUtil.finishRegenerateStructure(handle, rotation, toFinal);
				}
			}, delayInTickBetweenEach*(numOfOp+1));

			//finishRegenerateStructure(CachedSchematic schematic, int rotation, Location pastingLocation)

		} catch (Exception e)
		{
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


	private void calculateSize()
	{
		try
		{
			Vector maximumPoint = localSession.getClipboard().getClipboard().getMaximumPoint();
			Vector minimumPoint = localSession.getClipboard().getClipboard().getMinimumPoint();

			xSize = Math.abs(maximumPoint.getBlockX() - minimumPoint.getBlockX()) + 1;
			ySize = Math.abs(maximumPoint.getBlockY() - minimumPoint.getBlockY()) + 1;
			zSize = Math.abs(maximumPoint.getBlockZ() - minimumPoint.getBlockZ()) + 1;
		}
		catch (EmptyClipboardException e)
		{
			e.printStackTrace();
		}
	}

	public int getHighestMaterial(int x, int z)
	{
		for (int y = ySize - 1; y >= 0; y--)
		{
			Vector vector = new Vector(x, y, z);
			int material;
			try {
				material = localSession.getClipboard().getClipboard().getBlock(vector).getType();
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

	private static ClipboardHolder readSchematic(File file, WorldData worldData)
	{
		try
		{
			FileInputStream fileStream = new FileInputStream(file);
			BufferedInputStream bufferedStream = new BufferedInputStream(fileStream);
			ClipboardReader reader = ClipboardFormat.findByFile(file).getReader(bufferedStream);

			Clipboard clipboard = reader.read(worldData);
			Vector min = clipboard.getMinimumPoint();
			Vector max = clipboard.getMaximumPoint();
			Vector halfsize = max.subtract(min).divide(2);
			clipboard.setOrigin(min.add(halfsize));

			ClipboardHolder holder = new ClipboardHolder(clipboard, worldData);

			bufferedStream.close();
			return holder;

		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private static class VillagerInfo
	{
		public Location loc;
		public int id;
		public int amount;
	}

	public static class ChestInfo
	{
		public Location loc;
		public boolean restockable;
		public String lootTable;
		public int interval;
		public boolean perPlayer;

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

	}
}