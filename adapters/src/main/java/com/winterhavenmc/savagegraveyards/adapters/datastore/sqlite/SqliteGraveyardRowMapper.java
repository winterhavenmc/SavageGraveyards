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

import com.winterhavenmc.savagegraveyards.models.FailReason;
import com.winterhavenmc.savagegraveyards.models.Parameter;
import com.winterhavenmc.savagegraveyards.models.displayname.DisplayName;
import com.winterhavenmc.savagegraveyards.models.displayname.InvalidDisplayName;
import com.winterhavenmc.savagegraveyards.models.displayname.ValidDisplayName;
import com.winterhavenmc.savagegraveyards.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.models.graveyard.InvalidGraveyard;
import com.winterhavenmc.savagegraveyards.models.graveyard.attributes.*;
import com.winterhavenmc.savagegraveyards.models.location.ConfirmedLocation;
import com.winterhavenmc.savagegraveyards.models.location.InvalidLocation;
import com.winterhavenmc.savagegraveyards.models.location.ValidLocation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.UUID;


public final class SqliteGraveyardRowMapper
{
	public Graveyard map(final ResultSet resultSet) throws SQLException
	{
		DisplayName displayName = DisplayName.of(resultSet.getString("DisplayName"));

		return switch (displayName)
		{
			case InvalidDisplayName ignored -> new InvalidGraveyard(displayName, "\uD83C\uDF10", FailReason.PARAMETER_INVALID, Parameter.DISPLAY_NAME);
			case ValidDisplayName valid ->
			{
				ConfirmedLocation location = ConfirmedLocation.of(
						resultSet.getString("worldName"),
						new UUID(resultSet.getLong("WorldUidMsb"), resultSet.getLong("WorldUidLsb")),
						resultSet.getDouble("X"),
						resultSet.getDouble("Y"),
						resultSet.getDouble("Z"),
						resultSet.getFloat("Yaw"),
						resultSet.getFloat("Pitch"));

				Attributes attributes = new Attributes(
						Enabled.of(resultSet.getBoolean("Enabled")),
						Hidden.of(resultSet.getBoolean("Hidden")),
						DiscoveryRange.of(resultSet.getInt("DiscoveryRange")),
						DiscoveryMessage.of(resultSet.getString("DiscoveryMessage")),
						RespawnMessage.of(resultSet.getString("RespawnMessage")),
						Group.of(resultSet.getString("GroupName")),
						SafetyRange.of(resultSet.getInt("SafetyRange")),
						SafetyTime.of(Duration.ofSeconds(resultSet.getInt("safetyTime"))));

				yield switch (location)
				{
					case InvalidLocation ignored -> new InvalidGraveyard(displayName, "\uD83C\uDF10", FailReason.PARAMETER_INVALID, Parameter.LOCATION);
					case ValidLocation validLocation -> Graveyard.of(valid, attributes, validLocation);
				};
			}
		};
	}

}
