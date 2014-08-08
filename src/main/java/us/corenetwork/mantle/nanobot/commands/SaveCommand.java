package us.corenetwork.mantle.nanobot.commands;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.server.v1_7_R4.ItemStack;
import net.minecraft.server.v1_7_R4.NBTBase;
import net.minecraft.server.v1_7_R4.NBTTagByte;
import net.minecraft.server.v1_7_R4.NBTTagByteArray;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagDouble;
import net.minecraft.server.v1_7_R4.NBTTagFloat;
import net.minecraft.server.v1_7_R4.NBTTagInt;
import net.minecraft.server.v1_7_R4.NBTTagIntArray;
import net.minecraft.server.v1_7_R4.NBTTagList;
import net.minecraft.server.v1_7_R4.NBTTagLong;
import net.minecraft.server.v1_7_R4.NBTTagShort;
import net.minecraft.server.v1_7_R4.NBTTagString;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;

import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.nanobot.ArrayConvert;
import us.corenetwork.mantle.nanobot.NanobotModule;

public class SaveCommand extends NanobotBaseCommand {

	public SaveCommand() {
		needPlayer = true;
		adminCommand = true;
		desc = "Save item to file";
		permission = "save";
	}

	@SuppressWarnings("unchecked")
	public void run(CommandSender sender, String[] args) {

		if (args.length < 1) {
			sender.sendMessage("Syntax: /nbt save [name]");
			return;
		}

		Player player = (Player) sender;
		CraftItemStack inHand = (CraftItemStack) player.getItemInHand();
		ItemStack stack = CraftItemStack.asNMSCopy(inHand);

		YamlConfiguration yaml = new YamlConfiguration();

		if (stack.tag == null)
		{
			if (inHand.getType() == Material.POTION)
			{
				Potion potion = Potion.fromItemStack(inHand);
				
				PotionMeta meta = (PotionMeta) inHand.getItemMeta();
				for (PotionEffect effect : potion.getEffects())
					meta.addCustomEffect(effect, true);
				
				inHand.setItemMeta(meta);
				stack = CraftItemStack.asNMSCopy(inHand);
			}
			else
			{
				sender.sendMessage("This item has no data!");
				return;
			}
		}
		
		Set<String> tagKeys = stack.tag.c();
		for (String key : tagKeys) {
			addTag(yaml, key, stack.tag.get(key));
		}

		try {
			yaml.save(new File(NanobotModule.folder, args[0] + ".yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sender.sendMessage("Tag was saved sucessfully!");
	}

	public static void addTag(ConfigurationSection yaml, String name, NBTBase tag) {
		switch (tag.getTypeId()) {
		case 1: // Byte
			yaml.set(name + ".byte", ((NBTTagByte) tag).f());
			break;
		case 2: // Short
			yaml.set(name + ".short", ((NBTTagShort) tag).e());
			break;
		case 3: // Integer
			yaml.set(name + ".int", ((NBTTagInt) tag).d());
			break;
		case 4: // Long
			yaml.set(name + ".long", ((NBTTagLong) tag).c());
			break;
		case 5: // Float
			yaml.set(name + ".float", ((NBTTagFloat) tag).h());
			break;
		case 6: // Double
			yaml.set(name + ".double", ((NBTTagDouble) tag).g());
			break;
		case 7: // Byte Array
			yaml.set(name + ".byteArray", ArrayConvert.convert(((NBTTagByteArray) tag).c()));
			break;
		case 11: // Int array
			yaml.set(name + ".intArray", ArrayConvert.convert(((NBTTagIntArray) tag).c()));
			break;
		case 8: // String
			yaml.set(name, ((NBTTagString) tag).a_());
			break;
		case 9: // List
			try
			{
				NBTTagList listTag = (NBTTagList) tag;
				
				Field listField = NBTTagList.class.getDeclaredField("list");
				listField.setAccessible(true);
				
				List tags = (List) listField.get(listTag);
				
				List list = new ArrayList();
				if (listTag.get(0).getTypeId() == 8)
				{
					for (int i = 0; i < listTag.size(); i++) {
						list.add(((NBTTagString)tags.get(i)).a_());
					}
				}
				else
				{
					for (int i = 0; i < listTag.size(); i++) {
						ConfigurationSection listSection = new YamlConfiguration()
								.createSection("foo");
						addTagWithoutName(listSection, listTag.get(i));
						list.add(listSection);
					}
				}
				

				yaml.set(name, list.toArray());
			}
			catch (Exception e)
			{
				MLog.severe("Something went seriously wrong. Go bug matejdro.");
				e.printStackTrace();
			}
			break;
		case 10: // Compound
			ConfigurationSection newSection = yaml.createSection(name).createSection("compound");

			NBTTagCompound compoundTag = (NBTTagCompound) tag;
			Set<String> tagKeys = compoundTag.c();
			for (String key : tagKeys) {
				addTag(newSection, key, compoundTag.get(key));
			}
		}
	}

	public static void addTagWithoutName(ConfigurationSection yaml, NBTBase tag) {
		switch (tag.getTypeId()) {
		case 1: // Byte
			yaml.set("byte", ((NBTTagByte) tag).f());
			break;
		case 2: // Short
			yaml.set("short", ((NBTTagShort) tag).e());
			break;
		case 3: // Integer
			yaml.set("int", ((NBTTagInt) tag).d());
			break;
		case 4: // Long
			yaml.set("long", ((NBTTagLong) tag).c());
			break;
		case 5: // Float
			yaml.set("float", ((NBTTagFloat) tag).h());
			break;
		case 6: // Double
			yaml.set("double", ((NBTTagDouble) tag).g());
			break;
		case 7: // Byte Array
			yaml.set("byteArray",  ArrayConvert.convert(((NBTTagByteArray) tag).c()));
			break;
		case 11: // Int array
			yaml.set("intArray", ArrayConvert.convert(((NBTTagIntArray) tag).c()));
			break;
		case 10: // Compound
			ConfigurationSection newSection = yaml.createSection("compound");

			NBTTagCompound compoundTag = (NBTTagCompound) tag;
			Set<String> tagKeys = compoundTag.c();
			for (String key : tagKeys) {
				addTag(newSection, key, compoundTag.get(key));
			}

		}
	}
}
