package us.corenetwork.mantle.nanobot.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.server.v1_6_R2.ItemStack;
import net.minecraft.server.v1_6_R2.NBTBase;
import net.minecraft.server.v1_6_R2.NBTTagByte;
import net.minecraft.server.v1_6_R2.NBTTagByteArray;
import net.minecraft.server.v1_6_R2.NBTTagCompound;
import net.minecraft.server.v1_6_R2.NBTTagDouble;
import net.minecraft.server.v1_6_R2.NBTTagFloat;
import net.minecraft.server.v1_6_R2.NBTTagInt;
import net.minecraft.server.v1_6_R2.NBTTagIntArray;
import net.minecraft.server.v1_6_R2.NBTTagList;
import net.minecraft.server.v1_6_R2.NBTTagLong;
import net.minecraft.server.v1_6_R2.NBTTagShort;
import net.minecraft.server.v1_6_R2.NBTTagString;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.nanobot.ArrayConvert;
import us.corenetwork.mantle.nanobot.NanobotModule;
import us.corenetwork.mantle.nanobot.NanobotUtil;

public class LoadCommand extends NanobotBaseCommand {

	public LoadCommand() {
		needPlayer = true;
		adminCommand = true;
		desc = "Load and apply tag to item in hand";
		permission = "load";
	}

	@SuppressWarnings("unchecked")
	public void run(CommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.sendMessage("Syntax: /nbt load [name]");
			return;
		}

		Player player = (Player) sender;
		CraftItemStack inHand = (CraftItemStack) player.getItemInHand();
		ItemStack stack = CraftItemStack.asNMSCopy(inHand);

		if (inHand == null || inHand.getType() == Material.AIR)
		{
			sender.sendMessage("Your hands are empty!");
			return;
		}
		
		NBTTagCompound newTag = load(args[0]);
		
		if (newTag == null)
		{
			sender.sendMessage("Tag with that name was not found!");
			return;
		}
		
		stack.tag = newTag;

		player.setItemInHand(CraftItemStack.asCraftMirror(stack));	
		
		if (args.length < 2 || !args[1].equals("silent"))
		sender.sendMessage("Tag was loaded sucessfully!");
	}
	
	public static NBTTagCompound load(String name)
	{
		YamlConfiguration yaml = new YamlConfiguration();
		
		try {
			yaml.load(new File(NanobotModule.folder, name + ".yml"));
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			MLog.severe("Error while loading tag yml file - " + e.getMessage());
			e.printStackTrace();
			return null;
		} catch (InvalidConfigurationException e) {
			MLog.severe("Error while loading tag yml file - " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		
		return load(yaml.getValues(false));
	}
	
	public static NBTTagCompound load(Map<?,?> section)
	{
		NBTTagCompound newTag = new NBTTagCompound();
		
		for (Entry<?, ?> e : section.entrySet())
		{
			NBTBase tag =  loadTag(e.getValue(), e.getKey().equals("compound"));
			tag.setName((String) e.getKey());
			newTag.set((String) e.getKey(), tag);
		}
			
		return newTag;
	}
	
	public static NBTBase loadTag(Object tag)
	{
		return loadTag(tag, false);
	}
	
	public static NBTBase loadTag(Object tag, boolean isCompound)
	{
		if (tag instanceof String)
		{
			return new NBTTagString(null, NanobotUtil.fixFormatting((String) tag));
		}
		else if (tag instanceof ArrayList)
		{
			NBTTagList list = new NBTTagList(null);
			for (Object o : (ArrayList) tag)
				list.add(loadTag(o));
			
			return list;
		}
		else if (tag instanceof MemorySection || tag instanceof LinkedHashMap)
		{
			Map<String, Object> map;
			
			if (tag instanceof MemorySection)
			{
				MemorySection section = (MemorySection) tag;
				map = section.getValues(false);
			}
			else
				map = (Map) tag;
			
			if (isCompound)
			{
				NBTTagCompound compound = new NBTTagCompound(null);
								
				for (Entry<String, Object> ee : map.entrySet())
				{
					Bukkit.getServer().broadcastMessage("ee - " + ee.getKey());

					NBTBase eTag = loadTag(ee.getValue(), ee.getKey().equals("compound"));
					eTag.setName(ee.getKey());
					compound.set(ee.getKey(), eTag);
				}
				
				return compound;
			}
			
			for (Entry<String, Object> e : map.entrySet())
			{
				if (e.getKey().equals("byte"))
				{
					return new NBTTagByte(null, (byte) (int) (Integer) e.getValue());
				}
				else if (e.getKey().equals("short"))
				{
					return new NBTTagShort(null, (short) (int) (Integer) e.getValue());
				}
				else if (e.getKey().equals("int"))
				{
					return new NBTTagInt(null, (Integer) e.getValue());
				}
				else if (e.getKey().equals("long"))
				{
					return new NBTTagLong(null, (long) (int) (Integer) e.getValue());
				}
				else if (e.getKey().equals("float"))
				{
					return new NBTTagFloat(null, (float) (int) (Integer) e.getValue());
				}
				else if (e.getKey().equals("double"))
				{
					return new NBTTagDouble(null, (double) (int) (Integer) e.getValue());
				}
				else if (e.getKey().equals("byteArray"))
				{
					return new NBTTagByteArray(null, ArrayConvert.convert(((ArrayList<Integer>) e.getValue()).toArray(new Byte[0])));
				}
				else if (e.getKey().equals("intArray"))
				{
					return new NBTTagIntArray(null, ArrayConvert.convert(((ArrayList<Integer>) e.getValue()).toArray(new Integer[0])));
				}
				else if (e.getKey().equals("compound"))
				{
					NBTTagCompound compound = new NBTTagCompound(null);
					
					Map<String, Object> inMap = null;
					
					if (e.getValue() instanceof MemorySection)
					{
						MemorySection section = (MemorySection) e.getValue();
						inMap = section.getValues(false);
					}
					else
						inMap = (Map) e.getValue();
					
					for (Entry<String, Object> ee : inMap.entrySet())
					{
						NBTBase eTag = loadTag(ee.getValue(), ee.getKey().equals("compound"));
						eTag.setName(ee.getKey());
						compound.set(ee.getKey(), eTag);
					}
					
					return compound;
				}
			}
		}
				
		return null;
	}
		

}