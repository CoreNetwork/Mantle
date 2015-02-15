package us.corenetwork.mantle;

import java.io.IOException;
import java.util.Map;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.core_network.cornel.common.MinecraftNames;
import us.core_network.cornel.items.NbtYaml;
import us.corenetwork.mantle.nanobot.commands.LoadCommand;

/**
 * Created by Matej on 28.10.2014.
 */
public class YamlUtils
{
    public static void writeItemStack(Configuration config, String prefix, ItemStack itemStack)
    {
        config.set(prefix.concat(".ID"), itemStack.getTypeId());
        config.set(prefix.concat(".Amount"), itemStack.getAmount());
        config.set(prefix.concat(".Damage"), itemStack.getDurability());
    }

    public static ItemStack readItemStack(Map<String, Object> node)
    {
        if (node == null)
            return null;

        Integer id = (Integer) node.get("ID");
        if (id == null) {
            if (node.containsKey("Name")) {
                String name = (String) node.get("Name");
                Integer material = MinecraftNames.getMaterialId(name);
                if (material != null) {
                    id = material;
                } else {
                    MLog.severe("Can't find material for name " + name);
                }
            }
        }

        if (id == null)
        {
            MLog.severe("Invalid config! Item ID is missing!");
            return null;
        }

        Integer amount = (Integer) node.get("Amount");
        if (amount == null) amount = 1;

        Number damage = (Number) node.get("Damage");
        if (damage == null) damage = 0;

        ItemStack stack = new ItemStack(id, amount, damage.shortValue());

        Object yamlNbtTag = node.get("NBT");
        if (yamlNbtTag != null)
        {
            NBTTagCompound newTag = null;
            if (yamlNbtTag instanceof String)
            {
                try
                {
                    newTag = NbtYaml.loadFromFile((String) yamlNbtTag);
                }
                catch (Exception e)
                {
                    MLog.warning("Invalid config! Nanobot file " + ((String) yamlNbtTag) + ".yml could not be loaded!");
                    e.printStackTrace();
                }
            }
            else
            {
                newTag = NbtYaml.loadFromNodes((Map<?, ?>) yamlNbtTag);
            }

            if (newTag != null)
            {
                net.minecraft.server.v1_8_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
                nmsStack.setTag(newTag);
                stack = CraftItemStack.asCraftMirror(nmsStack);
            }

        }

        return stack;
    }

    public static PotionEffect readPotionEffect(Map<String, Object> node)
    {
        if (node == null)
            return null;

        final Integer id = (Integer) node.get("ID");
        if (id == null)
        {
            MLog.warning("Invalid config! Potion effect id is missing!");
            return null;
        }

        Object durationNode = node.get("Duration");
        int duration = 0;
        if (durationNode == null)
        {
            MLog.warning("Invalid config! Potion effect duration is missing!");
            return null;
        }
        else if (durationNode instanceof Number)
        {
            duration = ((Number) durationNode).intValue();
        }
        else
        {
            MLog.warning("Invalid config! Potion effect duration is invalid!");
            return null;
        }


        Object amplifierNode = node.get("Amplifier");
        int amplifier = 0;
        if (amplifierNode == null)
        {
            MLog.warning("Invalid config! Potion effect amplifier is missing!");
            return null;
        }
        else if (amplifierNode instanceof Number)
        {
            amplifier = ((Number) amplifierNode).intValue();
        }
        else
        {
            MLog.warning("Invalid config! Potion effect amplifier is invalid!");
            return null;
        }

        Boolean ambient = (Boolean) node.get("Ambient");
        if (ambient == null)
            ambient = false;

        return new PotionEffect(PotionEffectType.getById(id), duration, amplifier, ambient);
    }
}
