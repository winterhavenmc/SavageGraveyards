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

package com.winterhavenmc.savagegraveyards.plugin.storage;

import com.winterhavenmc.savagegraveyards.plugin.PluginMain;
import com.winterhavenmc.savagegraveyards.plugin.util.Config;

import org.bukkit.plugin.Plugin;


/**
 * DataStore interface
 */
public interface DataStore extends GraveyardDatastore, DiscoveryDatastore
{
	/**
	 * Create new data store of given type and convert old data store.<br>
	 * Two parameter version used when a datastore instance already exists
	 *
	 * @param plugin reference to plugin main class
	 * @return a new datastore instance of the given type
	 */
	static DataStore connect(final Plugin plugin)
	{
		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(Config.STORAGE_TYPE.getString(plugin.getConfig()));

		// get new data store of specified type
		DataStore newDataStore = dataStoreType.connect(plugin);

		// initialize new data store
		try {
			newDataStore.initialize();
		}
		catch (Exception e)
		{
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
		if (!currentType.equals(newType))
		{
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

}
