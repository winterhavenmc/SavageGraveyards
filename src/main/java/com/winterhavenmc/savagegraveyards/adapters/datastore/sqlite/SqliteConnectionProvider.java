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

import com.winterhavenmc.library.messagebuilder.resources.configuration.LocaleProvider;
import com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite.schema.SqliteSchemaUpdater;
import com.winterhavenmc.savagegraveyards.plugin.ports.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.plugin.ports.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.plugin.ports.datastore.GraveyardRepository;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;


public class SqliteConnectionProvider implements ConnectionProvider
{
	private final Plugin plugin;
	private final LocaleProvider localeProvider;
	private final String dataFilePath;
	private Connection connection;
	private boolean initialized;

	private SqliteDiscoveryRepository discoveryRepository;
	private SqliteGraveyardRepository graveyardRepository;


	public SqliteConnectionProvider(final Plugin plugin)
	{
		this.plugin = plugin;
		this.localeProvider = LocaleProvider.create(plugin);
		this.dataFilePath = plugin.getDataFolder() + File.separator + "graveyards.db";
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
	@Override
	public void connect() throws SQLException, ClassNotFoundException
	{
		// if data store is already initialized, log and return
		if (this.initialized)
		{
			plugin.getLogger().info(SqliteMessage.DATASTORE_INITIALIZED_ERROR.getLocalizedMessage(localeProvider.getLocale()));
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
		enableForeignKeys(connection, localeProvider);

		// instantiate datastore adapters
		discoveryRepository = new SqliteDiscoveryRepository(plugin.getLogger(), connection, localeProvider);
		graveyardRepository = new SqliteGraveyardRepository(plugin.getLogger(), connection, localeProvider);

		// update schema if necessary
		SqliteSchemaUpdater schemaUpdater = SqliteSchemaUpdater.create(plugin, connection, localeProvider, graveyardRepository, discoveryRepository);
		schemaUpdater.update();

		// create tables if necessary
		createGraveyardTable(connection, localeProvider);
		createDiscoveryTable(connection, localeProvider);

		// set initialized true
		this.initialized = true;
		plugin.getLogger().info(SqliteMessage.DATASTORE_INITIALIZED_NOTICE.getLocalizedMessage(localeProvider.getLocale()));
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
			plugin.getLogger().info(SqliteMessage.DATASTORE_CLOSED_NOTICE.getLocalizedMessage(localeProvider.getLocale()));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.DATASTORE_CLOSE_ERROR.getLocalizedMessage(localeProvider.getLocale()));
			plugin.getLogger().warning(sqlException.getMessage());
		}

		this.initialized = false;
	}


	private void enableForeignKeys(final Connection connection, final LocaleProvider localeProvider)
	{
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("EnableForeignKeys"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().severe(SqliteMessage.DATASTORE_FOREIGN_KEYS_ERROR.getLocalizedMessage(localeProvider.getLocale()));
		}
	}


	private void createGraveyardTable(final Connection connection, final LocaleProvider localeProvider)
	{
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("CreateGraveyardsTable"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.CREATE_GRAVEYARD_TABLE_ERROR.getLocalizedMessage(localeProvider.getLocale()));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}
	}


	private void createDiscoveryTable(final Connection connection, final LocaleProvider localeProvider)
	{
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("CreateDiscoveredTable"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.CREATE_DISCOVERY_TABLE_ERROR.getLocalizedMessage(localeProvider.getLocale()));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}
	}

}
