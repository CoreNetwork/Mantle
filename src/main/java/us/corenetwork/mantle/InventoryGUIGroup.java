package us.corenetwork.mantle;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftHumanEntity;

/**
 * Created by Matej on 15.12.2014.
 */
public abstract class InventoryGUIGroup<T extends InventoryGUIGroup.InventoryGUIGroupWindow>
{
    protected List<T> openedWindows;

    public InventoryGUIGroup()
    {
        openedWindows = new ArrayList<T>();
    }

    public boolean isAnyWindowOpened()
    {
        return !openedWindows.isEmpty();
    }

    public static abstract class InventoryGUIGroupWindow extends InventoryGUI
    {
        private InventoryGUIGroup parent;
        protected CraftHumanEntity player;

        public InventoryGUIGroupWindow(InventoryGUIGroup parent)
        {
            this.parent = parent;
        }

        @Override
        public void onClose(CraftHumanEntity player)
        {
            parent.openedWindows.remove(this);
        }

        @Override
        public void onOpen(CraftHumanEntity player)
        {
            this.player = player;
            parent.openedWindows.add(this);
        }
    }
}