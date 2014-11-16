package us.corenetwork.mantle.restockablechests;

import org.bukkit.Bukkit;
import org.bukkit.World;

class VillageInfoHelper 
	{
		int id;
		int villageX;
		int villageZ;
		int xSize;
		int zSize;
		int distance;
		World world;
		public VillageInfoHelper(int id, int villageX, int villageZ, int xSize, int zSize, int distance, String world)
		{
			this.id = id;
			this.villageX = villageX;
			this.villageZ = villageZ;
			this.xSize = xSize;
			this.zSize = zSize;
			this.distance = distance;
			this.world = Bukkit.getWorld(world);
		}
		
		
	}