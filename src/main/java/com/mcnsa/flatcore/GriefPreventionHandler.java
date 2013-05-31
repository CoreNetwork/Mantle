package com.mcnsa.flatcore;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimArray;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.CreateClaimResult;
import me.ryanhamshire.GriefPrevention.CreateClaimResult.Result;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.World;


public class GriefPreventionHandler {

	public static void secureOutpost(Location center)
	{
		int radius = Settings.getInt(Setting.OUTPOST_PROTECITON_RADIUS);
		World world = center.getWorld();

		int x1 = center.getBlockX() - radius;
		int z1 = center.getBlockZ() - radius;
		int x2 = center.getBlockX() + radius;
		int z2 = center.getBlockZ() + radius;

		CreateClaimResult bigClaimResult = GriefPrevention.instance.dataStore.createClaim(world, x1, x2, 1, 256, z1, z2, "", null, null, false);
		if (bigClaimResult.succeeded != Result.Success)
		{
			FCLog.severe("Creation of GriefPrevention claim around outpost at " + center.getBlockX() + " " + center.getBlockZ() + " failed! Please review that location manually (" + bigClaimResult.succeeded.toString() + ")");
			return;
		}
		Claim bigClaim = bigClaimResult.claim;
		bigClaim.setPermission("public", ClaimPermission.Inventory);

		FCLog.info("Creating claim " + bigClaim.getID());

		GriefPrevention.instance.dataStore.saveClaim(bigClaim);
	}

	public static void secureCampfire(Location center)
	{
		int radius = Settings.getInt(Setting.CAMPFIRE_PROTECTION_RADIUS);
		World world = center.getWorld();

		int x1 = center.getBlockX() - radius;
		int z1 = center.getBlockZ() - radius;
		int x2 = center.getBlockX() + radius;
		int z2 = center.getBlockZ() + radius;

		CreateClaimResult bigClaimResult = GriefPrevention.instance.dataStore.createClaim(world, x1, x2, 1, 256, z1, z2, "", null, null, false);
		if (bigClaimResult.succeeded != Result.Success)
		{
			FCLog.severe("Creation of GriefPrevention claim around chest at " + center.getBlockX() + " " + center.getBlockZ() + " failed! Please review that location manually (" + bigClaimResult.succeeded.toString() + ")");
			return;
		}
		Claim bigClaim = bigClaimResult.claim;
		bigClaim.setPermission("public", ClaimPermission.Access);

		CreateClaimResult chestClaimResult = GriefPrevention.instance.dataStore.createClaim(center.getWorld(), center.getBlockX(), center.getBlockX(), center.getBlockY(), center.getBlockY(), center.getBlockZ(), center.getBlockZ(), "", bigClaim, null, false);
		if (chestClaimResult.succeeded != Result.Success)
		{
			FCLog.severe("Creation of GriefPrevention claim for chest at " + center.getBlockX() + " " + center.getBlockZ() + " failed! Please review that location manually (" + chestClaimResult.succeeded.toString() + ")");
			return;
		}

		Claim chestClaim = chestClaimResult.claim;
		chestClaim.setPermission("public", ClaimPermission.Inventory);

		GriefPrevention.instance.dataStore.saveClaim(bigClaim);
		GriefPrevention.instance.dataStore.saveClaim(chestClaim);
	}

	public static boolean containsClaim(int x, int z, int xSize, int zSize, boolean adminOnly)
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
