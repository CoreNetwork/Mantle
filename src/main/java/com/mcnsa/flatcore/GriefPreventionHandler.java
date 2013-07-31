package com.mcnsa.flatcore;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimArray;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.CreateClaimResult;
import me.ryanhamshire.GriefPrevention.CreateClaimResult.Result;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.World;


public class GriefPreventionHandler {

	public static void secure(Location corner, List<Location> chestSubclaims, int xSize, int zSize, Integer claimPermission)
	{
		xSize /= 2;
		zSize /= 2;
		World world = corner.getWorld();
		
		int x1 = corner.getBlockX() - xSize;
		int z1 = corner.getBlockZ() - zSize;
		int x2 = corner.getBlockX() + xSize;
		int z2 = corner.getBlockZ() + zSize;

		CreateClaimResult bigClaimResult = GriefPrevention.instance.dataStore.createClaim(world, x1, x2, 1, 256, z1, z2, "", null, null, false, null);
		if (bigClaimResult.succeeded != Result.Success)
		{
			FCLog.severe("Creation of GriefPrevention claim at " + corner.getBlockX() + " " + corner.getBlockZ() + " failed! Please review that location manually (" + bigClaimResult.succeeded.toString() + ")");
			return;
		}
		
		Claim bigClaim = bigClaimResult.claim;

		FCLog.debug("Creating claim " + bigClaim.getID());
		
		if (claimPermission != null)
		{
			switch (claimPermission)
			{
			case 0:
				bigClaim.setPermission("public", ClaimPermission.Access);
				break;
			case 1:
				bigClaim.setPermission("public", ClaimPermission.Build);
				break;
			case 2:
				bigClaim.setPermission("public", ClaimPermission.Inventory);
				break;
			}
		}
		
		
		if (chestSubclaims != null)
		{			
			for (Location subClaimLoc : chestSubclaims)
			{
				CreateClaimResult chestClaimResult = GriefPrevention.instance.dataStore.createClaim(subClaimLoc.getWorld(), subClaimLoc.getBlockX(), subClaimLoc.getBlockX(), subClaimLoc.getBlockY(), subClaimLoc.getBlockY(), subClaimLoc.getBlockZ(), subClaimLoc.getBlockZ(), "", bigClaim, null, false, null);
				if (chestClaimResult.succeeded != Result.Success)
				{
					FCLog.severe("Creation of GriefPrevention sub claim at " + subClaimLoc.getBlockX() + " " + subClaimLoc.getBlockZ() + " failed! Please review that location manually (" + chestClaimResult.succeeded.toString() + ")");
					continue;
				}

				Claim chestClaim = chestClaimResult.claim;
				chestClaim.setPermission("public", ClaimPermission.Inventory);

				GriefPrevention.instance.dataStore.saveClaim(chestClaim);
			}

		}
		else
		{
			bigClaim.setPermission("public", ClaimPermission.Inventory);
		}
		
		GriefPrevention.instance.dataStore.saveClaim(bigClaim);
	}

	public static boolean containsClaim(World world, int x, int z, int xSize, int zSize, boolean adminOnly)
	{		
		int padding = Settings.getInt(Setting.RESORATION_VILLAGE_CHECK_PADDING);

		int villageMinX = x - padding;
		int villageMaxX = x + xSize + padding;
		int villageMinZ = z - padding;
		int villageMaxZ = z + zSize + padding;		

		ClaimArray ca = GriefPrevention.instance.dataStore.getClaimArray();
		for (int i = 0; i < ca.size(); i++)
		{
			Claim claim = ca.get(i);
			
			if (adminOnly && !claim.isAdminClaim())
				continue;
			
			if (claim.getLesserBoundaryCorner().getWorld() != world)
				continue;
			
			int claimMinX = Math.min(claim.getLesserBoundaryCorner().getBlockX(), claim.getGreaterBoundaryCorner().getBlockX());
			int claimMaxX = Math.max(claim.getLesserBoundaryCorner().getBlockX(), claim.getGreaterBoundaryCorner().getBlockX());
			int claimMinZ = Math.min(claim.getLesserBoundaryCorner().getBlockZ(), claim.getGreaterBoundaryCorner().getBlockZ());
			int claimMaxZ = Math.max(claim.getLesserBoundaryCorner().getBlockZ(), claim.getGreaterBoundaryCorner().getBlockZ());

			if ((((claimMinX < villageMaxX && claimMinX > villageMinX) || (claimMaxX < villageMaxX && claimMaxX > villageMinX)) &&
				((claimMinZ < villageMaxZ && claimMinZ > villageMinZ) || (claimMaxZ < villageMaxZ && claimMaxZ > villageMinZ)))
				||
				(((villageMinX < claimMaxX && villageMinX > claimMinX) || (villageMaxX < claimMaxX && villageMaxX > claimMinX)) &&
				((villageMinZ < claimMaxZ && villageMinZ > claimMinZ) || (villageMaxZ < claimMaxZ && villageMaxZ > claimMinZ))))
			{
				return true;
			}

		}

		return false;		
	}	
	
