package us.corenetwork.mantle.spellbooks.books;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import me.ryanhamshire.GriefPrevention.Claim;
import net.minecraft.server.v1_7_R4.RecipesFurnace;

import org.bukkit.CoalType;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
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


public class ForgingBook extends Spellbook {
	private final HashMap<ItemStack, ItemStack> FORGEITEMS = new HashMap<ItemStack, ItemStack>();
	private final List<ItemStack> FUEL = new LinkedList<ItemStack>();
	
	@SuppressWarnings("deprecation") //Screw you mojang, damage values are not going anywhere
	public ForgingBook() {
		super("Forging");
		
		//Get all recipes from vanilla furnaces
		for (Object recipeO : RecipesFurnace.getInstance().recipes.entrySet())
		{
			Entry<net.minecraft.server.v1_7_R4.ItemStack, net.minecraft.server.v1_7_R4.ItemStack> recipe = (Entry<net.minecraft.server.v1_7_R4.ItemStack, net.minecraft.server.v1_7_R4.ItemStack>) recipeO;
			FORGEITEMS.put(CraftItemStack.asCraftMirror(recipe.getKey()), CraftItemStack.asCraftMirror(recipe.getValue()));
		}
		
		FUEL.add(new ItemStack(Material.COAL, 8, CoalType.COAL.getData()));
		FUEL.add(new ItemStack(Material.COAL, 8, CoalType.CHARCOAL.getData()));
		FUEL.add(new ItemStack(Material.COAL_BLOCK, 72));
		FUEL.add(new ItemStack(Material.BLAZE_ROD, 12));
		
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
		
		int freeInventorySlots = FusingBook.getFreeInventorySlots(inventory);
		
		LinkedList<ItemStack> availableFuel = new LinkedList<ItemStack>();
		int totalAvailableFuel = 0;
		
		for (ItemStack item : player.getInventory().getContents())
		{
			if (item == null || item.getType() == Material.AIR)
				continue;
								
			for (ItemStack fuelItem : FUEL)
			{
				if (item.getType() == fuelItem.getType() && (item.getDurability() == fuelItem.getDurability() || fuelItem.getDurability() == 32767))
				{
					availableFuel.add(item.clone());
					totalAvailableFuel += item.getAmount() * fuelItem.getAmount(); //fuelItem.getAmount() is basically amount of items smelted per fuel
				}

			}
		}
		
		Collections.sort(availableFuel, new Comparator<ItemStack>() {
			@Override
			public int compare(ItemStack arg0, ItemStack arg1) {
				int efficiencyFirst = 0;
				int efficiencySecond = 0;
				
				for (ItemStack stack : FUEL)
				{
					if (stack.getType() == arg0.getType() && (arg0.getDurability() == stack.getDurability() || stack.getDurability() == 32767))
						efficiencyFirst = stack.getAmount();
					if (stack.getType() == arg1.getType() && (arg1.getDurability() == stack.getDurability() || stack.getDurability() == 32767))
						efficiencySecond = stack.getAmount();
				}
				
				return efficiencyFirst - efficiencySecond;
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
			int fuelToSpend = totalAvailableFuel - totalConsumedFuel;
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
			int efficiency = 0;
			for (ItemStack stack : FUEL)
			{
				if (stack.getType() == fuelStack.getType() && stack.getDurability() == fuelStack.getDurability())
					efficiency = stack.getAmount();
			}
			

			int amountToRemove = Math.min(fuelStack.getAmount(), (int) Math.ceil(totalConsumedFuel / (double) efficiency));

			FusingBook.removeItem(player.getInventory(), fuelStack.getType(), fuelStack.getDurability(), amountToRemove);
			
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

}