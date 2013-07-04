package com.mcnsa.flatcore;
import java.lang.reflect.Field;
import java.util.ArrayList;

import net.minecraft.server.v1_5_R3.ChunkProviderHell;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftFirework;
import org.bukkit.craftbukkit.v1_5_R3.generator.NormalChunkGenerator;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

public class Util {

	public static void placeSign(Block block, String message)
	{
		Block belowBlock = block.getRelative(BlockFace.DOWN);
		if (belowBlock != null && belowBlock.getType().isSolid())
		{
			block.setType(Material.SIGN_POST);
		}
		else
		{
			block.setType(Material.WALL_SIGN);
			
		}
			
		Sign sign = (Sign) block.getState();
		if (block.getType() == Material.WALL_SIGN)
		{
			//Rotate sign so it will be on wall
			org.bukkit.material.Sign data = (org.bukkit.material.Sign) sign.getData();
			for (BlockFace face : new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH})
			{
				Block nextBlock = block.getRelative(face);
				if (nextBlock != null && nextBlock.getType().isSolid())
				{
					data.setFacingDirection(face.getOppositeFace());
					sign.setData(data);
					
					break;
				}
			}
		}

		populateSign(message, sign);


		sign.update();
	}

	public static void populateSign(String message, Sign sign)
	{
		message = message.replaceAll("\\&([0-9abcdef])", ChatColor.COLOR_CHAR + "$1");
		String[] lines = message.split("\\[NEWLINE\\]");

		int max = Math.min(4, lines.length);
		for (int i = 0; i < max; i++)
		{
			sign.setLine(i, lines[i]);
		}
	}

	public static Block findBestSignLocation(ArrayList<Block> blocks)
	{
		for (Block b : blocks)
		{
			if (!b.isEmpty())
				continue;

			Block upperBlock = b.getRelative(BlockFace.UP);
			Block lowerBlock = b.getRelative(BlockFace.DOWN);

			if (upperBlock != null && upperBlock.isEmpty())
				return b;
		}

		for (Block b : blocks)
		{
			if (!b.isEmpty())
				continue;

			Block upperBlock = b.getRelative(BlockFace.UP);

			if (upperBlock != null && upperBlock.isEmpty())
				return b;
		}

		for (Block b : blocks)
		{
			if (!b.isEmpty())
				continue;

			Block lowerBlock = b.getRelative(BlockFace.DOWN);

			if (lowerBlock != null && lowerBlock.getType().isSolid())
				return b;
		}

		for (Block b : blocks)
		{
			if (b.isEmpty())
				return b;			
		}

		return blocks.get(0);
	}

	public static void Message(String message, CommandSender sender)
	{
		message = message.replaceAll("\\&([0-9abcdefklmnor])", ChatColor.COLOR_CHAR + "$1");

		final String newLine = "\\[NEWLINE\\]";
		String[] lines = message.split(newLine);

		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].trim();
			
			if (i == 0)
				continue;

			int lastColorChar = lines[i - 1].lastIndexOf(ChatColor.COLOR_CHAR);
			if (lastColorChar == -1 || lastColorChar >= lines[i - 1].length() - 1)
				continue;

			char lastColor = lines[i - 1].charAt(lastColorChar + 1);
			lines[i] = Character.toString(ChatColor.COLOR_CHAR).concat(Character.toString(lastColor)).concat(lines[i]);	
			System.out.println(lines[i]);
		}		
		
		for (int i = 0; i < lines.length; i++)
			sender.sendMessage(lines[i]);


	}

	public static void Broadcast(String message, String exclusion)
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (!p.getName().equals(exclusion))
				Util.Message(message, p);
		}

	}

	public static void MessagePermissions(String message, String permission)
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (p.hasPermission(permission))
				Util.Message(message, p);
		}
	}

	public static void showFirework(Location location, FireworkEffect effect)
	{
		Firework firework = location.getWorld().spawn(location, Firework.class);

		FireworkMeta meta = firework.getFireworkMeta();
		meta.clearEffects();
		meta.addEffect(effect);
		meta.setPower(0);
		firework.setFireworkMeta(meta);

		net.minecraft.server.v1_5_R3.EntityFireworks nmsFirework = ((CraftFirework) firework).getHandle();
		net.minecraft.server.v1_5_R3.World world = ((CraftWorld) location.getWorld()).getHandle();

		world.broadcastEntityEffect(nmsFirework, (byte) 17);

		firework.remove();
	}


	public static Boolean isInteger(String text) {
		try {
			Integer.parseInt(text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static Boolean isDouble(String text) {
		try {
			Double.parseDouble(text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}


	public static boolean isNetherFortress(Location location)
	{
		World world = location.getWorld();

		if (world.getEnvironment() != Environment.NETHER)
			return false;

		NormalChunkGenerator generator = (NormalChunkGenerator) ((CraftWorld) world).getHandle().chunkProviderServer.chunkProvider;

		Field f;
		try {
			f = generator.getClass().getSuperclass().getDeclaredField("provider");
			f.setAccessible(true);
			ChunkProviderHell provider = (ChunkProviderHell) f.get(generator);

			return provider.c.a(location.getBlockX(), location.getBlockY(), location.getBlockZ());

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

		return false;

	}
	
	public static void safeTeleport(final Player player, final Location location)
	{
		Chunk c = location.getChunk();
		if (!c.isLoaded())
			location.getChunk().load();
		player.teleport(location);
		
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MCNSAFlatcore.instance, new Runnable() {
			@Override
			public void run() {
				player.teleport(location);
				
			}
		}, 10);
	}

	public static int flatDistance(Location a, Location b)
	{
		return ((a.getBlockX() - b.getBlockX()) * (a.getBlockX() - b.getBlockX())) + ((a.getBlockZ() - b.getBlockZ()) * (a.getBlockZ() - b.getBlockZ()));
	}
	
	public static Location unserializeLocation(String text)
	{
		String[] split = text.split(";");
	
		World world = Bukkit.getWorld(split[0]);
		double x = Double.parseDouble(split[1]);
		double y = Double.parseDouble(split[2]);
		double z = Double.parseDouble(split[3]);
		float pitch = Float.parseFloat(split[4]);
		float yaw = Float.parseFloat(split[5]);
		
		return new Location(world, x, y, z, yaw, pitch);
	}
	
	public static String serializeLocation(Location location)
	{
		String locString = location.getWorld().getName().concat(";");
		locString = locString.concat(Double.toString(location.getX())).concat(";").concat(Double.toString(location.getY())).concat(";").concat(Double.toString(location.getZ())).concat(";");
		locString = locString.concat(Float.toString(location.getPitch())).concat(";").concat(Float.toString(location.getYaw()));
		
		return locString;

	}
}
