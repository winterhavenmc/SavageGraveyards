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

import com.winterhavenmc.savagegraveyards.plugin.models.discovery.Discovery;
import com.winterhavenmc.savagegraveyards.plugin.models.discovery.DiscoveryReason;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.SearchKey;
import com.winterhavenmc.savagegraveyards.plugin.ports.datastore.DiscoveryRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;


public class SqliteDiscoveryRepository implements DiscoveryRepository
{
	private final Logger logger;
	private final Connection connection;
	private final SqliteDiscoveryRowMapper discoveryMapper = new SqliteDiscoveryRowMapper();
	private final SqliteDiscoveryQueryExecutor queryExecutor = new SqliteDiscoveryQueryExecutor();


	/**
	 * Class constructor
	 */
	public SqliteDiscoveryRepository(final Logger logger, final Connection connection)
	{
		this.logger = logger;
		this.connection = connection;
	}


	/**
	 * Insert discovery record in the SQLite datastore
	 * @param discovery the discovery to be inserted
	 * @return true if successful, false if not
	 */
	@Override
	public boolean save(final Discovery.Valid discovery)
	{
		int rowsAffected = 0;

		try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("InsertDiscovery")))
		{
			rowsAffected = queryExecutor.insertDiscovery(discovery, preparedStatement);
		}
		catch (SQLException sqlException)
		{
			logger.warning(SqliteNotice.INSERT_DISCOVERY_FAILED.toString());
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
	public int saveAll(final Collection<Discovery.Valid> discoveries)
	{
		if (discoveries == null)
		{
			logger.warning("Could not insert graveyard records in data store "
					+ "because the collection parameter was null.");
			return 0;
		}

		int count = 0;

		for (Discovery.Valid validDiscovery : discoveries)
		{
			try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("InsertDiscovery")))
			{
				synchronized (this)
				{
					count += queryExecutor.insertDiscovery(validDiscovery, preparedStatement);
				}
			}
			catch (SQLException sqlException)
			{
				logger.warning("An error occurred while trying to "
						+ "insert a record into the discovered table in the SQLite datastore.");
				logger.warning(sqlException.getLocalizedMessage());
			}
		}

		return count;
	}


	@Override
	public boolean delete(final SearchKey.Valid searchKey, final UUID playerUid)
	{
		if (playerUid == null) return false;

		int rowsAffected = 0;

		try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("DeleteDiscovery")))
		{
			synchronized (this)
			{
				rowsAffected = queryExecutor.deleteDiscovery(searchKey, playerUid, preparedStatement);
			}
		}
		catch (SQLException e)
		{
			// output simple error message
			logger.warning("An error occurred while attempting to "
					+ "delete a ValidDiscovery record from the SQLite datastore.");
			logger.warning(e.getLocalizedMessage());
		}

		return rowsAffected > 0;
	}


	public Set<Discovery.Valid> getAll_v0()
	{
		final Set<Discovery.Valid> returnSet = new HashSet<>();

		try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectAllDiscoveryRecordsV0")))
		{
			ResultSet resultSet = queryExecutor.selectAllDiscoveries(preparedStatement);

			while (resultSet.next())
			{
				SearchKey searchKey = SearchKey.of(resultSet.getString("SearchKey"));
				String playerUidString = resultSet.getString("PlayerUid");
				UUID playerUid;

				if (searchKey instanceof SearchKey.Valid)
				{
					try
					{
						playerUid = UUID.fromString(playerUidString);
					} catch (IllegalArgumentException e)
					{
						logger.warning("A record in the Discovered table " +
								"has an invalid UUID! Skipping record.");
						logger.warning(e.getLocalizedMessage());
						continue;
					}

					if (Discovery.of((SearchKey.Valid) searchKey, playerUid) instanceof Discovery.Valid validDiscovery)
					{
						returnSet.add(validDiscovery);
					}
				}
			}
		} catch (SQLException e)
		{
			logger.warning("An error occurred while trying to " +
					"select all discovery records from the SQLite datastore.");
			logger.warning(e.getLocalizedMessage());
		}

		return returnSet;
	}


	public Set<Discovery.Valid> getAll_V1()
	{
		final Set<Discovery.Valid> returnSet = new HashSet<>();

		try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectAllDiscoveryRecords")))
		{
			final ResultSet resultSet = queryExecutor.selectAllDiscoveries(preparedStatement);

			while (resultSet.next())
			{
				switch (discoveryMapper.map(resultSet))
				{
					case Discovery.Valid valid -> returnSet.add(valid);
					case Discovery.Invalid(DiscoveryReason discoveryReason) -> logger
							.warning("A valid discovery could not be created: " + discoveryReason);
				}
			}
		}
		catch (SQLException e)
		{
			logger.warning("An error occurred while trying to " +
					"select all discovery records from the SQLite datastore.");
			logger.warning(e.getLocalizedMessage());
		}

		return returnSet;
	}

}
