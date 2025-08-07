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

package com.winterhavenmc.savagegraveyards.plugin.tasks;

import com.winterhavenmc.savagegraveyards.plugin.PluginMain;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.plugin.util.Config;
import com.winterhavenmc.savagegraveyards.plugin.util.Macro;
import com.winterhavenmc.savagegraveyards.plugin.util.MessageId;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.winterhavenmc.library.TimeUnit.SECONDS;


/**
 * Cancel mob targeting of players for configured period after respawn
 */
public final class SafetyManager
{
	// reference to main class
	private final PluginMain plugin;

	// safety cooldown map
	private final Map<UUID, BukkitRunnable> safetyCooldownMap;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public SafetyManager(final PluginMain plugin)
	{
		// set reference to main class
		this.plugin = plugin;

		// instantiate safety cooldown map
		safetyCooldownMap = new ConcurrentHashMap<>();
	}


	/**
	 * Insert player uuid into safety cooldown map
	 *
	 * @param player    the player whose uuid will be used as key in the safety cooldown map
	 * @param graveyard the graveyard where the player has respawned
	 */
	public void putPlayer(final Player player, Graveyard.Valid graveyard)
	{
		// get safety time from graveyard attributes
		Duration safetyDuration = graveyard.attributes().safetyTime().value();

		// if safetyTime is non-zero, run safetyTask
		if (!safetyDuration.isZero())
		{
			// if graveyard safetyTime is negative, use default from config.yml
			if (safetyDuration.isNegative())
			{
				safetyDuration = Config.SAFETY_TIME.getSeconds(plugin.getConfig());
			}

			// send player message
			plugin.messageBuilder.compose(player, MessageId.SAFETY_COOLDOWN_START)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.setMacro(Macro.DURATION, safetyDuration)
					.send();

			// create task to display message and remove player from safety map after safetyTime duration
			BukkitRunnable safetyTask = new SafetyTask(plugin, player);

			// schedule task to display safety expired message after configured amount of time
			safetyTask.runTaskLater(plugin, SECONDS.toTicks(safetyDuration.toSeconds()));

			// if player is already in cooldown map, cancel existing task
			if (isPlayerProtected(player))
			{
				safetyCooldownMap.get(player.getUniqueId()).cancel();
			}

			// add player to safety cooldown map
			safetyCooldownMap.put(player.getUniqueId(), safetyTask);
		}
	}


	/**
	 * Remove player from safety cooldown map
	 *
	 * @param player the player to be removed from the safety cooldown map
	 */
	public void removePlayer(final Player player)
	{
		safetyCooldownMap.remove(player.getUniqueId());
	}


	/**
	 * Check if player is in safety cooldown map
	 *
	 * @param player the player to test if in the safety cooldown map
	 * @return {@code true} if the player is in the safety cooldown map, {@code false} if they are not
	 */
	public boolean isPlayerProtected(final Player player)
	{
		return safetyCooldownMap.containsKey(player.getUniqueId());
	}

}
