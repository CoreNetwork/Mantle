package us.corenetwork.mantle.nanobot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;
import net.minecraft.server.v1_8_R3.NBTReadLimiter;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GrassSpecies;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.TexturedMaterial;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.spellbooks.SpellbooksSettings;

public class NanobotUtil {	
	public static ItemStack getMaterialFromItem(String name)
	{
		if (name == null)
			return null;
		else if (Util.isInteger(name))
		{
			return (new ItemStack(Integer.parseInt(name), 1));
		}
		else if (name.contains(":"))
		{
			try
			{
				
				int id = Integer.parseInt(name.substring(0, name.indexOf(":")));
				short data = Short.parseShort(name.substring(name.indexOf(":") + 1));
				
				return new ItemStack(id, 1, data);
			}
			catch (NumberFormatException e)
			{
				return null;
			}
		}
		else
		{
			return getMaterialFromItem(NanobotModule.materials.get(name));
		}		
	}
	
	public static String getItemName(Material material, Byte data)
	{		
		if (material == Material.INK_SACK)
		{
			switch (data)
			{
				case 0:
					return "Ink Sac";
				case 1:
					return "Rose Red Dye";
				case 2:
					return "Cactus Green Dye";
				case 3:
					return "Cocoa Beans";
				case 4:
					return "Lapis Lazuli";
				case 11:
					return "Dandelion Yellow Dye";
				case 15:
					return "Bone Meal";
				default:
					return getMaterialPrefix(material, data) + getMaterialName(material);
					
			}
		}
		else
			return getMaterialPrefix(material, data) + getMaterialName(material);
	}
	
	// Material name snippet by TechGuard
	private static String getMaterialName(Enum<?> material){
        String name = material.toString();
        if (material instanceof Material && material == Material.INK_SACK) name = "DYE";
        name = name.replaceAll("_", " ");
        if(name.contains(" ")){
            String[] split = name.split(" ");
            for(int i=0; i < split.length; i++){
                split[i] = split[i].substring(0, 1).toUpperCase()+split[i].substring(1).toLowerCase();
            }
            name = "";
            for(String s : split){
                name += " "+s;
            }
            name = name.substring(1);
        } else {
            name = name.substring(0, 1).toUpperCase()+name.substring(1).toLowerCase();
        }
        return name;
	}
	
	public static String getMaterialPrefix(Material material, Byte data)
	{
		String prefix = "";
		if (material == Material.WOOL || material == Material.INK_SACK)
			prefix = getMaterialName(DyeColor.getByData(data));
		else if ((material == Material.LOG || material == Material.SAPLING) && data != 0)
			prefix = getMaterialName(TreeSpecies.getByData(data));
		else if (material == Material.LONG_GRASS)
			prefix = getMaterialName(GrassSpecies.getByData(data));
		else if (material.getNewData(data) instanceof TexturedMaterial)
			prefix = getMaterialName(((TexturedMaterial) material.getNewData(data)).getMaterial());
		else if (data == -1)
			prefix = "Popravilo";
		if (!prefix.trim().equals("")) prefix += " ";
		
		return prefix;
	}

	public static String fixFormatting(String source)
	{
		source = source.replaceAll("(?<!&)&([klmnor0-9abcdef])", ChatColor.COLOR_CHAR + "$1");
		source = source.replaceAll("&&([klmnor0-9abcdef])", ChatColor.COLOR_CHAR + "$1");

        if (source.startsWith("ExpireAfterDays: "))
        {
            int days = Integer.parseInt(source.substring(17));

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, days);

            source = fixFormatting(SpellbooksSettings.expireDateStorageFormat.format(calendar.getTime()));
        }

