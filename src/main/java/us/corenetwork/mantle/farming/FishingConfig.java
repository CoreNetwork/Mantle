package us.corenetwork.mantle.farming;

import net.minecraft.server.v1_8_R1.EnchantmentManager;
import org.bukkit.Statistic;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.YamlUtils;

import java.util.*;

public class FishingConfig implements Listener {
    private List<FishingGroup> groups = new ArrayList<>();
    private Random random = MantlePlugin.random;

    public void loadConfig() {
        groups = new ArrayList<>();
        try {
            List<?> fishConfig =  FarmingModule.instance.config.getList("Fishing");
            for (Object groupObj : fishConfig) {
                Map<String, Object> groupMap = (Map<String, Object>) groupObj;
                int weight = (Integer) groupMap.get("Weight");
                Statistic statistic = Statistic.FISH_CAUGHT;
                if (groupMap.containsKey("Statistic")) {
                    statistic = Statistic.valueOf((String) groupMap.get("Statistic"));
                }
                FishingGroup currentGroup = new FishingGroup(weight, statistic);

                List<?> items = (List<?>) groupMap.get("Items");
                for (Object itemObj : items) {
                    Map<String, Object> itemMap = (Map<String, Object>) itemObj;
                    ItemStack mat = YamlUtils.readItemStack((Map<String, Object>) itemMap.get("Item"));
                    int itemWeight = (Integer) itemMap.get("Weight");
                    int xp = FishingItem.DONT_CHANGE_XP;
                    if (itemMap.containsKey("Exp")) {
                        xp = (Integer) itemMap.get("Exp");
                    }
                    int enchantLevel = FishingItem.DONT_ENCHANT;
                    if (itemMap.containsKey("EnchantLevel")) {
                        enchantLevel = (Integer) itemMap.get("EnchantLevel");
                    }
                    FishingItem currentItem = new FishingItem(mat, itemWeight, xp, enchantLevel);
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

            FishingGroup group = (FishingGroup) selectWeighted(groups);
            FishingItem selectedItem = (FishingItem) selectWeighted(group.getItems());
            ItemStack replace = selectedItem.getItem().clone();
            if (selectedItem.getXp() != FishingItem.DONT_CHANGE_XP) {
                event.setExpToDrop(selectedItem.getXp());
            }
            if (selectedItem.getEnchantLevel() != FishingItem.DONT_ENCHANT) {
                net.minecraft.server.v1_8_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(replace);
                nmsStack = EnchantmentManager.a(random, nmsStack, selectedItem.getEnchantLevel());
                replace = CraftItemStack.asBukkitCopy(nmsStack);
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

    public Weighted selectWeighted(Collection<? extends Weighted> from) {
        int sum = 0;
        for (Weighted w : from) {
            sum += w.getWeight();
        }

        int chosen = random.nextInt(sum);
        sum = 0;
        Weighted lastW = null;
        for (Weighted w : from) {
            sum += w.getWeight();
            if (chosen < sum) {
                return w;
            }
            lastW = w;
        }
        return lastW;
    }

    public static interface Weighted {
        public int getWeight();
    }

    public static class FishingGroup implements Weighted {
        private int weight;
        private List<FishingItem> items = new ArrayList<>();
        private Statistic statistic;

        public FishingGroup(int weight, Statistic statistic) {
            this.weight = weight;
            this.statistic = statistic;
        }

        public int getWeight() {
            return weight;
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

        public FishingItem(ItemStack item, int weight, int xp, int enchantLevel) {
            this.item = item;
            this.weight = weight;
            this.xp = xp;
            this.enchantLevel = enchantLevel;
        }

        public ItemStack getItem() {
            return item;
        }

        public int getWeight() {
            return weight;
        }

        public int getXp() {
            return xp;
        }

        public int getEnchantLevel() {
            return enchantLevel;
        }
    }
}
