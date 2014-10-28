package us.corenetwork.mantle;

import java.util.List;
import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.server.v1_7_R4.IInventory;
import net.minecraft.server.v1_7_R4.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
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
        Bukkit.broadcastMessage("onclose!");
    }

    public abstract String getTitle();

    public static class GUIVanillaInventory implements IInventory
    {
        private InventoryGUI parent;
        private final ItemStack[] items;

        protected GUIVanillaInventory()
        {
            this.items = new ItemStack[InventoryType.CHEST.getDefaultSize()];
        }

        protected void setParent(InventoryGUI parent)
        {
            this.parent = parent;
        }

        @Override
        public int getSize()
        {
            return InventoryType.CHEST.getDefaultSize();
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
        public String getInventoryName()
        {
            return parent.getTitle();
        }

        @Override
        public boolean k_()
        {
            return false;
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
        public void startOpen()
        {

        }

        @Override
        public void closeContainer()
        {

        }

        @Override
        public boolean b(int i, ItemStack itemStack)
        {
            return false;
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
    }
}
