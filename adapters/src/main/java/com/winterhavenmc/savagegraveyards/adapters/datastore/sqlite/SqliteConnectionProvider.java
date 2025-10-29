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

package com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite;

import com.winterhavenmc.library.messagebuilder.adapters.resources.configuration.BukkitConfigRepository;
import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;
import com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite.schema.SqliteSchemaUpdater;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.GraveyardRepository;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;


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
		return new SqliteConnectionProvider(plugin);
	}


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
			plugin.getLogger().info(SqliteMessage.DATASTORE_CLOSED_NOTICE.getLocalizedMessage(configRepository.locale()));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.DATASTORE_CLOSE_ERROR.getLocalizedMessage(configRepository.locale()));
			plugin.getLogger().warning(sqlException.getMessage());
		}

		this.initialized = false;
	}


	@Override
	public GraveyardRepository graveyards()
	{
		return this.graveyardRepository;
	}


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
			plugin.getLogger().info(SqliteMessage.DATASTORE_INITIALIZED_ERROR.getLocalizedMessage(configRepository.locale()));
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

		// instantiate datastore adapters
		discoveryRepository = new SqliteDiscoveryRepository(plugin.getLogger(), connection, configRepository);
		graveyardRepository = new SqliteGraveyardRepository(plugin.getLogger(), connection, configRepository);

		// update schema if necessary
		SqliteSchemaUpdater schemaUpdater = SqliteSchemaUpdater.create(plugin, connection, configRepository, graveyardRepository, discoveryRepository);
		schemaUpdater.update();

		// create tables if necessary
		createGraveyardTable(connection, configRepository);
		createDiscoveryTable(connection, configRepository);

		// set initialized true
		this.initialized = true;
		plugin.getLogger().info(SqliteMessage.DATASTORE_INITIALIZED_NOTICE.getLocalizedMessage(configRepository.locale()));
	}


	private void enableForeignKeys(final Connection connection, final ConfigRepository configRepository)
	{
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("EnableForeignKeys"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().severe(SqliteMessage.DATASTORE_FOREIGN_KEYS_ERROR.getLocalizedMessage(configRepository.locale()));
		}
	}


	private void createGraveyardTable(final Connection connection, final ConfigRepository configRepository)
	{
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("CreateGraveyardsTable"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.CREATE_GRAVEYARD_TABLE_ERROR.getLocalizedMessage(configRepository.locale()));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}
	}


	private void createDiscoveryTable(final Connection connection, final ConfigRepository configRepository)
	{
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("CreateDiscoveredTable"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.CREATE_DISCOVERY_TABLE_ERROR.getLocalizedMessage(configRepository.locale()));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}
	}

}