	public static Deque<Location> getAllClaims()
	{
		ArrayDeque<Location> list = new ArrayDeque<Location>();
		ClaimArray ca = GriefPrevention.instance.dataStore.getClaimArray();
		for (int i = 0; i < ca.size(); i++)
		{
			Claim claim = ca.get(i);

			int claimMinX = Math.min(claim.getLesserBoundaryCorner().getBlockX(), claim.getGreaterBoundaryCorner().getBlockX());
			int claimMinZ = Math.min(claim.getLesserBoundaryCorner().getBlockZ(), claim.getGreaterBoundaryCorner().getBlockZ());
			int claimSizeX = Math.abs(claim.getLesserBoundaryCorner().getBlockX() - claim.getGreaterBoundaryCorner().getBlockX());
			int claimSizeZ = Math.abs(claim.getLesserBoundaryCorner().getBlockZ() - claim.getGreaterBoundaryCorner().getBlockZ());

			int claimCenterX = claimMinX + claimSizeX / 2;
			int claimCenterZ = claimMinZ + claimSizeZ / 2;
			
			Location center = new Location(claim.getLesserBoundaryCorner().getWorld(), claimCenterX, 0, claimCenterZ);
			
			list.addLast(center);
		}
		
		return list;
	}
	
	public static Deque<Location> getPlayerClaims(String player, boolean inverse)
	{
		ArrayDeque<Location> list = new ArrayDeque<Location>();
		ClaimArray ca = GriefPrevention.instance.dataStore.getClaimArray();
		for (int i = 0; i < ca.size(); i++)
		{
			Claim claim = ca.get(i);

			if (inverse == claim.getOwnerName().equals(player))
					continue;
			
			int claimMinX = Math.min(claim.getLesserBoundaryCorner().getBlockX(), claim.getGreaterBoundaryCorner().getBlockX());
			int claimMinZ = Math.min(claim.getLesserBoundaryCorner().getBlockZ(), claim.getGreaterBoundaryCorner().getBlockZ());
			int claimSizeX = Math.abs(claim.getLesserBoundaryCorner().getBlockX() - claim.getGreaterBoundaryCorner().getBlockX());
			int claimSizeZ = Math.abs(claim.getLesserBoundaryCorner().getBlockZ() - claim.getGreaterBoundaryCorner().getBlockZ());

			int claimCenterX = claimMinX + claimSizeX / 2;
			int claimCenterZ = claimMinZ + claimSizeZ / 2;
			
			Location center = new Location(claim.getLesserBoundaryCorner().getWorld(), claimCenterX, 0, claimCenterZ);
			
			list.addLast(center);
		}
		
		return list;
	}
	
	public static boolean playerHasClaim(String player)
	{
		ClaimArray ca = GriefPrevention.instance.dataStore.getClaimArray();
		for (int i = 0; i < ca.size(); i++)
		{
			Claim claim = ca.get(i);

			if (claim.getOwnerName().equals(player))
					return true;			
		}
		
		return false;
	}
	
	public static Location findBiggestClaim(String player)
	{
		ClaimArray ca = GriefPrevention.instance.dataStore.getClaimArray();
		
		Claim biggest = null;
		int biggestSize = 0;
		for (int i = 0; i < ca.size(); i++)
		{
			Claim claim = ca.get(i);
			
			if (!claim.getOwnerName().equals(player))
					continue;
			
			if (biggestSize >= claim.getArea())
				continue;
			
			biggest = claim;
			biggestSize = claim.getArea();
			
		}
				
		if (biggest == null)
			return null;
		
		int claimMinX = Math.min(biggest.getLesserBoundaryCorner().getBlockX(), biggest.getGreaterBoundaryCorner().getBlockX());
		int claimMinZ = Math.min(biggest.getLesserBoundaryCorner().getBlockZ(), biggest.getGreaterBoundaryCorner().getBlockZ());
		int claimSizeX = Math.abs(biggest.getLesserBoundaryCorner().getBlockX() - biggest.getGreaterBoundaryCorner().getBlockX());
		int claimSizeZ = Math.abs(biggest.getLesserBoundaryCorner().getBlockZ() - biggest.getGreaterBoundaryCorner().getBlockZ());

		int claimCenterX = claimMinX + claimSizeX / 2;
		int claimCenterZ = claimMinZ + claimSizeZ / 2;
		
		Location center = new Location(biggest.getLesserBoundaryCorner().getWorld(), claimCenterX, 0, claimCenterZ);
		
		return center;
	}
}
