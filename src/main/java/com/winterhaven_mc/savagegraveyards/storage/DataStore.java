package com.winterhaven_mc.savagegraveyards.storage;

import com.winterhaven_mc.savagegraveyards.PluginMain;
import org.bukkit.entity.Player;

import java.util.*;


/**
 * Abstract datastore class
 */
public abstract class DataStore {

	private final static PluginMain plugin = PluginMain.instance;

	private boolean initialized;
	DataStoreType type;
	String filename;


	/**
	 * Initialize storage
	 *
	 * @throws Exception if datastore cannot be initialized
	 */
	abstract void initialize() throws Exception;


	/**
	 * get all graveyard records
	 *
	 * @return List of all graveyard objects in alphabetical order
	 */
	public abstract List<Graveyard> selectAllGraveyards();


	/**
	 * Get record
	 *
	 * @param displayName the name of the Graveyard to be retrieved
	 * @return Graveyard object or null if no matching record
	 */
	public abstract Graveyard selectGraveyard(final String displayName);


	/**
	 * Get undiscovered graveyards for player
	 *
	 * @param player the player for whom to retrieve undiscovered Graveyards
	 * @return HashSet of Graveyard objects that are undiscovered for player
	 */
	public abstract Set<Graveyard> getUndiscovered(final Player player);


	/**
	 * Get undiscovered graveyard keys for player
	 *
	 * @param player the player for whom to retrieve undiscovered Graveyard keys
	 * @return HashSet of Graveyard search keys that are undiscovered for player
	 */
	public abstract Set<String> getUndiscoveredKeys(final Player player);


	/**
	 * Gets closest graveyard to player's current location
	 *
	 * @param player the player for whom to retrieve the nearest Graveyard
	 * @return Graveyard object
	 */
	public abstract Graveyard selectNearestGraveyard(final Player player);


	/**
	 * Set graveyard to discovered for player
	 *
	 * @param player      the player for whom to set a Graveyard as discovered
	 * @param displayName display name or search key of the Graveyard to set as discovered
	 */
	public abstract void insertDiscovery(final Player player, final String displayName);


	/**
	 * Insert new record
	 *
	 * @param graveyard the Graveyard object to insert into the datastore
	 */
	public abstract void insertGraveyard(final Graveyard graveyard);


	/**
	 * Update record
	 *
	 * @param graveyard the Graveyard to update in the datastore
	 */
	public abstract void updateGraveyard(final Graveyard graveyard);


	/**
	 * Delete record
	 *
	 * @param displayName display name or search key of record to be deleted
	 * @return Deleted graveyard record
	 */
	public abstract Graveyard deleteGraveyard(final String displayName);


	/**
	 * Delete discovery record
	 *
	 * @param displayName display name or search key of record to be deleted
	 */
	public abstract boolean deleteDiscovery(final String displayName, final UUID playerUUID);


	/**
	 * Select player UUIDs that have discovered a graveyard
	 *
	 * @param displayName the display name or search key of the graveyard
	 * @return List of UUID - player UUIDs that have discovered the graveyard
	 */
	public abstract List<UUID> selectPlayersDiscovered(final String displayName);


	/**
	 * Close datastore connection
	 */
	public abstract void close();


	/**
	 * Sync datastore to disk if supported
	 */
	abstract void sync();


	/**
	 * Delete datastore
	 */
	@SuppressWarnings("UnusedReturnValue")
	abstract boolean delete();


	/**
	 * Check that datastore exists
	 *
	 * @return true if datastore exists, false if it does not
	 */
	abstract boolean exists();


	/**
	 * Get datastore filename or equivalent
	 *
	 * @return datastore filename
	 */
	String getFilename() {
		return this.filename;
	}


	/**
	 * Get datastore type
	 *
	 * @return Enum value of DataStoreType
	 */
	private DataStoreType getType() {
		return this.type;
	}


	/**
	 * Get datastore name
	 *
	 * @return String containing datastore name
	 */
	@Override
	public String toString() {
		return this.getType().toString();
	}


	/**
	 * Get datastore initialized field
	 *
	 * @return true if datastore is initialized, false if it is not
	 */
	boolean isInitialized() {
		return this.initialized;
	}


