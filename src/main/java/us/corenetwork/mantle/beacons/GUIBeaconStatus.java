package us.corenetwork.mantle.beacons;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.InventoryGUI;
import us.corenetwork.mantle.TimeFormat;
import us.corenetwork.mantle.nanobot.NanobotUtil;

/**
 * Created by Matej on 28.10.2014.
 */
public class GUIBeaconStatus extends InventoryGUI
{
    private static final int ITEM_POSITION_CANCEL_EFFECT = 0;
    private static final int ITEM_POSITION_HELP = 8;
    private static final int ITEM_POSITION_FUEL_LEFT = 4;
    private static final int ITEM_POSITION_CURRENT_EFFECT = 13;
    private static final int ITEM_POSITION_BEACON_STATUS = 22;

    private CustomBeaconTileEntity beacon;

    public GUIBeaconStatus(CustomBeaconTileEntity beacon)
    {
        this.beacon = beacon;

        setItem(ITEM_POSITION_HELP, BeaconsSettings.GUI_ITEM_HELP_SELECTED.itemStack());
        setItem(ITEM_POSITION_CANCEL_EFFECT, BeaconsSettings.GUI_ITEM_CANCEL_EFFECT.itemStack());

        if (beacon.isActive())
            setItem(ITEM_POSITION_BEACON_STATUS, BeaconsSettings.GUI_ITEM_BEACON_ON.itemStack());
        else
            setItem(ITEM_POSITION_BEACON_STATUS, BeaconsSettings.GUI_ITEM_BEACON_OFF.itemStack());

        ItemStack fuelLeftItem = beacon.getActiveEffect().getFuelIcon();
        fuelLeftItem = NanobotUtil.replaceStringInItem(fuelLeftItem, "<TimeLeft>", TimeFormat.formatTimeSeconds(beacon.getFuelLeftTicks() / 20));
        fuelLeftItem = NanobotUtil.replaceStringInItem(fuelLeftItem, "<FuelDuration>", TimeFormat.formatTimeMinutes(beacon.getFuelDuration()));
        setItem(ITEM_POSITION_FUEL_LEFT, fuelLeftItem);

        ItemStack effectIcon = beacon.getActiveEffect().getActiveEffectIcon();
        effectIcon = NanobotUtil.replaceStringInItem(effectIcon, "<Range>", Integer.toString(beacon.getRange()));
        effectIcon = NanobotUtil.replaceStringInItem(effectIcon, "<Name>", beacon.getEffectName());
        setItem(ITEM_POSITION_CURRENT_EFFECT, effectIcon);
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
        return BeaconsSettings.GUI_TITLE_BEACON_STATUS.string();
    }
}