		return source;
	}

	/**
	 * Replace all occurences of the string with another string in item's title and lore.
	 * @param item Item to replace strings in.
	 * @param source String to search for.
	 * @param replacement Replacement for said string.
	 * @return ItemStack with replaced strings.
	 */
    public static ItemStack replaceStringInItem(ItemStack item, String source, String replacement)
    {
        if (!item.hasItemMeta())
            return item;

        item = item.clone();

        ItemMeta meta = item.getItemMeta();

        if (meta.hasDisplayName())
            meta.setDisplayName(meta.getDisplayName().replace(source, replacement));

        if (meta.hasLore())
        {
            List<String> lore = meta.getLore();
            for (int i = 0; i < lore.size(); i++)
            {
                lore.set(i, lore.get(i).replace(source, replacement));
            }

            meta.setLore(lore);
        }

        item.setItemMeta(meta);
        return item;
    }

	/**
	 * Replace all occurences of the string with another string in NBT tag that contains item title and lore.
	 * @param tag NBT tag to replace occurences in.
	 * @param source String to search for.
	 * @param replacement Replacement string.
	 */
	public static void replaceStringInNBT(NBTTagCompound tag, String source, String replacement)
	{
		if (tag == null)
			return;

		NBTTagCompound displayTag = (NBTTagCompound) tag.get("display");
		if (displayTag == null)
			return;


		if (displayTag.hasKey("Name"))
			displayTag.setString("Name", displayTag.getString("Name").replace(source, replacement));

		if (displayTag.hasKey("Lore"))
		{
			NBTTagList lore = (NBTTagList) displayTag.get("Lore");
			NBTTagList newLore = new NBTTagList();
			for (int i = 0; i < lore.size(); i++)
			{
				newLore.add(new NBTTagString(lore.getString(i).replace(source, replacement)));
			}

			displayTag.set("Lore", newLore);
		}
	}


	/*
		Extracts NBT tags from the item as byte array
	 */
	public static byte[] getNBT(net.minecraft.server.v1_8_R3.ItemStack stack)
	{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataOutput = new DataOutputStream(byteStream);
		NBTTagCompound tag = stack.getTag();
		if (tag == null)
			return new byte[0];

		try {
			Method method = NBTTagCompound.class.getDeclaredMethod("write", DataOutput.class);
			method.setAccessible(true);

			method.invoke(tag, dataOutput);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return byteStream.toByteArray();
	}

	public static NBTReadLimiter UNLIMTED_NBT_READER_INSTANCE = new UnlimitedNBTLimiter();
	private static class UnlimitedNBTLimiter extends NBTReadLimiter
	{
		public UnlimitedNBTLimiter()
		{
			super(0);
		}

		@Override
		public void a(long l)
		{
		}
	}

	/*
		Loads NBT tags from byte array and inserts it into provided ItemStack.
	 */
	public static void loadNBT(byte[] nbt, net.minecraft.server.v1_8_R3.ItemStack stack)
	{
		if (nbt == null || nbt.length == 0)
			return;

		NBTTagCompound tag = new NBTTagCompound();

		ByteArrayInputStream stream = new ByteArrayInputStream(nbt);
		DataInputStream dataInput = new DataInputStream(stream);

		try {
			Method method = NBTTagCompound.class.getDeclaredMethod("load", DataInput.class, Integer.TYPE, NBTReadLimiter.class);
			method.setAccessible(true);

			method.invoke(tag, dataInput, 0, UNLIMTED_NBT_READER_INSTANCE);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		stack.setTag(tag);
	}

	/*
		Get NMS item stack from CraftItemStack. This is much faster than CraftItemStack.asNMSCopy() as it just retrieves internal variable rather than rebuilding whole NMS stack from scratch.
	 */
	public static net.minecraft.server.v1_8_R3.ItemStack getInternalNMSStack(ItemStack bukkitStack)
	{
		if (!(bukkitStack instanceof CraftItemStack))
			return null;

		try
		{
			Field handleField = CraftItemStack.class.getDeclaredField("handle");
			handleField.setAccessible(true);

			return (net.minecraft.server.v1_8_R3.ItemStack) handleField.get(bukkitStack);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/*
		@return true if item contains specific tag at NBT root
	 */
	public static boolean hasTag(ItemStack bukkitStack, String tag)
	{
		if (!(bukkitStack instanceof CraftItemStack))
			return false;

		net.minecraft.server.v1_8_R3.ItemStack nmsStack = getInternalNMSStack((CraftItemStack) bukkitStack);
		if (!nmsStack.hasTag())
			return false;

		return nmsStack.getTag().hasKey(tag);
	}

	/**
	 * Gets custom name from the NMS itemstack
	 * @param nmsStack Stack to get name from.
	 * @return Custom name or <b>null</b> if not set.
	 */
	public static String getStackName(net.minecraft.server.v1_8_R3.ItemStack nmsStack)
	{
		if (nmsStack == null)
			return null;

		if (!nmsStack.hasTag())
			return null;

		NBTTagCompound displayTag = (NBTTagCompound) nmsStack.getTag().get("display");
		if (displayTag == null)
			return null;

		if (!displayTag.hasKey("Name"))
			return null;

		return displayTag.getString("Name");
	}

}
