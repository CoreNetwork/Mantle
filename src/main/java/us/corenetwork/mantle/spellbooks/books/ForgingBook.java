package us.corenetwork.mantle.spellbooks.books;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import me.ryanhamshire.GriefPrevention.Claim;
import net.minecraft.server.v1_7_R3.RecipesFurnace;

import org.bukkit.Bukkit;
import org.bukkit.CoalType;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
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
			Entry<net.minecraft.server.v1_7_R3.ItemStack, net.minecraft.server.v1_7_R3.ItemStack> recipe = (Entry<net.minecraft.server.v1_7_R3.ItemStack, net.minecraft.server.v1_7_R3.ItemStack>) recipeO;
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
	public boolean onActivate(SpellbookItem spellbookItem, PlayerInteractEvent event) {
		Player player = event.getPlayer();
		
		Inventory inventory;
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && Util.isInventoryContainer(event.getClickedBlock().getTypeId()))
		{
			//Check for claim if clicking on chest
			Claim claim = GriefPreventionHandler.getClaimAt(player.getLocation());
			if (claim != null && claim.allowContainers(player) != null)
			{
				Util.Message(SpellbooksSettings.MESSAGE_NO_PERMISSION.string(), event.getPlayer());
				return false;
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
		
		for (ItemStack item : inventory.getContents())
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
		
		availableFuel.sort(new Comparator<ItemStack>() {
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
			int amountSource = 0;
			int existingTargetItemsFree = 0; //How many target items can we fit into existing stacks
			
			for (ItemStack stack : inventory.getContents())
			{
				if (stack == null)
					continue;
				
				if (stack.getType() == entry.getKey().getType() && (stack.getDurability() == entry.getKey().getDurability() || entry.getKey().getDurability() == 32767)) //Apparently 32767 means ignore data value
					amountSource += stack.getAmount();
				
				if (stack.getType() == entry.getValue().getType() && (stack.getDurability() == entry.getValue().getDurability() || entry.getValue().getDurability() == 32767))
					existingTargetItemsFree += stack.getMaxStackSize() - stack.getAmount();
			}
						
			amountSource = Math.min(amountSource, totalAvailableFuel - totalConsumedFuel);
			
			int divider = entry.getKey().getAmount() / entry.getValue().getAmount();
			int amountTarget = amountSource / divider;
			amountSource = amountTarget * divider;
			
			if (amountTarget < 1)
				continue;
			
			totalConsumedFuel += amountSource;
			
			int stacksRemoved = amountSource / entry.getKey().getMaxStackSize();
			int stacksAdded = (int) Math.ceil((double) (amountTarget - existingTargetItemsFree) / entry.getValue().getMaxStackSize());
			
			if (stacksRemoved + freeInventorySlots < stacksAdded)
				continue;
					
			FusingBook.removeItem(inventory, entry.getKey().getType(), entry.getKey().getDurability(), amountSource);
						
			while (amountTarget > 0)
			{
				int addAmount = Math.min(amountTarget, entry.getValue().getMaxStackSize());
				amountTarget -= addAmount;
				HashMap<Integer, ItemStack> invalidItems = inventory.addItem(new ItemStack(entry.getValue().getType(), addAmount, entry.getValue().getDurability()));
				//Just in case
				if (invalidItems.size() > 0)
				{
					MLog.warning("[Forging Book] ITEM SIZE CALCULATION WENT WRONG! Items were dropped to the ground! Go bug matejdro!");
					for (ItemStack itemToDrop : invalidItems.values())
						player.getWorld().dropItem(player.getLocation(), itemToDrop);
				}

			}

			freeInventorySlots -= stacksAdded;
		}
		
		
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

			FusingBook.removeItem(inventory, fuelStack.getType(), fuelStack.getDurability(), amountToRemove);
			
			if (amountToRemove >= fuelStack.getAmount())
				availableFuel.removeFirst();
			else
			{
				fuelStack.setAmount(fuelStack.getAmount() - amountToRemove);
				availableFuel.set(0, fuelStack);
			}
			
			totalConsumedFuel -= amountToRemove * efficiency;
		}
		
		if (inventory.getType() == InventoryType.PLAYER)
			player.updateInventory();
				
		FireworkEffect effect = FireworkEffect.builder().withColor(Color.ORANGE).withFade(Color.ORANGE).build();
		Location effectLoc = SpellbookUtil.getPointInFrontOfPlayer(player.getEyeLocation(), 2);
		Util.showFirework(effectLoc, effect);
		effectLoc.getWorld().playSound(effectLoc, Sound.FIRE, 1f, 1f);
		
		return true;
	}
		
	@Override
	protected boolean onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return false;
	}

}
