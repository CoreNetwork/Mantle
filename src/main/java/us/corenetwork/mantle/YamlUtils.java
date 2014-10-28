package us.corenetwork.mantle;

import java.util.Map;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.nanobot.commands.LoadCommand;

/**
 * Created by Matej on 28.10.2014.
 */
public class YamlUtils
{
    public static void writeItemStack(Configuration config, String prefix, ItemStack itemStack)
    {
        config.set(prefix.concat(".id"), itemStack.getTypeId());
        config.set(prefix.concat(".amount"), itemStack.getAmount());
        config.set(prefix.concat(".damage"), itemStack.getDurability());
    }

    public static ItemStack readItemStack(Map<String, Object> node)
    {
        Integer id = (Integer) node.get("id");
        if (id == null)
        {
            MLog.severe("Invalid config! Item ID is missing!");
            return null;
        }

        Integer amount = (Integer) node.get("amount");
        if (amount == null) amount = 1;

        Number damage = (Number) node.get("damage");
        if (damage == null) damage = 0;

        ItemStack stack = new ItemStack(id, amount, damage.shortValue());

        Object yamlNbtTag = node.get("nbt");
        if (yamlNbtTag != null)
        {
            NBTTagCompound newTag;
            if (yamlNbtTag instanceof String)
            {
                newTag = LoadCommand.load((String) yamlNbtTag);
                if (newTag == null)
                {
                    MLog.warning("Invalid config! Nanobot file " + ((String) yamlNbtTag) + ".yml is missing!");
                }
            }
            else
            {
                newTag = LoadCommand.load((Map<?,?>) yamlNbtTag);
            }

            if (newTag != null)
            {
                net.minecraft.server.v1_7_R4.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
                nmsStack.tag = newTag;
                stack = CraftItemStack.asCraftMirror(nmsStack);
            }

        }

        return stack;
    }
}
