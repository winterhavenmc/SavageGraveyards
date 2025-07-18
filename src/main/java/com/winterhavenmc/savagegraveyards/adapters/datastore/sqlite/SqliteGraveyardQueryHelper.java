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

import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.DisplayName;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.SearchKey;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class SqliteGraveyardQueryHelper
{
	public ResultSet selectUndiscoveredKeys(final Player player, final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setLong(  1, player.getWorld().getUID().getMostSignificantBits());
		preparedStatement.setLong(  2, player.getWorld().getUID().getLeastSignificantBits());
		preparedStatement.setLong(  3, player.getUniqueId().getMostSignificantBits());
		preparedStatement.setLong(  4, player.getUniqueId().getLeastSignificantBits());
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


	public int insertGraveyard(final Graveyard.Valid graveyard,
	                           final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setString( 1, graveyard.searchKey().string());
		preparedStatement.setString( 2, graveyard.displayName().colorString());
		preparedStatement.setBoolean(3, graveyard.attributes().enabled().value());
		preparedStatement.setBoolean(4, graveyard.attributes().hidden().value());
		preparedStatement.setInt(    5, graveyard.attributes().discoveryRange().value());
		preparedStatement.setString( 6, graveyard.attributes().discoveryMessage().value());
		preparedStatement.setString( 7, graveyard.attributes().respawnMessage().value());
		preparedStatement.setString( 8, graveyard.attributes().group().value());
		preparedStatement.setInt(    9, graveyard.attributes().safetyRange().value());
		preparedStatement.setLong(  10, graveyard.attributes().safetyTime().value().toSeconds());
		preparedStatement.setString(11, graveyard.location().world().name());
		preparedStatement.setLong(  12, graveyard.location().world().uid().getMostSignificantBits());
		preparedStatement.setLong(  13, graveyard.location().world().uid().getLeastSignificantBits());
		preparedStatement.setDouble(14, graveyard.location().x());
		preparedStatement.setDouble(15, graveyard.location().y());
		preparedStatement.setDouble(16, graveyard.location().z());
		preparedStatement.setFloat( 17, graveyard.location().yaw());
		preparedStatement.setFloat( 18, graveyard.location().pitch());
		return preparedStatement.executeUpdate();
	}


	public int updateGraveyard(final DisplayName.Valid oldDisplayName,
	                           final Graveyard.Valid graveyard,
	                           final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setString( 1, graveyard.searchKey().string());
		preparedStatement.setString( 2, graveyard.displayName().colorString());
		preparedStatement.setBoolean(3, graveyard.attributes().enabled().value());
		preparedStatement.setBoolean(4, graveyard.attributes().hidden().value());
		preparedStatement.setInt(    5, graveyard.attributes().discoveryRange().value());
		preparedStatement.setString( 6, graveyard.attributes().discoveryMessage().value());
		preparedStatement.setString( 7, graveyard.attributes().respawnMessage().value());
		preparedStatement.setString( 8, graveyard.attributes().group().value());
		preparedStatement.setInt(    9, graveyard.attributes().safetyRange().value());
		preparedStatement.setLong(  10, graveyard.attributes().safetyTime().value().toSeconds());
		preparedStatement.setString(11, graveyard.location().world().name());
		preparedStatement.setLong(  12, graveyard.location().world().uid().getMostSignificantBits());
		preparedStatement.setLong(  13, graveyard.location().world().uid().getLeastSignificantBits());
		preparedStatement.setDouble(14, graveyard.location().x());
		preparedStatement.setDouble(15, graveyard.location().y());
		preparedStatement.setDouble(16, graveyard.location().z());
		preparedStatement.setFloat( 17, graveyard.location().yaw());
		preparedStatement.setFloat( 18, graveyard.location().pitch());
		preparedStatement.setString(19, oldDisplayName.toSearchKey().string());
		return preparedStatement.executeUpdate();
	}


	@SuppressWarnings("UnusedReturnValue")
	public int deleteGraveyard(final SearchKey.Valid searchKey, final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setString(1, searchKey.string());
		return preparedStatement.executeUpdate();
	}

}
