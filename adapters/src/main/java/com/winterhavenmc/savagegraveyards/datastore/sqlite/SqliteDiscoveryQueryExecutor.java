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

import com.winterhavenmc.savagegraveyards.models.discovery.ValidDiscovery;
import com.winterhavenmc.savagegraveyards.models.searchkey.SearchKey;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;


public final class SqliteDiscoveryQueryExecutor
{
	public int insertDiscovery(final ValidDiscovery validDiscovery,
	                           final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setString(1, validDiscovery.searchKey().string());
		preparedStatement.setLong(  2, validDiscovery.playerUid().getMostSignificantBits());
		preparedStatement.setLong(  3, validDiscovery.playerUid().getLeastSignificantBits());
		preparedStatement.setTimestamp(4, (validDiscovery.getTimestamp() != null)
				? Timestamp.from(validDiscovery.getTimestamp())
				: Timestamp.from(Instant.now()));
		return preparedStatement.executeUpdate();
	}


	public int deleteDiscovery(final SearchKey searchKey,
	                           final UUID playerUid,
	                           final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setLong(  1, playerUid.getMostSignificantBits());
		preparedStatement.setLong(  2, playerUid.getLeastSignificantBits());
		preparedStatement.setString(3, searchKey.string());
		return preparedStatement.executeUpdate();
	}


	public ResultSet selectAllDiscoveries(final PreparedStatement preparedStatement) throws SQLException
	{
		return preparedStatement.executeQuery();
	}

}
