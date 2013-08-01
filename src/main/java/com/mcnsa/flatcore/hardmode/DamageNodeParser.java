package com.mcnsa.flatcore.hardmode;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gadberry.utility.expression.ArgumentCastException;
import com.gadberry.utility.expression.Expression;
import com.gadberry.utility.expression.InvalidExpressionException;
import com.mcnsa.flatcore.FCLog;
import com.mcnsa.flatcore.MCNSAFlatcore;
import com.mcnsa.flatcore.NodeParser;

public class DamageNodeParser extends NodeParser {
	private EntityDamageEvent event;
	private String eventName;
	YamlConfiguration config;
	
	private DamageNodeParser(EntityDamageEvent event, String eventName, YamlConfiguration config)
	{
		this.eventName = eventName;
		this.event = event;
		this.config = config;
	}

	private void parse()
	{
		List<?> node = (List<?>) config.getList("DamageModifiers." + eventName);
		if (node == null)
			return;

		parseNodeList(node);
	}

	public static void parseDamageEvent(EntityDamageEvent event, String eventName, YamlConfiguration config)
	{
		new DamageNodeParser(event, eventName, config).parse();
	}	

	public static void parseDamageEvent(EntityDamageEvent event, YamlConfiguration config)
	{
		parseDamageEvent(event, event.getCause().toString(), config);
	}

	@Override
	protected void parseNode(String type, LinkedHashMap<?, ?> node) {
		if (type.equalsIgnoreCase("setdamage"))
			parseSetDamage(node);
		else if (type.equalsIgnoreCase("adddamage"))
			parseAddDamage(node);
		else if (type.equalsIgnoreCase("multiplydamage"))
			parseMultiplyDamage(node);
		else if (type.equalsIgnoreCase("addpotioneffect"))
			parseAddPotionEffect(node);
	}


	private void parseSetDamage(LinkedHashMap<?,?> node)
	{
		Number amount = (Number) node.get("amount");

		if (amount == null)
		{
			FCLog.warning("Invalid Damage modifiers config! Set amount is missing!");
			return;

		}

		event.setDamage(amount.doubleValue());
	}

	private void parseAddDamage(LinkedHashMap<?,?> node)
	{
		Number amount = (Number) node.get("amount");

		if (amount == null)
		{
			FCLog.warning("Invalid Damage modifiers config! Add amount is missing!");
			return;

		}

		event.setDamage(event.getDamage() + amount.doubleValue());
	}


	private void parseMultiplyDamage(LinkedHashMap<?,?> node)
	{
		Number amount = (Number) node.get("amount");

		if (amount == null)
		{
			FCLog.warning("Invalid Damage modifiers config! Multiply amount is missing!");
			return;
		}

		event.setDamage((int) (event.getDamage() * amount.doubleValue()));
	}

	private void parseAddPotionEffect(LinkedHashMap<?,?> node)
	{
		//Protect admins against evil features
		if (event.getEntity() instanceof Player && ((Player) event.getEntity()).getGameMode() == GameMode.CREATIVE )
		{
			return;
		}

		final Integer id = (Integer) node.get("id");
		if (id == null)
		{
			FCLog.warning("Invalid Damage modifiers config! Effect id is missing!");
			return;
		}


		Object durationNode = node.get("duration");
		int duration = 0;
		if (durationNode == null)
		{
			FCLog.warning("Invalid Damage modifiers config! Effect duration is missing!");
			return;
		}
		else if (durationNode instanceof Integer)
		{
			duration = ((Integer) durationNode).intValue();
		}
		else if (durationNode instanceof String)
		{
			String expression = (String) durationNode;
			expression = expression.replace("damage", Double.toString(event.getDamage()));

			try {
				duration = Expression.evaluate(expression).toInteger();
			} catch (ArgumentCastException e) {
				e.printStackTrace();
			} catch (InvalidExpressionException e) {
				FCLog.warning("Invalid Damage modifiers config! Effect duration expression is invalid!");
				return;
			}
		}
		else
		{
			FCLog.warning("Invalid Damage modifiers config! Effect duration is invalid!");
			return;
		}


		Object amplifierNode = node.get("amplifier");
		int amplifier = 0;
		if (amplifierNode == null)
		{
			FCLog.warning("Invalid Damage modifiers config! Effect duration is missing!");
			return;
		}
		else if (amplifierNode instanceof Integer)
		{
			amplifier = ((Integer) amplifierNode).intValue();
		}
		else if (amplifierNode instanceof String)
		{
			String expression = (String) amplifierNode;
			expression = expression.replace("damage", Double.toString(event.getDamage()));

			try {
				amplifier = Expression.evaluate(expression).toInteger();
			} catch (ArgumentCastException e) {
				e.printStackTrace();
			} catch (InvalidExpressionException e) {
				FCLog.warning("Invalid Damage modifiers config! Effect amplifier expression is invalid!");
				return;
			}			
		}
		else
		{
			FCLog.warning("Invalid Damage modifiers config! Effect amplifier is invalid!");
			return;
		}

		final int fAmplifier = amplifier;
		final int fDuration = duration;

		final Boolean ambient = (Boolean) node.get("ambient");

		Bukkit.getScheduler().scheduleSyncDelayedTask(MCNSAFlatcore.instance, new Runnable() {
			@Override
			public void run() {
				((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.getById(id), fDuration, fAmplifier, ambient == null ? false : ambient));
			}
		});

	}


}