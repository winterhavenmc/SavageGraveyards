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

package com.winterhavenmc.savagegraveyards.plugin.ports.datastore;

import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.SearchKey;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;


public interface GraveyardRepository
{
	/**
	 * Get record
	 *
	 * @param searchKey the name of the Valid to be retrieved
	 * @return Valid object or null if no matching record
	 */
	Graveyard get(SearchKey.Valid searchKey);


	/**
	 * Get all graveyard records
	 *
	 * @return {@link Stream} of graveyard records
	 */
	Stream<Graveyard> getAll();


	/**
	 * Select a count of graveyards in the datastore
	 *
	 * @return the count of graveyard records in the datastore
	 */
	int getCount();


	/**
	 * Save graveyard record
	 *
	 * @return the saved graveyard record
	 */
	Graveyard save(Graveyard.Valid graveyard);


	/**
	 * Gets closest graveyard to player's current location
	 *
	 * @param player the player for whom to retrieve the nearest Valid
	 * @return Valid object
	 */
	Optional<Graveyard.Valid> getNearestGraveyard(Player player);


	/**
	 * Returns a list of enabled, valid graveyards in the player's current world for which
	 * the player has permission, returned in order of proximity to the player's location.
	 *
	 * @param player the player whose location is used as the origin, and permissions are checked
	 * @return a list of graveyards that match the criteria
	 */
	List<Graveyard.Valid> getNearestGraveyards(Player player);


	/**
	 * Retrieves a list of graveyard names that match a given prefix.
	 * Matches are case-insensitive, and match against stored searchKeys while treating
	 * spaces and underscores as equivalent.
	 * <p>
	 * This method is used by command TabCompleter methods th return a list of graveyard
	 * names that match a partially completed name prefix.
	 * <p>
	 * This is currently (11-Jul_2025) the only method that uses the <em>SelectGraveyardNamesMatchingPrefix</em> query.
	 *
	 * @param prefix the prefix to match
	 * @return List of Strings containing graveyard names matched by prefix
	 */
	List<String> getMatchingNames(String prefix);


	/**
	 * Get graveyard searchKeys that prefix match string
	 *
	 * @param prefix the prefix to match
	 * @return String collection of names with matching prefix
	 */
	List<String> getMatchingKeys(String prefix);


	/**
	 * Get undiscovered graveyards for player
	 *
	 * @param player the player for whom to retrieve undiscovered Graveyards
	 * @return Stream of Valid objects that are undiscovered for player
	 */
	Stream<Graveyard.Valid> getUndiscoveredGraveyards(Player player);


	/**
	 * Get undiscovered graveyard keys for player
	 *
	 * @param player the player for whom to retrieve undiscovered Valid keys
	 * @return HashSet of Valid search keys that are undiscovered for player
	 */
	Set<String> getUndiscoveredKeys(CommandSender player);


	@SuppressWarnings("UnusedReturnValue")
	Graveyard update(Graveyard.Valid graveyard);


	Graveyard update(SearchKey.Valid searchKey, Graveyard.Valid graveyard);


	/**
	 * Delete record
	 *
	 * @param searchKey display name or search key of record to be deleted
	 * @return Deleted graveyard record
	 */
	Graveyard delete(SearchKey.Valid searchKey);
}
