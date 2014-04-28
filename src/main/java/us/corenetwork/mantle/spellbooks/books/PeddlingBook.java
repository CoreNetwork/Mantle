package us.corenetwork.mantle.spellbooks.books;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import me.ryanhamshire.GriefPrevention.Claim;
import net.minecraft.server.v1_7_R3.EntityItem;
import net.minecraft.server.v1_7_R3.EntityVillager;
import net.minecraft.server.v1_7_R3.Items;
import net.minecraft.server.v1_7_R3.MerchantRecipe;
import net.minecraft.server.v1_7_R3.MerchantRecipeList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftItem;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.ParticleLibrary;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;
import us.corenetwork.mantle.spellbooks.SpellbooksSettings;


public class PeddlingBook extends Spellbook {
	
	@SuppressWarnings("deprecation") //Screw you mojang, damage values are not going anywhere
	public PeddlingBook() {
		super("Peddling");
		
		settings.setDefault(SETTING_TEMPLATE, "spell-peddling");
	}	
		
	@Override
	protected boolean onActivateEntity(SpellbookItem spellbookItem, PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();
		if (entity.getType() != EntityType.VILLAGER)
			return false;
		
		Player player = event.getPlayer();
		
		Claim claim = GriefPreventionHandler.getClaimAt(player.getLocation());
		if (claim != null && claim.allowContainers(player) != null)
		{
			Util.Message(SpellbooksSettings.MESSAGE_NO_PERMISSION.string(), event.getPlayer());
			return false;
		}
		
		EntityVillager nmsVillager = (EntityVillager) ((CraftEntity) entity).getHandle();
		MerchantRecipeList nmsRecipes = nmsVillager.getOffers(null); //This only works for TradeCraft - vanilla probably won't like null here.
		
		List<RecipeData> recipes = new LinkedList<RecipeData>();
		
		for (Object recipeObject : nmsRecipes)
		{
			MerchantRecipe recipe = (MerchantRecipe) recipeObject;
			
			if (recipe.g()) //Do not sell if recipe is locked
				continue;
			
			if (recipe.hasSecondItem())
				continue;
			
			if (recipe.getBuyItem3().getItem() != Items.EMERALD)
				continue;
			
			
			recipes.add(new RecipeData(recipe));
		}
		
		
		//Count all selling items that player has
		for (ItemStack item : player.getInventory().getContents())
		{
			if (item == null || item.getType() == Material.AIR)
				continue;
					
			net.minecraft.server.v1_7_R3.ItemStack nmsItem = SpellbookUtil.getNMSInnerItem(item);
			
			for (RecipeData recipe : recipes)
			{
				if (SpellbookUtil.compareItemTypes(nmsItem, recipe.sellItem))
				{
					recipe.amountPlayerHave += item.getAmount();
				}
			}
		}
		
		//Find which item player has most
		RecipeData winningRecipe = null;
		for (RecipeData recipe : recipes)
		{
			if (winningRecipe == null || winningRecipe.amountPlayerHave < recipe.amountPlayerHave)
			{
				winningRecipe = recipe;
			}
		}
		
		if (winningRecipe == null || winningRecipe.amountPlayerHave == 0)
		{
			Util.Message(SpellbooksSettings.MESSAGE_PEDDLING_NOTHING_TO_SELL.string(), event.getPlayer());
			return false;
		}
		
		int targetAmount = (int) Math.floor(winningRecipe.amountPlayerHave / winningRecipe.itemsPerEmerald);
		int sourceAmount = (int) Math.floor(targetAmount * winningRecipe.itemsPerEmerald);
		int restocks = (int) Math.max(0, Math.ceil(sourceAmount / winningRecipe.sellItem.count / 8.0) - 1);
		targetAmount -= restocks; //Reduce payment by amounts of emeralds needed to restock
				
		if (targetAmount == 0)
		{
			Util.Message(SpellbooksSettings.MESSAGE_PEDDLING_NOT_ENOUGH_TO_SELL.string(), event.getPlayer());
			return false;
		}
		
		//Remove items to sell
		removeItem(player.getInventory(), winningRecipe.sellItem, sourceAmount);
		
		//Give emeralds
		while (targetAmount > 0)
		{
			int stackAmount = Math.min(targetAmount, 64);
			ItemStack stack = new ItemStack(Material.EMERALD, stackAmount);
			
			HashMap<Integer, ItemStack> overflowItems = player.getInventory().addItem(stack);
			if (overflowItems.size() > 0) //Drop emerald0s on the ground if inventory is full
			{
				for (ItemStack itemToDrop : overflowItems.values())
				{
					player.getWorld().dropItem(player.getLocation(), itemToDrop);
				}
			}

			
			targetAmount -= stackAmount;
		}
		
		Location effectLoc = SpellbookUtil.getPointInFrontOfPlayer(event.getPlayer().getEyeLocation(), 2);
		Vector direction = event.getPlayer().getLocation().getDirection();

		ParticleLibrary.MOB_SPELL.sendToPlayer(event.getPlayer(), effectLoc, (float) (1.0 - direction.getX()), 0.5f, (float) (1.0 - direction.getZ()), 0, 10);
		event.getPlayer().playSound(effectLoc, Sound.LEVEL_UP, 1.0f, 1.0f);
		
		return true;

	}
	
	private static void removeItem(Inventory inventory, net.minecraft.server.v1_7_R3.ItemStack comparingItem, int amount)
	{
		for (int i = 0; i < inventory.getSize(); i++)
		{
			ItemStack stack = inventory.getItem(i);
			if (stack != null && stack.getType() != Material.AIR)
			{
				net.minecraft.server.v1_7_R3.ItemStack nmsStack = SpellbookUtil.getNMSInnerItem(stack);
				if (!SpellbookUtil.compareItemTypes(nmsStack, comparingItem))
					continue;
				
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
	public boolean onActivate(SpellbookItem item, PlayerInteractEvent event) {
		return false;
	}
	
	private static class RecipeData
	{
		public net.minecraft.server.v1_7_R3.ItemStack sellItem;
		public int amountPlayerHave;
		public double itemsPerEmerald;
		
		public RecipeData(MerchantRecipe recipe)
		{
			sellItem = recipe.getBuyItem1();
			itemsPerEmerald = recipe.getBuyItem1().count / (double) recipe.getBuyItem3().count;
			amountPlayerHave = 0;
		}
	}
}
