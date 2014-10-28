package us.corenetwork.mantle.beacons;

/**
 * Created by Matej on 28.10.2014.
 */
public enum BeaconEffect
{
    HASTE(BeaconsSettings.GUI_ITEM_EFFECT_HASTE),
    REGENERATION(BeaconsSettings.GUI_ITEM_EFFECT_REGENERATION),
    STRENGTH(BeaconsSettings.GUI_ITEM_EFFECT_STRENGTH),
    JUMP_BOOST(BeaconsSettings.GUI_ITEM_EFFECT_JUMP_BOOST),
    WATER_BREATHING(BeaconsSettings.GUI_ITEM_EFFECT_WATER_BREATHING),
    OVERCLOCK(BeaconsSettings.GUI_ITEM_EFFECT_OVERCLOCK),
    ANIMAL_GROWTH(BeaconsSettings.GUI_ITEM_EFFECT_ANIMAL_GROWTH);

    private BeaconEffect(BeaconsSettings icon)
    {
        this.icon = icon;
    }

    public BeaconsSettings icon;
}
