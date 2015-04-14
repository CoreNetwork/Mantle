package us.corenetwork.mantle.spellbooks.books;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimsMode;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import us.corenetwork.core.claims.BlockWorker;
import us.corenetwork.core.claims.ClaimsModule;
import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;
import us.corenetwork.mantle.spellbooks.SpellbooksSettings;
import us.corenetwork.mantle.util.InventoryUtil;


public class DecayBook extends Spellbook {

    private static final int EFFECT_AREA_HEIGHT_ABOVE_PLAYER = 31;
    private static final int EFFECT_AREA_HORIZONTAL_RADIUS = 32 / 2;
	
	public DecayBook() {
		super("Decay");
		
		settings.setDefault(SETTING_TEMPLATE, "spell-decay");
		settings.setDefault(SETTING_NO_ITEMS, "No rotten flesh");
	}

	@Override
	protected boolean usesContainers() {
		return true;
	}
	
	@Override
	public BookFinishAction onActivate(SpellbookItem item, PlayerInteractEvent event) {
		
		Player player = event.getPlayer();

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && Util.isInventoryContainer(event.getClickedBlock().getTypeId()))
		{
			//Check for claim if clicking on chest
			Claim claim = GriefPreventionHandler.getClaimAt(player.getLocation());
			if (claim != null && claim.allowContainers(player) != null)
			{
				Util.Message(SpellbooksSettings.MESSAGE_NO_PERMISSION.string(), event.getPlayer());
				return BookFinishAction.NOTHING;
			}

			//Only clean wool colors
			InventoryHolder container = (InventoryHolder) event.getClickedBlock().getState();
			removeWoolColors(container.getInventory());
		}
		else
		{
			Location playerLoc = player.getLocation();
			//Check for claims in effect area
			if (GriefPreventionHandler.containsClaim(playerLoc.getWorld(), playerLoc.getBlockX() - EFFECT_AREA_HORIZONTAL_RADIUS, playerLoc.getBlockZ() - EFFECT_AREA_HORIZONTAL_RADIUS, 0, 0, EFFECT_AREA_HORIZONTAL_RADIUS * 2, false, event.getPlayer()))
			{
				Util.Message(SpellbooksSettings.MESSAGE_NO_PERMISSION.string(), event.getPlayer());
				return BookFinishAction.NOTHING;
			}
			

			//Decay world around
            boolean removeGrass = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.GRASS;
            Block baseBlock = player.getLocation().getBlock();

            DecayBookWoker decayBookWoker = new DecayBookWoker(baseBlock, removeGrass);
            ClaimsModule.instance.pool.addWorker(decayBookWoker);
			
			removeWoolColors(player.getInventory());
			player.updateInventory();
		}

		FireworkEffect effect = FireworkEffect.builder().withColor(Color.OLIVE).withFade(Color.OLIVE).build();
		Location effectLoc = SpellbookUtil.getPointInFrontOfPlayer(player.getEyeLocation(), 2);
		Util.showFirework(effectLoc, effect);			
		effectLoc.getWorld().playSound(effectLoc, Sound.SKELETON_DEATH, 1f, 1f);		
		
		return BookFinishAction.BROADCAST_AND_CONSUME;
	}
	
	private static void removeWoolColors(Inventory inventory)
	{
		for (int i = 0; i < inventory.getSize(); i++)
		{
			ItemStack stack = inventory.getItem(i);
			if (stack != null && stack.getType() == Material.WOOL)
			{
				Wool materialData = (Wool) stack.getData();
				materialData.setColor(DyeColor.WHITE);
				stack.setDurability(materialData.getData());

				inventory.setItem(i, stack);
			}
		}
	}
	
	@Override
	protected BookFinishAction onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return BookFinishAction.NOTHING;
	}

    private static class DecayBookWoker extends BlockWorker
    {
        private int x;
        private int y;
        private int z;

        private Block startBlock;
        private boolean removeGrass;

        public DecayBookWoker(Block startBlock, boolean removeGrass)
        {
            this.startBlock = startBlock;
            this.removeGrass = removeGrass;
        }

        @Override
        public void init()
        {
            x = -EFFECT_AREA_HORIZONTAL_RADIUS;
            y = -1;
            z = -EFFECT_AREA_HORIZONTAL_RADIUS;
        }

        @Override
        public void onDone()
        {

        }

        @Override
        public long getTaskSize()
        {
            //X and Z size = RADIUS * 2 + 1 block for player position
            // Y size = height above player + 1 block for player position + 1 block for one layer below player
            return (EFFECT_AREA_HORIZONTAL_RADIUS * 2 + 1) * (EFFECT_AREA_HORIZONTAL_RADIUS * 2 + 1) * (EFFECT_AREA_HEIGHT_ABOVE_PLAYER + 2);
        }

        @Override
        public long work(long amountToWork)
        {
            Bukkit.broadcastMessage("work " + amountToWork);
            int worked = 0;

            outmost: for (; x <= EFFECT_AREA_HORIZONTAL_RADIUS; x++)
            {
                for (; y <= EFFECT_AREA_HEIGHT_ABOVE_PLAYER + 1; y++)
                {
                    if (y > EFFECT_AREA_HEIGHT_ABOVE_PLAYER)
                    {
                        y = -1;
                        break;
                    }

                    for (; z <= EFFECT_AREA_HORIZONTAL_RADIUS + 1; z++)
                    {
                        if (z > EFFECT_AREA_HORIZONTAL_RADIUS)
                        {
                            z = -EFFECT_AREA_HORIZONTAL_RADIUS;
                            break;
                        }

                        worked++;

                        Block block = startBlock.getRelative(x, y, z);

                        if (block.getType() == Material.LEAVES || block.getType() == Material.YELLOW_FLOWER || block.getType() == Material.RED_ROSE || (block.getType() == Material.DOUBLE_PLANT && block.getData() != 2) || block.getType() == Material.HUGE_MUSHROOM_1 || block.getType() == Material.HUGE_MUSHROOM_2)
                            block.breakNaturally();
                        else if (block.getType() == Material.LONG_GRASS || (block.getType() == Material.DOUBLE_PLANT && block.getData() == 2))
                            block.setType(Material.AIR);
                        else if (removeGrass && block.getType() == Material.GRASS)
                            block.setType(Material.DIRT);

                        if (worked >= amountToWork)
                        {
                            break  outmost;
                        }
                    }
                }
            }

            return worked;
        }
    }
}
