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

import com.winterhavenmc.savagegraveyards.util.Config;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.*;
import java.time.Duration;
import java.util.*;


/**
 * Concrete SQLite datastore class
 */
final class DataStoreSQLite extends DataStoreAbstract implements DataStore
{
	// reference to main class
	private final JavaPlugin plugin;

	// database connection object
	private Connection connection;

	// file path for datastore file
	private final String dataFilePath;

	// schema version
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
		if (this.isInitialized()) {
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

		catch (SQLException e) {
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
			if (Config.DEBUG.getBoolean(plugin.getConfig()))
			{
				e.printStackTrace();
			}
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
				Collection<Graveyard> existingGraveyardRecords = selectAllGraveyards();

				// select all discovery records
				Collection<Discovery> existingDiscoveryRecords = selectAllDiscoveries();

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
			// output simple error message
			plugin.getLogger().warning("An error occurred while closing the " + this + " datastore.");
			plugin.getLogger().warning(e.getMessage());

			// if debugging is enabled, output stack trace
			if (Config.DEBUG.getBoolean(plugin.getConfig()))
			{
				e.printStackTrace();
			}
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
	public Collection<Graveyard> selectAllGraveyards()
	{
		// create empty set for return collection
		final Collection<Graveyard> returnSet = new HashSet<>();

		try
		{
			PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectAllGraveyards"));

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next())
			{
				// get stored world name
				String worldName = rs.getString("WorldName");

				UUID worldUid = null;
				World world;
				int primaryKey;

				// if schema version 0, get primaryKey from field 'Id'; world by name
				if (schemaVersion == 0)
				{
					// get primary key (id)
					primaryKey = rs.getInt("Id");

					// get world by name
					world = plugin.getServer().getWorld(worldName);

					// get world uid
					if (world != null)
					{
						worldUid = world.getUID();
					}
				}

				// else get primaryKey from field 'Key'; world by uid
				else
				{
					// get primary key
					primaryKey = rs.getInt("Key");

					// get world uid components
					long worldUidMsb = rs.getLong("WorldUidMsb");
					long worldUidLsb = rs.getLong("WorldUidLsb");

					// reconstitute world uid from components
					worldUid = new UUID(worldUidMsb, worldUidLsb);

					// get world by uid
					world = plugin.getServer().getWorld(worldUid);
				}

				// if world is null, log warning
				if (world == null)
				{
					plugin.getLogger().warning("Stored record has invalid world: " + worldName);
				}
				else
				{
					worldName = world.getName();
				}

				// build graveyard object
				Graveyard graveyard = new Graveyard.Builder(plugin)
							.primaryKey(primaryKey)
							.searchKey(rs.getString("SearchKey"))
							.displayName(rs.getString("DisplayName"))
							.enabled(rs.getBoolean("Enabled"))
							.hidden(rs.getBoolean("Hidden"))
							.discoveryRange(rs.getInt("DiscoveryRange"))
							.discoveryMessage(rs.getString("DiscoveryMessage"))
							.respawnMessage(rs.getString("RespawnMessage"))
							.group(rs.getString("GroupName"))
							.safetyRange(rs.getInt("SafetyRange"))
							.safetyTime(Duration.ofSeconds(rs.getInt("safetyTime")))
							.worldName(worldName)
							.worldUid(worldUid)
							.x(rs.getDouble("X"))
							.y(rs.getDouble("Y"))
							.z(rs.getDouble("Z"))
							.yaw(rs.getFloat("Yaw"))
							.pitch(rs.getFloat("Pitch"))
							.build();

				// add graveyard to return collection
				returnSet.add(graveyard);
			}
			// close prepared statement
			preparedStatement.close();
		}
		catch (SQLException e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "select all graveyard records from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (Config.DEBUG.getBoolean(plugin.getConfig()))
			{
				e.printStackTrace();
			}
		}

		// return record collection
		return returnSet;
	}


	@Override
	public Optional<Graveyard> selectGraveyard(final String displayName)
	{
		if (displayName == null)
		{
			return Optional.empty();
		}

		// derive search key from displayName
		String searchKey = Graveyard.createSearchKey(displayName);

		// if key is empty, return empty optional record
		if (searchKey.isEmpty())
		{
			return Optional.empty();
		}

		Graveyard graveyard = null;

		try
		{
			PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectGraveyard"));

			preparedStatement.setString(1, searchKey);

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			// only zero or one record can match the unique search key
			if (rs.next()) {

				// get stored world name
				String worldName = rs.getString("worldname");

				// get stored world uid components
				long worldUidMsb = rs.getLong("WorldUidMsb");
				long worldUidLsb = rs.getLong("WorldUidLsb");

				// reconstitute world uid from components
				UUID worldUid = new UUID(worldUidMsb, worldUidLsb);

				// get world by uid
				World world = plugin.getServer().getWorld(worldUid);

				// if world is null, log warning
				if (world == null) {
					plugin.getLogger().warning("Stored record has invalid world: " + worldName);
				}
				// else if world is not null, get current world name
				else {
					worldName = world.getName();
				}

				// create graveyard object
				graveyard = new Graveyard.Builder(plugin)
						.primaryKey(rs.getInt("Key"))
						.displayName(rs.getString("displayName"))
						.searchKey(rs.getString("searchKey"))
						.enabled(rs.getBoolean("enabled"))
						.hidden(rs.getBoolean("hidden"))
						.discoveryRange(rs.getInt("discoveryRange"))
						.discoveryMessage(rs.getString("discoveryMessage"))
						.respawnMessage(rs.getString("respawnMessage"))
						.group(rs.getString("groupName"))
						.safetyRange(rs.getInt("safetyRange"))
						.safetyTime(Duration.ofSeconds(rs.getInt("safetyTime")))
						.worldName(worldName)
						.worldUid(worldUid)
						.x(rs.getDouble("x"))
						.y(rs.getDouble("y"))
						.z(rs.getDouble("z"))
						.yaw(rs.getFloat("yaw"))
						.pitch(rs.getFloat("pitch"))
						.build();
			}
		}
		catch (SQLException e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to " +
					"select a Graveyard record from the SQLite database.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (Config.DEBUG.getBoolean(plugin.getConfig()))
			{
				e.printStackTrace();
			}

			return Optional.empty();
		}

		return Optional.ofNullable(graveyard);
	}


	@Override
	public Optional<Graveyard> selectNearestGraveyard(final Player player)
	{
		// if player is null, return empty optional graveyard record
		if (player == null)
		{
			return Optional.empty();
		}

		Location playerLocation = player.getLocation();

		long worldUidMsb = player.getWorld().getUID().getMostSignificantBits();
		long worldUidLsb = player.getWorld().getUID().getLeastSignificantBits();

		long playerUidMsb = player.getUniqueId().getMostSignificantBits();
		long playerUidLsb = player.getUniqueId().getLeastSignificantBits();

		Graveyard closest = null;

		try
		{
			PreparedStatement preparedStatement =
					connection.prepareStatement(Queries.getQuery("SelectNearestGraveyards"));

			preparedStatement.setLong(1, worldUidMsb);
			preparedStatement.setLong(2, worldUidLsb);
			preparedStatement.setLong(3, playerUidMsb);
			preparedStatement.setLong(4, playerUidLsb);

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next())
			{
				String groupName = rs.getString("GroupName");
				String worldName = rs.getString("WorldName");

				UUID worldUid = new UUID(worldUidMsb, worldUidLsb);
				World world = plugin.getServer().getWorld(worldUid);

				if (world == null)
				{
					plugin.getLogger().warning("Stored record has invalid world: "
							+ worldName + ". Skipping record.");
					continue;
				}

				final Graveyard graveyard = createGraveyard(rs, groupName, world, worldUid);

				// if graveyard optional location has no value, skip to next graveyard
				if (graveyard.getOptLocation().isEmpty())
				{
					continue;
				}

				// unwrap graveyard optional location
				Location location = graveyard.getOptLocation().get();

				// check if graveyard has group and player is in group
				if (groupName == null || groupName.isEmpty() || player.hasPermission("group." + groupName))
				{
					// if closest is null, set to this graveyard (first pass through loop)
					if (closest == null)
					{
						closest = graveyard;
					}

					// else if closest graveyard has valid location, check if graveyard is closer than current closest
					else if (closest.getOptLocation().isPresent())
					{
						if (location.distanceSquared(playerLocation) < closest.getOptLocation().get().distanceSquared(playerLocation))
						{
							closest = graveyard;
						}
					}
				}
			}
		}
		catch (SQLException e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "fetch the select Graveyard from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (Config.DEBUG.getBoolean(plugin.getConfig())) {
				e.printStackTrace();
			}
		}

