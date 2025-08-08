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

package com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite.schema;

import com.winterhavenmc.library.messagebuilder.resources.configuration.LocaleProvider;
import com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite.SqliteMessage;
import com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite.SqliteQueries;
import com.winterhavenmc.savagegraveyards.plugin.models.discovery.Discovery;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.plugin.ports.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.plugin.ports.datastore.GraveyardRepository;
import com.winterhavenmc.savagegraveyards.plugin.util.Config;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;


public final class SqliteSchemaUpdaterV0 implements SqliteSchemaUpdater
{
	private final Plugin plugin;
	private final Connection connection;
	private final LocaleProvider localeProvider;
	private final GraveyardRepository graveyardRepository;
	private final DiscoveryRepository discoveryRepository;


	SqliteSchemaUpdaterV0(final Plugin plugin,
	                      final Connection connection,
	                      final LocaleProvider localeProvider,
	                      final GraveyardRepository graveyardRepository,
	                      final DiscoveryRepository discoveryRepository)
	{
		this.plugin = plugin;
		this.connection = connection;
		this.localeProvider = localeProvider;
		this.graveyardRepository = graveyardRepository;
		this.discoveryRepository = discoveryRepository;
	}


	@Override
	public void update()
	{
		// read schema version from database (pragma user_version)
		int schemaVersion = getSchemaVersion(connection);

		// if schema version is 0, migrate tables to schema version 1
		if (schemaVersion == 0)
		{
			if (tableExists(connection, "Graveyards"))
			{
				int count;

				// select all graveyard records
				Collection<Graveyard.Valid> existingGraveyardRecords = graveyardRepository.getAllValid();

				// create statement object
				try (final Statement statement = connection.createStatement())
				{
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
				}
				catch (SQLException sqlException)
				{
					plugin.getLogger().warning(SqliteMessage.SCHEMA_UPDATE_ERROR.getLocalizeMessage(localeProvider.getLocale()));
					plugin.getLogger().warning(sqlException.getLocalizedMessage());
				}

				// insert all graveyard records into graveyards table
				count = graveyardRepository.saveAll(existingGraveyardRecords);
				plugin.getLogger().info(count + " graveyard records migrated to schema v1.");
			}

			if (tableExists(connection, "Discovered"))
			{
				int count;

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
					// create discovered table with new schema
					statement.executeUpdate(SqliteQueries.getQuery("CreateDiscoveredTable"));
					if (Config.DEBUG.getBoolean(plugin.getConfig()))
					{
						plugin.getLogger().info("Discovered table created.");
					}
				}
				catch (SQLException sqlException)
				{
					plugin.getLogger().warning(SqliteMessage.SCHEMA_UPDATE_V1_ERROR.getLocalizeMessage(localeProvider.getLocale()));
					plugin.getLogger().warning(sqlException.getLocalizedMessage());
				}

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
			plugin.getLogger().warning(SqliteMessage.SCHEMA_UPDATE_ERROR.getLocalizeMessage(localeProvider.getLocale()));
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
			plugin.getLogger().warning(SqliteMessage.NO_SCHEMA_VERSION_ERROR.getLocalizeMessage(localeProvider.getLocale()));
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

}
