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

import com.winterhavenmc.savagegraveyards.models.discovery.Discovery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;


public final class SqliteDiscoveryRowMapperV1
{
	public Discovery map(ResultSet resultSet) throws SQLException
	{
		UUID graveyardUid = new UUID(resultSet.getLong(Column.GRAVEYARD_UID_MSB.label()), resultSet.getLong(Column.GRAVEYARD_UID_LSB.label()));
		UUID playerUid = UUID.fromString(resultSet.getString(Column.PLAYER_UID.label()));

		// if query returned
		graveyardUid = (graveyardUid.equals(new UUID(0,0)))
				? UUID.randomUUID()
				: graveyardUid;

		return Discovery.of(graveyardUid, playerUid, Instant.now());
	}


	private enum Column
	{
		GRAVEYARD_UID_MSB("GraveyardUidMsb"),
		GRAVEYARD_UID_LSB("GraveyardUidLsb"),
		PLAYER_UID("PlayerUid"),
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
