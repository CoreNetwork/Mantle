package com.mcnsa.flatcore;
import java.lang.reflect.Field;
import java.util.ArrayList;

import net.minecraft.server.v1_5_R3.ChunkProviderHell;

import org.bukkit.Bukkit;
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
	public static final String colorCharacter = "\u00A7";
	
	
	public static void placeSign(Block block, String message)
	{
		block.setType(Material.SIGN_POST);
		
		
		Sign sign = (Sign) block.getState();
		populateSign(message, sign);
		sign.update();
	}
	
	public static void populateSign(String message, Sign sign)
	{
		message = message.replaceAll("\\&([0-9abcdef])", colorCharacter + "$1");
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
						
			if (upperBlock != null && upperBlock.isEmpty() && lowerBlock != null && lowerBlock.getType().isSolid())
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
		message = message.replaceAll("\\&([0-9abcdef])", colorCharacter + "$1");

		String color = "f";
		final int maxLength = 59; //Max length of chat text message
		final String newLine = "[NEWLINE]";
		ArrayList<String> chat = new ArrayList<String>();
		chat.add(0, "");
		String[] words = message.split(" ");
		int lineNumber = 0;
		for (int i = 0; i < words.length; i++) {
			if (chat.get(lineNumber).replaceAll("\\" + colorCharacter + "([0-9abcdef])", "").length() + words[i].replaceAll("\\" + colorCharacter + "([0-9abcdef])", "").length() < maxLength && !words[i].equals(newLine)) {
				chat.set(lineNumber, chat.get(lineNumber) + (chat.get(lineNumber).length() > 0 ? " " : colorCharacter + color ) + words[i]);

				if (words[i].contains(colorCharacter)) color = Character.toString(words[i].charAt(words[i].lastIndexOf(colorCharacter) + 1));
			}
			else {
				lineNumber++;
				if (!words[i].equals(newLine)) {
					chat.add(lineNumber, colorCharacter + color + words[i]);
				}
				else
					chat.add(lineNumber, "");
			}
		}
		for (int i = 0; i < chat.size(); i++) {
			{
				sender.sendMessage(chat.get(i));
			}

		}
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
}
