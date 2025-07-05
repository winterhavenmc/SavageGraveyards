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

import com.winterhavenmc.savagegraveyards.models.graveyard.Graveyard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.UUID;


public class GraveyardAdapter implements Adapter
{
	public Graveyard adapt(ResultSet resultSet) throws SQLException
	{
		return Graveyard.of(
				resultSet.getString("SearchKey"),
				resultSet.getString("DisplayName"),
				resultSet.getBoolean("Enabled"),
				resultSet.getBoolean("Hidden"),
				resultSet.getInt("DiscoveryRange"),
				resultSet.getString("DiscoveryMessage"),
				resultSet.getString("RespawnMessage"),
				resultSet.getString("GroupName"),
				resultSet.getInt("SafetyRange"),
				Duration.ofSeconds(resultSet.getInt("safetyTime")),
				resultSet.getString("worldName"),
				new UUID(resultSet.getLong("WorldUidMsb"), resultSet.getLong("WorldUidLsb")),
				resultSet.getDouble("X"),
				resultSet.getDouble("Y"),
				resultSet.getDouble("Z"),
				resultSet.getFloat("Yaw"),
				resultSet.getFloat("Pitch"));
	}
}
