package us.corenetwork.mantle.animalspawning;

import net.minecraft.server.v1_8_R1.AttributeInstance;
import net.minecraft.server.v1_8_R1.EntityInsentient;
import net.minecraft.server.v1_8_R1.GenericAttributes;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.MemorySection;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import us.core_network.cornel.custom.NodeParser;
import us.corenetwork.core.CorePlugin;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;

public class AnimalSpawner {



	public static void spawnAnimal(Block block)
	{
		String animalSectionKey = NodeParser.pickNodeChance((MemorySection) AnimalSpawningModule.instance.config.get("Animals"), MantlePlugin.random);
		MemorySection animalSection = (MemorySection) AnimalSpawningModule.instance.config.get("Animals.".concat(animalSectionKey));
		
		EntityType animalType = EntityType.fromName(animalSectionKey);
		if (animalType == null)
		{
			MLog.severe("Cannot spawn animal! Unknown animal type: " + animalSectionKey);
			return;
		}
		
		//Spawn original mob
		spawn(block.getLocation(), animalType, animalSection);
		
		Integer maxAdditionalPackMobs = (Integer) animalSection.get("MaxAdditionalPackMobs");
		if (maxAdditionalPackMobs == null)
			maxAdditionalPackMobs = AnimalSpawningSettings.MAX_ADDITIONAL_PACK_MOBS.integer();
		
		Integer minAdditionalPackMobs = (Integer) animalSection.get("MinAdditionalPackMobs");
		if (minAdditionalPackMobs == null)
			minAdditionalPackMobs = AnimalSpawningSettings.MIN_ADDITIONAL_PACK_MOBS.integer();

		int diff = maxAdditionalPackMobs - minAdditionalPackMobs;
		
		if (diff <= 0)
			return;
		
		int additionalMobs = MantlePlugin.random.nextInt(diff) + minAdditionalPackMobs;
		for (int i = 0; i < additionalMobs; i++)
		{
			int xDiff = MantlePlugin.random.nextInt(10) - 5;
			int zDiff = MantlePlugin.random.nextInt(10) - 5;

			Block newBlock = block.getRelative(xDiff, 0, zDiff);
			if (newBlock.getType() != Material.AIR)
				continue;

			Block belowBlock = newBlock.getRelative(BlockFace.DOWN);
			
			if (belowBlock == null)
				continue;
			if (newBlock.getLightLevel() < 9)
				continue;
			if (belowBlock.getType() != Material.GRASS)
				continue;
		
			spawn(newBlock.getLocation(), animalType, animalSection);

		}
		
	}
	
	@SuppressWarnings("incomplete-switch")
	private static void spawn(Location location, EntityType type, MemorySection configSection)
	{
		AnimalSpawningListener.spawningMob = true;
		Entity entity = location.getWorld().spawnEntity(location, type);
		
		if (entity.isDead())
			return;
		
		switch (type)
		{
		case SHEEP:
			handleSheep(configSection, entity);
			break;
		case HORSE:
			handleHorse(configSection, entity);
			break;
		case RABBIT:
			handleRabbit(configSection, entity);
			break;

		}
	}
		
	private static void handleSheep(MemorySection section, Entity entity)
	{
		String colorName = NodeParser.pickNodeChance((MemorySection) section.get("Colors"), MantlePlugin.random);
		
		DyeColor color;
		try
		{
			color = DyeColor.valueOf(colorName.toUpperCase());
		}
		catch (IllegalArgumentException e)
		{
			MLog.severe("Unknown sheep color: " + colorName);
			color = DyeColor.WHITE;

		}
		
		((Sheep) entity).setColor(color);
	}
	
	private static void handleHorse(MemorySection section, Entity entity)
	{
		Horse horse = (Horse) entity;
		
		String colorName = NodeParser.pickNodeChance((MemorySection) section.get("Colors"), MantlePlugin.random);
		try
		{
			Horse.Color color = Horse.Color.valueOf(colorName.toUpperCase());
			horse.setColor(color);
		}
		catch (IllegalArgumentException e)
		{
			MLog.severe("Unknown horse color: " + colorName);
		}
		
		String styleName = NodeParser.pickNodeChance((MemorySection) section.get("Styles"), MantlePlugin.random);
		try
		{
			Horse.Style style = Horse.Style.valueOf(styleName.toUpperCase());
			horse.setStyle(style);
		}
		catch (IllegalArgumentException e)
		{
			MLog.severe("Unknown horse style: " + styleName);
		}
		
		String variantName = NodeParser.pickNodeChance((MemorySection) section.get("Variants"), MantlePlugin.random);
		try
		{
			Horse.Variant variant = Horse.Variant.valueOf(variantName.toUpperCase());
			horse.setVariant(variant);
		}
		catch (IllegalArgumentException e)
		{
			MLog.severe("Unknown horse variant: " + variantName);
		}
		
		horse.setMaxHealth(15.0 + MantlePlugin.random.nextInt(8) + MantlePlugin.random.nextInt(9));
		
		if (horse.getVariant() == Variant.DONKEY || horse.getVariant() == Variant.MULE)
			horse.setJumpStrength(0.5);
		else
			horse.setJumpStrength(0.4 + MantlePlugin.random.nextInt(2) * 0.2 + MantlePlugin.random.nextInt(2) * 0.2 + CorePlugin.random.nextInt(2)* 0.2);

		AttributeInstance attributes = ((EntityInsentient)((CraftLivingEntity)horse).getHandle()).getAttributeInstance(GenericAttributes.d);
        		
		if (horse.getVariant() == Variant.DONKEY || horse.getVariant() == Variant.MULE)
			attributes.setValue(0.1);
		else
			attributes.setValue(0.25 * (0.45 + MantlePlugin.random.nextInt(2) * 0.3 + MantlePlugin.random.nextInt(2) * 0.3 + CorePlugin.random.nextInt(2) * 0.3));
	}

	private static void handleRabbit(MemorySection section, Entity entity)
	{
		Rabbit rabbit = (Rabbit) entity;

		String rabbitTypeName = NodeParser.pickNodeChance((MemorySection) section.get("Types"), MantlePlugin.random);
		Rabbit.Type rabbitType = (Rabbit.Type) Util.findEnum(Rabbit.Type.values(), rabbitTypeName);
		if (rabbitType != null)
		{
			rabbit.setRabbitType(rabbitType);
		}
		else
		{
			MLog.severe("Unknown rabbit type: " + rabbitTypeName);
		}
	}
}
