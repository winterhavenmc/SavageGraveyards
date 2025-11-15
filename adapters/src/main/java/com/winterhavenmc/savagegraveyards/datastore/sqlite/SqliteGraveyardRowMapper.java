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
	private final static UUID INVALID_UUID = new UUID(0,0);

	/**
	 * Maps columns of a database query ResultSet to fields of a newly created graveyard object
	 *
	 * @param resultSet the query result set
	 * @return an instance of {@code ValidGraveyard} if field mapping was successful, or {@code InvalidGraveyard} if not
	 * @throws SQLException if the sql query fails
	 */
	public Graveyard map(final ResultSet resultSet) throws SQLException
	{
		DisplayName graveyardName = DisplayName.of(resultSet.getString(Field.GRAVEYARD_NAME.fieldName()));

		// return InvalidGraveyard if display name is invalid
		return switch (graveyardName)
		{
			case InvalidDisplayName ignored -> new InvalidGraveyard(graveyardName, "\uD83C\uDF10", FailReason.PARAMETER_INVALID, Parameter.DISPLAY_NAME);
			case ValidDisplayName validGraveyardName ->
			{
				// get graveyardUid from query result set
				UUID graveyardUid = new UUID(resultSet.getLong(Field.GRAVEYARD_UID_MSB.fieldName()),
						resultSet.getLong(Field.GRAVEYARD_UID_LSB.fieldName()));

				// if invalid uuid returned, create and assign random uuid to graveyard
				if (graveyardUid.equals(INVALID_UUID))
				{
					graveyardUid = UUID.randomUUID();
				}

				// get graveyard location from query result set
				final ConfirmedLocation location = ConfirmedLocation.of(
						resultSet.getString(Field.WORLD_NAME.fieldName()),
						new UUID(resultSet.getLong(Field.WORLD_UID_MSB.fieldName()), resultSet.getLong(Field.WORLD_UID_LSB.fieldName())),
						resultSet.getDouble(Field.X.fieldName()),
						resultSet.getDouble(Field.Y.fieldName()),
						resultSet.getDouble(Field.Z.fieldName()),
						resultSet.getFloat(Field.YAW.fieldName()),
						resultSet.getFloat(Field.PITCH.fieldName()));

				// get graveyard attributes from query result set
				final Attributes attributes = new Attributes(
						Enabled.of(resultSet.getBoolean(Attribute.ENABLED.attributeName())),
						Hidden.of(resultSet.getBoolean(Attribute.HIDDEN.attributeName())),
						DiscoveryRange.of(resultSet.getInt(Attribute.DISCOVERY_RANGE.attributeName())),
						DiscoveryMessage.of(resultSet.getString(Attribute.DISCOVERY_MESSAGE.attributeName())),
						RespawnMessage.of(resultSet.getString(Attribute.RESPAWN_MESSAGE.attributeName())),
						Group.of(resultSet.getString(Attribute.GROUP_NAME.attributeName())),
						SafetyRange.of(resultSet.getInt(Attribute.SAFETY_RANGE.attributeName())),
						SafetyTime.of(Duration.ofSeconds(resultSet.getInt(Attribute.SAFETY_TIME.attributeName()))));

				// return ValidGraveyard if location is valid, else return InvalidGraveyard
				yield switch (location)
				{
					case InvalidLocation ignored -> new InvalidGraveyard(graveyardName, "\uD83C\uDF10", FailReason.PARAMETER_INVALID, Parameter.LOCATION);
					case ValidLocation validLocation -> Graveyard.of(validGraveyardName, graveyardUid, validLocation, attributes);
				};
			}
		};
	}


	private enum Field
	{
		GRAVEYARD_NAME("GraveyardName"),
		GRAVEYARD_UID_MSB("GraveyardUidMsb"),
		GRAVEYARD_UID_LSB("GraveyardUidLsb"),
		WORLD_NAME("WorldName"),
		WORLD_UID_MSB("WorldUidMsb"),
		WORLD_UID_LSB("WorldUidLsb"),
		X("X"),
		Y("Y"),
		Z("Z"),
		YAW("Yaw"),
		PITCH("Pitch"),
		;

		private final String fieldName;


		Field(final String fieldName)
		{
			this.fieldName = fieldName;
		}


		String fieldName()
		{
			return this.fieldName;
		}
	}


	private enum Attribute
	{
		ENABLED("Enabled"),
		HIDDEN("Hidden"),
		DISCOVERY_RANGE("DiscoveryRange"),
		DISCOVERY_MESSAGE("DiscoveryMessage"),
		RESPAWN_MESSAGE("RespawnMessage"),
		GROUP_NAME("GroupName"),
		SAFETY_RANGE("SafetyRange"),
		SAFETY_TIME("SafetyTime"),
		;

		private final String attributeName;


		Attribute(final String attributeName)
		{
			this.attributeName = attributeName;
		}


		public String attributeName()
		{
			return this.attributeName;
		}
	}


}
