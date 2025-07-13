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

package com.winterhavenmc.savagegraveyards.plugin.storage;

import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.SearchKey;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface GraveyardDatastore
{
	Graveyard insertGraveyard(Graveyard.Valid graveyard);


	/**
	 * Update record
	 *
	 * @param graveyard the Valid to update in the datastore
	 * @return the graveyard
	 */
	Graveyard updateGraveyard(Graveyard.Valid graveyard);


	/**
	 * Delete record
	 *
	 * @param searchKey display name or search key of record to be deleted
	 * @return Deleted graveyard record
	 */
	Graveyard deleteGraveyard(SearchKey.Valid searchKey);


	/**
	 * Insert a collection of records
	 *
	 * @param graveyards a collection of graveyard records
	 * @return int - the number of records successfully inserted
	 */
	int insertGraveyards(Collection<Graveyard.Valid> graveyards);


	/**
	 * Get record
	 *
	 * @param searchKey the name of the Valid to be retrieved
	 * @return Valid object or null if no matching record
	 */
	Graveyard selectGraveyard(SearchKey.Valid searchKey);


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
	Stream<Graveyard.Valid> selectUndiscoveredGraveyards(Player player);


	/**
	 * Gets closest graveyard to player's current location
	 *
	 * @param player the player for whom to retrieve the nearest Valid
	 * @return Valid object
	 */
	Optional<Graveyard.Valid> selectNearestGraveyard(Player player);


	List<Graveyard.Valid> selectNearestGraveyards(Player player);

	/**
	 * Get graveyard names that prefix match string
	 *
	 * @param prefix the prefix to match
	 * @return String collection of names with matching prefix
	 */
	List<String> selectMatchingGraveyardNames(String prefix);


	/**
	 * Get graveyard searchKeys that prefix match string
	 *
	 * @param prefix the prefix to match
	 * @return String collection of names with matching prefix
	 */
	List<String> selectMatchingGraveyardKeys(String prefix);


	/**
	 * Get undiscovered graveyard keys for player
	 *
	 * @param player the player for whom to retrieve undiscovered Valid keys
	 * @return HashSet of Valid search keys that are undiscovered for player
	 */
	List<String> selectUndiscoveredKeys(Player player);


	/**
	 * Select a count of graveyards in the datastore
	 *
	 * @return the count of graveyard records in the datastore
	 */
	int selectGraveyardCount();
}
