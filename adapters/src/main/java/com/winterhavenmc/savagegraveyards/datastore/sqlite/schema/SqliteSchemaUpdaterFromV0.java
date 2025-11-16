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

package com.winterhavenmc.savagegraveyards.datastore.sqlite.schema;

import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;
import com.winterhavenmc.savagegraveyards.datastore.DatastoreMessage;
import com.winterhavenmc.savagegraveyards.datastore.sqlite.SqliteQueries;
import com.winterhavenmc.savagegraveyards.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.datastore.GraveyardRepository;
import com.winterhavenmc.savagegraveyards.models.discovery.ValidDiscovery;
import com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard;

import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import static com.winterhavenmc.savagegraveyards.datastore.DatastoreMessage.DATASTORE_NAME;


public final class SqliteSchemaUpdaterFromV0 implements SqliteSchemaUpdater
{
	private final Plugin plugin;
	private final Connection connection;
	private final ConfigRepository configRepository;
	private final GraveyardRepository graveyardRepository;
	private final DiscoveryRepository discoveryRepository;


	SqliteSchemaUpdaterFromV0(final Plugin plugin,
	                          final Connection connection,
	                          final ConfigRepository configRepository,
	                          final GraveyardRepository graveyardRepository,
	                          final DiscoveryRepository discoveryRepository)
	{
		this.plugin = plugin;
		this.connection = connection;
		this.configRepository = configRepository;
		this.graveyardRepository = graveyardRepository;
		this.discoveryRepository = discoveryRepository;
	}


	@Override
	public void update()
	{
		int schemaVersion = SqliteSchemaUpdater.getSchemaVersion(connection, plugin.getLogger(), configRepository);
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
		Collection<ValidGraveyard> existingGraveyardRecords = graveyardRepository.getAllValid();
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("DropGraveyardsTable"));
			statement.executeUpdate(SqliteQueries.getQuery("CreateGraveyardTable"));
			setSchemaVersion(connection, plugin.getLogger(), configRepository, version);
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.SCHEMA_UPDATE_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		int count = graveyardRepository.saveAll(existingGraveyardRecords);
		plugin.getLogger().info(DatastoreMessage.SCHEMA_GRAVEYARD_RECORDS_MIGRATED_NOTICE.getLocalizedMessage(configRepository.locale(), count, version));
	}


	private void updateDiscoveryTableSchema(final Connection connection, final int version)
	{
		Collection<ValidDiscovery> existingDiscoveryRecords = discoveryRepository.getAll();
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("DropDiscoveredTable"));
			statement.executeUpdate(SqliteQueries.getQuery("CreateDiscoveryTable"));
			setSchemaVersion(connection, plugin.getLogger(), configRepository, version);
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.SCHEMA_UPDATE_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		int count = discoveryRepository.saveAll(existingDiscoveryRecords);
		plugin.getLogger().info(DatastoreMessage.SCHEMA_DISCOVERY_RECORDS_MIGRATED_NOTICE.getLocalizedMessage(configRepository.locale(), count, version));
	}

}
