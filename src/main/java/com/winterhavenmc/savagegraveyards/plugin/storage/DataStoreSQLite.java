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

import com.winterhavenmc.savagegraveyards.plugin.models.discovery.Discovery;
import com.winterhavenmc.savagegraveyards.plugin.models.discovery.DiscoveryReason;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.DisplayName;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.GraveyardReason;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.SearchKey;
import com.winterhavenmc.savagegraveyards.plugin.storage.sqlite.DiscoveryMapper;
import com.winterhavenmc.savagegraveyards.plugin.storage.sqlite.DiscoveryQueryHandler;
import com.winterhavenmc.savagegraveyards.plugin.storage.sqlite.GraveyardMapper;
import com.winterhavenmc.savagegraveyards.plugin.storage.sqlite.GraveyardQueryHandler;
import com.winterhavenmc.savagegraveyards.plugin.util.Config;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.stream.Stream;

import static com.winterhavenmc.savagegraveyards.plugin.models.graveyard.GraveyardReason.*;


/**
 * Concrete SQLite datastore class
 */
final class DataStoreSQLite extends DataStoreAbstract implements DataStore
{
	private final Plugin plugin;
	private Connection connection;
	private final String dataFilePath;
	private int schemaVersion;
	private final GraveyardQueryHandler graveyardQueryHandler = new GraveyardQueryHandler();
	private final DiscoveryQueryHandler discoveryQueryHandler = new DiscoveryQueryHandler();
	private final GraveyardMapper graveyardMapper = new GraveyardMapper();
	private final DiscoveryMapper discoveryMapper = new DiscoveryMapper();


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public DataStoreSQLite(final Plugin plugin)
	{
		// reference to main class
		this.plugin = plugin;

		// set datastore type
		this.type = DataStoreType.SQLITE;

		// set datastore file path
		this.dataFilePath = plugin.getDataFolder() + File.separator + type.getStorageName();
	}


	@Override
	public void initialize() throws SQLException, ClassNotFoundException
	{
		// if data store is already initialized, do nothing and return
		if (this.isInitialized())
		{
			plugin.getLogger().info(SQLiteNotice.ALREADY_INITIALIZED.toString());
			return;
		}

		// register the driver
		final String jdbcDriverName = "org.sqlite.JDBC";

		Class.forName(jdbcDriverName);

		// create database url
		String jdbc = "jdbc:sqlite";
		String dbUrl = jdbc + ":" + dataFilePath;

		// create a database connection
		connection = DriverManager.getConnection(dbUrl);

		// enable foreign keys
		enableForeignKeys();

		// update schema if necessary
		updateSchema();

		// set initialized true
		setInitialized(true);
		plugin.getLogger().info(this + " datastore initialized.");
	}


	private void enableForeignKeys() throws SQLException
	{
		// create statement
		Statement statement = connection.createStatement();

		// enable foreign keys
		statement.executeUpdate(Queries.getQuery("EnableForeignKeys"));

		if (Config.DEBUG.getBoolean(plugin.getConfig())) {
			plugin.getLogger().info("Enabled foreign keys.");
		}

		// close statement
		statement.close();
	}


	private int getSchemaVersion()
	{
		int version = -1;

		try
		{
			final Statement statement = connection.createStatement();

			// execute query
			ResultSet rs = statement.executeQuery(Queries.getQuery("GetUserVersion"));

			// get user version
			while (rs.next())
			{
				version = rs.getInt(1);

				if (Config.DEBUG.getBoolean(plugin.getConfig()))
				{
					plugin.getLogger().info("Read schema version: " + version);
				}
			}

			// close statement
			statement.close();
		}

		catch (SQLException e)
		{
			plugin.getLogger().warning(SQLiteNotice.SCHEMA_VERSION_NOT_FOUND.toString());
		}
		return version;
	}


