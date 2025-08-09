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
	private final LocaleProvider localeProvider;
	private final Connection connection;
	private final SqliteDiscoveryRowMapper discoveryMapper = new SqliteDiscoveryRowMapper();
	private final SqliteDiscoveryQueryExecutor queryExecutor = new SqliteDiscoveryQueryExecutor();


	/**
	 * Class constructor
	 */
	public SqliteDiscoveryRepository(final Logger logger, final Connection connection, final LocaleProvider localeProvider)
	{
		this.logger = logger;
		this.connection = connection;
		this.localeProvider = localeProvider;
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
			logger.warning(SqliteMessage.INSERT_DISCOVERY_ERROR.getLocalizeMessage(localeProvider.getLocale()));
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
	public int saveAll(final Collection<Discovery.Valid> discoveries)
	{
		if (discoveries == null)
		{
			logger.warning(SqliteMessage.INSERT_DISCOVERIES_NULL_ERROR.getLocalizeMessage(localeProvider.getLocale()));
			return 0;
		}

		int count = 0;

		for (Discovery.Valid validDiscovery : discoveries)
		{
			try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("InsertDiscovery")))
			{
				count += queryExecutor.insertDiscovery(validDiscovery, preparedStatement);
			}
			catch (SQLException sqlException)
			{
				logger.warning(SqliteMessage.INSERT_DISCOVERY_ERROR.getLocalizeMessage(localeProvider.getLocale()));
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
			rowsAffected = queryExecutor.deleteDiscovery(searchKey, playerUid, preparedStatement);
		}
		catch (SQLException e)
		{
			// output simple error message
			logger.warning(SqliteMessage.DELETE_DISCOVERY_RECORD_ERROR.getLocalizeMessage(localeProvider.getLocale()));
			logger.warning(e.getLocalizedMessage());
		}

		return rowsAffected > 0;
	}


	@Override
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
					}
					catch (IllegalArgumentException exception)
					{
						logger.warning(SqliteMessage.SELECT_DISCOVERY_NULL_UUID_ERROR.getLocalizeMessage(localeProvider.getLocale()));
						logger.warning(exception.getLocalizedMessage());
						continue;
					}

					if (Discovery.of((SearchKey.Valid) searchKey, playerUid) instanceof Discovery.Valid validDiscovery)
					{
						returnSet.add(validDiscovery);
					}
				}
			}
		}
		catch (SQLException e)
		{
			logger.warning(SqliteMessage.SELECT_ALL_DISCOVERIES_ERROR.getLocalizeMessage(localeProvider.getLocale()));
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
							.warning(SqliteMessage.CREATE_DISCOVERY_ERROR
									.getLocalizeMessage(localeProvider.getLocale(), discoveryReason.getLocalizeMessage(localeProvider.getLocale())));
				}
			}
		}
		catch (SQLException sqlException)
		{
			logger.warning(SqliteMessage.SELECT_ALL_DISCOVERIES_ERROR.getLocalizeMessage(localeProvider.getLocale()));
			logger.warning(sqlException.getLocalizedMessage());
		}

		return returnSet;
	}

}
