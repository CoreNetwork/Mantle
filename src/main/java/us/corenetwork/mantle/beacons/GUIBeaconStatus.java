package us.corenetwork.mantle.beacons;

import org.bukkit.craftbukkit.v1_8_R1.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import us.core_network.cornel.custom.inventorygui.InventoryGUIGroup;
import us.core_network.cornel.custom.inventorygui.SharedInventoryGUI;
import us.core_network.cornel.items.ItemStackUtils;
import us.corenetwork.mantle.TimeFormat;

/**
 * Created by Matej on 28.10.2014.
 */

public class GUIBeaconStatus extends SharedInventoryGUI
{
    private static final int ITEM_POSITION_CANCEL_EFFECT = 0;
    private static final int ITEM_POSITION_HELP = 8;
    private static final int ITEM_POSITION_FUEL_LEFT = 4;
    private static final int ITEM_POSITION_CURRENT_EFFECT = 13;
    private static final int ITEM_POSITION_BEACON_STATUS = 31;

    private CustomBeaconTileEntity beacon;

    public GUIBeaconStatus(CustomBeaconTileEntity beacon)
    {
        this.beacon = beacon;

        setItem(ITEM_POSITION_HELP, BeaconsSettings.GUI_ITEM_HELP_SELECTED.itemStack());
        setItem(ITEM_POSITION_CANCEL_EFFECT, BeaconsSettings.GUI_ITEM_CANCEL_EFFECT.itemStack());
    }

    @Override
    public void onOpen(CraftHumanEntity player)
    {
        super.onOpen(player);

        updateBeaconStatus();
        updateFuelStatus();
        updateRange();
    }

    @Override
    public void onClick(HumanEntity player, ClickType clickType, int slot)
    {
        switch (slot)
        {
            case ITEM_POSITION_CANCEL_EFFECT:
                beacon.setActiveEffect(null);
                break;
        }

    }

    public void updateBeaconStatus()
    {
        if (beacon.isActive())
            setItem(ITEM_POSITION_BEACON_STATUS, BeaconsSettings.GUI_ITEM_BEACON_ON.itemStack());
        else
            setItem(ITEM_POSITION_BEACON_STATUS, BeaconsSettings.GUI_ITEM_BEACON_OFF.itemStack());
    }

    public void switchToEffectPicker()
    {
        for (HumanEntity player : getViewers())
            player.openInventory(beacon.effectPickerGUI);
    }

    public void updateFuelStatus()
    {
        ItemStack fuelLeftItem = beacon.getActiveEffect().getFuelIcon();
        fuelLeftItem = ItemStackUtils.replaceStringInItem(fuelLeftItem, "<TimeLeft>", TimeFormat.formatTimeSeconds(beacon.getFuelLeftTicks() / 20));
        fuelLeftItem = ItemStackUtils.replaceStringInItem(fuelLeftItem, "<FuelDuration>", TimeFormat.formatTimeMinutes(beacon.getFuelDurationMinutes()));
        fuelLeftItem = ItemStackUtils.replaceStringInItem(fuelLeftItem, "<ItemsConsumed>", Integer.toString(beacon.getAmountFuelItemsConsumed()));
        setItem(ITEM_POSITION_FUEL_LEFT, fuelLeftItem);
    }

    public void updateRange()
    {
        ItemStack effectIcon = beacon.getActiveEffect().getActiveEffectIcon();
        effectIcon = ItemStackUtils.replaceStringInItem(effectIcon, "<Range>", Integer.toString(beacon.getRange()));
        effectIcon = ItemStackUtils.replaceStringInItem(effectIcon, "<Name>", beacon.getEffectName());
        setItem(ITEM_POSITION_CURRENT_EFFECT, effectIcon);

    }

    @Override
    public String getTitle()
    {
        return BeaconsSettings.GUI_TITLE_BEACON_STATUS.string();
    }

    @Override
    public int getHeight()
    {
        return 4;
    }
}
