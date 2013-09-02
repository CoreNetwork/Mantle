package us.corenetwork.mantle.nanobot.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;

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
		
		Collection<NBTBase> c = stack.tag.c();
		for (NBTBase base : c) {
			addTag(yaml, base);
		}

		try {
			yaml.save(new File(NanobotModule.folder, args[0] + ".yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sender.sendMessage("Tag was saved sucessfully!");
	}

	public void addTag(ConfigurationSection yaml, NBTBase tag) {
		switch (tag.getTypeId()) {
		case 1: // Byte
			yaml.set(tag.getName() + ".byte", ((NBTTagByte) tag).data);
			break;
		case 2: // Short
			yaml.set(tag.getName() + ".short", ((NBTTagShort) tag).data);
			break;
		case 3: // Integer
			yaml.set(tag.getName() + ".int", ((NBTTagInt) tag).data);
			break;
		case 4: // Long
			yaml.set(tag.getName() + ".long", ((NBTTagLong) tag).data);
			break;
		case 5: // Float
			yaml.set(tag.getName() + ".float", ((NBTTagFloat) tag).data);
			break;
		case 6: // Double
			yaml.set(tag.getName() + ".double", ((NBTTagDouble) tag).data);
			break;
		case 7: // Byte Array
			yaml.set(tag.getName() + ".byteArray", ArrayConvert.convert(((NBTTagByteArray) tag).data));
			break;
		case 11: // Int array
			yaml.set(tag.getName() + ".intArray", ArrayConvert.convert(((NBTTagIntArray) tag).data));
			break;
		case 8: // String
			yaml.set(tag.getName(), ((NBTTagString) tag).data);
			break;
		case 9: // List
			NBTTagList listTag = (NBTTagList) tag;
			List list = new ArrayList();
			if (listTag.get(0).getTypeId() == 8)
			{
				for (int i = 0; i < listTag.size(); i++) {
					list.add(((NBTTagString)listTag.get(i)).data);
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
			

			yaml.set(tag.getName(), list.toArray());
			break;
		case 10: // Compound
			ConfigurationSection newSection = yaml.createSection(tag.getName()).createSection("compound");

			Collection<NBTBase> tags = ((NBTTagCompound) tag).c();
			for (NBTBase base : tags) {
				addTag(newSection, base);
			}

		}
	}

	public void addTagWithoutName(ConfigurationSection yaml, NBTBase tag) {
		switch (tag.getTypeId()) {
		case 1: // Byte
			yaml.set("byte", ((NBTTagByte) tag).data);
			break;
		case 2: // Short
			yaml.set("short", ((NBTTagShort) tag).data);
			break;
		case 3: // Integer
			yaml.set("int", ((NBTTagInt) tag).data);
			break;
		case 4: // Long
			yaml.set("long", ((NBTTagLong) tag).data);
			break;
		case 5: // Float
			yaml.set("float", ((NBTTagFloat) tag).data);
			break;
		case 6: // Double
			yaml.set("double", ((NBTTagDouble) tag).data);
			break;
		case 7: // Byte Array
			yaml.set("byteArray",  ArrayConvert.convert(((NBTTagByteArray) tag).data));
			break;
		case 11: // Int array
			yaml.set("intArray", ArrayConvert.convert(((NBTTagIntArray) tag).data));
			break;
		case 10: // Compound
			ConfigurationSection newSection = yaml.createSection("compound");

			Collection<NBTBase> tags = ((NBTTagCompound) tag).c();
			for (NBTBase base : tags) {
				addTag(newSection, base);
			}

		}
	}
}
