/*
 * Copyright (c) 2025 Tim Savage.
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

import com.winterhavenmc.savagegraveyards.core.SavageGraveyardsPluginController;
import com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard;
import org.bukkit.entity.Player;


public interface SafetyManager
{
	static RespawnSafetyManager create()
	{
		return new RespawnSafetyManager();
	}

	SafetyManager init(SavageGraveyardsPluginController.SafetyContextContainer safetyCtx);

	/**
	 * Insert player uuid into safety cooldown map
	 *
	 * @param player    the player whose uuid will be used as key in the safety cooldown map
	 * @param graveyard the graveyard where the player has respawned
	 */
	void put(Player player, ValidGraveyard graveyard);

	/**
	 * Remove player from safety cooldown map
	 *
	 * @param player the player to be removed from the safety cooldown map
	 */
	void remove(Player player);

	/**
	 * Check if player is in safety cooldown map
	 *
	 * @param player the player to test if in the safety cooldown map
	 * @return {@code true} if the player is in the safety cooldown map, {@code false} if they are not
	 */
	boolean isProtected(Player player);
}
