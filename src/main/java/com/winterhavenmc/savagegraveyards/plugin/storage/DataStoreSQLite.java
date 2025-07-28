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
import com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite.SqliteDiscoveryRepository;
import com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite.SqliteGraveyardRepository;
import com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite.SQLiteNotice;
import com.winterhavenmc.savagegraveyards.plugin.ports.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.plugin.ports.datastore.GraveyardRepository;
import com.winterhavenmc.savagegraveyards.plugin.util.Config;

import org.bukkit.plugin.Plugin;

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

	private DiscoveryRepository discoveryRepository;
	private GraveyardRepository graveyardRepository;


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
	public DiscoveryRepository discoveries()
	{
		return discoveryRepository;
	}


	@Override
	public GraveyardRepository graveyards()
	{
		return graveyardRepository;
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

		// instantiate datastore adapters
		discoveryRepository = new SqliteDiscoveryRepository(plugin.getLogger(), connection);
		graveyardRepository = new SqliteGraveyardRepository(plugin.getLogger(), connection);
	}


	private void enableForeignKeys() throws SQLException
	{
		// create statement
		Statement statement = connection.createStatement();

		// enable foreign keys
		statement.executeUpdate(Queries.getQuery("EnableForeignKeys"));

		if (Config.DEBUG.getBoolean(plugin.getConfig()))
		{
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
			ResultSet resultSet = statement.executeQuery(Queries.getQuery("GetUserVersion"));

			// get user version
			while (resultSet.next())
			{
				version = resultSet.getInt(1);

				if (Config.DEBUG.getBoolean(plugin.getConfig()))
				{
					plugin.getLogger().info("Read schema version: " + version);
				}
			}

			// close statement
			statement.close();
		}

		catch (SQLException sqlException)
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
				Collection<Graveyard.Valid> existingGraveyardRecords = graveyards().getAllValid();

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
				count = graveyards().saveAll(existingGraveyardRecords);
				plugin.getLogger().info(count + " graveyard records migrated to schema v1.");

				// insert all discovery records into discovered table
				count = discoveries().saveAll(existingDiscoveryRecords);
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
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SQLiteNotice.DATABASE_CLOSE_FAILED.toString());
			plugin.getLogger().warning(sqlException.getMessage());
		}

		setInitialized(false);
	}


	@Override
	public boolean delete()
	{
		// get path name to data store file
		File dataStoreFile = new File(dataFilePath);
		boolean result = false;
		if (dataStoreFile.exists())
		{
			result = dataStoreFile.delete();
		}
		return result;
	}


	private Set<Discovery.Valid> selectAllDiscoveries()
	{
		return (schemaVersion == 0)
				? discoveries().getAll_v0()
				: discoveries().getAll_V1();
	}

}
