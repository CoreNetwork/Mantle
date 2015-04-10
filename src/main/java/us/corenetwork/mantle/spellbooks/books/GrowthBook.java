package us.corenetwork.mantle.spellbooks.books;

import java.util.List;
import net.minecraft.server.v1_8_R2.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.TreeSpecies;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Tree;
import org.bukkit.util.Vector;
import us.corenetwork.mantle.ParticleLibrary;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.spellbooks.EntityIterator;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;
import us.corenetwork.mantle.util.InventoryUtil;


public class GrowthBook extends Spellbook {

	private static final int EFFECT_AREA_HEIGHT_ABOVE_PLAYER = 31;
    private static final int EFFECT_AREA_HORIZONTAL_RADIUS = 32 / 2;

    public GrowthBook() {
		super("Growth");
		
		settings.setDefault(SETTING_TEMPLATE, "spell-growth");
		settings.setDefault(SETTING_NO_ITEMS, "No bonemeal");
	}

	@Override
	public BookFinishAction onActivate(SpellbookItem item, PlayerInteractEvent event) {

		Player player = event.getPlayer();
        
		Location effectLoc = SpellbookUtil.getPointInFrontOfPlayer(event.getPlayer().getEyeLocation(), 2);
		Vector direction = event.getPlayer().getLocation().getDirection();

		ParticleLibrary.broadcastParticle(EnumParticle.VILLAGER_HAPPY, effectLoc, (float) (1.0 - direction.getX()), 0.5f, (float) (1.0 - direction.getZ()), 0, 10, null);
		event.getPlayer().playSound(effectLoc, Sound.LEVEL_UP, 1.0f, 1.0f);
		
		Block baseBlock = event.getPlayer().getLocation().getBlock();
		for (int x = -EFFECT_AREA_HORIZONTAL_RADIUS; x <= EFFECT_AREA_HORIZONTAL_RADIUS; x++)
		{
			for (int y = -1; y <= EFFECT_AREA_HEIGHT_ABOVE_PLAYER; y++) //Blocks from one block below player to 31 blocks above are affected.
			{
				for (int z = -EFFECT_AREA_HORIZONTAL_RADIUS; z <= EFFECT_AREA_HORIZONTAL_RADIUS; z++)
				{
					Block block = baseBlock.getRelative(x, y, z);
					processBlock(block);
				}
			}
		}

        Location raisedLocation = event.getPlayer().getLocation();
        raisedLocation.setY(raisedLocation.getY() + EFFECT_AREA_HEIGHT_ABOVE_PLAYER / 2);

		List<Entity> nearbyEntities = EntityIterator.getEntitiesInCube(raisedLocation, EFFECT_AREA_HORIZONTAL_RADIUS);
		for (Entity entity : nearbyEntities)
        {
            if (entity instanceof Ageable)
            {
                Ageable ageable = (Ageable) entity;
                if (!ageable.isAdult())
                    ageable.setAdult();
            }
            else if (entity instanceof Zombie)
            {
                Zombie zombie = (Zombie) entity;
                if (zombie.isBaby())
                    zombie.setBaby(false);
            }
        }

		return BookFinishAction.BROADCAST_AND_CONSUME;
	}

	private void processBlock(Block block)
	{
		if (block.getType() == Material.CROPS || block.getType() == Material.CARROT || block.getType() == Material.POTATO)
			block.setData((byte) 7);
		else if (block.getType() == Material.NETHER_WARTS)
			block.setData((byte) 3);
		else if (block.getType() == Material.MELON_STEM)
		{
            if (block.getData() < 7)
            {
                block.setData((byte) 7);
            }
            else
            {
                for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH })
                {
                    final Block neighbour = block.getRelative(face);
                    if (neighbour != null && neighbour.isEmpty())
                    {
                        Block downBlock = block.getRelative(BlockFace.DOWN);
                        if (downBlock != null && (downBlock.getType() == Material.GRASS || downBlock.getType() == Material.DIRT || downBlock.getType() == Material.SOIL))
                        {
                            neighbour.setType(Material.MELON_BLOCK);
                            break;
                        }
                    }
                }
            }
		}
        else if (block.getType() == Material.PUMPKIN_STEM)
        {
            if (block.getData() < 7)
            {
                block.setData((byte) 7);
            }
            else
            {
                for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH })
                {
                    final Block neighbour = block.getRelative(face);
                    if (neighbour != null && neighbour.isEmpty())
                    {
                        Block downBlock = block.getRelative(BlockFace.DOWN);
                        if (downBlock != null && (downBlock.getType() == Material.GRASS || downBlock.getType() == Material.DIRT || downBlock.getType() == Material.SOIL))
                        {
                            neighbour.setType(Material.PUMPKIN);
                            break;
                        }
                    }
                }
            }
        }
		else if (block.getType() == Material.SAPLING)
		{
			byte data = block.getData();
			TreeSpecies species = new Tree(block.getTypeId(), data).getSpecies();
			
			TreeType tree;
			
			switch (species)
			{
			case BIRCH:
				tree = TreeType.BIRCH;
				break;
			case JUNGLE:
				tree = TreeType.JUNGLE;
				break;
			case REDWOOD:
				tree = TreeType.REDWOOD;
				break;
			case ACACIA:
				tree = TreeType.ACACIA;
				break;
			case DARK_OAK:
				tree = TreeType.DARK_OAK;
				break;
			case GENERIC:
				tree = TreeType.TREE;
				break;

			default:
				tree = TreeType.BIG_TREE;
			}
			
			block.setType(Material.AIR);
			
			boolean generated = block.getWorld().generateTree(block.getLocation(), tree);
			if (!generated)
				block.setTypeIdAndData(Material.SAPLING.getId(), data, false);
		}
		
	}

	@Override
	protected BookFinishAction onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return BookFinishAction.NOTHING;
	}
}
