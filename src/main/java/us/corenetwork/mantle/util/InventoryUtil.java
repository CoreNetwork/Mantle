package us.corenetwork.mantle.util;

import net.minecraft.server.v1_8_R1.IInventory;
import net.minecraft.server.v1_8_R1.Item;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil
{
    /**
     * Get amount of empty slots in inventory
     */
    public static int getFreeInventorySlots(Inventory inventory)
    {
        int slots = 0;

        for (int i = 0; i < inventory.getSize(); i++)
        {
            ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.getType() == Material.AIR)
                slots++;
        }

        return slots;
    }

    /**
     * Get amount of specific items in specified inventory
     * @param inventory Inventory to search items in
     * @param material Material of the item to search.
     * @param durability durability/data of the item to search. Use {@link Short#MAX_VALUE} to search for all items with specified material type regardless of durability
     * @return
     */
    public static int getAmountOfItems(Inventory inventory, Material material, short durability)
    {
        int amount = 0;

        for (int i = 0; i < inventory.getSize(); i++)
        {
            ItemStack stack = inventory.getItem(i);
            if (stack != null && material == stack.getType() && (durability == stack.getDurability() || durability == Short.MAX_VALUE))
            {
                amount += stack.getAmount();
            }
        }

        return amount;
    }

    /**
     * Remove specific number of specific items from the inventory
     * @param inventory Inventory to remove items from.
     * @param material Material to remove
     * @param durability durability/data of the item to remove. Use {@link Short#MAX_VALUE} to remove all items with specified material type regardless of durability
     * @param amount amount of these items to remove
     */
    public static void removeItems(Inventory inventory, Material material, short durability, int amount)
    {
        for (int i = 0; i < inventory.getSize(); i++)
        {
            ItemStack stack = inventory.getItem(i);
            if (stack != null && material == stack.getType() && (durability == stack.getDurability() || durability == Short.MAX_VALUE))
            {
                int stackAmount = stack.getAmount();
                if (amount >= stackAmount)
                {
                    inventory.setItem(i, null);
                    amount -= stackAmount;
                    if (amount == 0)
                        break;
                }
                else
                {
                    stack.setAmount(stackAmount - amount);
                    break;
                }
            }
        }
    }

    /**
     * Convenience method to check if specific slot in NMS inventory contains wanted item type. It includes null checking.
     * @param inventory NMS inventory to check in
     * @param slot Item slot
     * @param item Item type
     * @return true if specified slot contains said item
     */
    public static boolean isItemTypeOnSlot(IInventory inventory, int slot, Item item)
    {
        net.minecraft.server.v1_8_R1.ItemStack stack = inventory.getItem(slot);
        if (stack == null)
            return false;

        return stack.getItem().equals(item);
    }
}
