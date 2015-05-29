package us.corenetwork.mantle.spellbooks.books;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import me.ryanhamshire.GriefPrevention.Claim;
import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.Items;
import net.minecraft.server.v1_8_R3.MerchantRecipe;
import net.minecraft.server.v1_8_R3.MerchantRecipeList;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.ParticleLibrary;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.nanobot.NanobotUtil;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;
import us.corenetwork.mantle.spellbooks.SpellbooksSettings;


public class PeddlingBook extends Spellbook {
	
	private static final String SETTING_MESSAGE_NOTHING_TO_SELL = "Messages.NothingToSell";
	private static final String SETTING_MESSAGE_NOT_ENOUGH_TO_SELL = "Messages.NotEnoughToSell";

	@SuppressWarnings("deprecation") //Screw you mojang, damage values are not going anywhere
	public PeddlingBook() {
		super("Peddling");
		
		settings.setDefault(SETTING_TEMPLATE, "spell-peddling");
		settings.setDefault(SETTING_MESSAGE_NOTHING_TO_SELL, "You have nothing to sell to this villager!");
		settings.setDefault(SETTING_MESSAGE_NOT_ENOUGH_TO_SELL, "You don't have enough items to sell to this villager!");

	}	
		
	@Override
	protected BookFinishAction onActivateEntity(SpellbookItem spellbookItem, PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();
		if (entity.getType() != EntityType.VILLAGER)
			return BookFinishAction.NOTHING;
		
		Player player = event.getPlayer();
		
		Claim claim = GriefPreventionHandler.getClaimAt(player.getLocation());
		if (claim != null && claim.allowContainers(player) != null)
		{
			Util.Message(SpellbooksSettings.MESSAGE_NO_PERMISSION.string(), event.getPlayer());
			return BookFinishAction.NOTHING;
		}
		
		EntityVillager nmsVillager = (EntityVillager) ((CraftEntity) entity).getHandle();
		MerchantRecipeList nmsRecipes = nmsVillager.getOffers(null); //This only works for TradeCraft - vanilla probably won't like null here.
		
		List<RecipeData> recipes = new LinkedList<RecipeData>();
		
		for (Object recipeObject : nmsRecipes)
		{
			MerchantRecipe recipe = (MerchantRecipe) recipeObject;
			
			if (recipe.h()) //Do not sell if recipe is locked
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
					
			net.minecraft.server.v1_8_R3.ItemStack nmsItem = NanobotUtil.getInternalNMSStack(item);
			
			for (RecipeData recipe : recipes)
			{
				if (net.minecraft.server.v1_8_R3.ItemStack.c(nmsItem, recipe.sellItem))
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
			Util.Message(settings.getString(SETTING_MESSAGE_NOTHING_TO_SELL), event.getPlayer());
			return BookFinishAction.NOTHING;
		}
		
		int targetAmount = (int) Math.floor(winningRecipe.amountPlayerHave / winningRecipe.itemsPerEmerald);
		int sourceAmount = (int) Math.floor(targetAmount * winningRecipe.itemsPerEmerald);
		int restocks = (int) Math.max(0, Math.ceil(sourceAmount / winningRecipe.sellItem.count / 8.0) - 1);
		targetAmount -= restocks * 2; //Reduce payment by amounts of emeralds needed to restock
				
		if (targetAmount == 0)
		{
			Util.Message(settings.getString(SETTING_MESSAGE_NOT_ENOUGH_TO_SELL), event.getPlayer());
			return BookFinishAction.NOTHING;
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
		
		Location effectLoc = entity.getLocation();
        effectLoc.setY(effectLoc.getY() + 1.5);
        ParticleLibrary.broadcastParticle(EnumParticle.CRIT_MAGIC, effectLoc, 0.3f, 0.3f, 0.3f, 0, 30, null);
        event.getPlayer().playSound(effectLoc, Sound.VILLAGER_YES, 1.0f, 1.0f);
		
		return BookFinishAction.BROADCAST_AND_CONSUME;

	}
	
	private static void removeItem(Inventory inventory, net.minecraft.server.v1_8_R3.ItemStack comparingItem, int amount)
	{
		for (int i = 0; i < inventory.getSize(); i++)
		{
			ItemStack stack = inventory.getItem(i);
			if (stack != null && stack.getType() != Material.AIR)
			{
				net.minecraft.server.v1_8_R3.ItemStack nmsStack = NanobotUtil.getInternalNMSStack(stack);
				if (!net.minecraft.server.v1_8_R3.ItemStack.c(nmsStack, comparingItem))
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
	public BookFinishAction onActivate(SpellbookItem item, PlayerInteractEvent event) {
		return BookFinishAction.NOTHING;
	}
	
	private static class RecipeData
	{
		public net.minecraft.server.v1_8_R3.ItemStack sellItem;
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
