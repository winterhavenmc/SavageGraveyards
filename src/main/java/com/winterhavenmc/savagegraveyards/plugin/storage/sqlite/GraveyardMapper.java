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

import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.DisplayName;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.attributes.*;
import com.winterhavenmc.savagegraveyards.plugin.models.location.ImmutableLocation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.UUID;

import static com.winterhavenmc.savagegraveyards.plugin.models.graveyard.GraveyardReason.GRAVEYARD_STORED_DISPLAY_NAME_INVALID;
import static com.winterhavenmc.savagegraveyards.plugin.models.graveyard.GraveyardReason.GRAVEYARD_STORED_LOCATION_INVALID;


public class GraveyardMapper
{
	public Graveyard map(final ResultSet resultSet) throws SQLException
	{
		DisplayName displayName = DisplayName.of(resultSet.getString("DisplayName"));

		return switch (displayName)
		{
			case DisplayName.Invalid ignored -> new Graveyard.Invalid(displayName, "\uD83C\uDF10", GRAVEYARD_STORED_DISPLAY_NAME_INVALID);
			case DisplayName.Valid valid ->
			{
				ImmutableLocation location = ImmutableLocation.of(
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
					case ImmutableLocation.Invalid ignored ->	new Graveyard.Invalid(displayName, "\uD83C\uDF10", GRAVEYARD_STORED_LOCATION_INVALID);
					case ImmutableLocation.Valid validLocation -> Graveyard.of(valid, attributes, validLocation);
				};
			}
		};
	}

}
