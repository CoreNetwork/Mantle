package us.corenetwork.mantle.farming;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class FishingConfig implements Listener {
    private List<FishingGroup> groups = new ArrayList<>();
    private Random random = new Random();

    public void loadConfig() {
        List<?> fishConfig =  FarmingModule.instance.config.getList("Fishing");
        for (Object groupObj : fishConfig) {
            Map<String, Object> groupMap = (Map<String, Object>) groupObj;
            String groupName = (String) groupMap.get("Name");
            int weight = (Integer) groupMap.get("Weight");
            FishingGroup currentGroup = new FishingGroup(groupName, weight);

            List<?> items = (List<?>) groupMap.get("Items");
            for (Object itemObj : items) {
                Map<String, Object> itemMap = (Map<String, Object>) itemObj;
                ItemStack mat = new ItemStack(Material.getMaterial((String) itemMap.get("Item")));
                int itemWeight = (Integer) itemMap.get("Weight");
                int xp = (Integer) itemMap.get("Xp");
                FishingItem currentItem = new FishingItem(currentGroup, mat, itemWeight, xp);
                currentGroup.getItems().add(currentItem);
            }
            groups.add(currentGroup);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFished(PlayerFishEvent event) {
        if (event.getCaught() instanceof Item) {
            Item item = (Item) event.getCaught();

            FishingGroup group = (FishingGroup) selectWeighted(groups);
            FishingItem selectedItem = (FishingItem) selectWeighted(group.getItems());
            item.setItemStack(selectedItem.getItem());
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
        private String name;
        private int weight;
        private List<FishingItem> items = new ArrayList<>();

        // TODO weight modifiers for enchantments


        public FishingGroup(String name, int weight) {
            this.name = name;
            this.weight = weight;
        }

        public String getName() {
            return name;
        }

        public int getWeight() {
            return weight;
        }

        public List<FishingItem> getItems() {
            return items;
        }
    }

    public static class FishingItem implements Weighted {
        private FishingGroup group;
        private ItemStack item;
        private int weight;
        private int xp;

        public FishingItem(FishingGroup group, ItemStack item, int weight, int xp) {
            this.group = group;
            this.item = item;
            this.weight = weight;
            this.xp = xp;
        }

        public FishingGroup getGroup() {
            return group;
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
    }
}
