package us.corenetwork.mantle.beacons;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import us.corenetwork.mantle.InventoryGUI;

/**
 * Created by Matej on 28.10.2014.
 */
public class GUIEffectPicker extends InventoryGUI
{
    private static final int ITEM_POSITION_HELP = 8;

    private CustomBeaconTileEntity beacon;

    private Map<Integer, BeaconEffect> effectIconsPositions = new HashMap<Integer, BeaconEffect>();

    public GUIEffectPicker(CustomBeaconTileEntity beacon)
    {
        this.beacon = beacon;

        int amountOfEffects = BeaconEffect.STORAGE.effects.size();
        int start = (int) Math.ceil((9 - amountOfEffects) / 2.0) + 9;

        setItem(ITEM_POSITION_HELP, BeaconsSettings.GUI_ITEM_HELP.itemStack());

        for (int i = 0; i < amountOfEffects; i++)
        {
            int pos = start + i;
            BeaconEffect effect = BeaconEffect.STORAGE.effects.get(i);
            setItem(pos, effect.getEffectIcon());

            effectIconsPositions.put(pos, effect);
        }
    }



    @Override
    public void onClick(HumanEntity player, ClickType clickType, int slot)
    {
        BeaconEffect selection = effectIconsPositions.get(slot);
        if (selection == null)
            return;

        beacon.setActiveEffect(selection);
        beacon.updateFuel();
        player.openInventory(new GUIBeaconStatus(beacon));
    }

    @Override
    public String getTitle()
    {
        return BeaconsSettings.GUI_TITLE_PICK_EFFECT.string();
    }
}
