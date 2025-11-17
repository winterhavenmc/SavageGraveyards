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

package com.winterhavenmc.savagegraveyards.datastore.sqlite.schema;

import com.winterhavenmc.library.messagebuilder.models.DefaultSymbol;
import com.winterhavenmc.savagegraveyards.models.FailReason;
import com.winterhavenmc.savagegraveyards.models.Parameter;
import com.winterhavenmc.savagegraveyards.models.discovery.Discovery;
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
import java.time.Instant;
import java.util.UUID;

import static com.winterhavenmc.savagegraveyards.datastore.sqlite.schema.SchemaUpdater.INVALID_UUID;

public final class Version1 implements Schema
{
	public static final class SqliteGraveyardRowMapper implements GraveyardRowMapper
	{
		/**
		 * Maps columns of a database query ResultSet to fields of a newly created graveyard object
		 *
		 * @param resultSet the query result set
		 * @return an instance of {@code ValidGraveyard} if field mapping was successful, or {@code InvalidGraveyard} if not
		 * @throws SQLException if the sql query fails
		 */
		@Override
		public Graveyard map(final ResultSet resultSet) throws SQLException
		{
			DisplayName graveyardName = DisplayName.of(resultSet.getString(Column.GRAVEYARD_NAME.label()));

			// return InvalidGraveyard if display name is invalid
			return switch (graveyardName)
			{
				case InvalidDisplayName ignored ->
						new InvalidGraveyard(graveyardName, DefaultSymbol.UNKNOWN_WORLD.symbol(), FailReason.PARAMETER_INVALID, Parameter.DISPLAY_NAME);
				case ValidDisplayName validGraveyardName ->
				{
					// get graveyardUid from query result set
					UUID graveyardUid = new UUID(resultSet.getLong(Column.GRAVEYARD_UID_MSB.label()),
							resultSet.getLong(Column.GRAVEYARD_UID_LSB.label()));

					// if invalid uuid returned, create and assign random uuid to graveyard
					if (graveyardUid.equals(INVALID_UUID))
					{
						graveyardUid = UUID.randomUUID();
					}

					// get graveyard location from query result set
					final ConfirmedLocation location = ConfirmedLocation.of(
							resultSet.getString(Column.WORLD_NAME.label()),
							new UUID(resultSet.getLong(Column.WORLD_UID_MSB.label()), resultSet.getLong(Column.WORLD_UID_LSB.label())),
							resultSet.getDouble(Column.X.label()),
							resultSet.getDouble(Column.Y.label()),
							resultSet.getDouble(Column.Z.label()),
							resultSet.getFloat(Column.YAW.label()),
							resultSet.getFloat(Column.PITCH.label()));

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
						case InvalidLocation ignored ->
								new InvalidGraveyard(graveyardName, DefaultSymbol.UNKNOWN_WORLD.symbol(), FailReason.PARAMETER_INVALID, Parameter.LOCATION);
						case ValidLocation validLocation ->
								Graveyard.of(validGraveyardName, graveyardUid, validLocation, attributes);
					};
				}
			};
		}


		public String queryKey()
		{
			return Table.QUERY_KEY.getString();
		}


		private enum Table
		{
			NAME("Graveyards"),
			QUERY_KEY("SelectAllGraveyardRecordsV1"),
			;

			private final String string;

			Table(final String string)
			{
				this.string = string;
			}

			String getString()
			{
				return this.string;
			}
		}


		private enum Column
		{
			GRAVEYARD_NAME("DisplayName"),
			GRAVEYARD_UID_MSB("UidMsb"),
			GRAVEYARD_UID_LSB("UidLsb"),
			WORLD_NAME("WorldName"),
			WORLD_UID_MSB("WorldUidMsb"),
			WORLD_UID_LSB("WorldUidLsb"),
			X("X"),
			Y("Y"),
			Z("Z"),
			YAW("Yaw"),
			PITCH("Pitch"),
			;

			private final String label;


			Column(final String label)
			{
				this.label = label;
			}


			String label()
			{
				return this.label;
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


	public static final class SqliteDiscoveryRowMapper implements DiscoveryRowMapper
	{
		public Discovery map(ResultSet resultSet) throws SQLException
		{
			UUID graveyardUid = new UUID(resultSet.getLong(Column.GRAVEYARD_UID_MSB.label()), resultSet.getLong(Column.GRAVEYARD_UID_LSB.label()));
			UUID playerUid = new UUID(resultSet.getLong(Column.PLAYER_UID_MSB.label()), resultSet.getLong(Column.PLAYER_UID_LSB.label()));

			return Discovery.of(graveyardUid, playerUid, Instant.now());
		}


		public String queryKey()
		{
			return Table.QUERY_KEY.getString();
		}


		private enum Table
		{
			NAME("Discovered"),
			QUERY_KEY("SelectAllDiscoveryRecordsV1"),
			;

			private final String string;

			Table(final String string)
			{
				this.string = string;
			}

			String getString()
			{
				return this.string;
			}
		}


		private enum Column
		{
			GRAVEYARD_UID_MSB("GraveyardUidMsb"),
			GRAVEYARD_UID_LSB("GraveyardUidLsb"),
			PLAYER_UID_MSB("playerUidMsb"),
			PLAYER_UID_LSB("playerUidLsb"),
			;

			private final String label;

			Column(final String label)
			{
				this.label = label;
			}

			String label()
			{
				return this.label;
			}
		}
	}

}