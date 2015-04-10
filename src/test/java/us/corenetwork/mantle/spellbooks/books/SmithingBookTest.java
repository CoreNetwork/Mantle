package us.corenetwork.mantle.spellbooks.books;

import net.minecraft.server.v1_8_R2.DispenserRegistry;
import org.bukkit.CoalType;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftInventoryPlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Coal;
import org.junit.Before;
import org.junit.Test;
import us.corenetwork.mantle.test.BukkitDummy;
import us.corenetwork.mantle.util.InventoryUtil;

import static org.junit.Assert.*;

public class SmithingBookTest
{
    @Before
    public void initMinecraftBlocks()
    {
        DispenserRegistry.c(); //Initialize dispenser registry before accessing Blocks class (Minecraft code complains otherwise)
        BukkitDummy.injectNewServer();
    }

    @Test
    public void testSmithingBook() throws Exception
    {
        int INVENTORY_SIZE = 3 * 9;

        Inventory testInventory = new CraftInventoryCustom(null, INVENTORY_SIZE);

        //Try smithing iron chestplate without any fuel
        testInventory.addItem(new ItemStack(Material.IRON_CHESTPLATE, 1));
        boolean anythingSmitted = SmithingBook.smith(testInventory, testInventory);
        assertFalse(anythingSmitted);
        assertEquals(INVENTORY_SIZE - 1, InventoryUtil.getFreeInventorySlots(testInventory));
        assertEquals(Material.IRON_CHESTPLATE, testInventory.getItem(0).getType());

        //Try smithing iron chestplate with 1 Coal
        testInventory.addItem(new ItemStack(Material.COAL, 1));
        anythingSmitted = SmithingBook.smith(testInventory, testInventory);
        assertTrue(anythingSmitted);
        assertEquals(INVENTORY_SIZE - 1, InventoryUtil.getFreeInventorySlots(testInventory));
        assertEquals(Material.IRON_INGOT, testInventory.getItem(0).getType());
        assertEquals(5, testInventory.getItem(0).getAmount());

        testInventory.clear();

        //Try smithing iron chestplate with 1/2 durability
        testInventory.addItem(new ItemStack(Material.IRON_CHESTPLATE, 1, (short) (Material.IRON_CHESTPLATE.getMaxDurability() / 2)));
        testInventory.addItem(new ItemStack(Material.COAL, 1));
        anythingSmitted = SmithingBook.smith(testInventory, testInventory);
        assertTrue(anythingSmitted);
        assertEquals(INVENTORY_SIZE - 1, InventoryUtil.getFreeInventorySlots(testInventory));
        assertEquals(Material.IRON_INGOT, testInventory.getItem(0).getType());
        assertEquals(2, testInventory.getItem(0).getAmount());

        testInventory.clear();

        //Try smithing iron chestplate with 99% durability used up (generates piece of charcoal)
        testInventory.addItem(new ItemStack(Material.IRON_CHESTPLATE, 1, (short) (Material.IRON_CHESTPLATE.getMaxDurability() * 0.99)));
        testInventory.addItem(new ItemStack(Material.COAL, 1));
        anythingSmitted = SmithingBook.smith(testInventory, testInventory);
        assertTrue(anythingSmitted);
        assertEquals(INVENTORY_SIZE - 1, InventoryUtil.getFreeInventorySlots(testInventory));
        assertEquals(Material.COAL, testInventory.getItem(0).getType());
        assertEquals(CoalType.CHARCOAL.getData(), testInventory.getItem(0).getDurability());
        assertEquals(1, testInventory.getItem(0).getAmount());

        testInventory.clear();

        //Try smithing iron chestplate with 1% durability used up
        testInventory.addItem(new ItemStack(Material.IRON_CHESTPLATE, 1, (short) (Material.IRON_CHESTPLATE.getMaxDurability() * 0.01)));
        testInventory.addItem(new ItemStack(Material.COAL, 1));
        anythingSmitted = SmithingBook.smith(testInventory, testInventory);
        assertTrue(anythingSmitted);
        assertEquals(INVENTORY_SIZE - 1, InventoryUtil.getFreeInventorySlots(testInventory));
        assertEquals(Material.IRON_INGOT, testInventory.getItem(0).getType());
        assertEquals(4, testInventory.getItem(0).getAmount());

        testInventory.clear();

        //Try smithing full chest of gold swords (except for last slot which is filled with Coal)
        for (int i = 0; i < INVENTORY_SIZE - 1; i++)
            testInventory.addItem(new ItemStack(Material.GOLD_SWORD, 1));
        testInventory.addItem(new ItemStack(Material.COAL, INVENTORY_SIZE - 1));
        anythingSmitted = SmithingBook.smith(testInventory, testInventory);
        assertTrue(anythingSmitted);
        assertEquals(INVENTORY_SIZE - 5, InventoryUtil.getFreeInventorySlots(testInventory));
        for (int i = 0; i < 5; i++)
            assertEquals(Material.GOLD_NUGGET, testInventory.getItem(i).getType());
        assertEquals(312, InventoryUtil.getAmountOfItems(testInventory, Material.GOLD_NUGGET, Short.MAX_VALUE));

        testInventory.clear();

        //Try smithing full chest of gold swords with coal in separate inventory
        Inventory coalInventory = new CraftInventoryCustom(null, INVENTORY_SIZE);
        for (int i = 0; i < INVENTORY_SIZE; i++)
            testInventory.addItem(new ItemStack(Material.GOLD_SWORD, 1));
        coalInventory.addItem(new ItemStack(Material.COAL, INVENTORY_SIZE));
        anythingSmitted = SmithingBook.smith(testInventory, coalInventory);
        assertTrue(anythingSmitted);
        assertEquals(INVENTORY_SIZE - 6, InventoryUtil.getFreeInventorySlots(testInventory));
        for (int i = 0; i < 6; i++)
            assertEquals(Material.GOLD_NUGGET, testInventory.getItem(i).getType());
        assertEquals(324, InventoryUtil.getAmountOfItems(testInventory, Material.GOLD_NUGGET, Short.MAX_VALUE));
        assertEquals(INVENTORY_SIZE, InventoryUtil.getFreeInventorySlots(coalInventory));

        testInventory.clear();


        //Try smithing with half stack of ingots already in
        testInventory.addItem(new ItemStack(Material.GOLD_INGOT, 32));
        testInventory.addItem(new ItemStack(Material.GOLD_BOOTS, 1));
        testInventory.addItem(new ItemStack(Material.COAL, 1));
        anythingSmitted = SmithingBook.smith(testInventory, testInventory);
        assertTrue(anythingSmitted);
        assertEquals(INVENTORY_SIZE - 1, InventoryUtil.getFreeInventorySlots(testInventory));
        assertEquals(Material.GOLD_INGOT, testInventory.getItem(0).getType());
        assertEquals(32 + 2, testInventory.getItem(0).getAmount());

        testInventory.clear();

        //Try smithing with too much coal  - leftover must stay
        testInventory.addItem(new ItemStack(Material.GOLD_CHESTPLATE, 1));
        testInventory.addItem(new ItemStack(Material.GOLD_HELMET, 1));
        testInventory.addItem(new ItemStack(Material.GOLD_SWORD, 1));
        testInventory.addItem(new ItemStack(Material.COAL, 64));
        anythingSmitted = SmithingBook.smith(testInventory, testInventory);
        assertTrue(anythingSmitted);
        assertEquals(INVENTORY_SIZE - 3, InventoryUtil.getFreeInventorySlots(testInventory));
        assertEquals(Material.GOLD_INGOT, testInventory.getItem(0).getType());
        assertEquals(5 + 3, testInventory.getItem(0).getAmount());
        assertEquals(Material.GOLD_NUGGET, testInventory.getItem(1).getType());
        assertEquals(12, testInventory.getItem(1).getAmount());
        assertEquals(Material.COAL, testInventory.getItem(3).getType());
        assertEquals(64 - 3, testInventory.getItem(3).getAmount());

        testInventory.clear();

        //Try smithing with both Coal and Charcoal
        testInventory.addItem(new ItemStack(Material.COAL, 1));
        testInventory.addItem(new ItemStack(Material.COAL, 1, CoalType.CHARCOAL.getData()));
        testInventory.addItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1));
        testInventory.addItem(new ItemStack(Material.CHAINMAIL_LEGGINGS, 1));
        anythingSmitted = SmithingBook.smith(testInventory, testInventory);
        assertTrue(anythingSmitted);
        assertEquals(INVENTORY_SIZE - 1, InventoryUtil.getFreeInventorySlots(testInventory));
        assertEquals(Material.IRON_INGOT, testInventory.getItem(2).getType());
        assertEquals(6 + 5, testInventory.getItem(2).getAmount());

        //Try smithing player inventory with armor equipped (it should not be smitted)
        PlayerInventory testPlayerInventory = new CraftInventoryPlayer(new net.minecraft.server.v1_8_R2.PlayerInventory(null));
        testPlayerInventory.setHelmet(new ItemStack(Material.GOLD_HELMET, 1));
        testPlayerInventory.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1));
        testPlayerInventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS, 1));
        testPlayerInventory.setBoots(new ItemStack(Material.GOLD_BOOTS, 1));
        anythingSmitted = SmithingBook.smith(testPlayerInventory, testPlayerInventory);
        assertFalse(anythingSmitted);
        assertEquals(testPlayerInventory.getSize(), InventoryUtil.getFreeInventorySlots(testPlayerInventory));
        assertEquals(Material.GOLD_HELMET, testPlayerInventory.getHelmet().getType());
        assertEquals(Material.CHAINMAIL_CHESTPLATE, testPlayerInventory.getChestplate().getType());
        assertEquals(Material.IRON_LEGGINGS, testPlayerInventory.getLeggings().getType());
        assertEquals(Material.GOLD_BOOTS, testPlayerInventory.getBoots().getType());
    }
}