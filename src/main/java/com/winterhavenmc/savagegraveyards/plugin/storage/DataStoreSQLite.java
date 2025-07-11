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
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.plugin.storage.sqlite.DiscoveryAdapter;
import com.winterhavenmc.savagegraveyards.plugin.storage.sqlite.GraveyardQueryHandler;
import com.winterhavenmc.savagegraveyards.plugin.util.Config;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.*;


/**
 * Concrete SQLite datastore class
 */
final class DataStoreSQLite extends DataStoreAbstract implements DataStore
{
	private final Plugin plugin;
	private Connection connection;
	private final String dataFilePath;
	private int schemaVersion;
	GraveyardQueryHandler graveyardQueryHandler = new GraveyardQueryHandler();
	DiscoveryAdapter discoveryAdapter = new DiscoveryAdapter();

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
			plugin.getLogger().info(this + " datastore already initialized.");
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
			plugin.getLogger().warning("Could not read schema version!");
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
			ResultSet rs = statement.executeQuery(Queries.getQuery("SelectGraveyardsTable"));
			if (rs.next())
			{
				returnValue = true;
			}
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning("An error occurred while trying to check the existence of a table.");
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
					plugin.getLogger().warning("An error occurred while trying to update the datastore to schema v1.");
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
			plugin.getLogger().warning("An error occurred while trying to update the " + this + " datastore schema.");
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}
	}


	@Override
	public void close()
	{
		try
		{
			connection.close();
			plugin.getLogger().info(this + " datastore connection closed.");
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning("An error occurred while closing the " + this + " datastore.");
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
				switch (graveyardQueryHandler.selectGraveyard(resultSet))
				{
					case Graveyard.Valid valid -> returnList.add(valid);
					case Graveyard.Invalid(String displayName, String worldName, String reason) -> plugin.getLogger()
							.warning("A valid graveyard '" + displayName + "' could not be created: " + reason);
				}
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning("An error occurred while trying to "
					+ "select all graveyard records from the SQLite datastore.");
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
				returnList.add(graveyardQueryHandler.selectGraveyard(resultSet));
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning("An error occurred while trying to "
					+ "select all graveyard records from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return returnList;
	}


	@Override
	public Graveyard selectGraveyard(final String displayName)
	{
		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectGraveyard")))
		{
			preparedStatement.setString(1, Graveyard.searchKey(displayName));
			final ResultSet resultSet = preparedStatement.executeQuery();

			// only zero or one record can match the unique search key
			if (resultSet.next())
			{
				return graveyardQueryHandler.selectGraveyard(resultSet);
			}
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning("An error occurred while trying to " +
					"select a Valid record from the SQLite database.");
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		return new Graveyard.Invalid(displayName, "∅", "No matching graveyard found.");
	}


	@Override
	public Optional<Graveyard.Valid> selectNearestGraveyard(final Player player)
	{
		if (player == null) { return Optional.empty(); }

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectNearestGraveyards")))
		{
			preparedStatement.setLong(1, player.getWorld().getUID().getMostSignificantBits());
			preparedStatement.setLong(2, player.getWorld().getUID().getLeastSignificantBits());
			preparedStatement.setLong(3, player.getUniqueId().getMostSignificantBits());
			preparedStatement.setLong(4, player.getUniqueId().getLeastSignificantBits());
			preparedStatement.setDouble(5, player.getLocation().getX());
			preparedStatement.setDouble(6, player.getLocation().getY());
			preparedStatement.setDouble(7, player.getLocation().getZ());
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next())
			{
				if (graveyardQueryHandler.selectGraveyard(resultSet) instanceof Graveyard.Valid valid)
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
			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "fetch the select Valid from the SQLite datastore.");
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
			preparedStatement.setLong(1, player.getWorld().getUID().getMostSignificantBits());
			preparedStatement.setLong(2, player.getWorld().getUID().getLeastSignificantBits());
			preparedStatement.setLong(3, player.getUniqueId().getMostSignificantBits());
			preparedStatement.setLong(4, player.getUniqueId().getLeastSignificantBits());
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next())
			{
				if (graveyardQueryHandler.selectGraveyard(resultSet) instanceof Graveyard.Valid valid)
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
			plugin.getLogger().warning("An error occurred while trying to "
					+ "fetch the select Valid from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return returnList;
	}


	@Override
	public List<String> selectMatchingGraveyardNames(final String match)
	{
		if (match == null) return Collections.emptyList();

		final List<String> returnList = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectMatchingGraveyardNames")))
		{
			preparedStatement.setString(1, match + "%");
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next())
			{
				//TODO: fix column label;
				returnList.add(resultSet.getString("Valid").replace("_", " "));
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning("An error occurred while trying to "
					+ "fetch matching Valid records from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return returnList;
	}


	@Override
	public Set<Graveyard.Valid> selectUndiscoveredGraveyards(final Player player)
	{
		if (player == null) return Collections.emptySet();

		final Set<Graveyard.Valid> returnSet = new HashSet<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectUndiscoveredGraveyards")))
		{
			preparedStatement.setLong(1, player.getWorld().getUID().getMostSignificantBits());
			preparedStatement.setLong(2, player.getWorld().getUID().getLeastSignificantBits());
			preparedStatement.setLong(3, player.getUniqueId().getMostSignificantBits());
			preparedStatement.setLong(4, player.getUniqueId().getLeastSignificantBits());
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next())
			{
				// adapt graveyard object
				switch (graveyardQueryHandler.selectGraveyard(resultSet))
				{
					case Graveyard.Valid valid -> returnSet.add(valid);
					case Graveyard.Invalid(String displayName, String worldName, String reason) -> plugin.getLogger()
							.warning("A valid graveyard named " + displayName + " could not be created: " + reason);
				}
			}
		}
		catch (Exception e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "select undiscovered Valid records from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		// return results
		return returnSet;
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
				returnSet.add(resultSet.getString("Valid"));
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning("An error occurred while trying to "
					+ "select undiscovered Valid keys from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return returnSet;
	}


	@Override
	public boolean insertDiscovery(final Graveyard.Valid graveyard, final UUID playerUid)
	{
		new asyncInsertDiscovery(graveyard, playerUid).runTaskAsynchronously(plugin);
		return true;
	}


	@Override
	public int insertDiscoveries(final Collection<Discovery.Valid> discoveries)
	{
		if (discoveries == null)
		{
			plugin.getLogger().warning("Could not insert graveyard records in data store "
					+ "because the collection parameter is null.");
			return 0;
		}

		int count = 0;

		for (Discovery.Valid validDiscovery : discoveries)
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertDiscovery")))
			{
				synchronized (this)
				{
					count += discoveryAdapter.insertDiscovery(validDiscovery, preparedStatement);
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
			return new Graveyard.Invalid(graveyard.displayName(), "∅", "Could not insert graveyard in datastore.");
		}

		return graveyard;
	}


	@Override
	public Graveyard updateGraveyard(final Graveyard.Valid graveyard)
	{
		new asyncUpdateGraveyard(graveyard).runTaskAsynchronously(plugin);
		return graveyard;
	}


	@Override
	public Graveyard deleteGraveyard(final String displayName)
	{
		// return deleted record or invalid if not found
		if (selectGraveyard(displayName) instanceof Graveyard.Valid valid)
		{
			new asyncDeleteGraveyard(displayName).runTaskAsynchronously(plugin);
			return valid;
		}
		else
		{
			return new Graveyard.Invalid(displayName, "∅", "No graveyard was found to delete.");
		}
	}


	private Set<Discovery.Valid> selectAllDiscoveries()
	{
		final Set<Discovery.Valid> returnSet = new HashSet<>();

		if (schemaVersion == 0)
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectAllDiscoveryRecordsV0")))
			{
				ResultSet rs = preparedStatement.executeQuery();

				while (rs.next())
				{
					int graveyardKey = rs.getInt("graveyardKey");
					String playerUidString = rs.getString("PlayerUid");
					UUID playerUid;

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

					if (Discovery.of(graveyardKey, playerUid) instanceof Discovery.Valid validDiscovery)
					{
						returnSet.add(validDiscovery);
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
				final ResultSet resultSet = preparedStatement.executeQuery();

				while (resultSet.next())
				{
					switch (discoveryAdapter.selectDiscovery(resultSet))
					{
						case Discovery.Valid valid -> returnSet.add(valid);
						case Discovery.Invalid(String reason) -> plugin.getLogger()
								.warning("A valid discovery could not be created: " + reason);
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
				rowsAffected = discoveryAdapter.deleteDiscovery(searchKey, playerUid, preparedStatement);
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


//	private class asyncInsertDiscovery extends BukkitRunnable
//	{
//		private final Discovery discovery;
//
//
//		public asyncInsertDiscovery(Discovery discovery)
//		{
//			this.discovery = discovery;
//		}
//
//		@Override
//		public void run()
//		{
//			if (discovery instanceof Discovery.Valid valid)
//			{
//				try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertDiscovery")))
//				{
//					synchronized (this)
//					{
//						discoveryAdapter.insertDiscovery(valid, preparedStatement);
//					}
//				}
//				catch (SQLException e)
//				{
//					plugin.getLogger().warning("An error occurred while trying to "
//							+ "insert a record into the discovered table in the SQLite datastore.");
//					plugin.getLogger().warning(e.getLocalizedMessage());
//				}
//			}
//		}
//	}


	private class asyncInsertDiscovery extends BukkitRunnable
	{
		private final String searchKey;
		private final UUID playerUid;


		public asyncInsertDiscovery(final Graveyard.Valid graveyard, final UUID playerUid)
		{
			this.searchKey = graveyard.searchKey();
			this.playerUid = playerUid;
		}

		@Override
		public void run()
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertDiscovery")))
			{
				synchronized (this)
				{
					discoveryAdapter.insertDiscovery(searchKey, playerUid, preparedStatement);
				}
			}
			catch (SQLException e)
			{
				plugin.getLogger().warning("An error occurred while trying to "
						+ "insert a record into the discovered table in the SQLite datastore.");
				plugin.getLogger().warning(e.getLocalizedMessage());
			}
		}
	}


	private class asyncUpdateGraveyard extends BukkitRunnable
	{
		private final Graveyard.Valid graveyard;

		public asyncUpdateGraveyard(Graveyard.Valid graveyard)
		{
			this.graveyard = graveyard;
		}

		@Override
		public void run()
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("UpdateGraveyard")))
			{
				synchronized (this)
				{
					graveyardQueryHandler.insertGraveyard(graveyard, preparedStatement);
				}
			}
			catch (SQLException e)
			{
				plugin.getLogger().warning("An error occurred while trying to " +
						"update a Valid record into the SQLite datastore.");
				plugin.getLogger().warning(e.getLocalizedMessage());
			}
		}
	}


	private class asyncDeleteGraveyard extends BukkitRunnable
	{
		private final String displayName;


		public asyncDeleteGraveyard(String displayName)
		{
			this.displayName = displayName;
		}

		@Override
		public void run()
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("DeleteGraveyard")))
			{
				// synchronize on connection
				synchronized (this)
				{
					preparedStatement.setString(1, Graveyard.searchKey(displayName));
					preparedStatement.executeUpdate();
				}
			}
			catch (SQLException e)
			{
				plugin.getLogger().warning("An error occurred while attempting to "
						+ "delete a Valid record from the SQLite datastore.");
				plugin.getLogger().warning(e.getLocalizedMessage());
			}
		}
	}

}
