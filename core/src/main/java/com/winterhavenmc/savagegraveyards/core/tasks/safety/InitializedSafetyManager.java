/*
 * Copyright (c) 2022-2025 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.savagegraveyards.core.tasks.safety;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.savagegraveyards.core.context.SafetyCtx;
import com.winterhavenmc.savagegraveyards.core.util.Config;
import com.winterhavenmc.savagegraveyards.core.util.Macro;
import com.winterhavenmc.savagegraveyards.core.util.MessageId;
import com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard;

import com.winterhavenmc.library.messagebuilder.models.time.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Cancel mob targeting of players for configured period after respawn
 */
public final class InitializedSafetyManager implements ValidSafetyManager
{
	private final Plugin plugin;
	private final MessageBuilder messageBuilder;

	private final Map<UUID, BukkitRunnable> safetyCooldownMap;


	/**
	 * Class constructor
	 */
	public InitializedSafetyManager(final Plugin plugin, final MessageBuilder messageBuilder)
	{
		this.plugin = plugin;
		this.messageBuilder = messageBuilder;

		this.safetyCooldownMap = new HashMap<>();
	}


	InitializedSafetyManager(final SafetyCtx ctx)
	{
		this.plugin = ctx.plugin();
		this.messageBuilder = ctx.messageBuilder();

		this.safetyCooldownMap = new HashMap<>();
	}


	/**
	 * Insert player uuid into safety cooldown map
	 *
	 * @param player    the player whose uuid will be used as key in the safety cooldown map
	 * @param graveyard the graveyard where the player has respawned
	 */
	public void put(final Player player, final ValidGraveyard graveyard)
	{
		// get safety time from graveyard attributes
		Duration safetyDuration = graveyard.attributes().safetyTime().value();

		// if safetyTime is zero, do nothing and return
		if (safetyDuration.isZero())
		{
			return;
		}
		// if graveyard safetyTime is negative, use configured default
		else if (safetyDuration.isNegative())
		{
			safetyDuration = Config.SAFETY_TIME.getSeconds(plugin.getConfig());
		}

		// send player message
		messageBuilder.compose(player, MessageId.SAFETY_COOLDOWN_START)
				.setMacro(Macro.GRAVEYARD, graveyard)
				.setMacro(Macro.DURATION, safetyDuration)
				.send();

		// create task to display message and remove player from safety map after safetyTime duration
		BukkitRunnable safetyTask = new SafetyTask(this, messageBuilder, player);

		// schedule task to display safety expired message after configured amount of time
		safetyTask.runTaskLater(plugin, TimeUnit.SECONDS.toTicks(safetyDuration.toSeconds()));

		// if player is already in cooldown map, cancel existing task
		if (isProtected(player))
		{
			safetyCooldownMap.get(player.getUniqueId()).cancel();
		}

		// add player to safety cooldown map
		safetyCooldownMap.put(player.getUniqueId(), safetyTask);
	}


	/**
	 * Remove player from safety cooldown map
	 *
	 * @param player the player to be removed from the safety cooldown map
	 */
	public void remove(final Player player)
	{
		safetyCooldownMap.remove(player.getUniqueId());
	}


	/**
	 * Check if player is in safety cooldown map
	 *
	 * @param player the player to test if in the safety cooldown map
	 * @return {@code true} if the player is in the safety cooldown map, {@code false} if they are not
	 */
	public boolean isProtected(final Player player)
	{
		return safetyCooldownMap.containsKey(player.getUniqueId());
	}

}
