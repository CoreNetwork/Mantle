package us.corenetwork.mantle.nanobot.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.server.v1_8_R1.ItemStack;
import net.minecraft.server.v1_8_R1.NBTBase;
import net.minecraft.server.v1_8_R1.NBTTagByte;
import net.minecraft.server.v1_8_R1.NBTTagByteArray;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.NBTTagDouble;
import net.minecraft.server.v1_8_R1.NBTTagFloat;
import net.minecraft.server.v1_8_R1.NBTTagInt;
import net.minecraft.server.v1_8_R1.NBTTagIntArray;
import net.minecraft.server.v1_8_R1.NBTTagList;
import net.minecraft.server.v1_8_R1.NBTTagLong;
import net.minecraft.server.v1_8_R1.NBTTagShort;
import net.minecraft.server.v1_8_R1.NBTTagString;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
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
		
		stack.setTag(newTag);

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
			return new NBTTagString(NanobotUtil.fixFormatting((String) tag));
		}
		else if (tag instanceof ArrayList)
		{
			NBTTagList list = new NBTTagList();
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
				NBTTagCompound compound = new NBTTagCompound();
								
				for (Entry<String, Object> ee : map.entrySet())
				{
					Bukkit.getServer().broadcastMessage("ee - " + ee.getKey());

					NBTBase eTag = loadTag(ee.getValue(), ee.getKey().equals("compound"));
					compound.set(ee.getKey(), eTag);
				}
				
				return compound;
			}
			
			for (Entry<String, Object> e : map.entrySet())
			{
				if (e.getKey().equals("byte"))
				{
					return new NBTTagByte((byte) (int) (Integer) e.getValue());
				}
				else if (e.getKey().equals("short"))
				{
					return new NBTTagShort((short) (int) (Integer) e.getValue());
				}
				else if (e.getKey().equals("int"))
				{
					return new NBTTagInt((Integer) e.getValue());
				}
				else if (e.getKey().equals("long"))
				{
					return new NBTTagLong((long) (int) (Integer) e.getValue());
				}
				else if (e.getKey().equals("float"))
				{
					return new NBTTagFloat((float) (int) (Integer) e.getValue());
				}
				else if (e.getKey().equals("double"))
				{
					return new NBTTagDouble((double) (int) (Integer) e.getValue());
				}
				else if (e.getKey().equals("byteArray"))
				{
					return new NBTTagByteArray(ArrayConvert.convert(((ArrayList<Integer>) e.getValue()).toArray(new Byte[0])));
				}
				else if (e.getKey().equals("intArray"))
				{
					return new NBTTagIntArray(ArrayConvert.convert(((ArrayList<Integer>) e.getValue()).toArray(new Integer[0])));
				}
				else if (e.getKey().equals("compound"))
				{
					NBTTagCompound compound = new NBTTagCompound();
					
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
						compound.set(ee.getKey(), eTag);
					}
					
					return compound;
				}
			}
		}
				
		return null;
	}
		

}
