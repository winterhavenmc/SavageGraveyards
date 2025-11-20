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

import com.winterhavenmc.savagegraveyards.datastore.DatastoreMessage;
import com.winterhavenmc.savagegraveyards.datastore.sqlite.SqliteDiscoveryQueryExecutor;
import com.winterhavenmc.savagegraveyards.datastore.sqlite.SqliteGraveyardQueryExecutor;
import com.winterhavenmc.savagegraveyards.datastore.sqlite.SqliteQueries;

import com.winterhavenmc.savagegraveyards.models.FailReason;
import com.winterhavenmc.savagegraveyards.models.Parameter;
import com.winterhavenmc.savagegraveyards.models.discovery.Discovery;
import com.winterhavenmc.savagegraveyards.models.discovery.InvalidDiscovery;
import com.winterhavenmc.savagegraveyards.models.discovery.ValidDiscovery;
import com.winterhavenmc.savagegraveyards.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard;

import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;

import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.*;
import java.util.stream.Stream;

import static com.winterhavenmc.savagegraveyards.datastore.DatastoreMessage.DATASTORE_NAME;
import static com.winterhavenmc.savagegraveyards.datastore.sqlite.SqliteConnectionProvider.*;


public final class SqliteSchemaUpdater implements SchemaUpdater
{
	private final Plugin plugin;
	private final Connection connection;
	private final ConfigRepository config;
	private final RowMapper<Graveyard> currentGraveyardRowMapper;
	private final RowMapper<Discovery> discoveryRowMapper;
	private final SqliteDiscoveryQueryExecutor queryExecutor;


	SqliteSchemaUpdater(final Plugin plugin,
	                    final Connection connection,
	                    final ConfigRepository configRepository)
	{
		this.plugin = plugin;
		this.connection = connection;
		this.config = configRepository;
		int storageSchemaVersion = getSchemaVersion(connection, configRepository, plugin.getLogger());
		this.currentGraveyardRowMapper = selectGraveyardRowMapper(storageSchemaVersion);
		this.discoveryRowMapper = selectDiscoveryRowMapper(storageSchemaVersion);
		this.queryExecutor = new SqliteDiscoveryQueryExecutor();
	}


	@Override
	public void update()
	{
		int schemaVersion = getSchemaVersion(connection, config, plugin.getLogger());

		if (schemaVersion < Schema.VERSION)
		{
			updateTables(connection);
			setSchemaVersion(connection, plugin.getLogger(), config, Schema.VERSION);
			cleanup(connection);
		}
	}


	private void updateTables(final Connection connection)
	{
		final int storedSchemaVersion = getSchemaVersion(connection, config, plugin.getLogger());
		final RowMapper<Graveyard> graveyardRowMapper = selectGraveyardRowMapper(storedSchemaVersion);
		final RowMapper<Discovery> discoveryRowMapper = selectDiscoveryRowMapper(storedSchemaVersion);

		if (tableExists(connection, graveyardRowMapper.tableName()))
		{
			updateGraveyardTableSchema(connection);
		}

		if (tableExists(connection, discoveryRowMapper.tableName()))
		{
			updateDiscoveryTableSchema(connection);
		}
	}


