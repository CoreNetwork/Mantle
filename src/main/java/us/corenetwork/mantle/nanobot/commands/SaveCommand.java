package us.corenetwork.mantle.nanobot.commands;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagByte;
import net.minecraft.server.v1_8_R3.NBTTagByteArray;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagDouble;
import net.minecraft.server.v1_8_R3.NBTTagFloat;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagIntArray;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagLong;
import net.minecraft.server.v1_8_R3.NBTTagShort;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import us.core_network.cornel.items.NbtYaml;
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

		if (!stack.hasTag())
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

		try
		{
			NbtYaml.saveToFile(args[0], stack.getTag());
		}
		catch (Exception e)
		{
			sender.sendMessage("Error while saving tag!");
			e.printStackTrace();
		}

		sender.sendMessage("Tag was saved sucessfully!");
	}

}
