package us.corenetwork.mantle.nanobot.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.server.v1_8_R2.ItemStack;
import net.minecraft.server.v1_8_R2.NBTBase;
import net.minecraft.server.v1_8_R2.NBTTagByte;
import net.minecraft.server.v1_8_R2.NBTTagByteArray;
import net.minecraft.server.v1_8_R2.NBTTagCompound;
import net.minecraft.server.v1_8_R2.NBTTagDouble;
import net.minecraft.server.v1_8_R2.NBTTagFloat;
import net.minecraft.server.v1_8_R2.NBTTagInt;
import net.minecraft.server.v1_8_R2.NBTTagIntArray;
import net.minecraft.server.v1_8_R2.NBTTagList;
import net.minecraft.server.v1_8_R2.NBTTagLong;
import net.minecraft.server.v1_8_R2.NBTTagShort;
import net.minecraft.server.v1_8_R2.NBTTagString;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import us.core_network.cornel.items.NbtYaml;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.nanobot.ArrayConvert;
import us.corenetwork.mantle.nanobot.NanobotModule;

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

		NBTTagCompound newTag = null;
		try
		{
			newTag = NbtYaml.loadFromFile(args[0]);
		}
		catch (FileNotFoundException e)
		{
			sender.sendMessage("Tag with that name was not found!");
			return;
		}
		catch (IOException e)
		{
			sender.sendMessage("Error while loading nbt file!");
			e.printStackTrace();
			return;
		}
		catch (InvalidConfigurationException e)
		{
			sender.sendMessage("Error: invalid YAML file!");
			e.printStackTrace();
			return;
		}

		if (newTag == null)
		{
			return;
		}
		
		stack.setTag(newTag);

		player.setItemInHand(CraftItemStack.asCraftMirror(stack));	
		
		if (args.length < 2 || !args[1].equals("silent"))
		sender.sendMessage("Tag was loaded sucessfully!");
	}
}
