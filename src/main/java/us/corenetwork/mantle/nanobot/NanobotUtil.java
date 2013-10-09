package us.corenetwork.mantle.nanobot;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GrassSpecies;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.TexturedMaterial;

import us.corenetwork.mantle.Util;

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
	
	public static boolean hasPermission(CommandSender player, String permission)
    {
    	while (true)
    	{
    		if (player.hasPermission(permission))
    			return true;
    		
    		if (permission.length() < 2)
    			return false;
    		
    		if (permission.endsWith("*"))
    			permission = permission.substring(0, permission.length() - 2);
    		
    		int lastIndex = permission.lastIndexOf(".");
    		if (lastIndex < 0)
    			return false;
    		
    		permission = permission.substring(0, lastIndex).concat(".*");  
    	}
    }

	public static String fixFormatting(String source)
	{
		source = source.replaceAll("(?<!&)&([klmnor0-9abcdef])", ChatColor.COLOR_CHAR + "$1");
		source = source.replaceAll("&&([klmnor0-9abcdef])", ChatColor.COLOR_CHAR + "$1");
	
		return source;
	}
}
