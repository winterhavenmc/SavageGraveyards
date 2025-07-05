/*
 * Copyright (c) 2022 Tim Savage.
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

package com.winterhavenmc.savagegraveyards.storage;

import com.winterhavenmc.savagegraveyards.PluginMain;

import com.winterhavenmc.savagegraveyards.models.discovery.Discovery;
import com.winterhavenmc.savagegraveyards.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.util.Config;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import java.util.*;


/**
 * DataStore interface
 */
public interface DataStore
{
	/**
	 * Create new data store of given type and convert old data store.<br>
	 * Two parameter version used when a datastore instance already exists
	 *
	 * @param plugin reference to plugin main class
	 * @return a new datastore instance of the given type
	 */
	static DataStore connect(final JavaPlugin plugin)
	{
		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(Config.STORAGE_TYPE.getString(plugin.getConfig()));

		// get new data store of specified type
		DataStore newDataStore = dataStoreType.connect(plugin);

		// initialize new data store
		try {
			newDataStore.initialize();
		}
		catch (Exception e) {
			plugin.getLogger().severe("Could not initialize " + newDataStore + " datastore!");
			plugin.getLogger().severe(e.getLocalizedMessage());
		}

		// convert any existing data stores to new type
		DataStoreType.convertAll(plugin, newDataStore);

		// return initialized data store
		return newDataStore;
	}


	/**
	 * Reload data store if configured type has changed
	 *
	 * @param plugin reference to plugin main class
	 */
	static void reload(final PluginMain plugin)
	{
		// get current datastore type
		DataStoreType currentType = plugin.dataStore.getType();

		// get configured datastore type
		DataStoreType newType = DataStoreType.match(Config.STORAGE_TYPE.getString(plugin.getConfig()));

		// if current datastore type does not match configured datastore type, create new datastore
		if (!currentType.equals(newType)) {

			// create new datastore
			plugin.dataStore = connect(plugin);
		}
	}


	/**
	 * Initialize storage
	 *
	 * @throws Exception if datastore cannot be initialized
	 */
	void initialize() throws Exception;


	/**
	 * Check if datastore is initialized
	 *
	 * @return boolean true if datastore is initialized, false if not
	 */
	boolean isInitialized();


	/**
	 * Get data store type
	 *
	 * @return the datastore type
	 */
	DataStoreType getType();


	/**
	 * Close datastore connection
	 */
	void close();


	/**
	 * Sync datastore to disk if supported
	 */
	void sync();


	/**
	 * Delete datastore
	 */
	@SuppressWarnings("UnusedReturnValue")
	boolean delete();


	/**
	 * get all graveyard records
	 *
	 * @return List of all graveyard objects in alphabetical order
	 */
	Collection<Graveyard.Valid> selectAllGraveyards();


	/**
	 * Get record
	 *
	 * @param displayName the name of the Valid to be retrieved
	 * @return Valid object or null if no matching record
	 */
	Graveyard selectGraveyard(final String displayName);


	/**
	 * Get undiscovered graveyards for player
	 *
	 * @param player the player for whom to retrieve undiscovered Graveyards
	 * @return HashSet of Valid objects that are undiscovered for player
	 */
	Collection<Graveyard.Valid> selectUndiscoveredGraveyards(final Player player);


	/**
	 * Get undiscovered graveyard keys for player
	 *
	 * @param player the player for whom to retrieve undiscovered Valid keys
	 * @return HashSet of Valid search keys that are undiscovered for player
	 */
	Collection<String> selectUndiscoveredKeys(final Player player);


	/**
	 * Gets closest graveyard to player's current location
	 *
	 * @param player the player for whom to retrieve the nearest Valid
	 * @return Valid object
	 */
	Optional<Graveyard.Valid> selectNearestGraveyard(final Player player);


	/**
	 * Get records that prefix match string
	 *
	 * @param match the prefix to match
	 * @return String collection of names with matching prefix
	 */
	List<String> selectMatchingGraveyardNames(final String match);


	/**
	 * Insert discovery record
	 *
	 * @param record the discovery record to be inserted
	 */
	void insertDiscovery(final Discovery.Valid record);


	/**
	 * Insert discovery records
	 *
	 * @param discoveries collection of valid records to be inserted
	 * @return number of records successfully inserted
	 */
	int insertDiscoveries(final Collection<Discovery.Valid> discoveries);

//	int insertDiscoveries(final Collection<ValidDiscovery> insertSet);


	/**
	 * Insert a collection of records
	 *
	 * @param graveyards a collection of graveyard records
	 * @return int - the number of records successfully inserted
	 */
	int insertGraveyards(final Collection<Graveyard.Valid> graveyards);


	/**
	 * Update record
	 *
	 * @param graveyard the Valid to update in the datastore
	 */
	void updateGraveyard(final Graveyard.Valid graveyard);


	/**
	 * Delete record
	 *
	 * @param displayName display name or search key of record to be deleted
	 * @return Deleted graveyard record
	 */
	Graveyard deleteGraveyard(final String displayName);


	/**
	 * Delete discovery record
	 *
	 * @param displayName display name or search key of record to be deleted
	 * @param playerUid the player unique id
	 * @return boolean - {@code true} if deletion was successful, {@code false} if not
	 */
	boolean deleteDiscovery(final String displayName, final UUID playerUid);


	/**
	 * select graveyard keys that player has discovered
	 *
	 * @param playerUid the player uid to query
	 * @return Collection of String - graveyard keys
	 */
	Collection<String> selectDiscoveredKeys(final UUID playerUid);


	/**
	 * Select players who have discovered any graveyards
	 *
	 * @return Collection of String - player names with discovered graveyards
	 */
	Collection<String> selectPlayersWithDiscoveries();


	/**
	 * Select a count of graveyards in the datastore
	 *
	 * @return the count of graveyard records in the datastore
	 */
	int selectGraveyardCount();

}
