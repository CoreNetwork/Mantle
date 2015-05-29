package us.corenetwork.mantle;

import java.util.List;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IInventory;
import net.minecraft.server.v1_8_R3.ItemStack;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Created by Matej on 28.10.2014.
 */
public abstract class InventoryGUI extends CraftInventory
{

    public InventoryGUI()
    {
        super(new GUIVanillaInventory());
        ((GUIVanillaInventory) getInventory()).setParent(this);
    }

    public void clickEvent(InventoryClickEvent event)
    {
        int slot = event.getRawSlot();
        if (slot > 26)
            return;

        onClick(event.getWhoClicked(), event.getClick(), slot);

    }

    public abstract void onClick(HumanEntity player, ClickType clickType, int slot);

    public void onOpen(CraftHumanEntity player)
    {
    }

    public void onClose(CraftHumanEntity player)
    {
    }

    public abstract String getTitle();

    public int getHeight()
    {
        return 3;
    }

    public static class GUIVanillaInventory implements IInventory
    {
        private InventoryGUI parent;
        private ItemStack[] items;

        protected GUIVanillaInventory()
        {

        }

        protected void setParent(InventoryGUI parent)
        {
            this.parent = parent;
            this.items = new ItemStack[9 * parent.getHeight()];
        }

        @Override
        public int getSize()
        {
            return items.length;
        }

        @Override
        public ItemStack getItem(int i)
        {
            return items[i];
        }

        @Override
        public ItemStack splitStack(int i, int i2)
        {
            return null;
        }

        @Override
        public ItemStack splitWithoutUpdate(int i)
        {
            return null;
        }

        @Override
        public void setItem(int i, ItemStack itemStack)
        {
            items[i] = itemStack;
        }


        @Override
        public int getMaxStackSize()
        {
            return 64;
        }

        @Override
        public void update()
        {

        }

        @Override
        public boolean a(EntityHuman entityHuman)
        {
            return true;
        }

        @Override
        public void startOpen(EntityHuman entityHuman)
        {

        }

        @Override
        public void closeContainer(EntityHuman entityHuman)
        {

        }

        @Override
        public boolean b(int i, ItemStack itemStack)
        {
            return true;
        }

        @Override
        public int getProperty(int i)
        {
            return 0;
        }

        @Override
        public void b(int i, int i1)
        {

        }

        @Override
        public int g()
        {
            return 0;
        }

        @Override
        public void l()
        {

        }

        @Override
        public ItemStack[] getContents()
        {
            return new ItemStack[0];
        }

        @Override
        public void onOpen(CraftHumanEntity craftHumanEntity)
        {
            parent.onOpen(craftHumanEntity);
        }

        @Override
        public void onClose(CraftHumanEntity craftHumanEntity)
        {
            parent.onClose(craftHumanEntity);
        }

        public void click(InventoryClickEvent event)
        {
            parent.clickEvent(event);
        }

        @Override
        public List<HumanEntity> getViewers()
        {
            return null;
        }

        @Override
        public InventoryHolder getOwner()
        {
            return null;
        }

        @Override
        public void setMaxStackSize(int i)
        {
        }

        @Override
        public String getName()
        {
            return parent.getTitle();
        }

        @Override
        public boolean hasCustomName()
        {
            return true;
        }

        @Override
        public IChatBaseComponent getScoreboardDisplayName()
        {
            return new ChatComponentText(getName());
        }
    }
}