	/**
	 * Set initialized field
	 *
	 * @param initialized the initialized value of the datastore
	 */
	void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}


	/**
	 * Get records that prefix match string
	 *
	 * @param match the prefix to match
	 * @return String list of names with matching prefix
	 */
	public abstract List<String> selectMatchingGraveyardNames(final String match);


	/**
	 * Create new data store of given type.<br>
	 * No parameter version used when no current datastore exists
	 * and datastore type should be read from configuration
	 *
	 * @return new datastore of configured type
	 */
	public static DataStore create() {

		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(plugin.getConfig().getString("storage-type"));
		if (dataStoreType == null) {
			dataStoreType = DataStoreType.getDefaultType();
		}
		return create(dataStoreType, null);
	}


	/**
	 * Create new data store of given type and convert old data store.<br>
	 * Two parameter version used when a datastore instance already exists
	 *
	 * @param dataStoreType new datastore type
	 * @param oldDataStore  existing datastore reference
	 * @return a new datastore instance of the given type
	 */
	private static DataStore create(final DataStoreType dataStoreType, final DataStore oldDataStore) {

		// get new data store of specified type
		DataStore newDataStore = dataStoreType.create();

		// initialize new data store
		try {
			newDataStore.initialize();
		}
		catch (Exception e) {
			plugin.getLogger().severe("Could not initialize " + newDataStore.toString() + " datastore!");
			if (plugin.debug) {
				e.printStackTrace();
			}
		}

		// if old data store was passed, convert to new data store
		if (oldDataStore != null) {
			convert(oldDataStore, newDataStore);
		}
		else {
			convertAll(newDataStore);
		}
		// return initialized data store
		return newDataStore;
	}


	public static void reload() {

		// get current datastore type
		DataStoreType currentType = plugin.dataStore.getType();

		// get configured datastore type
		DataStoreType newType = DataStoreType.match(plugin.getConfig().getString("storage-type"));

		// if current datastore type does not match configured datastore type, create new datastore
		if (!currentType.equals(newType)) {

			// create new datastore
			plugin.dataStore = create(newType, plugin.dataStore);
		}

	}


	/**
	 * convert old data store to new data store
	 *
	 * @param oldDataStore the old datastore to convert from
	 * @param newDataStore the new datastore to convert to
	 */
	private static void convert(final DataStore oldDataStore, final DataStore newDataStore) {

		// if datastores are same type, do not convert
		if (oldDataStore.getType().equals(newDataStore.getType())) {
			return;
		}

		// if old datastore file exists, attempt to read all records
		if (oldDataStore.exists()) {

			plugin.getLogger().info("Converting existing " + oldDataStore.toString() + " datastore to "
					+ newDataStore.toString() + " datastore...");

			// initialize old datastore if necessary
			if (!oldDataStore.isInitialized()) {
				try {
					oldDataStore.initialize();
				}
				catch (Exception e) {
					plugin.getLogger().warning("Could not initialize "
							+ oldDataStore.toString() + " datastore for conversion.");
					plugin.getLogger().warning(e.getLocalizedMessage());
					return;
				}
			}

			final List<Graveyard> allRecords = oldDataStore.selectAllGraveyards();

			int count = 0;
			for (Graveyard record : allRecords) {
				newDataStore.insertGraveyard(record);
				count++;
			}
			plugin.getLogger().info(count + " records converted to " + newDataStore.toString() + " datastore.");

			newDataStore.sync();

			oldDataStore.close();
			oldDataStore.delete();
		}
	}


	/**
	 * convert all existing data stores to new data store
	 *
	 * @param newDataStore the new datastore to convert all other existing datastores into
	 */
	private static void convertAll(final DataStore newDataStore) {

		// get array list of all data store types
		ArrayList<DataStoreType> dataStores = new ArrayList<>(Arrays.asList(DataStoreType.values()));

		// remove newDataStore from list of types to convert
		dataStores.remove(newDataStore.getType());

		for (DataStoreType type : dataStores) {

			// create oldDataStore holder
			DataStore oldDataStore = null;

			if (type.equals(DataStoreType.SQLITE)) {
				oldDataStore = new DataStoreSQLite(plugin);
			}

			// add additional datastore types here as they become available

			if (oldDataStore != null) {
				convert(oldDataStore, newDataStore);
			}
		}
	}

}
