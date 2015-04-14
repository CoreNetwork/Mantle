package us.corenetwork.mantle.spellbooks.books;

import java.util.List;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Zombie;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Leaves;
import org.bukkit.material.Wool;
import us.corenetwork.core.claims.BlockWorker;
import us.corenetwork.core.claims.ClaimsModule;
import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.spellbooks.EntityIterator;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;
import us.corenetwork.mantle.spellbooks.SpellbooksSettings;


public class PruningBook extends Spellbook {

    private static final int EFFECT_AREA_HEIGHT_ABOVE_PLAYER = 31;
    private static final int EFFECT_AREA_HORIZONTAL_RADIUS = 32 / 2;
    private static final byte LEAVES_DATA_MASK = ~0x4 & ~0x8;

	public PruningBook() {
		super("Pruning");
		
		settings.setDefault(SETTING_TEMPLATE, "spell-pruning");
		settings.setDefault(SETTING_NO_ITEMS, "Not enough flint!");
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
			


            //Prune world around
            Block baseBlock = player.getLocation().getBlock();
            PruneBookWoker pruneBookWoker = new PruneBookWoker(baseBlock);
            ClaimsModule.instance.pool.addWorker(pruneBookWoker);

            //Remove nearby sheep wool
            Location raisedLocation = event.getPlayer().getLocation();
            raisedLocation.setY(raisedLocation.getY() + EFFECT_AREA_HEIGHT_ABOVE_PLAYER / 2);

            List<Entity> nearbyEntities = EntityIterator.getEntitiesInCube(raisedLocation, EFFECT_AREA_HORIZONTAL_RADIUS);
            for (Entity entity : nearbyEntities)
            {
                if (entity instanceof Sheep)
                {
                    Sheep sheep = (Sheep) entity;
                    if (sheep.isSheared())
                        continue;

                    DyeColor sheepColor = sheep.getColor();

                    int amountToDrop = 1 + MantlePlugin.random.nextInt(3);
                    ItemStack stackToDrop = new ItemStack(Material.WOOL, amountToDrop, sheepColor.getWoolData());
                    entity.getWorld().dropItemNaturally(entity.getLocation(), stackToDrop);

                    sheep.setSheared(true);
                }
                else if (entity instanceof Zombie)
                {
                    Zombie zombie = (Zombie) entity;
                    if (zombie.isBaby())
                        zombie.setBaby(false);
                }
            }


            player.updateInventory();
		}

		Location effectLoc = SpellbookUtil.getPointInFrontOfPlayer(player.getEyeLocation(), 2);
		effectLoc.getWorld().playSound(effectLoc, Sound.SHEEP_SHEAR, 1f, 2f);
		
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

    private static class PruneBookWoker extends BlockWorker
    {
        private int x;
        private int y;
        private int z;

        private Block startBlock;

        public PruneBookWoker(Block startBlock)
        {
            this.startBlock = startBlock;
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

                        if (block.getType() == Material.LEAVES || block.getType() == Material.LEAVES_2)
                        {
                            ItemStack stackToDrop = new ItemStack(block.getType(), 1, (short) (block.getData() & LEAVES_DATA_MASK));
                            block.setType(Material.AIR);

                            block.getWorld().dropItemNaturally(Util.getLocationInBlockCenter(block), stackToDrop);
                        }

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
