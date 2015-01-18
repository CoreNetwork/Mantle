package us.corenetwork.mantle.farming;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.server.v1_8_R1.EnchantmentManager;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.YamlUtils;

public class FishingConfig implements Listener {
    private List<FishingGroup> groups = new ArrayList<>();
    private Random random = MantlePlugin.random;

    public void loadConfig() {
        try {
            groups = new ArrayList<>();
            YamlConfiguration config = FarmingModule.instance.config;
            Collection list = null;
            if (config.get("Fishing") == null) {
                return;
            }
            ConfigurationSection fishing = config.getConfigurationSection("Fishing");
            for (String groupNodeName : fishing.getValues(false).keySet()) {
                ConfigurationSection groupSection = fishing.getConfigurationSection(groupNodeName);
                int weight = groupSection.getInt("Weight");
                Statistic statistic = Statistic.valueOf(groupSection.getString("Statistic", "FISH_CAUGHT").toUpperCase());

                Map<Enchantment, Integer> enchantments = new HashMap<>();
                if (groupSection.isConfigurationSection("Enchantments")) {
                    ConfigurationSection enchantmentSection = groupSection.getConfigurationSection("Enchantments");

                    for (Map.Entry<String, Object> entry : enchantmentSection.getValues(false).entrySet()) {
                        Enchantment e = Enchantment.getByName(entry.getKey());
                        if (e != null) {
                            enchantments.put(e, (Integer) entry.getValue());
                        }
                    }
                }
                FishingGroup currentGroup = new FishingGroup(weight, statistic, enchantments);

                List<?> items = (List<?>) groupSection.getMapList("Items");
                for (Object itemObj : items) {
                    Map<String, Object> itemMap = (Map<String, Object>) ((Map<String, Object>) itemObj).get("Item"); // ridel pls
                    ItemStack mat = YamlUtils.readItemStack(itemMap); // ridel pls
                    int itemWeight = 1;
                    if (itemMap.containsKey("Weight")) {
                        itemWeight = (Integer) itemMap.get("Weight");
                    }
                    int xp = 0;
                    if (itemMap.containsKey("Exp")) {
                        xp = (Integer) itemMap.get("Exp");
                    }
                    int enchantLevel = FishingItem.DONT_ENCHANT;
                    if (itemMap.containsKey("EnchantLevel")) {
                        enchantLevel = (Integer) itemMap.get("EnchantLevel");
                    }
                    int penalty = 0;
                    if (itemMap.containsKey("Penalty")) {
                        penalty = (Integer) itemMap.get("Penalty");
                    }
                    FishingItem currentItem = new FishingItem(mat, itemWeight, xp, enchantLevel, penalty);
                    currentGroup.getItems().add(currentItem);
                }
                groups.add(currentGroup);
            }
            allowStatIncrement = false;
        } catch (Exception e) {
            MLog.warning("[Farming] No fishing config found. Fishing loot will be untouched.");
            e.printStackTrace();
            groups = null;
            allowStatIncrement = true;
        }
    }

    private boolean allowStatIncrement = false;

    @EventHandler(ignoreCancelled = true)
    public void onFished(PlayerFishEvent event) {
        if (groups != null && event.getCaught() instanceof Item) {
            Item item = (Item) event.getCaught();

            ItemStack itemInHand = event.getPlayer().getItemInHand();
            FishingGroup group = (FishingGroup) selectWeighted(groups, itemInHand);
            FishingItem selectedItem = (FishingItem) selectWeighted(group.getItems(), itemInHand);
            ItemStack replace = selectedItem.getItem().clone();
            if (selectedItem.getXp() != FishingItem.DONT_CHANGE_XP) {
                event.setExpToDrop(selectedItem.getXp());
            }
            if (selectedItem.getEnchantLevel() != FishingItem.DONT_ENCHANT) {
                net.minecraft.server.v1_8_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(replace);
                nmsStack = EnchantmentManager.a(random, nmsStack, selectedItem.getEnchantLevel());
                replace = CraftItemStack.asBukkitCopy(nmsStack);
            }
            if (selectedItem.getPenalty() > 0) {
                itemInHand.setDurability((short) (itemInHand.getDurability() + selectedItem.getPenalty()));
                event.getPlayer().setItemInHand(itemInHand);
            }
            item.setItemStack(replace);
            allowStatIncrement = true;
            event.getPlayer().incrementStatistic(group.getStatistic());
            allowStatIncrement = false;
        }
    }

    private HashSet<Statistic> blockedStats = new HashSet<>();

    {
        blockedStats.add(Statistic.JUNK_FISHED);
        blockedStats.add(Statistic.FISH_CAUGHT);
        blockedStats.add(Statistic.TREASURE_FISHED);
    }

    @EventHandler(ignoreCancelled = true)
    public void onStatisticIncrement(PlayerStatisticIncrementEvent event) {
        if (blockedStats.contains(event.getStatistic()) && !allowStatIncrement) {
            event.setCancelled(true);
        }
    }

    public Weighted selectWeighted(Collection<? extends Weighted> from, ItemStack tool) {
        int sum = 0;
        for (Weighted w : from) {
            sum += w.getWeight(tool);
        }

        int chosen = random.nextInt(sum);
        sum = 0;
        Weighted lastW = null;
        for (Weighted w : from) {
            sum += w.getWeight(tool);
            if (chosen < sum) {
                return w;
            }
            lastW = w;
        }
        return lastW;
    }

    public static interface Weighted {
        public int getWeight(ItemStack tool);
    }

    public static class FishingGroup implements Weighted {
        private int weight;
        private Map<Enchantment, Integer> enchantmentWeightIncr;
        private List<FishingItem> items = new ArrayList<>();
        private Statistic statistic;

        public FishingGroup(int weight, Statistic statistic, Map<Enchantment, Integer> enchantments) {
            this.weight = weight;
            this.statistic = statistic;
            this.enchantmentWeightIncr = enchantments;
        }

        public int getWeight(ItemStack tool) {
            int ret = weight;
            for (Map.Entry<Enchantment, Integer> enchantment : enchantmentWeightIncr.entrySet()) {
                ret += tool.getEnchantmentLevel(enchantment.getKey()) * enchantment.getValue();
            }
            return ret;
        }

        public List<FishingItem> getItems() {
            return items;
        }

        public Statistic getStatistic() {
            return statistic;
        }
    }

    public static class FishingItem implements Weighted {
        public static final int DONT_ENCHANT = -1;
        public static final int DONT_CHANGE_XP = -1;
        private ItemStack item;
        private int weight;
        private int xp;
        private int enchantLevel;
        private int penalty;

        public FishingItem(ItemStack item, int weight, int xp, int enchantLevel, int penalty) {
            this.item = item;
            this.weight = weight;
            this.xp = xp;
            this.enchantLevel = enchantLevel;
            this.penalty = penalty;
        }

        public ItemStack getItem() {
            return item;
        }

        public int getWeight(ItemStack tool) {
            return weight;
        }

        public int getXp() {
            return xp;
        }

        public int getEnchantLevel() {
            return enchantLevel;
        }

        public int getPenalty() {
            return penalty;
        }
    }
}
