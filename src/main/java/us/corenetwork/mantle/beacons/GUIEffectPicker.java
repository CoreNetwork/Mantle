package us.corenetwork.mantle.beacons;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import us.core_network.cornel.custom.inventorygui.InventoryGUIGroup;

/**
 * Created by Matej on 28.10.2014.
 */

public class GUIEffectPicker extends InventoryGUIGroup<GUIEffectPicker.GUIEffectPickerWindow>
{
    private CustomBeaconTileEntity beacon;

    public GUIEffectPicker(CustomBeaconTileEntity beacon)
    {
        this.beacon = beacon;
    }

    public void openNewWindow(HumanEntity player)
    {
        player.openInventory(new GUIEffectPickerWindow(this, beacon));
    }

    public void switchToEffectStatus()
    {
        for (GUIEffectPickerWindow window : openedWindows.toArray(new GUIEffectPickerWindow[0]))
            window.switchToEffectStatus();
    }

    public static class GUIEffectPickerWindow extends InventoryGUIGroup.InventoryGUIGroupWindow
    {
        private static final int ITEM_POSITION_HELP = 8;

        private CustomBeaconTileEntity beacon;

        private Map<Integer, BeaconEffect> effectIconsPositions = new HashMap<Integer, BeaconEffect>();

        public GUIEffectPickerWindow(InventoryGUIGroup group, CustomBeaconTileEntity beacon)
        {
            super(group);

            this.beacon = beacon;

            int amountOfEffects = BeaconEffect.STORAGE.effects.size();
            int start = (int) Math.ceil((9 - amountOfEffects) / 2.0) + 18;

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
        }

        public void switchToEffectStatus()
        {
            beacon.beaconStatusGUI.openNewWindow(player);
        }

        @Override
        public String getTitle()
        {
            return BeaconsSettings.GUI_TITLE_PICK_EFFECT.string();
        }

        @Override
        public int getHeight()
        {
            return 4;
        }
    }

}
