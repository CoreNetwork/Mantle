package us.corenetwork.mantle.slimeballs;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.corenetwork.mantle.YamlUtils;

/**
 * Created by Matej on 20.11.2014.
 */
public class SlimeballItem
{
    private static ItemStack template;

    public static boolean init()
    {
        ConfigurationSection section = SlimeballsModule.instance.config.getConfigurationSection("Item");
        if (section == null)
        {
            System.out.println("Slimeball item not configured! Disabling module...");
            return false;
        }

        template = YamlUtils.readItemStack(section.getValues(false));
        if (template == null)
        {
            System.out.println("Slimeball wrongly configured! Disabling module...");
            return false;
        }

        return true;
    }

    public static ItemStack create(int amount)
    {
        ItemStack clone = template.clone();
        clone.setAmount(amount);

        return clone;
    }

    public static boolean isSlimeball(ItemStack itemstack)
    {
        if (itemstack.getTypeId() != template.getTypeId())
            return false;

        ItemMeta comparingMeta = itemstack.getItemMeta();
        if (!comparingMeta.hasDisplayName())
            return false;

        ItemMeta templateMeta = template.getItemMeta();
        return comparingMeta.getDisplayName().equals(templateMeta.getDisplayName());
    }
}