		// return closest result
		return Optional.ofNullable(closest);
	}


	private @NotNull Graveyard createGraveyard(ResultSet rs, String groupName, World world, UUID worldUid) throws SQLException {
		return new Graveyard.Builder(plugin)
				.primaryKey(rs.getInt("Key"))
				.searchKey(rs.getString("SearchKey"))
				.displayName(rs.getString("DisplayName"))
				.enabled(rs.getBoolean("Enabled"))
				.hidden(rs.getBoolean("Hidden"))
				.discoveryRange(rs.getInt("DiscoveryRange"))
				.discoveryMessage(rs.getString("DiscoveryMessage"))
				.respawnMessage(rs.getString("RespawnMessage"))
				.group(groupName)
				.safetyRange(rs.getInt("SafetyRange"))
				.safetyTime(Duration.ofSeconds(rs.getInt("SafetyTime")))
				.worldName(world.getName())
				.worldUid(worldUid)
				.x(rs.getDouble("X"))
				.y(rs.getDouble("Y"))
				.z(rs.getDouble("Z"))
				.yaw(rs.getFloat("Yaw"))
				.pitch(rs.getFloat("Pitch"))
				.build();
	}


	@Override
	public List<String> selectMatchingGraveyardNames(final String match)
	{
		// if match is null, return empty list
		if (match == null)
		{
			return Collections.emptyList();
		}

		// create empty return list
		List<String> returnList = new ArrayList<>();

		try
		{
			PreparedStatement preparedStatement =
					connection.prepareStatement(Queries.getQuery("SelectMatchingGraveyardNames"));

			preparedStatement.setString(1, match.toLowerCase() + "%");

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next())
			{
				returnList.add(rs.getString("SearchKey"));
			}
		}
		catch (SQLException e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "fetch matching Graveyard records from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (Config.DEBUG.getBoolean(plugin.getConfig())) {
				e.printStackTrace();
			}
		}

		// return list of search key strings
		return returnList;
	}


	@Override
	public Collection<Graveyard> selectUndiscoveredGraveyards(final Player player)
	{
		// if player is null, return empty set
		if (player == null)
		{
			return Collections.emptySet();
		}

		// create empty set of Graveyard for return
		Collection<Graveyard> returnSet = new HashSet<>();

		try
		{
			PreparedStatement preparedStatement =
					connection.prepareStatement(Queries.getQuery("SelectUndiscoveredGraveyards"));

			preparedStatement.setLong(1, player.getWorld().getUID().getMostSignificantBits());
			preparedStatement.setLong(2, player.getWorld().getUID().getLeastSignificantBits());
			preparedStatement.setLong(3, player.getUniqueId().getMostSignificantBits());
			preparedStatement.setLong(4, player.getUniqueId().getLeastSignificantBits());

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next())
			{
				// get stored world name
				String worldName = rs.getString("WorldName");

				// get world uid components
				long worldUidMsb = rs.getLong("WorldUidMsb");
				long worldUidLsb = rs.getLong("WorldUidLsb");

				// reconstitute world uid from components
				UUID worldUid = new UUID(worldUidMsb, worldUidLsb);

				// get world by uid
				World world = plugin.getServer().getWorld(worldUid);

				// if world is null, log error and skip to next record
				if (world == null)
				{
					plugin.getLogger().warning("Stored record has unloaded world: "
							+ worldName + ". Skipping record.");
					continue;
				}

				final Graveyard graveyard = createGraveyard(rs, rs.getString("GroupName"), world, worldUid);

				returnSet.add(graveyard);
			}
		}
		catch (Exception e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "select undiscovered Graveyard records from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (Config.DEBUG.getBoolean(plugin.getConfig()))
			{
				e.printStackTrace();
			}
		}

		// return results
		return returnSet;
	}


	@Override
	public Collection<String> selectDiscoveredKeys(final UUID playerUid)
	{
		// if playerUid is null, return empty set
		if (playerUid == null)
		{
			return Collections.emptySet();
		}

		// create empty set of Graveyard for return
		Collection<String> returnSet = new HashSet<>();

		try
		{
			PreparedStatement preparedStatement =
					connection.prepareStatement(Queries.getQuery("SelectGraveyardsKnownByPlayer"));

			preparedStatement.setLong(1, playerUid.getMostSignificantBits());
			preparedStatement.setLong(2, playerUid.getLeastSignificantBits());

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next())
			{
				// add display name to return set
				returnSet.add(rs.getString("searchKey"));
			}
		}
		catch (Exception e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "select discovered Graveyard records from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (Config.DEBUG.getBoolean(plugin.getConfig())) {
				e.printStackTrace();
			}
		}

		// return results
		return returnSet;
	}


	@Override
	public Collection<String> selectUndiscoveredKeys(final Player player) {

		// if player is null, return empty set
		if (player == null)
		{
			return Collections.emptySet();
		}

		// create empty set for return
		Collection<String> returnSet = new HashSet<>();

		try
		{
			PreparedStatement preparedStatement =
					connection.prepareStatement(Queries.getQuery("SelectUndiscoveredGraveyardKeys"));

			preparedStatement.setLong(1, player.getWorld().getUID().getMostSignificantBits());
			preparedStatement.setLong(2, player.getWorld().getUID().getLeastSignificantBits());
			preparedStatement.setLong(3, player.getUniqueId().getMostSignificantBits());
			preparedStatement.setLong(4, player.getUniqueId().getLeastSignificantBits());

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next())
			{
				returnSet.add(rs.getString("SearchKey"));
			}
		}
		catch (SQLException e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "select undiscovered Graveyard keys from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (Config.DEBUG.getBoolean(plugin.getConfig()))
			{
				e.getStackTrace();
			}
		}

		// return results
		return returnSet;
	}


	@Override
	public void insertDiscovery(final Discovery discovery)
	{
		// if discovery is null, do nothing and return
		if (discovery == null)
		{
			return;
		}

		final UUID playerUid = discovery.playerUid();
		final String searchKey = discovery.searchKey();

		new BukkitRunnable() {
			@Override
			public void run() {

				try
				{
					// synchronize on instance
					synchronized (this)
					{

						PreparedStatement preparedStatement =
								connection.prepareStatement(Queries.getQuery("InsertDiscovery"));

						preparedStatement.setString(1, Graveyard.createSearchKey(searchKey));
						preparedStatement.setLong(2, playerUid.getMostSignificantBits());
						preparedStatement.setLong(3, playerUid.getLeastSignificantBits());

						// execute prepared statement
						preparedStatement.executeUpdate();
					}
				}
				catch (SQLException e)
				{
					// output simple error message
					plugin.getLogger().warning("An error occurred while trying to "
							+ "insert a record into the discovered table in the SQLite datastore.");
					plugin.getLogger().warning(e.getLocalizedMessage());

					// if debugging is enabled, output stack trace
					if (Config.DEBUG.getBoolean(plugin.getConfig()))
					{
						e.printStackTrace();
					}
				}
			}
		}.runTaskAsynchronously(plugin);
	}


	@Override
	public int insertDiscoveries(final Collection<Discovery> discoveries)
	{
		// if discoveries is null, return int 0
		if (discoveries == null) {
			if (Config.DEBUG.getBoolean(plugin.getConfig()))
			{
				plugin.getLogger().warning("Could not insert graveyard records in data store "
						+ "because collection is null!");
			}
			return 0;
		}

		int count = 0;

		for (Discovery record : discoveries)
		{
			try
			{
				// synchronize on instance
				synchronized (this)
				{
					PreparedStatement preparedStatement =
							connection.prepareStatement(Queries.getQuery("InsertDiscovery"));

					preparedStatement.setString(1, record.searchKey());
					preparedStatement.setLong(2, record.playerUid().getMostSignificantBits());
					preparedStatement.setLong(3, record.playerUid().getLeastSignificantBits());

					// execute prepared statement
					preparedStatement.executeUpdate();

					// increment count
					count++;
				}
			}
			catch (SQLException e)
			{
				// output simple error message
				plugin.getLogger().warning("An error occurred while trying to "
						+ "insert a record into the discovered table in the SQLite datastore.");
				plugin.getLogger().warning(e.getLocalizedMessage());

				// if debugging is enabled, output stack trace
				if (Config.DEBUG.getBoolean(plugin.getConfig())) {
					e.printStackTrace();
				}
			}
		}

		return count;
	}


	@Override
	public int insertGraveyards(final Collection<Graveyard> graveyards)
	{
		// if graveyard collection is null, do nothing and return
		if (graveyards == null)
		{
			if (Config.DEBUG.getBoolean(plugin.getConfig()))
			{
				plugin.getLogger().warning("Could not insert graveyard records in data store "
						+ "because collection is null!");
			}
			return 0;
		}

		int count = 0;

		for (Graveyard graveyard : graveyards)
		{
			// get world name from record
			String worldName = graveyard.getWorldName();

			// get world uid from record
			UUID worldUid = graveyard.getWorldUid();

			// get world by uid
			final World world = plugin.getServer().getWorld(graveyard.getWorldUid());

			// if world is null, log warning
			if (world == null) {
				plugin.getLogger().warning("Record has invalid world: " + worldName);
			}
			// else get current world name
			else {
				worldName = world.getName();
			}

			try {
				// synchronize on connection
				synchronized (this)
				{
					// create prepared statement
					PreparedStatement preparedStatement =
							connection.prepareStatement(Queries.getQuery("InsertGraveyard"));

					preparedStatement.setString(1, graveyard.getSearchKey());
					preparedStatement.setString(2, graveyard.getDisplayName());
					preparedStatement.setBoolean(3, graveyard.isEnabled());
					preparedStatement.setBoolean(4, graveyard.isHidden());
					preparedStatement.setInt(5, graveyard.getDiscoveryRange());
					preparedStatement.setString(6, graveyard.getDiscoveryMessage());
					preparedStatement.setString(7, graveyard.getRespawnMessage());
					preparedStatement.setString(8, graveyard.getGroup());
					preparedStatement.setInt(9, graveyard.getSafetyRange());
					preparedStatement.setLong(10, graveyard.getSafetyTime().getSeconds());
					preparedStatement.setString(11, worldName);
					preparedStatement.setLong(12, worldUid.getMostSignificantBits());
					preparedStatement.setLong(13, worldUid.getLeastSignificantBits());
					preparedStatement.setDouble(14, graveyard.getX());
					preparedStatement.setDouble(15, graveyard.getY());
					preparedStatement.setDouble(16, graveyard.getZ());
					preparedStatement.setFloat(17, graveyard.getYaw());
					preparedStatement.setFloat(18, graveyard.getPitch());

					// execute prepared statement
					preparedStatement.executeUpdate();
				}
			}
			catch (Exception e)
			{
				// output simple error message
				plugin.getLogger().warning("An error occurred while inserting a Graveyard record "
						+ "into the SQLite datastore.");
				plugin.getLogger().warning(e.getLocalizedMessage());

				// if debugging is enabled, output stack trace
				if (Config.DEBUG.getBoolean(plugin.getConfig()))
				{
					e.printStackTrace();
				}
			}
			count++;
		}
		return count;
	}


	@Override
	public void updateGraveyard(final Graveyard graveyard)
	{

		// if graveyard is null do nothing and return
		if (graveyard == null)
		{
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {

				try
				{
					// synchronize on connection
					synchronized (this)
					{
						// create prepared statement
						PreparedStatement preparedStatement =
								connection.prepareStatement(Queries.getQuery("UpdateGraveyard"));

						preparedStatement.setString(1, graveyard.getSearchKey());
						preparedStatement.setString(2, graveyard.getDisplayName());
						preparedStatement.setBoolean(3, graveyard.isEnabled());
						preparedStatement.setBoolean(4, graveyard.isHidden());
						preparedStatement.setInt(5, graveyard.getDiscoveryRange());
						preparedStatement.setString(6, graveyard.getDiscoveryMessage());
						preparedStatement.setString(7, graveyard.getRespawnMessage());
						preparedStatement.setString(8, graveyard.getGroup());
						preparedStatement.setInt(9, graveyard.getSafetyRange());
						preparedStatement.setLong(10, graveyard.getSafetyTime().getSeconds());
						preparedStatement.setString(11, graveyard.getWorldName());
						preparedStatement.setLong(12, graveyard.getWorldUid().getMostSignificantBits());
						preparedStatement.setLong(13, graveyard.getWorldUid().getLeastSignificantBits());
						preparedStatement.setDouble(14, graveyard.getX());
						preparedStatement.setDouble(15, graveyard.getY());
						preparedStatement.setDouble(16, graveyard.getZ());
						preparedStatement.setFloat(17, graveyard.getYaw());
						preparedStatement.setFloat(18, graveyard.getPitch());
						preparedStatement.setInt(19, graveyard.getPrimaryKey());

						// execute prepared statement
						preparedStatement.executeUpdate();
					}
				}
				catch (SQLException e)
				{
					// output simple error message
					plugin.getLogger().warning("An error occurred while trying to " +
							"update a Graveyard record into the SQLite datastore.");
					plugin.getLogger().warning(e.getLocalizedMessage());

					// if debugging is enabled, output stack trace
					if (Config.DEBUG.getBoolean(plugin.getConfig())) {
						e.printStackTrace();
					}
				}
			}
		}.runTaskAsynchronously(plugin);
	}


	@Override
	public Optional<Graveyard> deleteGraveyard(final String displayName)
	{
		// if displayName is null, return empty optional
		if (displayName == null)
		{
			return Optional.empty();
		}

		// get destination record to be deleted, for return
		final Optional<Graveyard> graveyard = this.selectGraveyard(displayName);

		new BukkitRunnable() {
			@Override
			public void run() {

				int rowsAffected;
				try
				{
					// synchronize on connection
					synchronized (this)
					{
						// create prepared statement
						PreparedStatement preparedStatement =
								connection.prepareStatement(Queries.getQuery("DeleteGraveyard"));

						preparedStatement.setString(1, Graveyard.createSearchKey(displayName));

						// execute prepared statement
						rowsAffected = preparedStatement.executeUpdate();
					}

					// output debugging information
					if (Config.DEBUG.getBoolean(plugin.getConfig()))
					{
						plugin.getLogger().info(rowsAffected + " graveyards deleted.");
					}
				}
				catch (SQLException e)
				{
					// output simple error message
					plugin.getLogger().warning("An error occurred while attempting to "
							+ "delete a Graveyard record from the SQLite datastore.");
					plugin.getLogger().warning(e.getLocalizedMessage());

					// if debugging is enabled, output stack trace
					if (Config.DEBUG.getBoolean(plugin.getConfig()))
					{
						e.getStackTrace();
					}
				}
			}
		}.runTaskAsynchronously(plugin);

		return graveyard;
	}


	private Collection<Discovery> selectAllDiscoveries()
	{
		Collection<Discovery> returnSet = new HashSet<>();

		if (schemaVersion == 0)
		{
			try
			{
				PreparedStatement preparedStatement =
						connection.prepareStatement(Queries.getQuery("SelectAllDiscoveryRecordsV0"));

				// execute sql query
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
						if (Config.DEBUG.getBoolean(plugin.getConfig()))
						{
							e.printStackTrace();
						}
						continue;
					}

					// create new discovery record
					Discovery record = new Discovery(searchKey, playerUid);

					// add record to return set
					returnSet.add(record);
				}
			}
			catch (SQLException e)
			{
				plugin.getLogger().warning("An error occurred while trying to " +
						"select all discovery records from the SQLite datastore.");
				plugin.getLogger().warning(e.getLocalizedMessage());
				if (Config.DEBUG.getBoolean(plugin.getConfig()))
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			try {
				PreparedStatement preparedStatement =
						connection.prepareStatement(Queries.getQuery("SelectAllDiscoveryRecords"));

				// execute sql query
				ResultSet rs = preparedStatement.executeQuery();

				while (rs.next())
				{
					// get primary key
					String key = rs.getString("GraveyardSearchKey");

					// get player uid components
					long playerUidMsb = rs.getLong("PlayerUidMsb");
					long playerUidLsb = rs.getLong("PlayerUidLsb");

					// reconstitute player uid from components
					UUID playerUid = new UUID(playerUidMsb, playerUidLsb);

					// create new discovery record
					Discovery record = new Discovery(key, playerUid);

					// add record to return set
					returnSet.add(record);
				}

				// close statement
				preparedStatement.close();
			}
			catch (SQLException e)
			{
				plugin.getLogger().warning("An error occurred while trying to " +
						"select all discovery records from the SQLite datastore.");
				plugin.getLogger().warning(e.getLocalizedMessage());
				if (Config.DEBUG.getBoolean(plugin.getConfig())) {
					e.printStackTrace();
				}
			}
		}

		return returnSet;
	}


	@Override
	public Collection<String> selectPlayersWithDiscoveries()
	{
		Collection<String> returnSet = new HashSet<>();

		try
		{
			PreparedStatement preparedStatement =
					connection.prepareStatement(Queries.getQuery("SelectPlayersWithDiscovery"));

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next())
			{
				// get player uid components
				long playerUidMsb = rs.getLong("PlayerUidMsb");
				long playerUidLsb = rs.getLong("PlayerUidLsb");

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

			// close statement
			preparedStatement.close();
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning("An error occurred while trying to " +
					"select all discovery records from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
			if (Config.DEBUG.getBoolean(plugin.getConfig())) {
				e.printStackTrace();
			}
		}

		return returnSet;
	}


	@Override
	public boolean deleteDiscovery(final String displayName, final UUID playerUid)
	{
		// if parameter is null, return false
		if (displayName == null || playerUid == null)
		{
			return false;
		}

		int rowsAffected;
		boolean result = true;

		try
		{
			// synchronize on connection
			synchronized (this)
			{
				// create prepared statement
				PreparedStatement preparedStatement =
						connection.prepareStatement(Queries.getQuery("DeleteDiscovery"));

				preparedStatement.setLong(1, playerUid.getMostSignificantBits());
				preparedStatement.setLong(2, playerUid.getLeastSignificantBits());
				preparedStatement.setString(3, Graveyard.createSearchKey(displayName));

				// execute prepared statement
				rowsAffected = preparedStatement.executeUpdate();
			}

			if (rowsAffected < 1)
			{
				result = false;
			}

			// output debugging information
			if (Config.DEBUG.getBoolean(plugin.getConfig()))
			{
				plugin.getLogger().info(rowsAffected + " discoveries deleted.");
			}
		}
		catch (SQLException e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while attempting to "
					+ "delete a Discovery record from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (Config.DEBUG.getBoolean(plugin.getConfig())) {
				e.printStackTrace();
			}
		}
		return result;
	}


	@Override
	public int selectGraveyardCount()
	{
		int count = 0;

		try
		{
			PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectGraveyardCount"));
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next())
			{
				count = rs.getInt("GraveyardCount");
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning("An error occurred while attempting to retrieve a count of all graveyard records.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return count;
	}

}
