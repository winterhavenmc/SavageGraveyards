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

import com.winterhavenmc.savagegraveyards.models.discovery.Discovery;
import com.winterhavenmc.savagegraveyards.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.storage.sqlite.GraveyardAdapter;
import com.winterhavenmc.savagegraveyards.util.Config;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.*;


/**
 * Concrete SQLite datastore class
 */
final class DataStoreSQLite extends DataStoreAbstract implements DataStore
{
	private final JavaPlugin plugin;
	private Connection connection;
	private final String dataFilePath;
	private int schemaVersion;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public DataStoreSQLite(final JavaPlugin plugin)
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

		try {
			// create statement
			final Statement statement = connection.createStatement();

			// execute query
			ResultSet rs = statement.executeQuery(Queries.getQuery("GetUserVersion"));

			// get user version
			while (rs.next()) {
				version = rs.getInt(1);

				if (Config.DEBUG.getBoolean(plugin.getConfig())) {
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
		try
		{
			Statement statement = connection.createStatement();

			// update schema version in database
			statement.executeUpdate("PRAGMA user_version = " + version);

			// update schema version field
			schemaVersion = 1;

			// close statement
			statement.close();
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
	 * @throws SQLException on sql error
	 */
	private boolean tableExists() throws SQLException
	{
		boolean returnValue = false;

		final Statement statement = connection.createStatement();
		ResultSet rs = statement.executeQuery(Queries.getQuery("SelectGraveyardsTable"));
		if (rs.next())
		{
			returnValue = true;
		}
		statement.close();
		return returnValue;
	}


	private void updateSchema() throws SQLException
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
				Collection<Graveyard.Valid> existingGraveyardRecords = selectAllGraveyards();

				// select all discovery records
				Collection<Discovery.Valid> existingDiscoveryRecords = selectAllDiscoveries();

				// create statement object
				Statement statement = connection.createStatement();

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

				// close statement
				statement.close();

				// insert all graveyard records into graveyards table
				count = insertGraveyards(existingGraveyardRecords);
				plugin.getLogger().info(count + " graveyard records migrated to schema v1.");

				// insert all discovery records into discovered table
				count = insertDiscoveries(existingDiscoveryRecords);
				plugin.getLogger().info(count + " discovery records migrated to schema v1.");
			}
		}

		// create statement object
		Statement statement = connection.createStatement();

		// set schema to version 1
		setSchemaVersion(1);

		// execute table creation statement
		statement.executeUpdate(Queries.getQuery("CreateGraveyardsTable"));

		// execute index creation statement
		statement.executeUpdate(Queries.getQuery("CreateDiscoveredTable"));

		// close statement
		statement.close();
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


	@Override
	public Set<Graveyard.Valid> selectAllGraveyards()
	{
		final Set<Graveyard.Valid> returnSet = new HashSet<>();
		final GraveyardAdapter adapter = new GraveyardAdapter();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectAllGraveyards")))
		{
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next())
			{
				// adapt graveyard object
				switch (adapter.adapt(resultSet))
				{
					case Graveyard.Valid valid -> returnSet.add(valid);
					case Graveyard.Invalid(String reason) -> plugin.getLogger()
							.warning("A valid graveyard could not be created: " + reason);
				}
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning("An error occurred while trying to "
					+ "select all graveyard records from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return returnSet;
	}


	@Override
	public Graveyard selectGraveyard(final String displayName)
	{
		GraveyardAdapter adapter = new GraveyardAdapter();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectGraveyard")))
		{
			preparedStatement.setString(1, Graveyard.createSearchKey(displayName));


			// execute sql query
			ResultSet resultSet = preparedStatement.executeQuery();

			// only zero or one record can match the unique search key
			if (resultSet.next())
			{
				return adapter.adapt(resultSet);
			}
		}
		catch (SQLException e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to " +
					"select a Valid record from the SQLite database.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return new Graveyard.Invalid("A database error occurred.");
	}


	@Override
	public Optional<Graveyard.Valid> selectNearestGraveyard(final Player player)
	{
		if (player == null) { return Optional.empty(); }

		Optional<Graveyard.Valid> closest = Optional.empty();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectNearestGraveyard")))
		{
			preparedStatement.setLong(1, player.getWorld().getUID().getMostSignificantBits());
			preparedStatement.setLong(2, player.getWorld().getUID().getLeastSignificantBits());
			preparedStatement.setLong(3, player.getUniqueId().getMostSignificantBits());
			preparedStatement.setLong(4, player.getUniqueId().getLeastSignificantBits());

			ResultSet resultSet = preparedStatement.executeQuery();

			GraveyardAdapter adapter = new GraveyardAdapter();

			while (resultSet.next())
			{
				if (adapter.adapt(resultSet) instanceof Graveyard.Valid valid)
				{
					// check if graveyard has group and player is in group
					if (valid.group() == null || valid.group().isBlank() || player.hasPermission("group." + valid.group()))
					{
						closest = Optional.of(valid);
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

		// return closest result
		return closest;
	}


	@Override
	public List<String> selectMatchingGraveyardNames(final String match)
	{
		if (match == null) return Collections.emptyList();

		List<String> returnList = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectMatchingGraveyardNames")))
		{
			preparedStatement.setString(1, match.toLowerCase() + "%");

			// execute sql query
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next())
			{
				returnList.add(resultSet.getString("SearchKey"));
			}
		}
		catch (SQLException e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "fetch matching Valid records from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		// return list of search key strings
		return returnList;
	}


	@Override
	public Set<Graveyard.Valid> selectUndiscoveredGraveyards(final Player player)
	{
		if (player == null) return Collections.emptySet();

		Set<Graveyard.Valid> returnSet = new HashSet<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectUndiscoveredGraveyards")))
		{
			preparedStatement.setLong(1, player.getWorld().getUID().getMostSignificantBits());
			preparedStatement.setLong(2, player.getWorld().getUID().getLeastSignificantBits());
			preparedStatement.setLong(3, player.getUniqueId().getMostSignificantBits());
			preparedStatement.setLong(4, player.getUniqueId().getLeastSignificantBits());

			ResultSet resultSet = preparedStatement.executeQuery();

			GraveyardAdapter adapter = new GraveyardAdapter();

			while (resultSet.next())
			{
				// adapt graveyard object
				switch (adapter.adapt(resultSet))
				{
					case Graveyard.Valid valid -> returnSet.add(valid);
					case Graveyard.Invalid(String reason) -> plugin.getLogger()
							.warning("A valid graveyard could not be created: " + reason);
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
	public Set<String> selectDiscoveredKeys(final UUID playerUid)
	{
		if (playerUid == null) return Collections.emptySet();

		Set<String> returnSet = new HashSet<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectGraveyardsKnownByPlayer")))
		{
			preparedStatement.setLong(1, playerUid.getMostSignificantBits());
			preparedStatement.setLong(2, playerUid.getLeastSignificantBits());

			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next())
			{
				returnSet.add(rs.getString("searchKey"));
			}
		}
		catch (Exception e)
		{
			plugin.getLogger().warning("An error occurred while trying to "
					+ "select discovered Valid records from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return returnSet;
	}


	@Override
	public Collection<String> selectUndiscoveredKeys(final Player player)
	{
		if (player == null) return Collections.emptySet();

		Collection<String> returnSet = new HashSet<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectUndiscoveredGraveyardKeys")))
		{
			preparedStatement.setLong(1, player.getWorld().getUID().getMostSignificantBits());
			preparedStatement.setLong(2, player.getWorld().getUID().getLeastSignificantBits());
			preparedStatement.setLong(3, player.getUniqueId().getMostSignificantBits());
			preparedStatement.setLong(4, player.getUniqueId().getLeastSignificantBits());

			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next())
			{
				returnSet.add(rs.getString("SearchKey"));
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
	public void insertDiscovery(final Discovery.Valid discovery)
	{
		final String searchKey = discovery.searchKey();
		final UUID playerUid = discovery.playerUid();

		new asyncInsertDiscovery(searchKey, playerUid).runTaskAsynchronously(plugin);
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

		for (Discovery.Valid record : discoveries)
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertDiscovery")))
			{
				// synchronize on instance
				synchronized (this)
				{
					preparedStatement.setString(1, record.searchKey());
					preparedStatement.setLong(2, record.playerUid().getMostSignificantBits());
					preparedStatement.setLong(3, record.playerUid().getLeastSignificantBits());
					preparedStatement.executeUpdate();
					count++;
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
				// synchronize on connection
				synchronized (this)
				{
					preparedStatement.setString(1, graveyard.searchKey());
					preparedStatement.setString(2, graveyard.displayName());
					preparedStatement.setBoolean(3, graveyard.enabled());
					preparedStatement.setBoolean(4, graveyard.hidden());
					preparedStatement.setInt(5, graveyard.discoveryRange());
					preparedStatement.setString(6, graveyard.discoveryMessage());
					preparedStatement.setString(7, graveyard.respawnMessage());
					preparedStatement.setString(8, graveyard.group());
					preparedStatement.setInt(9, graveyard.safetyRange());
					preparedStatement.setLong(10, graveyard.safetyTime().getSeconds());
					preparedStatement.setString(11, graveyard.worldName());
					preparedStatement.setLong(12, graveyard.worldUid().getMostSignificantBits());
					preparedStatement.setLong(13, graveyard.worldUid().getLeastSignificantBits());
					preparedStatement.setDouble(14, graveyard.x());
					preparedStatement.setDouble(15, graveyard.y());
					preparedStatement.setDouble(16, graveyard.z());
					preparedStatement.setFloat(17, graveyard.yaw());
					preparedStatement.setFloat(18, graveyard.pitch());

					preparedStatement.executeUpdate();
				}
			}
			catch (Exception e)
			{
				plugin.getLogger().warning("An error occurred while inserting a Valid record "
						+ "into the SQLite datastore.");
				plugin.getLogger().warning(e.getLocalizedMessage());
			}
			count++;
		}
		return count;
	}


	@Override
	public void updateGraveyard(final Graveyard.Valid graveyard)
	{
		new asyncUpdateGraveyard(graveyard).runTaskAsynchronously(plugin);
	}


	@Override
	public Graveyard deleteGraveyard(final String displayName)
	{
		// get destination record to be deleted, for return
		final Graveyard graveyard = this.selectGraveyard(displayName);
		new asyncDeleteGraveyard(displayName).runTaskAsynchronously(plugin);
		return graveyard;
	}


	private Set<Discovery.Valid> selectAllDiscoveries()
	{
		Set<Discovery.Valid> returnSet = new HashSet<>();

		if (schemaVersion == 0)
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectAllDiscoveryRecordsV0")))
			{
				ResultSet rs = preparedStatement.executeQuery();

				while (rs.next())
				{
					// get graveyard search key
					String searchKey = rs.getString("SearchKey");

					// get player uid as string
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

					if (Discovery.of(searchKey, playerUid) instanceof Discovery.Valid validDiscovery)
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
				ResultSet rs = preparedStatement.executeQuery();

				while (rs.next())
				{
					String searchKey = rs.getString("GraveyardSearchKey");

					// get player uid components
					long playerUidMsb = rs.getLong("PlayerUidMsb");
					long playerUidLsb = rs.getLong("PlayerUidLsb");

					// reconstitute player uid from components
					UUID playerUid = new UUID(playerUidMsb, playerUidLsb);

					// add record to return set
					if (Discovery.of(searchKey, playerUid) instanceof Discovery.Valid validDiscovery)
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

		return returnSet;
	}


	@Override
	public Set<String> selectPlayersWithDiscoveries()
	{
		Set<String> returnSet = new HashSet<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectPlayersWithDiscovery")))
		{
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next())
			{
				// get player uid components
				long playerUidMsb = resultSet.getLong("PlayerUidMsb");
				long playerUidLsb = resultSet.getLong("PlayerUidLsb");

				// reconstitute player uid from components
				UUID playerUid = new UUID(playerUidMsb, playerUidLsb);

				// get offline player from uid
				OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(playerUid);

				// if offline player name is not null, add to return set
				if (offlinePlayer.getName() != null)
				{
					returnSet.add(offlinePlayer.getName());
				}
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning("An error occurred while trying to " +
					"select all discovery records from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return returnSet;
	}


	@Override
	public boolean deleteDiscovery(final String displayName, final UUID playerUid)
	{
		if (displayName == null || playerUid == null) return false;

		int rowsAffected;
		boolean result = true;

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("DeleteDiscovery")))
		{
			// synchronize on connection
			synchronized (this)
			{
				preparedStatement.setLong(1, playerUid.getMostSignificantBits());
				preparedStatement.setLong(2, playerUid.getLeastSignificantBits());
				preparedStatement.setString(3, Graveyard.createSearchKey(displayName));
				rowsAffected = preparedStatement.executeUpdate();
			}

			if (rowsAffected < 1)
			{
				result = false;
			}
		}
		catch (SQLException e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while attempting to "
					+ "delete a ValidDiscovery record from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return result;
	}


	@Override
	public int selectGraveyardCount()
	{
		int count = 0;

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectGraveyardCount")))
		{
			ResultSet resultSet = preparedStatement.executeQuery();

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
		private final String searchKey;
		private final UUID playerUid;


		public asyncInsertDiscovery(String searchKey, UUID playerUid)
		{
			this.searchKey = searchKey;
			this.playerUid = playerUid;
		}

		@Override
		public void run()
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertDiscovery")))
			{
				// synchronize on instance
				synchronized (this)
				{
					preparedStatement.setString(1, Graveyard.createSearchKey(searchKey));
					preparedStatement.setLong(2, playerUid.getMostSignificantBits());
					preparedStatement.setLong(3, playerUid.getLeastSignificantBits());
					preparedStatement.executeUpdate();
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
					preparedStatement.setString(1, graveyard.searchKey());
					preparedStatement.setString(2, graveyard.displayName());
					preparedStatement.setBoolean(3, graveyard.enabled());
					preparedStatement.setBoolean(4, graveyard.hidden());
					preparedStatement.setInt(5, graveyard.discoveryRange());
					preparedStatement.setString(6, graveyard.discoveryMessage());
					preparedStatement.setString(7, graveyard.respawnMessage());
					preparedStatement.setString(8, graveyard.group());
					preparedStatement.setInt(9, graveyard.safetyRange());
					preparedStatement.setLong(10, graveyard.safetyTime().getSeconds());
					preparedStatement.setString(11, graveyard.worldName());
					preparedStatement.setLong(12, graveyard.worldUid().getMostSignificantBits());
					preparedStatement.setLong(13, graveyard.worldUid().getLeastSignificantBits());
					preparedStatement.setDouble(14, graveyard.x());
					preparedStatement.setDouble(15, graveyard.y());
					preparedStatement.setDouble(16, graveyard.z());
					preparedStatement.setFloat(17, graveyard.yaw());
					preparedStatement.setFloat(18, graveyard.pitch());

					preparedStatement.executeUpdate();
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
					preparedStatement.setString(1, Graveyard.createSearchKey(displayName));
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
