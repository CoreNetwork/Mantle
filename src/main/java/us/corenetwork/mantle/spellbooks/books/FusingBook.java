package us.corenetwork.mantle.spellbooks.books;

import java.util.HashMap;
import java.util.Map.Entry;

import me.ryanhamshire.GriefPrevention.Claim;

import org.bukkit.DyeColor;
import org.bukkit.Material;
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
import us.corenetwork.mantle.spellbooks.SpellbooksSettings;


public class FusingBook extends Spellbook {
	private final HashMap<ItemStack, ItemStack> FUSEITEMS = new HashMap<ItemStack, ItemStack>();
	
	@SuppressWarnings("deprecation") //Screw you mojang, damage values are not going anywhere
	public FusingBook() {
		super("Fusing");
		
		FUSEITEMS.put(new ItemStack(Material.COAL, 9), new ItemStack(Material.COAL_BLOCK, 1));
		FUSEITEMS.put(new ItemStack(Material.IRON_INGOT, 9), new ItemStack(Material.IRON_BLOCK, 1));
		FUSEITEMS.put(new ItemStack(Material.GOLD_NUGGET, 9), new ItemStack(Material.GOLD_INGOT, 1));
		FUSEITEMS.put(new ItemStack(Material.GOLD_INGOT, 9), new ItemStack(Material.GOLD_BLOCK, 1));
		FUSEITEMS.put(new ItemStack(Material.REDSTONE, 9), new ItemStack(Material.REDSTONE_BLOCK, 1));
		FUSEITEMS.put(new ItemStack(Material.EMERALD, 9), new ItemStack(Material.EMERALD_BLOCK, 1));
		FUSEITEMS.put(new ItemStack(Material.DIAMOND, 9), new ItemStack(Material.DIAMOND_BLOCK, 1));
		FUSEITEMS.put(new ItemStack(Material.INK_SACK, 9, DyeColor.BLUE.getDyeData()), new ItemStack(Material.LAPIS_BLOCK, 1));
		FUSEITEMS.put(new ItemStack(Material.GLOWSTONE_DUST, 4), new ItemStack(Material.GLOWSTONE, 1));
		FUSEITEMS.put(new ItemStack(Material.QUARTZ, 4), new ItemStack(Material.QUARTZ_BLOCK, 1));
		FUSEITEMS.put(new ItemStack(Material.WHEAT, 9), new ItemStack(Material.HAY_BLOCK, 1));
		FUSEITEMS.put(new ItemStack(Material.MELON, 9), new ItemStack(Material.MELON_BLOCK, 1));
		FUSEITEMS.put(new ItemStack(Material.NETHER_BRICK_ITEM, 4), new ItemStack(Material.NETHER_BRICK, 1));
		
		settings.setDefault(SETTING_TEMPLATE, "spell-fusing");
	}
	
	@Override
	protected boolean usesContainers() {
		return true;
	}
		
	@Override
	public boolean onActivate(SpellbookItem item, PlayerInteractEvent event) {
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
		
		int freeInventorySlots = getFreeInventorySlots(inventory);
		
		for (Entry<ItemStack, ItemStack> entry : FUSEITEMS.entrySet())
		{			
			int amountSource = 0;
			int existingTargetItemsFree = 0; //How many target items can we fit into existing stacks

			for (ItemStack stack : inventory.getContents())
			{
				if (stack == null)
					continue;
				
				if (stack.getType() == entry.getKey().getType() && stack.getDurability() == entry.getKey().getDurability())
					amountSource += stack.getAmount();
				
				if (stack.getType() == entry.getValue().getType() && stack.getDurability() == entry.getValue().getDurability())
					existingTargetItemsFree += stack.getMaxStackSize() - stack.getAmount();
			}
			
			
			int divider = entry.getKey().getAmount() / entry.getValue().getAmount();
			int amountTarget = amountSource / divider;
			amountSource = amountTarget * divider;

			
			if (amountTarget < 1)
				continue;
			
			int stacksRemoved = amountSource / entry.getKey().getMaxStackSize();
			int stacksAdded = (int) Math.ceil((double) (amountTarget - existingTargetItemsFree) / entry.getValue().getMaxStackSize());
			
			if (stacksRemoved + freeInventorySlots < stacksAdded)
				continue;
					
			removeItem(inventory, entry.getKey().getType(), entry.getKey().getDurability(), amountSource);
						
			while (amountTarget > 0)
			{
				int addAmount = Math.min(amountTarget, entry.getValue().getMaxStackSize());
				amountTarget -= addAmount;
				HashMap<Integer, ItemStack> invalidItems = inventory.addItem(new ItemStack(entry.getValue().getType(), addAmount, entry.getValue().getDurability()));
				//Just in case
				if (invalidItems.size() > 0)
				{
					MLog.warning("[Fusing Book] ITEM SIZE CALCULATION WENT WRONG! Items were dropped to the ground! Go bug matejdro!");
					for (ItemStack itemToDrop : invalidItems.values())
						player.getWorld().dropItem(player.getLocation(), itemToDrop);
				}

			}

			freeInventorySlots -= stacksAdded;
		}
		
		if (inventory.getType() == InventoryType.PLAYER)
			player.updateInventory();
		
		return true;
	}
	

	private static int getFreeInventorySlots(Inventory inventory)
	{
		int slots = 0;
		
		for (int i = 0; i < inventory.getSize(); i++)
		{
			ItemStack stack = inventory.getItem(i);
			if (stack == null || stack.getType() == Material.AIR)
				slots++;
		}
		
		return slots;
	}
	
	private static void removeItem(Inventory inventory, Material material, short durability, int amount)
	{
		for (int i = 0; i < inventory.getSize(); i++)
		{
			ItemStack stack = inventory.getItem(i);
			if (stack != null && material == stack.getType() && durability == stack.getDurability())
			{
				int stackAmount = stack.getAmount();
				if (amount >= stackAmount)
				{
					inventory.setItem(i, null);
					amount -= stackAmount;
					if (amount == 0)
						break;
				}
				else
				{
					stack.setAmount(stackAmount - amount);
					break;
				}
			}
		}
	}
	
	@Override
	protected boolean onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return false;
	}

}
