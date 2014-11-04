package us.corenetwork.mantle.beacons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.Configuration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import us.corenetwork.mantle.YamlUtils;

/**
 * Created by Matej on 28.10.2014.
 */
public class BeaconEffect
{
    public static BeaconEffectStorage STORAGE = new BeaconEffectStorage();

    private String name;
    private EffectType effectType;
    private PotionEffect potionEffectParameters;
    private ItemStack effectIcon;
    private ItemStack activeEffectIcon;
    private ItemStack fuelIcon;
    private int fuelDuration;
    private int fuelDurationStrongEffect;
    private int strongEffectModifier;
    private String effectNameStrong;
    private String effectNameWeak;

    public BeaconEffect(String name, EffectType effectType, ItemStack effectIcon, ItemStack activeEffectIcon, ItemStack fuelIcon, int fuelDuration, int fuelDurationStrongEffect, int strongEffectModifier, String effectNameStrong, String effectNameWeak)
    {
        this.name = name;
        this.effectType = effectType;
        this.effectIcon = effectIcon;
        this.activeEffectIcon = activeEffectIcon;
        this.fuelIcon = fuelIcon;
        this.fuelDuration = fuelDuration;
        this.fuelDurationStrongEffect = fuelDurationStrongEffect;
        this.strongEffectModifier = strongEffectModifier;
        this.effectNameStrong = effectNameStrong;
        this.effectNameWeak = effectNameWeak;
    }

    public static BeaconEffect getFromConfig(String name, Map<String, Object> section)
    {
        String effectTypeName = (String) section.get("Type");
        if (effectTypeName == null)
        {
            System.out.println("Invalid config: beacon effect has no type defined.");
            return null;
        }
        EffectType effectType = EffectType.fromName(effectTypeName);
        if (effectType == null)
        {
            System.out.println("Invalid config: invalid beacon effect type (" + effectTypeName + ")");
            return null;
        }

        ItemStack effectIcon = YamlUtils.readItemStack((Map<String, Object>) section.get("EffectIcon"));
        if (effectIcon == null)
        {
            System.out.println("Invalid config: missing effect icon");
            return null;
        }

        ItemStack activeEffectIcon = YamlUtils.readItemStack((Map<String, Object>) section.get("ActiveEffectIcon"));
        if (activeEffectIcon == null)
        {
            System.out.println("Invalid config: missing active effect icon");
            return null;
        }

        ItemStack fuelIcon = YamlUtils.readItemStack((Map<String, Object>) section.get("FuelIcon"));
        if (fuelIcon == null)
        {
            System.out.println("Invalid config: missing fuel icon");
            return null;
        }

        Integer fuelDuration = (Integer) section.get("FuelDuration");
        if (fuelDuration == null)
        {
            System.out.println("Invalid config: beacon effect has no FuelDuration defined.");
            return null;
        }

        Integer fuelDurationStrongEffect = (Integer) section.get("FuelDurationStrongEffect");
        if (fuelDurationStrongEffect == null)
        {
            System.out.println("Invalid config: beacon effect has no FuelDurationStrongEffect defined.");
            return null;
        }


        Integer strongEffectModifier = (Integer) section.get("StrongEffectModifier");
        if (strongEffectModifier == null)
        {
            System.out.println("Invalid config: beacon effect has no StrongEffectModifier defined.");
            return null;
        }

        String effectNameWeak = (String) section.get("EffectNameWeak");
        if (effectNameWeak == null)
        {
            System.out.println("Invalid config: beacon effect has no EffectNameWeak defined.");
            return null;
        }

        String effectNameStrong = (String) section.get("EffectNameStrong");
        if (effectNameStrong == null)
        {
            System.out.println("Invalid config: beacon effect has no EffectNameStrong defined.");
            return null;
        }


        BeaconEffect effect = new BeaconEffect(name, effectType, effectIcon, activeEffectIcon, fuelIcon, fuelDuration, fuelDurationStrongEffect, strongEffectModifier, effectNameStrong, effectNameWeak);

        if (effectType == EffectType.POTION)
        {
            PotionEffect potionEffect = YamlUtils.readPotionEffect((Map<String, Object>) section.get("Potion"));
            if (potionEffect == null)
            {
                System.out.println("Invalid config: missing potion effect data!");
                return null;
            }

            effect.potionEffectParameters = potionEffect;
        }

        return effect;
    }

    public String getName()
    {
        return name;
    }

    public EffectType getEffectType()
    {
        return effectType;
    }

    public PotionEffect getPotionEffectParameters()
    {
        return potionEffectParameters;
    }

    public ItemStack getEffectIcon()
    {
        return effectIcon;
    }

    public ItemStack getActiveEffectIcon()
    {
        return activeEffectIcon;
    }

    public ItemStack getFuelIcon()
    {
        return fuelIcon;
    }

    public int getFuelDuration()
    {
        return fuelDuration;
    }

    public int getFuelDurationStrongEffect()
    {
        return fuelDurationStrongEffect;
    }

    public int getStrongEffectModifier()
    {
        return strongEffectModifier;
    }

    public String getEffectNameStrong()
    {
        return effectNameStrong;
    }

    public String getEffectNameWeak()
    {
        return effectNameWeak;
    }

    public static class BeaconEffectStorage
    {
        public List<BeaconEffect> effects = new ArrayList<BeaconEffect>();

        public void load(Configuration configuration)
        {
            effects.clear();

            List<Map<String, Object>> effectMap = (List<Map<String, Object>>) configuration.getList("Effects");
            for (Map<String, Object> effectNode : effectMap)
            {
                String effectName = effectNode.keySet().toArray(new String[0])[0];
                Map<String, Object> effectInnerNode = (Map<String, Object>) effectNode.get(effectName);

                BeaconEffect effect = BeaconEffect.getFromConfig(effectName, effectInnerNode);
                if (effect != null)
                    effects.add(effect);
            }
        }
    }

    public static enum EffectType
    {
        POTION("Potion"),
        OVERCLOCK("Overclock"),
        FAST_GROWTH("FastGrowth");

        private String name;

        private EffectType(String name)
        {
            this.name = name;
        }

        public static EffectType fromName(String name)
        {
            for (EffectType type : EffectType.values())
            {
                if (name.equalsIgnoreCase(type.name))
                    return type;
            }

            return null;
        }
    }
}
