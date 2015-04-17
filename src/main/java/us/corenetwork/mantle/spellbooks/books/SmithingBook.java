package us.corenetwork.mantle.spellbooks.books;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import me.ryanhamshire.GriefPrevention.Claim;
import net.minecraft.server.v1_8_R2.EnumParticle;
import org.bukkit.CoalType;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.ParticleLibrary;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;
import us.corenetwork.mantle.spellbooks.SpellbooksSettings;
import us.corenetwork.mantle.util.InventoryUtil;


public class SmithingBook extends Spellbook {
	private static final LinkedHashMap<ItemStack, ItemStack> SMITHITEMS = new LinkedHashMap<ItemStack, ItemStack>();
    private static final List<ForgingBook.Fuel> FUEL = new LinkedList<ForgingBook.Fuel>();

    static
    {
        /**
         * WARNING: This book only supports unstackable items and items with durability (for now)
         */

        SMITHITEMS.put(new ItemStack(Material.IRON_CHESTPLATE, 1), new ItemStack(Material.IRON_INGOT, 5));
        SMITHITEMS.put(new ItemStack(Material.IRON_LEGGINGS, 1), new ItemStack(Material.IRON_INGOT, 4));
        SMITHITEMS.put(new ItemStack(Material.IRON_HELMET, 1), new ItemStack(Material.IRON_INGOT, 3));
        SMITHITEMS.put(new ItemStack(Material.IRON_BOOTS, 1), new ItemStack(Material.IRON_INGOT, 2));
        SMITHITEMS.put(new ItemStack(Material.IRON_SWORD, 1), new ItemStack(Material.IRON_INGOT, 1));
        SMITHITEMS.put(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1), new ItemStack(Material.IRON_INGOT, 6));
        SMITHITEMS.put(new ItemStack(Material.CHAINMAIL_LEGGINGS, 1), new ItemStack(Material.IRON_INGOT, 5));
        SMITHITEMS.put(new ItemStack(Material.CHAINMAIL_HELMET, 1), new ItemStack(Material.IRON_INGOT, 4));
        SMITHITEMS.put(new ItemStack(Material.CHAINMAIL_BOOTS, 1), new ItemStack(Material.IRON_INGOT, 2));
        SMITHITEMS.put(new ItemStack(Material.GOLD_CHESTPLATE, 1), new ItemStack(Material.GOLD_INGOT, 5));
        SMITHITEMS.put(new ItemStack(Material.GOLD_LEGGINGS, 1), new ItemStack(Material.GOLD_INGOT, 4));
        SMITHITEMS.put(new ItemStack(Material.GOLD_HELMET, 1), new ItemStack(Material.GOLD_INGOT, 3));
        SMITHITEMS.put(new ItemStack(Material.GOLD_BOOTS, 1), new ItemStack(Material.GOLD_INGOT, 2));
        SMITHITEMS.put(new ItemStack(Material.GOLD_SWORD, 1), new ItemStack(Material.GOLD_NUGGET, 12));

