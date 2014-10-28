package us.corenetwork.mantle.beacons;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.InventoryGUI;

/**
 * Created by Matej on 28.10.2014.
 */
public class GUIBeaconStatus extends InventoryGUI
{
    private static final int ITEM_POSITION_HELP = 0;
    private static final int ITEM_POSITION_CANCEL_EFFECT = 18;
    private static final int ITEM_POSITION_FUEL_LEFT = 8;
    private static final int ITEM_POSITION_CURRENT_EFFECT = 17;
    private static final int ITEM_POSITION_PYRAMID_LEVEL = 26;

    private CustomBeaconTileEntity beacon;

    public GUIBeaconStatus(CustomBeaconTileEntity beacon)
    {
        this.beacon = beacon;

        setItem(ITEM_POSITION_HELP, BeaconsSettings.GUI_ITEM_HELP.itemStack());
        setItem(ITEM_POSITION_CANCEL_EFFECT, BeaconsSettings.GUI_ITEM_CANCEL_EFFECT.itemStack());
        setItem(ITEM_POSITION_FUEL_LEFT, BeaconsSettings.GUI_ITEM_FUEL_LEFT.itemStack());
        setItem(ITEM_POSITION_CURRENT_EFFECT, beacon.getActiveEffect().icon.itemStack());

        ItemStack pyramidLevelItem = BeaconsSettings.GUI_ITEM_PYRAMID_LEVEL.itemStack();
        pyramidLevelItem.setAmount(beacon.getPyramidSize());
        setItem(ITEM_POSITION_PYRAMID_LEVEL, pyramidLevelItem);
    }



    @Override
    public void onClick(HumanEntity player, ClickType clickType, int slot)
    {
        switch (slot)
        {
            case ITEM_POSITION_CANCEL_EFFECT:
                beacon.setActiveEffect(null);
                player.openInventory(new GUIEffectPicker(beacon));
                break;
        }

    }

    @Override
    public String getTitle()
    {
        return BeaconsSettings.GUI_TITLE_PICK_EFFECT.string();
    }
}
