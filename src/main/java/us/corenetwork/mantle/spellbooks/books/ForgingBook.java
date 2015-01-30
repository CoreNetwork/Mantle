package us.corenetwork.mantle.spellbooks.books;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import me.ryanhamshire.GriefPrevention.Claim;
import net.minecraft.server.v1_8_R1.RecipesFurnace;
import org.bukkit.CoalType;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;
import us.corenetwork.mantle.spellbooks.SpellbooksSettings;
import us.core_network.cornel.items.InventoryUtil;


public class ForgingBook extends Spellbook {
	private final HashMap<ItemStack, ItemStack> FORGEITEMS = new HashMap<ItemStack, ItemStack>();
	private final List<Fuel> FUEL = new LinkedList<Fuel>();
	
	@SuppressWarnings("deprecation") //Screw you mojang, damage values are not going anywhere
	public ForgingBook() {
		super("Forging");
		
		//Get all recipes from vanilla furnaces
		for (Object recipeO : RecipesFurnace.getInstance().recipes.entrySet())
		{
			Entry<net.minecraft.server.v1_8_R1.ItemStack, net.minecraft.server.v1_8_R1.ItemStack> recipe = (Entry<net.minecraft.server.v1_8_R1.ItemStack, net.minecraft.server.v1_8_R1.ItemStack>) recipeO;
			FORGEITEMS.put(CraftItemStack.asCraftMirror(recipe.getKey()), CraftItemStack.asCraftMirror(recipe.getValue()));
		}
		
		FUEL.add(new Fuel(Material.COAL, 8, CoalType.COAL.getData()));
		FUEL.add(new Fuel(Material.COAL, 8, CoalType.CHARCOAL.getData()));
		FUEL.add(new Fuel(Material.COAL_BLOCK, 72));
		FUEL.add(new Fuel(Material.BLAZE_ROD, 12));
		FUEL.add(new Fuel(Material.WOOD, 1.5));
		FUEL.add(new Fuel(Material.CHEST, 1.5));
		FUEL.add(new Fuel(Material.TRAPPED_CHEST, 1.5));
		FUEL.add(new Fuel(Material.WORKBENCH, 1.5));
		FUEL.add(new Fuel(Material.NOTE_BLOCK, 1.5));
		FUEL.add(new Fuel(Material.JUKEBOX, 1.5));
		FUEL.add(new Fuel(Material.FENCE_GATE, 1.5));
		FUEL.add(new Fuel(Material.FENCE, 1.5));
		FUEL.add(new Fuel(Material.ACACIA_FENCE, 1.5));
		FUEL.add(new Fuel(Material.ACACIA_FENCE_GATE, 1.5));
		FUEL.add(new Fuel(Material.BIRCH_FENCE, 1.5));
		FUEL.add(new Fuel(Material.BIRCH_FENCE_GATE, 1.5));
		FUEL.add(new Fuel(Material.DARK_OAK_FENCE, 1.5));
		FUEL.add(new Fuel(Material.DARK_OAK_FENCE_GATE, 1.5));
		FUEL.add(new Fuel(Material.JUNGLE_FENCE, 1.5));
		FUEL.add(new Fuel(Material.JUNGLE_FENCE_GATE, 1.5));
		FUEL.add(new Fuel(Material.JUNGLE_FENCE_GATE, 1.5));
		FUEL.add(new Fuel(Material.WOOD_STAIRS, 1.5));
		FUEL.add(new Fuel(Material.ACACIA_STAIRS, 1.5));
		FUEL.add(new Fuel(Material.BIRCH_WOOD_STAIRS, 1.5));
		FUEL.add(new Fuel(Material.DARK_OAK_STAIRS, 1.5));
		FUEL.add(new Fuel(Material.JUNGLE_WOOD_STAIRS, 1.5));
		FUEL.add(new Fuel(Material.WOOD_DOUBLE_STEP, 1.5));
		FUEL.add(new Fuel(Material.WOOD_STEP, 1.5));

		settings.setDefault(SETTING_TEMPLATE, "spell-forging");
	}
	
	@Override
	protected boolean usesContainers() {
		return true;
	}
		
	@Override
	public BookFinishAction onActivate(SpellbookItem spellbookItem, PlayerInteractEvent event) {
		Player player = event.getPlayer();
		
		Inventory inventory;
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
		}
		else
		{
			inventory = player.getInventory();
		}	
		
		int freeInventorySlots = InventoryUtil.getFreeInventorySlots(inventory);
		
		LinkedList<ItemStack> availableFuel = new LinkedList<ItemStack>();
		double totalAvailableFuel = 0;
		
