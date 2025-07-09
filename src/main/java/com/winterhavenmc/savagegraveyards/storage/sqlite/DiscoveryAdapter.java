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

package com.winterhavenmc.savagegraveyards.storage.sqlite;

import com.winterhavenmc.savagegraveyards.models.discovery.Discovery;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


public class DiscoveryAdapter implements SqlAdapter
{
	@Override
	public Discovery adapt(ResultSet resultSet) throws SQLException
	{
		return Discovery.of(
				resultSet.getString("GraveyardSearchKey"),
				new UUID(resultSet.getLong("WorldUidMsb"), resultSet.getLong("WorldUidLsb"))
		);
	}

	public PreparedStatement adapt(final Discovery.Valid validDiscovery, final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setString(1, validDiscovery.displayName());
		preparedStatement.setLong(2, validDiscovery.playerUid().getMostSignificantBits());
		preparedStatement.setLong(3, validDiscovery.playerUid().getLeastSignificantBits());
		return preparedStatement;
	}
}
