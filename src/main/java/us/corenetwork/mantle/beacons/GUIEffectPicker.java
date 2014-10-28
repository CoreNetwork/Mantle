package us.corenetwork.mantle.beacons;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import us.corenetwork.mantle.InventoryGUI;

/**
 * Created by Matej on 28.10.2014.
 */
public class GUIEffectPicker extends InventoryGUI
{
    private static final int ITEM_POSITION_HELP = 0;
    private static final int ITEM_POSITION_EFFECT_HASTE = 10;
    private static final int ITEM_POSITION_EFFECT_REGENERATION = 11;
    private static final int ITEM_POSITION_EFFECT_STRENGTH = 12;
    private static final int ITEM_POSITION_EFFECT_JUMP_BOOST = 13;
    private static final int ITEM_POSITION_EFFECT_WATER_BREATHING = 14;
    private static final int ITEM_POSITION_EFFECT_OVERCLOCK = 15;
    private static final int ITEM_POSITION_EFFECT_ANIMAL_GROWTH = 16;

    private CustomBeaconTileEntity beacon;

    public GUIEffectPicker(CustomBeaconTileEntity beacon)
    {
        this.beacon = beacon;

        setItem(ITEM_POSITION_HELP, BeaconsSettings.GUI_ITEM_HELP.itemStack());
        setItem(ITEM_POSITION_EFFECT_HASTE, BeaconsSettings.GUI_ITEM_EFFECT_HASTE.itemStack());
        setItem(ITEM_POSITION_EFFECT_REGENERATION, BeaconsSettings.GUI_ITEM_EFFECT_REGENERATION.itemStack());
        setItem(ITEM_POSITION_EFFECT_STRENGTH, BeaconsSettings.GUI_ITEM_EFFECT_STRENGTH.itemStack());
        setItem(ITEM_POSITION_EFFECT_JUMP_BOOST, BeaconsSettings.GUI_ITEM_EFFECT_JUMP_BOOST.itemStack());
        setItem(ITEM_POSITION_EFFECT_WATER_BREATHING, BeaconsSettings.GUI_ITEM_EFFECT_WATER_BREATHING.itemStack());
        setItem(ITEM_POSITION_EFFECT_OVERCLOCK, BeaconsSettings.GUI_ITEM_EFFECT_OVERCLOCK.itemStack());
        setItem(ITEM_POSITION_EFFECT_ANIMAL_GROWTH, BeaconsSettings.GUI_ITEM_EFFECT_ANIMAL_GROWTH.itemStack());
    }



    @Override
    public void onClick(HumanEntity player, ClickType clickType, int slot)
    {
        BeaconEffect selection;

        switch (slot)
        {
            case ITEM_POSITION_EFFECT_HASTE:
                selection = BeaconEffect.HASTE;
                break;
            case ITEM_POSITION_EFFECT_REGENERATION:
                selection = BeaconEffect.REGENERATION;
                break;
            case ITEM_POSITION_EFFECT_STRENGTH:
                selection = BeaconEffect.STRENGTH;
                break;
            case ITEM_POSITION_EFFECT_JUMP_BOOST:
                selection = BeaconEffect.JUMP_BOOST;
                break;
            case ITEM_POSITION_EFFECT_WATER_BREATHING:
                selection = BeaconEffect.WATER_BREATHING;
                break;
            case ITEM_POSITION_EFFECT_OVERCLOCK:
                selection = BeaconEffect.OVERCLOCK;
                break;
            case ITEM_POSITION_EFFECT_ANIMAL_GROWTH:
                selection = BeaconEffect.ANIMAL_GROWTH;
                break;
            default:
                return;
        }

        beacon.setActiveEffect(selection);
        player.openInventory(new GUIBeaconStatus(beacon));
    }

    @Override
    public String getTitle()
    {
        return BeaconsSettings.GUI_TITLE_PICK_EFFECT.string();
    }
}
