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

import com.winterhavenmc.savagegraveyards.models.graveyard.Attributes;
import com.winterhavenmc.savagegraveyards.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.models.location.ImmutableLocation;
import com.winterhavenmc.savagegraveyards.models.location.InvalidLocation;
import com.winterhavenmc.savagegraveyards.models.location.ValidLocation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.UUID;


public class GraveyardAdapter implements SqlAdapter
{
	public Graveyard adapt(ResultSet resultSet) throws SQLException
	{
		Attributes attributes = new Attributes(
				resultSet.getBoolean("Enabled"),
				resultSet.getBoolean("Hidden"),
				resultSet.getInt("DiscoveryRange"),
				resultSet.getString("DiscoveryMessage"),
				resultSet.getString("RespawnMessage"),
				resultSet.getString("GroupName"),
				resultSet.getInt("SafetyRange"),
				Duration.ofSeconds(resultSet.getInt("safetyTime")));

		ImmutableLocation location = ImmutableLocation.of(
				resultSet.getString("worldName"),
				new UUID(resultSet.getLong("WorldUidMsb"), resultSet.getLong("WorldUidLsb")),
				resultSet.getDouble("X"),
				resultSet.getDouble("Y"),
				resultSet.getDouble("Z"),
				resultSet.getFloat("Yaw"),
				resultSet.getFloat("Pitch"));

		String displayName = resultSet.getString("DisplayName");

		return switch (location)
		{
			case InvalidLocation ignored -> new Graveyard.Invalid(displayName, "\uD83C\uDF10", "The stored location is invalid.");
			case ValidLocation validLocation -> Graveyard.of(displayName, attributes, validLocation);
		};
	}


	public PreparedStatement adapt(Graveyard.Valid graveyard, PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setString( 1, graveyard.searchKey());
		preparedStatement.setString( 2, graveyard.displayName());
		preparedStatement.setBoolean(3, graveyard.attributes().enabled());
		preparedStatement.setBoolean(4, graveyard.attributes().hidden());
		preparedStatement.setInt(    5, graveyard.attributes().discoveryRange());
		preparedStatement.setString( 6, graveyard.attributes().discoveryMessage());
		preparedStatement.setString( 7, graveyard.attributes().respawnMessage());
		preparedStatement.setString( 8, graveyard.attributes().group());
		preparedStatement.setInt(    9, graveyard.attributes().safetyRange());
		preparedStatement.setLong(  10, graveyard.attributes().safetyTime().getSeconds());
		preparedStatement.setString(11, graveyard.location().world().name());
		preparedStatement.setLong(  12, graveyard.location().world().uid().getMostSignificantBits());
		preparedStatement.setLong(  13, graveyard.location().world().uid().getLeastSignificantBits());
		preparedStatement.setDouble(14, graveyard.location().x());
		preparedStatement.setDouble(15, graveyard.location().y());
		preparedStatement.setDouble(16, graveyard.location().z());
		preparedStatement.setFloat( 17, graveyard.location().yaw());
		preparedStatement.setFloat( 18, graveyard.location().pitch());

		return preparedStatement;
	}
}
