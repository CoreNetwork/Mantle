package us.corenetwork.mantle.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;

public class BlockTraits {
    public static final Set<Material> NO_PISTON_PUSH_BLOCKS = new HashSet<>();
    public static final Set<Material> FLUID_BLOCKS = new HashSet<>();

    private static void populate(Set<Material> set, Material... mats) {
        Collections.addAll(set, mats);
    }

    static {
        populate(NO_PISTON_PUSH_BLOCKS, Material.OBSIDIAN, Material.FURNACE, Material.BURNING_FURNACE,
                Material.CHEST, Material.TRAPPED_CHEST, Material.HOPPER, Material.ENCHANTMENT_TABLE,
                Material.BREWING_STAND);

        populate(FLUID_BLOCKS, Material.WATER, Material.STATIONARY_LAVA, Material.STATIONARY_WATER, Material.LAVA);
    }
}
