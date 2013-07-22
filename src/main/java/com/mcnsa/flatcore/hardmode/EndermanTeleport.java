package com.mcnsa.flatcore.hardmode;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.FCLog;
import com.mcnsa.flatcore.MCNSAFlatcore;
import com.mcnsa.flatcore.Util;

public class EndermanTeleport {
	public static void teleportPlayer(Player player, Enderman enderman)
	{
		//Pick teleportation spot
		int maxTries = HardmodeSettings.ENDERMAN_TELEPORT_MAX_TRIES.integer();
		int maxDistance = HardmodeSettings.ENDERMAN_TELEPORT_RANGE.integer();
		int minDistance = HardmodeSettings.ENDERMAN_TELEPORT_EXCLUDING_ZONE.integer();
		int diff = maxDistance - minDistance;
		
		int tries = 0;
		Location teleportSpot = null;
		
		do
		{
			tries++;
			if (tries > maxTries)
			{
				Builder builder = FireworkEffect.builder();
				Util.showFirework(enderman.getLocation(), builder.trail(false).withColor(Color.PURPLE).build());	
				player.getWorld().playSound(enderman.getLocation(), Sound.PORTAL_TRIGGER, 1, 1);

				enderman.remove();
				return;
			}
			
			int x = player.getLocation().getBlockX() + MCNSAFlatcore.random.nextInt(diff) + minDistance;
			int z = player.getLocation().getBlockZ() + MCNSAFlatcore.random.nextInt(diff) + minDistance;
			
			teleportSpot = new Location(player.getWorld(), x + 0.5, player.getLocation().getY(), z + 0.5);
		}
		while (!isValidLocation(teleportSpot));
		
		//Calculating proper pitch and yaw
		
		Location enderHigherLocation = enderman.getLocation().clone().add(0, 1, 0);
		double distance = enderHigherLocation.distance(teleportSpot);

		//Calculating pitch
		double heightDifference = Math.abs(teleportSpot.getY() - enderHigherLocation.getY());
		
		float pitch = 0;
		if (heightDifference != 0)
			pitch = (float) -Math.toDegrees(Math.asin(heightDifference / distance));
		
		if (heightDifference < 0)
			pitch = -pitch;
				
		teleportSpot.setPitch(pitch);
		
		//Calculating yaw
		double zDifference = teleportSpot.getZ() - enderHigherLocation.getZ();
		double xDifference = teleportSpot.getX() - enderHigherLocation.getX();
		
		float yaw = (float) Math.toDegrees(Math.atan2(zDifference, xDifference));
		yaw += 90;
		if (yaw > 180)
			yaw = -360 + yaw; 
				
		teleportSpot.setYaw(yaw);
		
		player.teleport(teleportSpot);
		player.getWorld().playSound(teleportSpot, Sound.ENDERMAN_TELEPORT, 1, 1);

	}
	
	public static boolean isValidLocation(Location location)
	{
		Block pickedBlock = location.getBlock();

		if (!pickedBlock.isEmpty())
			return false;
		
		Block belowBlock = pickedBlock.getRelative(BlockFace.DOWN);

		if (belowBlock == null || !belowBlock.getType().isSolid())
			return false;
		
		Block aboveBlock = pickedBlock.getRelative(BlockFace.UP);
		if (aboveBlock == null || !aboveBlock.isEmpty())
			return false;
		
		return true;

	}
	
	public static boolean canMove(Location a, Location b)
	{
		int minX = Math.min(a.getBlockX(), b.getBlockX());
		int minZ = Math.min(a.getBlockZ(), b.getBlockZ());
		
		int maxX = Math.max(a.getBlockX(), b.getBlockX());
		int maxZ = Math.max(a.getBlockZ(), b.getBlockZ());
		
		int minY = Math.min(a.getBlockY(), b.getBlockY());
		int maxY = Math.max(a.getBlockY(), b.getBlockY()) + 3;
		
		for (int x = minX; x <= maxX; x++)
		{
			for (int z = minZ; z <= maxZ; z++)
			{
				for (int y = minY; y < maxY; y++)
				{
					Block relativeBlock = a.getWorld().getBlockAt(x, y, z);
					if (relativeBlock != null && !relativeBlock.isEmpty())
						return false;
				}
			}
		}
		
		return true;
	}
}
