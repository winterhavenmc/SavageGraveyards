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

package com.winterhavenmc.savagegraveyards.adapters.out.persistence.sqlite;

import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.plugin.ports.out.persistence.GraveyardDatastore;
import com.winterhavenmc.savagegraveyards.plugin.storage.Queries;
import com.winterhavenmc.savagegraveyards.plugin.storage.sqlite.GraveyardQueryHandler;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class SqliteGraveyardDatastore implements GraveyardDatastore
{
	private final Connection connection;
	private final GraveyardQueryHandler queryHandler;
	private final Logger logger = Logger.getLogger(this.getClass().getName());


	public SqliteGraveyardDatastore(final Connection connection, final GraveyardQueryHandler queryHandler)
	{
		this.connection = connection;
		this.queryHandler = queryHandler;
	}


	@Override
	public Optional<Graveyard.Valid> getNearestRespawn(Player player)
	{
		if (player == null) { return Optional.empty(); }

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectNearestGraveyards")))
		{
			preparedStatement.setLong(1, player.getWorld().getUID().getMostSignificantBits());
			preparedStatement.setLong(2, player.getWorld().getUID().getLeastSignificantBits());
			preparedStatement.setLong(3, player.getUniqueId().getMostSignificantBits());
			preparedStatement.setLong(4, player.getUniqueId().getLeastSignificantBits());
			preparedStatement.setDouble(5, player.getLocation().getX());
			preparedStatement.setDouble(6, player.getLocation().getY());
			preparedStatement.setDouble(7, player.getLocation().getZ());
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next())
			{
				if (queryHandler.instantiateGraveyard(resultSet) instanceof Graveyard.Valid valid)
				{
					// check if graveyard has group and player is in group
					if (valid.attributes().group() == null
							|| valid.attributes().group().value().isBlank()
							|| player.hasPermission("group." + valid.attributes().group()))
					{
						return Optional.of(valid);
					}
				}
			}
		}
		catch (SQLException e)
		{
			// output simple error message
			this.logger.warning("An error occurred while trying to "
					+ "fetch the select Valid from the SQLite datastore.");
			this.logger.warning(e.getLocalizedMessage());
		}

		return Optional.empty();
	}


	@Override
	public List<Graveyard> getListForDisplay()
	{
		final List<Graveyard> returnList = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectAllGraveyards")))
		{
			final ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next())
			{
				returnList.add(queryHandler.instantiateGraveyard(resultSet));
			}
		}
		catch (SQLException e)
		{
			this.logger.warning("An error occurred while trying to "
					+ "select all graveyard records from the SQLite datastore.");
			this.logger.warning(e.getLocalizedMessage());
		}

		return returnList;
	}


	@Override
	public List<String> selectUndiscoveredKeys(final Player player)
	{
		if (player == null) return Collections.emptyList();

		final List<String> returnSet = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectUndiscoveredGraveyardKeys")))
		{
			ResultSet resultSet = queryHandler.selectUndiscoveredKeys(player, preparedStatement);

			while (resultSet.next())
			{
				returnSet.add(resultSet.getString("SearchKey"));
			}
		}
		catch (SQLException e)
		{
			logger.warning("An error occurred while trying to "
					+ "select undiscovered Valid keys from the SQLite datastore.");
			logger.warning(e.getLocalizedMessage());
		}

		return returnSet;
	}
}