	private void cleanup(final Connection connection)
	{
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("DropGraveyardsTable")); // table renamed to 'graveyard'
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.DROP_GRAVEYARD_TABLE_ERROR.getLocalizedMessage(config.locale(), DATASTORE_NAME));
		}

		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("DropDiscoveredTable")); // table renamed to 'discovery'
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.DROP_DISCOVERY_TABLE_ERROR.getLocalizedMessage(config.locale(), DATASTORE_NAME));
		}
	}


	private void updateGraveyardTableSchema(final Connection connection)
	{
		Stream<ValidGraveyard> existingGraveyardRecords = getAllGraveyardRecords(currentGraveyardRowMapper);

		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("CreateGraveyardTable"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.SCHEMA_UPDATE_ERROR.getLocalizedMessage(config.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		int count = saveAllGraveyards(existingGraveyardRecords);

		plugin.getLogger().info(DatastoreMessage.SCHEMA_GRAVEYARD_RECORDS_MIGRATED_NOTICE
				.getLocalizedMessage(config.locale(), count, Schema.VERSION));
	}


	/**
	 * Select a collection of all valid graveyards from the Sqlite datastore
	 *
	 * @return A {@link Stream} of all ValidGraveyard records in the Sqlite datastore
	 * @param graveyardRowMapper the row mapper to use for the schema version to be used
	 */
	public Stream<ValidGraveyard> getAllGraveyardRecords(RowMapper<Graveyard> graveyardRowMapper)
	{
		final List<ValidGraveyard> returnList = new ArrayList<>();

		try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery(graveyardRowMapper.queryKey()));
		     final ResultSet resultSet = preparedStatement.executeQuery())
		{
			while (resultSet.next())
			{
				if (graveyardRowMapper.map(resultSet) instanceof ValidGraveyard validGraveyard)
				{
					returnList.add(validGraveyard);
				}
			}
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.SELECT_ALL_GRAVEYARDS_ERROR.getLocalizedMessage(config.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		return returnList.stream();
	}


	/**
	 * Insert a {@link Stream} of ValidGraveyard records into the Sqlite datastore
	 *
	 * @param graveyards a stream of graveyard records
	 * @return int - the number of records successfully inserted
	 */
	private int saveAllGraveyards(final Stream<ValidGraveyard> graveyards)
	{
		if (graveyards == null) return 0;

		int count = 0;

		SqliteGraveyardQueryExecutor graveyardQueryExecutor = new SqliteGraveyardQueryExecutor();

		//TODO: use summing collector with stream to get count
		for (ValidGraveyard graveyard : graveyards.toList())
		{
			try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("InsertGraveyard")))
			{
				count += graveyardQueryExecutor.insertGraveyard(graveyard, preparedStatement);
			}
			catch (SQLException sqlException)
			{
				plugin.getLogger().warning(DatastoreMessage.INSERT_GRAVEYARD_ERROR.getLocalizedMessage(config.locale(), DATASTORE_NAME));
				plugin.getLogger().warning(sqlException.getLocalizedMessage());
			}
		}
		return count;
	}


	private void updateDiscoveryTableSchema(final Connection connection)
	{
		Collection<ValidDiscovery> existingDiscoveryRecords = getAllDiscoveryRecords();
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("CreateDiscoveryTable"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.SCHEMA_UPDATE_ERROR.getLocalizedMessage(config.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		int count = saveAllDiscoveries(existingDiscoveryRecords);
		plugin.getLogger().info(DatastoreMessage.SCHEMA_DISCOVERY_RECORDS_MIGRATED_NOTICE.getLocalizedMessage(config.locale(), count, Schema.VERSION));
	}


	/**
	 * Select a collection of all valid discoveries from the Sqlite datastore
	 *
	 * @return A {@link Stream} of all ValidDiscovery records in the Sqlite datastore
	 */
	public Set<ValidDiscovery> getAllDiscoveryRecords()
	{
		final Set<ValidDiscovery> returnSet = new HashSet<>();

		try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery(discoveryRowMapper.queryKey()));
		     final ResultSet resultSet = queryExecutor.selectAllDiscoveries(preparedStatement))
		{
			while (resultSet.next())
			{
				switch (discoveryRowMapper.map(resultSet))
				{
					case ValidDiscovery valid -> returnSet.add(valid);
					case InvalidDiscovery(FailReason failReason, Parameter ignored) -> plugin.getLogger()
							.warning(DatastoreMessage.CREATE_DISCOVERY_ERROR
									.getLocalizedMessage(config.locale(), failReason.getLocalizedMessage(config.locale())));
					default -> throw new IllegalStateException("Unexpected value: " + discoveryRowMapper.map(resultSet));
				}
			}
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.SELECT_ALL_DISCOVERIES_ERROR.getLocalizedMessage(config.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		return returnSet;
	}


	/**
	 * Insert discovery records
	 *
	 * @param discoveries collection of valid records to be inserted
	 * @return number of records successfully inserted
	 */
	private int saveAllDiscoveries(final Collection<ValidDiscovery> discoveries)
	{
		SqliteDiscoveryQueryExecutor queryExecutor = new SqliteDiscoveryQueryExecutor();

		if (discoveries == null)
		{
			plugin.getLogger().warning(DatastoreMessage.INSERT_DISCOVERIES_NULL_ERROR.getLocalizedMessage(config.locale()));
			return 0;
		}

		int count = 0;

		for (ValidDiscovery validDiscovery : discoveries)
		{
			try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("InsertDiscovery")))
			{
				count += queryExecutor.insertDiscovery(validDiscovery, preparedStatement);
			}
			catch (SQLException sqlException)
			{
				plugin.getLogger().warning(DatastoreMessage.INSERT_DISCOVERY_ERROR.getLocalizedMessage(config.locale(), DATASTORE_NAME));
				plugin.getLogger().warning(sqlException.getLocalizedMessage());
			}
		}

		return count;
	}

}
