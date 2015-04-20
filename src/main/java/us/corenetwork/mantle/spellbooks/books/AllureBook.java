package us.corenetwork.mantle.spellbooks.books;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.server.v1_8_R2.EntityExperienceOrb;
import net.minecraft.server.v1_8_R2.EntityHuman;
import net.minecraft.server.v1_8_R2.EntityItem;
import net.minecraft.server.v1_8_R2.EnumParticle;
import net.minecraft.server.v1_8_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftItem;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftWolf;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.ParticleLibrary;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.spellbooks.EntityIterator;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;


public class AllureBook extends Spellbook {
    private static final int EFFECT_AREA_HEIGHT_ABOVE_PLAYER = 61;
    private static final int EFFECT_AREA_HORIZONTAL_RADIUS = 64 / 2;

    private static final int EFFECT_ITERATIONS = 3 * 20 / 4;

    public AllureBook() {
		super("Allure");

		settings.setDefault(SETTING_TEMPLATE, "spell-allure");
	}

	@Override
	public BookFinishAction onActivate(SpellbookItem item, PlayerInteractEvent event) {
		Player player = event.getPlayer();

        Location raisedLocation = event.getPlayer().getLocation();
        raisedLocation.setY(raisedLocation.getY() + EFFECT_AREA_HEIGHT_ABOVE_PLAYER / 2);

        PlayerShoverContents showerContents = new PlayerShoverContents();

        List<Entity> nearbyEntities = EntityIterator.getEntitiesInCube(raisedLocation, EFFECT_AREA_HORIZONTAL_RADIUS);
        for (Entity entity : nearbyEntities)
        {
            if (entity instanceof ExperienceOrb)
            {
                showerContents.expAmount += ((ExperienceOrb) entity).getExperience();
                entity.remove();
            }
            else if (entity instanceof Item)
            {
                EntityItem vanillaItem = (EntityItem) ((CraftItem) entity).getHandle();
                if (vanillaItem.m() != null) //Do not include items with specified owner
                    continue;

                showerContents.stacksToDrop.add(((Item) entity).getItemStack());
                entity.remove();
            }
        }

        showerContents.dropsPerIteration = Math.max( showerContents.getTotalAmountOfItems() / EFFECT_ITERATIONS, 1);
        showerContents.expPerIteration = Math.max(showerContents.expAmount / EFFECT_ITERATIONS, 1);
        showerContents.lastKnownLocation = player.getLocation();
        showerContents.player = player;

        Bukkit.getScheduler().runTask(MantlePlugin.instance, new PlayerShower(showerContents));

        ParticleLibrary.broadcastParticle(EnumParticle.CRIT, SpellbookUtil.getPointInFrontOfPlayer(player.getEyeLocation(), 0.3), 0.3f, 0.3f, 0.3f, 0, 30, null);

        return BookFinishAction.BROADCAST_AND_CONSUME;
	}

    @Override
	protected BookFinishAction onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return BookFinishAction.NOTHING;
	}

    private static class PlayerShower implements Runnable
    {
        private PlayerShoverContents contents;

        public PlayerShower(PlayerShoverContents contents)
        {
            this.contents = contents;
        }

        @Override
        public void run()
        {
            if (!contents.player.isOnline())
            {
                dropAll();
                return;
            }

            Block dropBlock = contents.player.getEyeLocation().getBlock();
            for (int i = 0; i < 2; i++) //Try moving drop location up to 2 blocks above player
            {
                Block aboveBlock = dropBlock.getRelative(BlockFace.UP);
                if (aboveBlock.isEmpty())
                    dropBlock = aboveBlock;
            }

            Location dropLocation = Util.getLocationInBlockCenter(dropBlock);
            contents.lastKnownLocation = dropLocation;

            int drops = contents.dropsPerIteration;
            while (drops > 0 && !contents.stacksToDrop.isEmpty())
            {
                ItemStack firstItem = contents.stacksToDrop.peekFirst();

                net.minecraft.server.v1_8_R2.ItemStack singleItem = CraftItemStack.asNMSCopy(firstItem);
                singleItem.count = 1;

                NotCombiningItem item = new NotCombiningItem(((CraftWorld) dropLocation.getWorld()).getHandle(), dropLocation.getX(), dropLocation.getY(), dropLocation.getZ());
                ((CraftWorld) dropLocation.getWorld()).getHandle().addEntity(item);
                item.setItemStack(singleItem);

                int newCount = firstItem.getAmount() - 1;
                if (newCount == 0)
                    contents.stacksToDrop.removeFirst();
                else
                    firstItem.setAmount(newCount);

                drops--;
            }

            int expDrop = Math.min(contents.expPerIteration, contents.expAmount);
            if (expDrop > 0)
            {
                contents.player.giveExp(expDrop);
                contents.player.playSound(contents.player.getEyeLocation(), Sound.ORB_PICKUP, 0.1f, 0.5F * ((MantlePlugin.random.nextFloat() - MantlePlugin.random.nextFloat()) * 0.7F + 1.8F));

                contents.expAmount -= expDrop;
            }

            if (!contents.stacksToDrop.isEmpty() || contents.expAmount > 0)
            {
                Bukkit.getScheduler().runTaskLater(MantlePlugin.instance, this, 4);
            }
        }

        private void dropAll()
        {
            for (ItemStack stack : contents.stacksToDrop)
                contents.lastKnownLocation.getWorld().dropItem(contents.lastKnownLocation, stack);

            ExperienceOrb orb = contents.lastKnownLocation.getWorld().spawn(contents.lastKnownLocation, ExperienceOrb.class);
            orb.setExperience(contents.expAmount);
        }
    }

    private class PlayerShoverContents
    {
        public LinkedList<ItemStack> stacksToDrop = new LinkedList<ItemStack>();
        public int expAmount = 0;
        public int dropsPerIteration;
        public int expPerIteration;
        public Player player;
        public Location lastKnownLocation = null;

        public int getTotalAmountOfItems()
        {
            int amount = 0;
            for (ItemStack stack : stacksToDrop)
                amount += stack.getAmount();

            return amount;
        }
    }

    private static class NotCombiningItem extends EntityItem
    {

        public NotCombiningItem(World world, double d0, double d1, double d2)
        {
            super(world, d0, d1, d2);
            this.pickupDelay = Short.MAX_VALUE;
        }

        @Override
        public void d(EntityHuman entityhuman)
        {
            this.pickupDelay = 0;
            super.d(entityhuman);
            this.pickupDelay = Short.MAX_VALUE;
        }
    }
}