	@SuppressWarnings("SameParameterValue")
	private void setSchemaVersion(final int version)
	{
		try (Statement statement = connection.createStatement())
		{
			// update schema version in database
			statement.executeUpdate("PRAGMA user_version = " + version);

			// set schema version field
			this.schemaVersion = 1;
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning("Could not set schema user version!");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}
	}


	/**
	 * Test for existence of graveyards table
	 *
	 * @return boolean {@code true} if table exists, {@code false} if not
	 */
	private boolean tableExists()
	{
		boolean returnValue = false;

		try (final Statement statement = connection.createStatement())
		{
			ResultSet resultSet = statement.executeQuery(Queries.getQuery("SelectGraveyardsTable"));
			if (resultSet.next())
			{
				returnValue = true;
			}
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SQLiteNotice.TABLE_NOT_FOUND.toString());
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		return returnValue;
	}


	private void updateSchema()
	{
		// read schema version from database (pragma user_version)
		schemaVersion = getSchemaVersion();

		// if schema version is 0, migrate tables to schema version 1
		if (schemaVersion == 0)
		{
			if (tableExists())
			{
				int count;

				// select all graveyard records
				Collection<Graveyard.Valid> existingGraveyardRecords = selectAllValidGraveyards();

				// select all discovery records
				Collection<Discovery.Valid> existingDiscoveryRecords = selectAllDiscoveries();

				// create statement object
				try(Statement statement = connection.createStatement())
				{
					// drop discovered table with old schema
					statement.executeUpdate(Queries.getQuery("DropDiscoveredTable"));
					if (Config.DEBUG.getBoolean(plugin.getConfig()))
					{
						plugin.getLogger().info("Discovered table dropped.");
					}

					// drop graveyards table with old schema
					statement.executeUpdate(Queries.getQuery("DropGraveyardsTable"));
					if (Config.DEBUG.getBoolean(plugin.getConfig()))
					{
						plugin.getLogger().info("Graveyards table dropped.");
					}

					// create graveyards table with new schema
					statement.executeUpdate(Queries.getQuery("CreateGraveyardsTable"));
					if (Config.DEBUG.getBoolean(plugin.getConfig()))
					{
						plugin.getLogger().info("Graveyards table created.");
					}

					// create discovered table with new schema
					statement.executeUpdate(Queries.getQuery("CreateDiscoveredTable"));
					if (Config.DEBUG.getBoolean(plugin.getConfig()))
					{
						plugin.getLogger().info("Discovered table created.");
					}
				}
				catch (SQLException sqlException)
				{
					plugin.getLogger().warning(SQLiteNotice.SCHEMA_UPDATE_V1_FAILED.toString());
					plugin.getLogger().warning(sqlException.getLocalizedMessage());
				}

				// insert all graveyard records into graveyards table
				count = insertGraveyards(existingGraveyardRecords);
				plugin.getLogger().info(count + " graveyard records migrated to schema v1.");

				// insert all discovery records into discovered table
				count = insertDiscoveries(existingDiscoveryRecords);
				plugin.getLogger().info(count + " discovery records migrated to schema v1.");
			}
		}

		try (Statement statement = connection.createStatement())
		{
			setSchemaVersion(1);
			statement.executeUpdate(Queries.getQuery("CreateGraveyardsTable"));
			statement.executeUpdate(Queries.getQuery("CreateDiscoveredTable"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SQLiteNotice.SCHEMA_UPDATE_FAILED.toString());
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}
	}


	@Override
	public void close()
	{
		try
		{
			connection.close();
			plugin.getLogger().info(SQLiteNotice.DATABASE_CLOSE_SUCCESS.toString());
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning(SQLiteNotice.DATABASE_CLOSE_FAILED.toString());
			plugin.getLogger().warning(e.getMessage());
		}

		setInitialized(false);
	}


	@Override
	public void sync()
	{
		// no action necessary for this storage type
	}


	@Override
	public boolean delete()
	{
		// get path name to data store file
		File dataStoreFile = new File(dataFilePath);
		boolean result = false;
		if (dataStoreFile.exists()) {
			result = dataStoreFile.delete();
		}
		return result;
	}


	/**
	 * Select all valid graveyard records from the datastore, maintaining order returned by the query. Records
	 * that produce an invalid graveyard are not included in the returned collection. Invalid graveyards are most
	 * likely a result of the graveyard's location world not loaded at the time of query.
	 *
	 * @return a {@link List} containing all graveyard records in the order they were returned by the query
	 */
	@Override
	public List<Graveyard.Valid> selectAllValidGraveyards()
	{
		final List<Graveyard.Valid> returnList = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectAllGraveyards")))
		{
			final ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next())
			{
				switch (graveyardMapper.map(resultSet))
				{
					case Graveyard.Valid valid -> returnList.add(valid);
					case Graveyard.Invalid(DisplayName displayName, String ignored, GraveyardReason graveyardReason) -> plugin.getLogger()
							.warning("A valid graveyard '" + displayName.colorString() + "' could not be created: " + graveyardReason);
				}
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning(SQLiteNotice.SELECT_ALL_VALID_GRAVEYARDS_FAILED.toString());
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return returnList;
	}


	/**
	 * Select all graveyard records from the datastore, maintaining order returned by the query.
	 *
	 * @return a {@link List} containing all graveyard records in the order they were returned by the query
	 */
	@Override
	public List<Graveyard> selectAllGraveyards()
	{
		final List<Graveyard> returnList = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectAllGraveyards")))
		{
			final ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next())
			{
				returnList.add(graveyardMapper.map(resultSet));
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning(SQLiteNotice.SELECT_ALL_GRAVEYARDS_FAILED.toString());
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return returnList;
	}


	@Override
	public Graveyard selectGraveyard(final SearchKey.Valid searchKey)
	{
		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectGraveyard")))
		{
			preparedStatement.setString(1, searchKey.string());
			final ResultSet resultSet = preparedStatement.executeQuery();

			// only zero or one record can match the unique search key
			if (resultSet.next())
			{
				return graveyardMapper.map(resultSet);
			}
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SQLiteNotice.GRAVEYARD_RECORD_NOT_FOUND.toString());
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		return new Graveyard.Invalid(searchKey.toDisplayName(), "∅", GRAVEYARD_MATCH_NOT_FOUND);
	}


	@Override
	public Optional<Graveyard.Valid> selectNearestGraveyard(final Player player)
	{
		if (player == null) { return Optional.empty(); }

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectNearestGraveyards")))
		{
			ResultSet resultSet = graveyardQueryHandler.selectNearestGraveyard(player, preparedStatement);

			while (resultSet.next())
			{
				if (graveyardMapper.map(resultSet) instanceof Graveyard.Valid valid)
				{
					// check if graveyard has group and player is in group
					if (valid.attributes().group() == null
							|| valid.attributes().group().value().isBlank()
							|| player.hasPermission("group." + valid.attributes().group()))
					{
						return Optional.of(valid);
					}
				}
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning(SQLiteNotice.SELECT_NEAREST_GRAVEYARD_FAILED.toString());
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return Optional.empty();
	}


	/**
	 * Returns a list of enabled, valid graveyards in the player's current world for which
	 * the player has permission, returned in order of proximity to the player's location.
	 *
	 * @param player the player whose location is used as the origin, and permissions are checked
	 * @return a list of graveyards that match the criteria
	 */
	@Override
	public List<Graveyard.Valid> selectNearestGraveyards(final Player player)
	{
		if (player == null) { return Collections.emptyList(); }

		final List<Graveyard.Valid> returnList = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectNearestGraveyards")))
		{
			ResultSet resultSet = graveyardQueryHandler.SelectNearestGraveyards(player, preparedStatement);

			while (resultSet.next())
			{
				if (graveyardMapper.map(resultSet) instanceof Graveyard.Valid valid)
				{
					// check if graveyard has group and player is in group
					if (valid.attributes().group() == null
							|| valid.attributes().group().value().isBlank()
							|| player.hasPermission("group." + valid.attributes().group()))
					{
						returnList.add(valid);
					}
				}
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning(SQLiteNotice.SELECT_NEAREST_GRAVEYARDS_FAILED.toString());
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return returnList;
	}


	/**
	 * Performs a sql query to retrieve a list of graveyard names that match a given prefix.
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
	@Override
	public List<String> selectMatchingGraveyardNames(final String prefix)
	{
		if (prefix == null) return Collections.emptyList();

		final List<String> returnList = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectGraveyardNamesMatchingPrefix")))
		{
			ResultSet resultSet = graveyardQueryHandler.selectMatchingGraveyardNames(prefix, preparedStatement);
			while (resultSet.next())
			{
				returnList.add(resultSet.getString("SearchKey").replace("_", " "));
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning(SQLiteNotice.SELECT_MATCHING_GRAVEYARD_NAMES_FAILED.toString());
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return returnList;
	}


	@Override
	public List<String> selectMatchingGraveyardKeys(final String prefix)
	{
		if (prefix == null) return Collections.emptyList();

		final List<String> returnList = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectGraveyardNamesMatchingPrefix")))
		{
			ResultSet resultSet = graveyardQueryHandler.selectMatchingGraveyardKeys(prefix, preparedStatement);

			while (resultSet.next())
			{
				returnList.add(resultSet.getString("SearchKey"));
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning(SQLiteNotice.SELECT_MATCHING_GRAVEYARD_KEYS_FAILED.toString());
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return returnList;
	}


	@Override
	public Stream<Graveyard.Valid> selectUndiscoveredGraveyards(final Player player)
	{
		if (player == null) return Stream.empty();

		final Set<Graveyard.Valid> returnSet = new HashSet<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectUndiscoveredGraveyards")))
		{
			ResultSet resultSet = graveyardQueryHandler.selectUndiscoveredGraveyards(player, preparedStatement);

			while (resultSet.next())
			{
				switch (graveyardMapper.map(resultSet))
				{
					case Graveyard.Valid valid -> returnSet.add(valid);
					case Graveyard.Invalid(DisplayName displayName, String ignored, GraveyardReason reason) -> plugin.getLogger()
							.warning("A valid graveyard named " + displayName.colorString() + " could not be created: " + reason);
				}
			}
		}
		catch (Exception e)
		{
			// output simple error message
			plugin.getLogger().warning(SQLiteNotice.SELECT_UNDISCOVERED_GRAVEYARD_RECORDS.toString());
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		// return results
		return returnSet.stream();
	}


	@Override
	public List<String> selectUndiscoveredKeys(final Player player)
	{
		if (player == null) return Collections.emptyList();

		final List<String> returnSet = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectUndiscoveredGraveyardKeys")))
		{
			ResultSet resultSet = graveyardQueryHandler.selectUndiscoveredKeys(player, preparedStatement);

			while (resultSet.next())
			{
				returnSet.add(resultSet.getString("SearchKey"));
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning(SQLiteNotice.SELECT_UNDISCOVERED_GRAVEYARD_KEYS.toString());
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return returnSet;
	}


	@Override
	public boolean insertDiscovery(final Discovery.Valid discovery)
	{
	new asyncInsertDiscovery(discovery).runTaskAsynchronously(plugin);

		return true;
	}


	@Override
	public int insertDiscoveries(final Collection<Discovery.Valid> discoveries)
	{
		if (discoveries == null)
		{
			plugin.getLogger().warning("Could not insert graveyard records in data store "
					+ "because the collection parameter was null.");
			return 0;
		}

		int count = 0;

		for (Discovery.Valid validDiscovery : discoveries)
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertDiscovery")))
			{
				synchronized (this)
				{
					count += discoveryQueryHandler.insertDiscovery(validDiscovery, preparedStatement);
				}
			}
			catch (SQLException e)
			{
				plugin.getLogger().warning("An error occurred while trying to "
						+ "insert a record into the discovered table in the SQLite datastore.");
				plugin.getLogger().warning(e.getLocalizedMessage());
			}
		}

		return count;
	}


	@Override
	public int insertGraveyards(final Collection<Graveyard.Valid> graveyards)
	{
		if (graveyards == null) return 0;

		int count = 0;

		for (Graveyard.Valid graveyard : graveyards)
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertGraveyard")))
			{
				synchronized (this)
				{
					count += graveyardQueryHandler.insertGraveyard(graveyard, preparedStatement);
				}
			}
			catch (Exception e)
			{
				plugin.getLogger().warning("An error occurred while inserting a Valid record "
						+ "into the SQLite datastore.");
				plugin.getLogger().warning(e.getLocalizedMessage());
			}
		}
		return count;
	}


	@Override
	public Graveyard insertGraveyard(final Graveyard.Valid graveyard)
	{
		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertGraveyard")))
		{
			// synchronize on connection
			synchronized (this)
			{
				graveyardQueryHandler.insertGraveyard(graveyard, preparedStatement);
			}
		}
		catch (Exception e)
		{
			plugin.getLogger().warning("An error occurred while inserting a Valid record "
					+ "into the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
			return new Graveyard.Invalid(graveyard.displayName(), "∅", GRAVEYARD_INSERT_FAILED);
		}

		return graveyard;
	}


	@Override
	public Graveyard updateGraveyard(final Graveyard.Valid graveyard)
	{
		new asyncUpdateGraveyard(graveyard.displayName(), graveyard).runTaskAsynchronously(plugin);
		return graveyard;
	}


	@Override
	public Graveyard updateGraveyard(final DisplayName.Valid oldDisplayName, final Graveyard.Valid graveyard)
	{
		new asyncUpdateGraveyard(oldDisplayName, graveyard).runTaskAsynchronously(plugin);
		return graveyard;
	}


	@Override
	public Graveyard deleteGraveyard(final SearchKey.Valid searchKey)
	{
		// return deleted record or invalid if not found
		if (selectGraveyard(searchKey) instanceof Graveyard.Valid valid)
		{
			new asyncDeleteGraveyard(searchKey).runTaskAsynchronously(plugin);
			return valid;
		}
		else
		{
			return new Graveyard.Invalid(DisplayName.of(searchKey.string()), "∅", GRAVEYARD_DELETE_FAILED);
		}
	}


	private Set<Discovery.Valid> selectAllDiscoveries()
	{
		final Set<Discovery.Valid> returnSet = new HashSet<>();

		if (schemaVersion == 0)
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectAllDiscoveryRecordsV0")))
			{
				ResultSet resultSet = discoveryQueryHandler.selectAllDiscoveries(preparedStatement);

				while (resultSet.next())
				{
					SearchKey searchKey = SearchKey.of(resultSet.getString("SearchKey"));
					String playerUidString = resultSet.getString("PlayerUid");
					UUID playerUid;

					if (searchKey instanceof SearchKey.Valid)
					{
						try
						{
							playerUid = UUID.fromString(playerUidString);
						}
						catch (IllegalArgumentException e)
						{
							plugin.getLogger().warning("A record in the Discovered table " +
									"has an invalid UUID! Skipping record.");
							plugin.getLogger().warning(e.getLocalizedMessage());
							continue;
						}

						if (Discovery.of((SearchKey.Valid) searchKey, playerUid) instanceof Discovery.Valid validDiscovery)
						{
							returnSet.add(validDiscovery);
						}
					}
				}
			}
			catch (SQLException e)
			{
				plugin.getLogger().warning("An error occurred while trying to " +
						"select all discovery records from the SQLite datastore.");
				plugin.getLogger().warning(e.getLocalizedMessage());
			}
		}
		else
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectAllDiscoveryRecords")))
			{
				final ResultSet resultSet = discoveryQueryHandler.selectAllDiscoveries(preparedStatement);

				while (resultSet.next())
				{
					switch (discoveryMapper.map(resultSet))
					{
						case Discovery.Valid valid -> returnSet.add(valid);
						case Discovery.Invalid(DiscoveryReason discoveryReason) -> plugin.getLogger()
								.warning("A valid discovery could not be created: " + discoveryReason);
					}
				}
			}
			catch (SQLException e)
			{
				plugin.getLogger().warning("An error occurred while trying to " +
						"select all discovery records from the SQLite datastore.");
				plugin.getLogger().warning(e.getLocalizedMessage());
			}
		}

		return returnSet;
	}


	@Override
	public boolean deleteDiscovery(final String searchKey, final UUID playerUid)
	{
		if (searchKey == null || playerUid == null) return false;

		int rowsAffected = 0;

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("DeleteDiscovery")))
		{
			synchronized (this)
			{
				rowsAffected = discoveryQueryHandler.deleteDiscovery(searchKey, playerUid, preparedStatement);
			}
		}
		catch (SQLException e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while attempting to "
					+ "delete a ValidDiscovery record from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return rowsAffected > 0;
	}


	@Override
	public int selectGraveyardCount()
	{
		int count = 0;

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectGraveyardCount")))
		{
			final ResultSet resultSet = preparedStatement.executeQuery();

			if (resultSet.next())
			{
				count = resultSet.getInt("GraveyardCount");
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning("An error occurred while attempting to retrieve a count of all graveyard records.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return count;
	}


	private class asyncInsertDiscovery extends BukkitRunnable
	{
		private final Discovery.Valid discovery;


		public asyncInsertDiscovery(final Discovery.Valid discovery)
		{
			this.discovery = discovery;
		}

		@Override
		public void run()
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertDiscovery")))
			{
				synchronized (this)
				{
					discoveryQueryHandler.insertDiscovery(discovery, preparedStatement);
				}
			}
			catch (SQLException e)
			{
				plugin.getLogger().warning(SQLiteNotice.INSERT_DISCOVERY_FAILED.toString());
				plugin.getLogger().warning(e.getLocalizedMessage());
			}
		}
	}


	private class asyncUpdateGraveyard extends BukkitRunnable
	{
		private final DisplayName.Valid oldDisplayName;
		private final Graveyard.Valid graveyard;

		public asyncUpdateGraveyard(DisplayName.Valid oldDisplayName, Graveyard.Valid graveyard)
		{
			this.oldDisplayName = oldDisplayName;
			this.graveyard = graveyard;
		}

		@Override
		public void run()
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("UpdateGraveyard")))
			{
				synchronized (this)
				{
					graveyardQueryHandler.updateGraveyard(oldDisplayName, graveyard, preparedStatement);
				}
			}
			catch (SQLException e)
			{
				plugin.getLogger().warning(SQLiteNotice.UPDATE_GRAVEYARD_RECORD_FAILED.toString());
				plugin.getLogger().warning(e.getLocalizedMessage());
			}
		}
	}


	private class asyncDeleteGraveyard extends BukkitRunnable
	{
		private final SearchKey.Valid searchKey;


		public asyncDeleteGraveyard(SearchKey.Valid searchKey)
		{
			this.searchKey = searchKey;
		}

		@Override
		public void run()
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("DeleteGraveyard")))
			{
				// synchronize on connection
				synchronized (this)
				{
					graveyardQueryHandler.deleteGraveyard(searchKey, preparedStatement);
					preparedStatement.setString(1, searchKey.string());
					preparedStatement.executeUpdate();
				}
			}
			catch (SQLException e)
			{
				plugin.getLogger().warning(SQLiteNotice.DELETE_GRAVEYARD_RECORD_FAILED.toString());
				plugin.getLogger().warning(e.getLocalizedMessage());
			}
		}
	}


}
