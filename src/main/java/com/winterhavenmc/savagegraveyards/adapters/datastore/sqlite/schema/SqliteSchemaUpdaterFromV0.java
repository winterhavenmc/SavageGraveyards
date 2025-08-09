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

import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;


public final class SqliteSchemaUpdaterFromV0 implements SqliteSchemaUpdater
{
	private final Plugin plugin;
	private final Connection connection;
	private final LocaleProvider localeProvider;
	private final GraveyardRepository graveyardRepository;
	private final DiscoveryRepository discoveryRepository;


	SqliteSchemaUpdaterFromV0(final Plugin plugin,
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
		int schemaVersion = SqliteSchemaUpdater.getSchemaVersion(connection, plugin.getLogger(), localeProvider);
		if (schemaVersion == 0)
		{
			if (tableExists(connection, "Graveyards"))
			{
				updateGraveyardTableSchema(connection, schemaVersion);
			}

			if (tableExists(connection, "Discovered"))
			{
				updateDiscoveryTableSchema(connection, schemaVersion);
			}
		}
	}


	private void updateGraveyardTableSchema(final Connection connection, final int version)
	{
		int count;
		Collection<Graveyard.Valid> existingGraveyardRecords = graveyardRepository.getAllValid();
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("DropGraveyardsTable"));
			statement.executeUpdate(SqliteQueries.getQuery("CreateGraveyardsTable"));
			setSchemaVersion(connection, plugin.getLogger(), localeProvider, version);
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.SCHEMA_UPDATE_ERROR.getLocalizeMessage(localeProvider.getLocale()));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		count = graveyardRepository.saveAll(existingGraveyardRecords);
		plugin.getLogger().info(SqliteMessage.SCHEMA_GRAVEYARD_RECORDS_MIGRATED_NOTICE.getLocalizeMessage(localeProvider.getLocale(), count, version));
	}


	private void updateDiscoveryTableSchema(final Connection connection, final int version)
	{
		int count;
		Collection<Discovery.Valid> existingDiscoveryRecords = discoveryRepository.getAll_v0();
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("DropDiscoveredTable"));
			statement.executeUpdate(SqliteQueries.getQuery("CreateDiscoveredTable"));
			setSchemaVersion(connection, plugin.getLogger(), localeProvider, version);
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.SCHEMA_UPDATE_ERROR.getLocalizeMessage(localeProvider.getLocale()));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		count = discoveryRepository.saveAll(existingDiscoveryRecords);
		plugin.getLogger().info(SqliteMessage.SCHEMA_DISCOVERY_RECORDS_MIGRATED_NOTICE.getLocalizeMessage(localeProvider.getLocale(), count, version));
	}

}
