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
import com.winterhavenmc.savagegraveyards.plugin.models.discovery.Discovery;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.plugin.ports.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.plugin.ports.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.plugin.ports.datastore.GraveyardRepository;
import com.winterhavenmc.savagegraveyards.plugin.util.Config;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;
import java.util.Collection;


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
			plugin.getLogger().info(SqliteNotice.ALREADY_INITIALIZED_NOTICE.getLocalizeMessage(localeProvider.getLocale()));
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
		enableForeignKeys(connection);

		// update schema if necessary
		updateSchema(connection);

		// set initialized true
		this.initialized = true;
		plugin.getLogger().info(this + " datastore initialized.");

		// instantiate datastore adapters
		discoveryRepository = new SqliteDiscoveryRepository(plugin.getLogger(), connection, localeProvider);
		graveyardRepository = new SqliteGraveyardRepository(plugin.getLogger(), connection, localeProvider);
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
			plugin.getLogger().info(SqliteNotice.DATASTORE_CLOSED_NOTICE.getLocalizeMessage(localeProvider.getLocale()));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteNotice.DATASTORE_CLOSE_ERROR.getLocalizeMessage(localeProvider.getLocale()));
			plugin.getLogger().warning(sqlException.getMessage());
		}

		this.initialized = false;
	}


	private void enableForeignKeys(final Connection connection)
	{
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("EnableForeignKeys"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().severe(SqliteNotice.ENABLE_FOREIGN_KEYS_ERROR.getLocalizeMessage(localeProvider.getLocale()));
		}
	}


	private void updateSchema(final Connection connection)
	{
		// read schema version from database (pragma user_version)
		int schemaVersion = getSchemaVersion(connection);

		// if schema version is 0, migrate tables to schema version 1
		if (schemaVersion == 0)
		{
			if (tableExists(connection))
			{
				int count;

				// select all graveyard records
				Collection<Graveyard.Valid> existingGraveyardRecords = graveyardRepository.getAllValid();

				// select all discovery records
				Collection<Discovery.Valid> existingDiscoveryRecords = discoveryRepository.getAll_v0();

				// create statement object
				try (final Statement statement = connection.createStatement())
				{
					// drop discovered table with old schema
					statement.executeUpdate(SqliteQueries.getQuery("DropDiscoveredTable"));
					if (Config.DEBUG.getBoolean(plugin.getConfig()))
					{
						plugin.getLogger().info("Discovered table dropped.");
					}

					// drop graveyards table with old schema
					statement.executeUpdate(SqliteQueries.getQuery("DropGraveyardsTable"));
					if (Config.DEBUG.getBoolean(plugin.getConfig()))
					{
						plugin.getLogger().info("Graveyards table dropped.");
					}

					// create graveyards table with new schema
					statement.executeUpdate(SqliteQueries.getQuery("CreateGraveyardsTable"));
					if (Config.DEBUG.getBoolean(plugin.getConfig()))
					{
						plugin.getLogger().info("Graveyards table created.");
					}

					// create discovered table with new schema
					statement.executeUpdate(SqliteQueries.getQuery("CreateDiscoveredTable"));
					if (Config.DEBUG.getBoolean(plugin.getConfig()))
					{
						plugin.getLogger().info("Discovered table created.");
					}
				}
				catch (SQLException sqlException)
				{
					plugin.getLogger().warning(SqliteNotice.SCHEMA_UPDATE_V1_ERROR.getLocalizeMessage(localeProvider.getLocale()));
					plugin.getLogger().warning(sqlException.getLocalizedMessage());
				}

				// insert all graveyard records into graveyards table
				count = graveyardRepository.saveAll(existingGraveyardRecords);
				plugin.getLogger().info(count + " graveyard records migrated to schema v1.");

				// insert all discovery records into discovered table
				count = discoveryRepository.saveAll(existingDiscoveryRecords);
				plugin.getLogger().info(count + " discovery records migrated to schema v1.");
			}
		}

		try (final Statement statement = connection.createStatement())
		{
			setSchemaVersion(connection, 1);
			statement.executeUpdate(SqliteQueries.getQuery("CreateGraveyardsTable"));
			statement.executeUpdate(SqliteQueries.getQuery("CreateDiscoveredTable"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteNotice.SCHEMA_UPDATE_ERROR.getLocalizeMessage(localeProvider.getLocale()));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}
	}


	private int getSchemaVersion(final Connection connection)
	{
		int version = 0;

		try (final Statement statement = connection.createStatement())
		{
			// execute query
			ResultSet resultSet = statement.executeQuery(SqliteQueries.getQuery("GetUserVersion"));

			// get user version
			if (resultSet.next())
			{
				version = resultSet.getInt(1);
			}
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteNotice.NO_SCHEMA_VERSION_ERROR.getLocalizeMessage(localeProvider.getLocale()));
		}
		return version;
	}


	@SuppressWarnings("SameParameterValue")
	private void setSchemaVersion(final Connection connection, final int version)
	{
		try (final Statement statement = connection.createStatement())
		{
			// update schema version in database
			statement.executeUpdate("PRAGMA user_version = " + version);
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning("Could not set schema user version!");
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}
	}


	/**
	 * Test for existence of graveyards table
	 *
	 * @return boolean {@code true} if table exists, {@code false} if not
	 */
	private boolean tableExists(final Connection connection)
	{
		boolean returnValue = false;

		try (final Statement statement = connection.createStatement())
		{
			ResultSet resultSet = statement.executeQuery(SqliteQueries.getQuery("SelectGraveyardsTable"));
			if (resultSet.next())
			{
				returnValue = true;
			}
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteNotice.TABLE_NOT_FOUND_ERROR.getLocalizeMessage(localeProvider.getLocale()));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		return returnValue;
	}

}
