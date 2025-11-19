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

package com.winterhavenmc.savagegraveyards.datastore.sqlite;

import com.winterhavenmc.library.messagebuilder.adapters.resources.configuration.BukkitConfigRepository;
import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;

import com.winterhavenmc.savagegraveyards.datastore.DatastoreMessage;
import com.winterhavenmc.savagegraveyards.datastore.sqlite.schema.*;
import com.winterhavenmc.savagegraveyards.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.datastore.GraveyardRepository;

import com.winterhavenmc.savagegraveyards.models.discovery.Discovery;
import com.winterhavenmc.savagegraveyards.models.graveyard.Graveyard;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;
import java.util.logging.Logger;

import static com.winterhavenmc.savagegraveyards.datastore.DatastoreMessage.DATASTORE_NAME;


public final class SqliteConnectionProvider implements ConnectionProvider
{

	private final Plugin plugin;
	private final ConfigRepository configRepository;
	private final String dataFilePath;
	private Connection connection;
	private boolean initialized;

	private SqliteDiscoveryRepository discoveryRepository;
	private SqliteGraveyardRepository graveyardRepository;


	private SqliteConnectionProvider(final Plugin plugin)
	{
		this.plugin = plugin;
		this.configRepository = BukkitConfigRepository.create(plugin);
		this.dataFilePath = plugin.getDataFolder() + File.separator + "graveyards.db";
	}


	public static ConnectionProvider create(final Plugin plugin)
	{
		ConnectionProvider connectionProvider = new SqliteConnectionProvider(plugin);
		return connectionProvider.connect();
	}


	public static int getSchemaVersion(final Connection connection,
	                                   final ConfigRepository configRepository,
	                                   final Logger logger)
	{
		int version = 0;
		try (PreparedStatement statement = connection.prepareStatement(SqliteQueries.getQuery("GetUserVersion")))
		{
			try (ResultSet resultSet = statement.executeQuery())
			{
				if (resultSet.next())
				{
					version = resultSet.getInt(1);
				}
			}
		}
		catch (SQLException sqlException)
		{
			logger.warning(DatastoreMessage.SCHEMA_VERSION_ERROR.getLocalizedMessage(configRepository.locale()));
			logger.warning(sqlException.getLocalizedMessage());
		}

		return version;
	}


	public boolean hasSchemaVersion()
	{
		return hasSchemaVersion(connection, plugin.getLogger(), configRepository);
	}


	public static boolean hasSchemaVersion(final Connection connection,
	                                       final Logger logger,
	                                       final ConfigRepository configRepository)
	{
		try (PreparedStatement statement = connection.prepareStatement(SqliteQueries.getQuery("GetUserVersion")))
		{
			try (ResultSet resultSet = statement.executeQuery())
			{
				return resultSet.next();
			}
		}
		catch (SQLException sqlException)
		{
			logger.warning(DatastoreMessage.SCHEMA_VERSION_ERROR.getLocalizedMessage(configRepository.locale()));
			logger.warning(sqlException.getLocalizedMessage());
		}
		return false;
	}


	/**
	 * Initialize datastore
	 */
	@Override
	public ConnectionProvider connect()
	{
		// initialize data store
		try
		{
			this.initialize();
		}
		catch (Exception exception)
		{
			plugin.getLogger().severe("Could not initialize the datastore!");
			plugin.getLogger().severe(exception.getLocalizedMessage());
		}

		// return initialized data store
		return this;
	}


	/**
	 * Close SQLite datastore connection
	 */
	@Override
	public void close()
	{
		try
		{
			connection.close();
			plugin.getLogger().info(DatastoreMessage.DATASTORE_CLOSED_NOTICE.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.DATASTORE_CLOSE_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getMessage());
		}

		this.initialized = false;
	}


	/**
	 * Get instance of GraveyardRepository
	 *
	 * @return {@link GraveyardRepository}
	 */
	@Override
	public GraveyardRepository graveyards()
	{
		return this.graveyardRepository;
	}


	/**
	 * Get instance of DiscoveryRepository
	 *
	 * @return {@link DiscoveryRepository}
	 */
	@Override
	public DiscoveryRepository discoveries()
	{
		return this.discoveryRepository;
	}


	/**
	 * Initialize datastore
	 */
	private void initialize() throws SQLException, ClassNotFoundException
	{
		// if data store is already initialized, log and return
		if (this.initialized)
		{
			plugin.getLogger().info(DatastoreMessage.DATASTORE_INITIALIZED_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
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
		enableForeignKeys(connection, configRepository);

		// update schema if necessary
		SchemaUpdater schemaUpdater = SchemaUpdater.create(plugin, connection, configRepository);
		createTables(schemaUpdater);
		schemaUpdater.update();

		// instantiate discovery repository
		RowMapper<Discovery> discoveryRowMapper = selectDiscoveryRowMapper(getSchemaVersion(connection, configRepository, plugin.getLogger()));
		discoveryRepository = new SqliteDiscoveryRepository(connection, configRepository, plugin.getLogger());

		// instantiate graveyard repository
		RowMapper<Graveyard> graveyardRowMapper = selectGraveyardRowMapper(getSchemaVersion(connection, configRepository, plugin.getLogger()));
		graveyardRepository = new SqliteGraveyardRepository(connection, configRepository, graveyardRowMapper, plugin.getLogger());

		// set initialized field true
		this.initialized = true;

		// log success
		plugin.getLogger().info(DatastoreMessage.DATASTORE_INITIALIZED_NOTICE.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
	}


	public static RowMapper<Graveyard> selectGraveyardRowMapper(final int version)
	{
		return switch (version)
		{
			case 0 -> new Version0.GraveyardRowMapper();
			case 1 -> new Version1.GraveyardRowMapper();
			default -> new Version2.GraveyardRowMapper();
		};
	}


	public static RowMapper<Discovery> selectDiscoveryRowMapper(final int version)
	{
		return switch (version)
		{
			case 0 -> new Version0.DiscoveryRowMapper();
			case 1 -> new Version1.DiscoveryRowMapper();
			default -> new Version2.DiscoveryRowMapper();
		};
	}


	private void createTables(final SchemaUpdater schemaUpdater)
	{
		// if no schema version ste, set current schema version
		if (!hasSchemaVersion(connection, plugin.getLogger(), configRepository))
		{
			schemaUpdater.setSchemaVersion(connection, plugin.getLogger(), configRepository, Schema.VERSION);
		}

		// create tables if necessary
		createGraveyardTable(connection, configRepository);
		createDiscoveryTable(connection, configRepository);
	}


	private void enableForeignKeys(final Connection connection, final ConfigRepository configRepository)
	{
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("EnableForeignKeys"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().severe(DatastoreMessage.DATASTORE_FOREIGN_KEYS_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
		}
	}


	private void createGraveyardTable(final Connection connection, final ConfigRepository configRepository)
	{
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("CreateGraveyardTable"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.CREATE_GRAVEYARD_TABLE_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}
	}


	private void createDiscoveryTable(final Connection connection, final ConfigRepository configRepository)
	{
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("CreateDiscoveryTable"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.CREATE_DISCOVERY_TABLE_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}
	}

}