		for (ItemStack item : player.getInventory().getContents())
		{
			if (item == null || item.getType() == Material.AIR)
				continue;
								
			for (Fuel fuelItem : FUEL)
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
			return BookFinishAction.NOTHING;
		
		Collections.sort(availableFuel, new Comparator<ItemStack>() {
			@Override
			public int compare(ItemStack arg0, ItemStack arg1) {
				double efficiencyFirst = 0;
				double efficiencySecond = 0;
				
				for (Fuel fuelItem : FUEL)
				{
					if (arg0.getType() == fuelItem.getMaterial() && (arg0.getDurability() == fuelItem.getDurability() || fuelItem.getDurability() == 32767))
						efficiencyFirst = fuelItem.getSmeltedAmount();
					if (fuelItem.getMaterial() == arg1.getType() && (arg1.getDurability() == fuelItem.getDurability() || fuelItem.getDurability() == 32767))
						efficiencySecond = fuelItem.getSmeltedAmount();
				}
				
				return (int) (efficiencyFirst - efficiencySecond);
			}
		}); //Sort by efficiency		

				
		int totalConsumedFuel = 0;
		
		for (Entry<ItemStack, ItemStack> entry : FORGEITEMS.entrySet())
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
															
			int amountFree = freeInventorySlots * outputItemType.getMaxStackSize() + existingTargetItemsFree;
			int amountToSmelt = 0;
			int fuelToSpend = (int) (totalAvailableFuel - totalConsumedFuel);
			if (fuelToSpend < 1)
				break;
			
			for (int i = 0; i < inventory.getSize(); i++)
			{
				ItemStack stack = inventory.getItem(i);
				if (stack != null && inputItemType.getType() == stack.getType() && (inputItemType.getDurability() == stack.getDurability() || inputItemType.getDurability()  == 32767))
				{
					int stackAmount = stack.getAmount();
					if (fuelToSpend >= stackAmount && stackAmount <= amountFree + outputItemType.getMaxStackSize())
					{
						inventory.setItem(i, null);
						fuelToSpend -= stackAmount;
						amountToSmelt += stackAmount;
						amountFree += outputItemType.getMaxStackSize() - stackAmount;
						
						if (fuelToSpend == 0)
							break;
					}
					else
					{
						int amountToRemove = Math.min(fuelToSpend, amountFree);
						stack.setAmount(stackAmount - amountToRemove);
						fuelToSpend -= amountToRemove;
						amountToSmelt += amountToRemove;
						amountFree -= amountToRemove;
						
						break;
					}
				}
			}
			
			if (amountToSmelt < 1)
				continue;
			
			totalConsumedFuel += amountToSmelt;
									
			while (amountToSmelt > 0)
			{
				int addAmount = Math.min(amountToSmelt, outputItemType.getMaxStackSize());
				amountToSmelt -= addAmount;
				HashMap<Integer, ItemStack> invalidItems = inventory.addItem(new ItemStack(outputItemType.getType(), addAmount, outputItemType.getDurability()));
				//Just in case
				if (invalidItems.size() > 0)
				{
					MLog.warning("[Forging Book] ITEM SIZE CALCULATION WENT WRONG! Items were dropped to the ground! Go bug matejdro!");
					for (ItemStack itemToDrop : invalidItems.values())
						player.getWorld().dropItem(player.getLocation(), itemToDrop);
				}

			}

			freeInventorySlots -= Math.ceil(amountToSmelt / (double) entry.getValue().getMaxStackSize());
		}
		
		boolean anythingSmelted = totalConsumedFuel > 0;
		
		//Remove fuel
		while (totalConsumedFuel > 0)
		{
			ItemStack fuelStack = availableFuel.getFirst();
			double efficiency = 0;
			for (Fuel fuelType : FUEL)
			{
				if (fuelType.getMaterial() == fuelStack.getType() && (fuelType.getDurability() == fuelStack.getDurability() || fuelType.getDurability() == Short.MAX_VALUE))
				{
					efficiency = fuelType.getSmeltedAmount();
					break;
				}

			}

			int amountToRemove = Math.min(fuelStack.getAmount(), (int) Math.ceil(totalConsumedFuel / efficiency));

			InventoryUtil.removeItems(player.getInventory(), fuelStack.getType(), fuelStack.getDurability(), amountToRemove);
			
			if (amountToRemove >= fuelStack.getAmount())
				availableFuel.removeFirst();
			else
			{
				fuelStack.setAmount(fuelStack.getAmount() - amountToRemove);
				availableFuel.set(0, fuelStack);
			}
			
			totalConsumedFuel -= amountToRemove * efficiency;
		}
		
		player.updateInventory();
				
		FireworkEffect effect = FireworkEffect.builder().withColor(Color.ORANGE).withFade(Color.ORANGE).build();
		Location effectLoc = SpellbookUtil.getPointInFrontOfPlayer(player.getEyeLocation(), 2);
		Util.showFirework(effectLoc, effect);
		effectLoc.getWorld().playSound(effectLoc, Sound.FIRE, 1f, 1f);
		
		if (anythingSmelted)
			return BookFinishAction.BROADCAST_AND_CONSUME;
		else
			return BookFinishAction.CONSUME;
	}
		
	@Override
	protected BookFinishAction onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return BookFinishAction.NOTHING;
	}

	public static class Fuel
	{
		private Material material;
		private double smeltItems;
		private short data;

		public Fuel(Material material, double smeltItems)
		{
			this.material = material;
			this.smeltItems = smeltItems;
			this.data = Short.MAX_VALUE;
		}

		public Fuel(Material material, double smeltItems, short data)
		{
			this.material = material;
			this.smeltItems = smeltItems;
			this.data = data;
		}

		public Material getMaterial()
		{
			return material;
		}

		public double getSmeltedAmount()
		{
			return smeltItems;
		}

		public short getDurability()
		{
			return data;
		}
	}

}
