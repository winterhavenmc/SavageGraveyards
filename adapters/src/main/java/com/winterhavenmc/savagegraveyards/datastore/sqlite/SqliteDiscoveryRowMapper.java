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

import com.winterhavenmc.savagegraveyards.plugin.models.discovery.Discovery;
import com.winterhavenmc.savagegraveyards.plugin.models.discovery.DiscoveryReason;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.SearchKey;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SqliteDiscoveryRowMapper
{
	public Discovery map(ResultSet resultSet) throws SQLException
	{
		SearchKey searchKey = SearchKey.of(resultSet.getString("searchKey"));
		UUID playerUid = new UUID(resultSet.getLong("playerUidMsb"), resultSet.getLong("playerUidLsb"));

		if (searchKey instanceof SearchKey.Valid)
		{
			return Discovery.of((SearchKey.Valid) searchKey, playerUid);
		}
		else
		{
			return new Discovery.Invalid(DiscoveryReason.SEARCH_KEY_INVALID);
		}
	}
}
