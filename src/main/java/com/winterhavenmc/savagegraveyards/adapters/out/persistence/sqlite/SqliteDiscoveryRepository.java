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

package com.winterhavenmc.savagegraveyards.adapters.out.persistence.sqlite;

import com.winterhavenmc.savagegraveyards.plugin.models.discovery.Discovery;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.SearchKey;
import com.winterhavenmc.savagegraveyards.plugin.ports.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.plugin.storage.Queries;
import com.winterhavenmc.savagegraveyards.plugin.storage.SQLiteNotice;
import com.winterhavenmc.savagegraveyards.plugin.storage.sqlite.DiscoveryQueryHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;


public class SqliteDiscoveryRepository implements DiscoveryRepository
{
	private final Logger logger;
	private final Connection connection;
	private final DiscoveryQueryHelper discoveryQueryHelper = new DiscoveryQueryHelper();


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

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertDiscovery")))
		{
			rowsAffected = discoveryQueryHelper.insertDiscovery(discovery, preparedStatement);
		}
		catch (SQLException sqlException)
		{
			logger.warning(SQLiteNotice.INSERT_DISCOVERY_FAILED.toString());
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
			logger.warning("Could not insert graveyard records in data store "
					+ "because the collection parameter was null.");
			return 0;
		}

		int count = 0;

		for (Discovery.Valid validDiscovery : discoveries)
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertDiscovery")))
			{
				synchronized (this)
				{
					count += discoveryQueryHelper.insertDiscovery(validDiscovery, preparedStatement);
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

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("DeleteDiscovery")))
		{
			synchronized (this)
			{
				rowsAffected = DiscoveryQueryHelper.deleteDiscovery(searchKey, playerUid, preparedStatement);
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

}
