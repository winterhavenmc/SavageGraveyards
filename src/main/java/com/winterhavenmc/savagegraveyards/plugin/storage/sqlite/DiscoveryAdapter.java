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

package com.winterhavenmc.savagegraveyards.plugin.storage.sqlite;

import com.winterhavenmc.savagegraveyards.plugin.models.discovery.Discovery;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


public class DiscoveryAdapter
{
	public Discovery selectDiscovery(ResultSet resultSet) throws SQLException
	{
		return Discovery.of(
				resultSet.getInt("graveyardKey"),
				new UUID(resultSet.getLong("playerUidMsb"), resultSet.getLong("playerUidLsb"))
		);
	}


	public int insertDiscovery(final Discovery.Valid validDiscovery,
	                           final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setInt(1, validDiscovery.graveyardKey());
		preparedStatement.setLong(2, validDiscovery.playerUid().getMostSignificantBits());
		preparedStatement.setLong(3, validDiscovery.playerUid().getLeastSignificantBits());
		return preparedStatement.executeUpdate();
	}


	public int insertDiscovery(final String searchKey,
	                           final UUID playerUid,
	                           final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setString(1, searchKey);
		preparedStatement.setLong(2, playerUid.getMostSignificantBits());
		preparedStatement.setLong(3, playerUid.getLeastSignificantBits());
		return preparedStatement.executeUpdate();
	}


	public int deleteDiscovery(final String searchKey,
	                           final UUID playerUid,
	                           final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setLong(1, playerUid.getMostSignificantBits());
		preparedStatement.setLong(2, playerUid.getLeastSignificantBits());
		preparedStatement.setString(3, searchKey);
		return preparedStatement.executeUpdate();
	}

}
