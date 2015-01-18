package us.corenetwork.mantle;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.CreateClaimResult;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class GriefPreventionHandler {

	public static void secure(Location corner, List<Location> chestSubclaims, int xSize, int zSize, int padding, Integer claimPermission)
	{
		World world = corner.getWorld();
		
		int x1 = corner.getBlockX() - padding;
		int z1 = corner.getBlockZ() - padding;
		int x2 = corner.getBlockX() + xSize + padding - 1;
		int z2 = corner.getBlockZ() + zSize + padding - 1;

		
		CreateClaimResult bigClaimResult = GriefPrevention.instance.dataStore.createClaim(world, x1, x2, 1, 256, z1, z2, null, null, null);
		if (bigClaimResult.succeeded != true)
		{
			MLog.severe("Creation of GriefPrevention claim at " + corner.getBlockX() + " " + corner.getBlockZ() + " failed! Please review that location manually");
			return;
		}
		
		Claim bigClaim = bigClaimResult.claim;

		MLog.debug("Creating claim " + bigClaim.getID());
		
		if (claimPermission != null)
		{
			switch (claimPermission)
			{
			case 0:
				bigClaim.setPermission("public", ClaimPermission.Access);
				break;
			case 1:
				bigClaim.setPermission("public", ClaimPermission.Inventory);
				break;
			case 2:
				bigClaim.setPermission("public", ClaimPermission.Build);
				break;
			}
		}
		
		
		if (chestSubclaims != null)
		{			
			for (Location subClaimLoc : chestSubclaims)
			{
				CreateClaimResult chestClaimResult = GriefPrevention.instance.dataStore.createClaim(subClaimLoc.getWorld(), subClaimLoc.getBlockX(), subClaimLoc.getBlockX(), subClaimLoc.getBlockY(), subClaimLoc.getBlockY(), subClaimLoc.getBlockZ(), subClaimLoc.getBlockZ(), null, bigClaim, null);
				if (chestClaimResult.succeeded != true)
				{
					MLog.severe("Creation of GriefPrevention sub claim at " + subClaimLoc.getBlockX() + " " + subClaimLoc.getBlockZ() + " failed! Please review that location manually");
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

	public static Claim getClaimAt(Location location)
	{
		return GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
	}
	
	public static boolean containsClaim(World world, int x, int z, int xSize, int zSize, int padding, boolean adminOnly, Player player)
	{		
		Rectangle villageRectangle = new Rectangle(x - padding, z - padding, xSize + padding, zSize + padding);
		
		ArrayList<Claim> ca =  getAllClaims();
		for (int i = 0; i < ca.size(); i++)
		{
			Claim claim = ca.get(i);
			
			if (adminOnly && !claim.isAdminClaim())
				continue;
			
			if (claim.getGreaterBoundaryCorner().getWorld().getName().equals(world.getName()) == false)
				continue;
			
			if (player != null && (claim.allowAccess(player) == null))
				continue;
			
			int claimMinX = Math.min(claim.getLesserBoundaryCorner().getBlockX(), claim.getGreaterBoundaryCorner().getBlockX());
			int claimMaxX = Math.max(claim.getLesserBoundaryCorner().getBlockX(), claim.getGreaterBoundaryCorner().getBlockX());
			int claimMinZ = Math.min(claim.getLesserBoundaryCorner().getBlockZ(), claim.getGreaterBoundaryCorner().getBlockZ());
			int claimMaxZ = Math.max(claim.getLesserBoundaryCorner().getBlockZ(), claim.getGreaterBoundaryCorner().getBlockZ());

			Rectangle claimRectangle = new Rectangle(claimMinX, claimMinZ, claimMaxX - claimMinX + 1, claimMaxZ - claimMinZ + 1);
			if (villageRectangle.intersects(claimRectangle))
				return true;

		}

		return false;		
	}
	
	public static List<Claim> getClaimsInside(World world, int x, int z, int xSize, int zSize, int padding, boolean adminOnly, Player player)
	{
		List<Claim> claims = new LinkedList<Claim>();
		
		Rectangle villageRectangle = new Rectangle(x - padding, z - padding, xSize + padding, zSize + padding);

		ArrayList<Claim> ca =  getAllClaims();
		for (int i = 0; i < ca.size(); i++)
		{
			Claim claim = ca.get(i);
			
			if (adminOnly && !claim.isAdminClaim())
				continue;
			
			if (claim.getGreaterBoundaryCorner().getWorld().getName().equals(world.getName()) == false)
				continue;
			
			if (player != null && (claim.allowAccess(player) == null))
				continue;
			
			int claimMinX = Math.min(claim.getLesserBoundaryCorner().getBlockX(), claim.getGreaterBoundaryCorner().getBlockX());
			int claimMaxX = Math.max(claim.getLesserBoundaryCorner().getBlockX(), claim.getGreaterBoundaryCorner().getBlockX());
			int claimMinZ = Math.min(claim.getLesserBoundaryCorner().getBlockZ(), claim.getGreaterBoundaryCorner().getBlockZ());
			int claimMaxZ = Math.max(claim.getLesserBoundaryCorner().getBlockZ(), claim.getGreaterBoundaryCorner().getBlockZ());

			Rectangle claimRectangle = new Rectangle(claimMinX, claimMinZ, claimMaxX - claimMinX, claimMaxZ - claimMinZ);
			if (villageRectangle.intersects(claimRectangle))
				claims.add(claim);
		}
		
		return claims;
	}
	
	public static void deleteClaimsInside(World world, int x, int z, int xSize, int zSize, int padding, boolean adminOnly, Player player)
	{		
		List<Claim> claimsToDelete = getClaimsInside(world, x, z, xSize, zSize, padding, adminOnly, player);
		
		for (Claim claim : claimsToDelete)
			GriefPrevention.instance.dataStore.deleteClaim(claim);
	}
	
	public static void enableExplosions(World world)
	{
		for(Claim claim : getAllClaims())
		{
			if (claim.getGreaterBoundaryCorner().getWorld().getName().equals(world.getName()))
				claim.areExplosivesAllowed = true;
		}
	}
	
	private static ArrayList<Claim> getAllClaims()
	{
		ArrayList<Claim> ca = new ArrayList<Claim>();
		try
		{
			Field privateField = DataStore.class.getDeclaredField("claims");
			privateField.setAccessible(true);
			ca = (ArrayList<Claim>) privateField.get(GriefPrevention.instance.dataStore);
		} catch (Exception e)
		{
			MLog.severe("Reflection error, blah.");
			e.printStackTrace();
		}
		return ca;
	}
}
