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

import com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard;
import com.winterhavenmc.savagegraveyards.models.searchkey.ValidSearchKey;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


public final class SqliteGraveyardQueryExecutor
{
	public ResultSet selectUndiscoveredKeys(final Player player, final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setLong(1, player.getWorld().getUID().getMostSignificantBits());
		preparedStatement.setLong(2, player.getWorld().getUID().getLeastSignificantBits());
		preparedStatement.setLong(3, player.getUniqueId().getMostSignificantBits());
		preparedStatement.setLong(4, player.getUniqueId().getLeastSignificantBits());
		return preparedStatement.executeQuery();
	}


	public ResultSet selectNearestGraveyard(final Player player, final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setLong(  1, player.getWorld().getUID().getMostSignificantBits());
		preparedStatement.setLong(  2, player.getWorld().getUID().getLeastSignificantBits());
		preparedStatement.setLong(  3, player.getUniqueId().getMostSignificantBits());
		preparedStatement.setLong(  4, player.getUniqueId().getLeastSignificantBits());
		preparedStatement.setDouble(5, player.getLocation().getX());
		preparedStatement.setDouble(6, player.getLocation().getY());
		preparedStatement.setDouble(7, player.getLocation().getZ());
		return preparedStatement.executeQuery();
	}


	public ResultSet SelectNearestGraveyards(final Player player, final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setLong(  1, player.getWorld().getUID().getMostSignificantBits());
		preparedStatement.setLong(  2, player.getWorld().getUID().getLeastSignificantBits());
		preparedStatement.setLong(  3, player.getUniqueId().getMostSignificantBits());
		preparedStatement.setLong(  4, player.getUniqueId().getLeastSignificantBits());
		preparedStatement.setDouble(5, player.getLocation().getX());
		preparedStatement.setDouble(6, player.getLocation().getY());
		preparedStatement.setDouble(7, player.getLocation().getZ());
		return preparedStatement.executeQuery();
	}


	public ResultSet selectUndiscoveredGraveyards(final Player player, final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setLong(1, player.getWorld().getUID().getMostSignificantBits());
		preparedStatement.setLong(2, player.getWorld().getUID().getLeastSignificantBits());
		preparedStatement.setLong(3, player.getUniqueId().getMostSignificantBits());
		preparedStatement.setLong(4, player.getUniqueId().getLeastSignificantBits());
		return preparedStatement.executeQuery();
	}


	public ResultSet selectMatchingGraveyardKeys(final String prefix, final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setString(1, prefix + "%");
		return preparedStatement.executeQuery();
	}


	public ResultSet selectMatchingGraveyardNames(final String prefix, final PreparedStatement preparedStatement) throws  SQLException
	{
		preparedStatement.setString(1, prefix + "%");
		return preparedStatement.executeQuery();
	}


	public ResultSet selectGraveyard(final UUID graveyardUid, final PreparedStatement preparedStatement) throws  SQLException
	{
		preparedStatement.setLong(1, graveyardUid.getMostSignificantBits());
		preparedStatement.setLong(2, graveyardUid.getLeastSignificantBits());
		return preparedStatement.executeQuery();
	}


	public int insertGraveyard(final ValidGraveyard graveyard,
	                           final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setString( 1, graveyard.searchKey().string());
		preparedStatement.setString( 2, graveyard.displayName().colorString());
		preparedStatement.setLong(   3, graveyard.uid().getMostSignificantBits());
		preparedStatement.setLong(   4, graveyard.uid().getLeastSignificantBits());
		preparedStatement.setBoolean(5, graveyard.attributes().enabled().value());
		preparedStatement.setBoolean(6, graveyard.attributes().hidden().value());
		preparedStatement.setInt(    7, graveyard.attributes().discoveryRange().value());
		preparedStatement.setString( 8, graveyard.attributes().discoveryMessage().value());
		preparedStatement.setString( 9, graveyard.attributes().respawnMessage().value());
		preparedStatement.setString(10, graveyard.attributes().group().value());
		preparedStatement.setInt(   11, graveyard.attributes().safetyRange().value());
		preparedStatement.setLong(  12, graveyard.attributes().safetyTime().value().toSeconds());
		preparedStatement.setString(13, graveyard.location().world().name());
		preparedStatement.setLong(  14, graveyard.location().world().uid().getMostSignificantBits());
		preparedStatement.setLong(  15, graveyard.location().world().uid().getLeastSignificantBits());
		preparedStatement.setDouble(16, graveyard.location().x());
		preparedStatement.setDouble(17, graveyard.location().y());
		preparedStatement.setDouble(18, graveyard.location().z());
		preparedStatement.setFloat( 19, graveyard.location().yaw());
		preparedStatement.setFloat( 20, graveyard.location().pitch());
		return preparedStatement.executeUpdate();
	}


	@SuppressWarnings("UnusedReturnValue")
	public int updateGraveyard(final ValidSearchKey searchKey,
	                           final ValidGraveyard graveyard,
	                           final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setString( 1, graveyard.searchKey().string());
		preparedStatement.setString( 2, graveyard.displayName().colorString());
		preparedStatement.setLong(   3, graveyard.uid().getMostSignificantBits());
		preparedStatement.setLong(   4, graveyard.uid().getLeastSignificantBits());
		preparedStatement.setBoolean(5, graveyard.attributes().enabled().value());
		preparedStatement.setBoolean(6, graveyard.attributes().hidden().value());
		preparedStatement.setInt(    7, graveyard.attributes().discoveryRange().value());
		preparedStatement.setString( 8, graveyard.attributes().discoveryMessage().value());
		preparedStatement.setString( 9, graveyard.attributes().respawnMessage().value());
		preparedStatement.setString(10, graveyard.attributes().group().value());
		preparedStatement.setInt(   11, graveyard.attributes().safetyRange().value());
		preparedStatement.setLong(  12, graveyard.attributes().safetyTime().value().toSeconds());
		preparedStatement.setString(13, graveyard.location().world().name());
		preparedStatement.setLong(  14, graveyard.location().world().uid().getMostSignificantBits());
		preparedStatement.setLong(  15, graveyard.location().world().uid().getLeastSignificantBits());
		preparedStatement.setDouble(16, graveyard.location().x());
		preparedStatement.setDouble(17, graveyard.location().y());
		preparedStatement.setDouble(18, graveyard.location().z());
		preparedStatement.setFloat( 19, graveyard.location().yaw());
		preparedStatement.setFloat( 20, graveyard.location().pitch());
		preparedStatement.setString(21, searchKey.string());
		return preparedStatement.executeUpdate();
	}


	@SuppressWarnings("UnusedReturnValue")
	public int deleteGraveyard(final ValidSearchKey searchKey, final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setString(1, searchKey.string());
		return preparedStatement.executeUpdate();
	}

}
