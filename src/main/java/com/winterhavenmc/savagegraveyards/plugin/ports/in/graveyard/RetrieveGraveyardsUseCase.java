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

package com.winterhavenmc.savagegraveyards.plugin.ports.in.graveyard;

import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface RetrieveGraveyardsUseCase
{
	/**
	 * Get record
	 *
	 * @param displayName the name of the Valid to be retrieved
	 * @return Valid object or null if no matching record
	 */
	Graveyard selectGraveyard(final String displayName); //TODO: this finds by search key


	/**
	 * get all graveyard records
	 *
	 * @return List of all graveyard objects in alphabetical order
	 */
	List<Graveyard> selectAllGraveyards();


	/**
	 * get all valid graveyard records
	 *
	 * @return List of all graveyard objects in alphabetical order
	 */
	List<Graveyard.Valid> selectAllValidGraveyards();


	/**
	 * Get undiscovered graveyards for player
	 *
	 * @param player the player for whom to retrieve undiscovered Graveyards
	 * @return HashSet of Valid objects that are undiscovered for player
	 */
	Set<Graveyard.Valid> selectUndiscoveredGraveyards(final Player player);


	/**
	 * Gets closest graveyard to player's current location.
	 *
	 * @param player the player for whom to retrieve the nearest Valid
	 * @return Valid object
	 */
	Optional<Graveyard.Valid> selectNearestGraveyard(final Player player);


	List<Graveyard.Valid> selectNearestGraveyards(Player player);


	/**
	 * Get undiscovered graveyard keys for player
	 *
	 * @param player the player for whom to retrieve undiscovered Valid keys
	 * @return HashSet of Valid search keys that are undiscovered for player
	 */
	Collection<String> selectUndiscoveredKeys(final Player player);


	/**
	 * Get records that prefix match string
	 *
	 * @param match the prefix to match
	 * @return String collection of names with matching prefix
	 */
	List<String> selectMatchingGraveyardNames(final String match);

}
