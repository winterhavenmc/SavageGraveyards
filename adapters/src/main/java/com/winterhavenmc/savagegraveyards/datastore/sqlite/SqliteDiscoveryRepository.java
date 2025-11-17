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

import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;

import com.winterhavenmc.savagegraveyards.datastore.DatastoreMessage;
import com.winterhavenmc.savagegraveyards.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.datastore.sqlite.schema.*;
import com.winterhavenmc.savagegraveyards.models.FailReason;
import com.winterhavenmc.savagegraveyards.models.Parameter;
import com.winterhavenmc.savagegraveyards.models.discovery.InvalidDiscovery;
import com.winterhavenmc.savagegraveyards.models.discovery.ValidDiscovery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static com.winterhavenmc.savagegraveyards.datastore.DatastoreMessage.DATASTORE_NAME;


public final class SqliteDiscoveryRepository implements DiscoveryRepository
{
	private final Logger logger;
	private final ConfigRepository configRepository;
	private final Connection connection;
	private final SqliteDiscoveryQueryExecutor queryExecutor = new SqliteDiscoveryQueryExecutor();


	/**
	 * Class constructor
	 */
	public SqliteDiscoveryRepository(final Logger logger, final Connection connection, final ConfigRepository configRepository)
	{
		this.logger = logger;
		this.connection = connection;
		this.configRepository = configRepository;
	}


	/**
	 * Insert discovery record in the SQLite datastore
	 * @param discovery the discovery to be inserted
	 * @return true if successful, false if not
	 */
	@Override
	public boolean save(final ValidDiscovery discovery)
	{
		int rowsAffected = 0;

		try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("InsertDiscovery")))
		{
			rowsAffected = queryExecutor.insertDiscovery(discovery, preparedStatement);
		}
		catch (SQLException sqlException)
		{
			logger.warning(DatastoreMessage.INSERT_DISCOVERY_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			logger.warning(sqlException.getLocalizedMessage());
		}

		return rowsAffected > 0;
	}


	/**
	 * Insert discovery records
	 *
	 * @param discoveries collection of valid records to be inserted
	 * @return number of records successfully inserted
	 */
	@Override
	public int saveAll(final Collection<ValidDiscovery> discoveries)
	{
		if (discoveries == null)
		{
			logger.warning(DatastoreMessage.INSERT_DISCOVERIES_NULL_ERROR.getLocalizedMessage(configRepository.locale()));
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
				logger.warning(DatastoreMessage.INSERT_DISCOVERY_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
				logger.warning(sqlException.getLocalizedMessage());
			}
		}

		return count;
	}


	@Override
	public boolean delete(final UUID graveyardUid, final UUID playerUid)
	{
		if (graveyardUid == null) return false;
		if (playerUid == null) return false;

		int rowsAffected = 0;

		try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("DeleteDiscovery")))
		{
			rowsAffected = queryExecutor.deleteDiscovery(graveyardUid, playerUid, preparedStatement);
		}
		catch (SQLException e)
		{
			// output simple error message
			logger.warning(DatastoreMessage.DELETE_DISCOVERY_RECORD_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			logger.warning(e.getLocalizedMessage());
		}

		return rowsAffected > 0;
	}


	private DiscoveryRowMapper selectRowMapper(final int version)
	{
		return switch (version)
		{
			case 0 -> new Version0.SqliteDiscoveryRowMapper();
			case 1 -> new Version1.SqliteDiscoveryRowMapper();
			default -> new Version2.SqliteDiscoveryRowMapper();
		};
	}


	@Override
	public Set<ValidDiscovery> getAll()
	{
		return getAll(selectRowMapper(SqliteConnectionProvider.getSchemaVersion(connection, logger, configRepository)));
	}


	public Set<ValidDiscovery> getAll(final DiscoveryRowMapper rowMapper)
	{
		final Set<ValidDiscovery> returnSet = new HashSet<>();

		try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery(rowMapper.queryKey()));
		     final ResultSet resultSet = queryExecutor.selectAllDiscoveries(preparedStatement))
		{
			while (resultSet.next())
			{
				switch (rowMapper.map(resultSet))
				{
					case ValidDiscovery valid -> returnSet.add(valid);
					case InvalidDiscovery(FailReason failReason, Parameter parameter) -> logger
							.warning(DatastoreMessage.CREATE_DISCOVERY_ERROR
									.getLocalizedMessage(configRepository.locale(), failReason.getLocalizedMessage(configRepository.locale())));
					default -> throw new IllegalStateException("Unexpected value: " + rowMapper.map(resultSet));
				}
			}
		}
		catch (SQLException sqlException)
		{
			logger.warning(DatastoreMessage.SELECT_ALL_DISCOVERIES_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			logger.warning(sqlException.getLocalizedMessage());
		}

		return returnSet;
	}

}
