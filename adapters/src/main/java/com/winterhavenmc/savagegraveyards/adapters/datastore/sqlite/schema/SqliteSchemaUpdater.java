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

package com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite.schema;

import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;
import com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite.SqliteMessage;
import com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite.SqliteQueries;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.GraveyardRepository;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.logging.Logger;


public sealed interface SqliteSchemaUpdater permits SqliteSchemaUpdaterFromV0, SqliteSchemaUpdaterNoOp
{
	void update();


	static SqliteSchemaUpdater create(final Plugin plugin,
	                                  final Connection connection,
	                                  final ConfigRepository configRepository,
	                                  final GraveyardRepository graveyardRepository,
	                                  final DiscoveryRepository discoveryRepository)
	{
		int schemaVersion = getSchemaVersion(connection, plugin.getLogger(), configRepository);
		return (schemaVersion == 0)
				? new SqliteSchemaUpdaterFromV0(plugin, connection, configRepository, graveyardRepository, discoveryRepository)
				: new SqliteSchemaUpdaterNoOp(plugin, configRepository);
	}


	static int getSchemaVersion(final Connection connection,
	                            final Logger logger,
	                            final ConfigRepository configRepository)
	{
		int version = 0;
		try (PreparedStatement statement = connection.prepareStatement(SqliteQueries.getQuery("GetUserVersion")))
		{
			try (ResultSet resultSet = statement.executeQuery())
			{
				if (resultSet.next())
				{
					version = resultSet.getInt(1);
				}
			}
		}
		catch (SQLException sqlException)
		{
			logger.warning(SqliteMessage.SCHEMA_VERSION_ERROR.getLocalizedMessage(configRepository.locale()));
			logger.warning(sqlException.getLocalizedMessage());
		}

		return version;
	}


	default void setSchemaVersion(final Connection connection,
	                             final Logger logger,
	                             final ConfigRepository configRepository,
	                             final int version)
	{
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate("PRAGMA user_version = " + version);
		}
		catch (SQLException sqlException)
		{
			logger.warning(SqliteMessage.SCHEMA_UPDATE_ERROR.getLocalizedMessage(configRepository.locale()));
			logger.warning(sqlException.getLocalizedMessage());
		}
	}


	default boolean tableExists(final Connection connection, final String tableName)
	{
		try (PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectTable")))
		{
			preparedStatement.setString(1, tableName);
			try (ResultSet resultSet = preparedStatement.executeQuery())
			{
				return resultSet.next(); // returns true if a row is found
			}
		}
		catch (SQLException sqlException)
		{
			return false;
		}
	}

}