        FUEL.add(new ForgingBook.Fuel(Material.COAL, 1, CoalType.COAL.getData()));
        FUEL.add(new ForgingBook.Fuel(Material.COAL, 1, CoalType.CHARCOAL.getData()));
    }

	@SuppressWarnings("deprecation") //Screw you mojang, damage values are not going anywhere
	public SmithingBook() {
		super("Smithing");

        settings.setDefault(SETTING_TEMPLATE, "spell-smithing");
	}
	
	@Override
	protected boolean usesContainers() {
		return true;
	}
		
	@Override
	public BookFinishAction onActivate(SpellbookItem item, PlayerInteractEvent event) {
		Player player = event.getPlayer();
		
		Inventory inventory;
        Inventory fuelInventory = player.getInventory();
        Location effectLoc;
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && Util.isInventoryContainer(event.getClickedBlock().getTypeId()))
		{
			//Check for claim if clicking on chest
			Claim claim = GriefPreventionHandler.getClaimAt(event.getClickedBlock().getLocation());
			if (claim != null && claim.allowContainers(player) != null)
			{
				Util.Message(SpellbooksSettings.MESSAGE_NO_PERMISSION.string(), event.getPlayer());
				return BookFinishAction.NOTHING;
			}

			
			InventoryHolder container = (InventoryHolder) event.getClickedBlock().getState();
			inventory = container.getInventory();
            effectLoc = Util.getLocationInBlockCenter(event.getClickedBlock());
		}
		else
		{
			inventory = player.getInventory();
            effectLoc = player.getEyeLocation();
		}

        ParticleLibrary.broadcastParticleRing(EnumParticle.FLAME, effectLoc, 2);
        effectLoc.getWorld().playSound(effectLoc, Sound.BLAZE_HIT, 0.5f, 0.3f);

        boolean anythingSmelted = smith(inventory, fuelInventory);
        player.updateInventory();

        if (anythingSmelted)
            return BookFinishAction.BROADCAST_AND_CONSUME;
        else
            return BookFinishAction.CONSUME;
    }


    protected static boolean smith(Inventory inventory, Inventory fuelInventory)
    {
        //Get list of total fuel in inventory
        LinkedList<ItemStack> availableFuel = new LinkedList<ItemStack>();
        double totalAvailableFuel = 0;

        for (ItemStack item : fuelInventory)
        {
            if (item == null || item.getType() == Material.AIR)
                continue;

            for (ForgingBook.Fuel fuelItem : FUEL)
            {
                if (item.getType() == fuelItem.getMaterial() && (item.getDurability() == fuelItem.getDurability() || fuelItem.getDurability() == Short.MAX_VALUE))
                {
                    availableFuel.add(item.clone());
                    totalAvailableFuel += item.getAmount() * fuelItem.getSmeltedAmount(); //fuelItem.getAmount() is basically amount of items smelted per fuel
                }

            }
        }

        totalAvailableFuel = Math.floor(totalAvailableFuel);
        if (totalAvailableFuel == 0)
            return false;

        //Sort fuel by efficiency (so least efficient fuel goes first and thus it wastes less fuel when fuel used can smelt more than actual smelted item count)
        Collections.sort(availableFuel, new Comparator<ItemStack>()
        {
            @Override
            public int compare(ItemStack arg0, ItemStack arg1)
            {
                double efficiencyFirst = 0;
                double efficiencySecond = 0;

                for (ForgingBook.Fuel fuelItem : FUEL)
                {
                    if (arg0.getType() == fuelItem.getMaterial() && (arg0.getDurability() == fuelItem.getDurability() || fuelItem.getDurability() == 32767))
                        efficiencyFirst = fuelItem.getSmeltedAmount();
                    if (fuelItem.getMaterial() == arg1.getType() && (arg1.getDurability() == fuelItem.getDurability() || fuelItem.getDurability() == 32767))
                        efficiencySecond = fuelItem.getSmeltedAmount();
                }

                return (int) (efficiencyFirst - efficiencySecond);
            }
        });


        int totalConsumedFuel = 0;
        for (Entry<ItemStack, ItemStack> entry : SMITHITEMS.entrySet())
        {
            ItemStack inputItemType = entry.getKey();
            ItemStack outputItemType = entry.getValue();

            int existingTargetItemsFree = 0; //How many target items can we fit into existing stacks
            for (ItemStack stack : inventory.getContents())
            {
                if (stack == null)
                    continue;

                if (stack.getType() == outputItemType.getType() && (stack.getDurability() == outputItemType.getDurability() || outputItemType.getDurability() == 32767))
                    existingTargetItemsFree += stack.getMaxStackSize() - stack.getAmount();
            }

            int totalOutputAmount = 0;
            int totalCharcoalOutputAmount = 0;

            int fuelToSpend = (int) (totalAvailableFuel - totalConsumedFuel);
            if (fuelToSpend < 1)
                break;

            for (int i = 0; i < inventory.getSize(); i++)
            {
                ItemStack stack = inventory.getItem(i);
                if (stack != null && inputItemType.getType() == stack.getType())
                {
                    //Scale output by item durability
                    int outputAmount = outputItemType.getAmount();
                    outputAmount = outputAmount * (stack.getType().getMaxDurability() - stack.getDurability()) / stack.getType().getMaxDurability();

                    inventory.setItem(i, null);
                    fuelToSpend--;
                    totalConsumedFuel++;

                    if (outputAmount == 0)
                        totalCharcoalOutputAmount++;
                    else
                        totalOutputAmount += outputAmount;

                    if (fuelToSpend == 0)
                        break;
                }
            }

            if (totalOutputAmount == 0 && totalCharcoalOutputAmount == 0)
                continue;

            while (totalOutputAmount > 0)
            {
                int addAmount = Math.min(totalOutputAmount, outputItemType.getMaxStackSize());
                totalOutputAmount -= addAmount;
                HashMap<Integer, ItemStack> invalidItems = inventory.addItem(new ItemStack(outputItemType.getType(), addAmount, outputItemType.getDurability()));
                //Just in case
                if (invalidItems.size() > 0)
                {
                    MLog.warning("[Forging Book] ITEM SIZE CALCULATION WENT WRONG! Items could not fit into inventory! Go bug matejdro! Missed items:");
                    for (ItemStack itemToDrop : invalidItems.values())
                        MLog.warning(itemToDrop.toString());
                }
            }

            while (totalCharcoalOutputAmount > 0)
            {
                int addAmount = Math.min(totalCharcoalOutputAmount, Material.COAL.getMaxStackSize());
                totalCharcoalOutputAmount -= addAmount;
                HashMap<Integer, ItemStack> invalidItems = inventory.addItem(new ItemStack(Material.COAL, addAmount, CoalType.CHARCOAL.getData()));
                //Just in case
                if (invalidItems.size() > 0)
                {
                    MLog.warning("[Forging Book] ITEM SIZE CALCULATION WENT WRONG! Items could not fit into inventory! Go bug matejdro! Missed items:");
                    for (ItemStack itemToDrop : invalidItems.values())
                        MLog.warning(itemToDrop.toString());
                }

            }
        }

        boolean anythingSmithed = totalConsumedFuel > 0;

        //Remove fuel
        while (totalConsumedFuel > 0)
        {
            ItemStack fuelStack = availableFuel.getFirst();
            double efficiency = 0;
            for (ForgingBook.Fuel fuelType : FUEL)
            {
                if (fuelType.getMaterial() == fuelStack.getType() && (fuelType.getDurability() == fuelStack.getDurability() || fuelType.getDurability() == Short.MAX_VALUE))
                {
                    efficiency = fuelType.getSmeltedAmount();
                    break;
                }

            }

            int amountToRemove = Math.min(fuelStack.getAmount(), (int) Math.ceil(totalConsumedFuel / efficiency));

            InventoryUtil.removeItems(fuelInventory, fuelStack.getType(), fuelStack.getDurability(), amountToRemove);

            if (amountToRemove >= fuelStack.getAmount())
                availableFuel.removeFirst();
            else
            {
                fuelStack.setAmount(fuelStack.getAmount() - amountToRemove);
                availableFuel.set(0, fuelStack);
            }

            totalConsumedFuel -= amountToRemove * efficiency;
        }

        return anythingSmithed;
    }

	@Override
	protected BookFinishAction onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return BookFinishAction.NOTHING;
	}

}
