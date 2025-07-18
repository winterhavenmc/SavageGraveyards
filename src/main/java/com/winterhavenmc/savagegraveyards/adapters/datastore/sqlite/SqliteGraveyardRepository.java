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
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.GraveyardReason;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.SearchKey;
import com.winterhavenmc.savagegraveyards.plugin.ports.datastore.GraveyardRepository;
import com.winterhavenmc.savagegraveyards.plugin.storage.Queries;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class SqliteGraveyardRepository implements GraveyardRepository
{
	private final Logger logger;
	private final Connection connection;
	private final SqliteGraveyardRowMapper graveyardMapper = new SqliteGraveyardRowMapper();
	private final SqliteGraveyardQueryHelper queryHandler = new SqliteGraveyardQueryHelper();


	public SqliteGraveyardRepository(final Logger logger, final Connection connection)
	{
		this.logger = logger;
		this.connection = connection;
	}


	/**
	 * Get record
	 *
	 * @param searchKey the name of the Valid to be retrieved
	 * @return Valid object or null if no matching record
	 */
	@Override
	public Graveyard get(final SearchKey.Valid searchKey)
	{
		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectGraveyard")))
		{
			preparedStatement.setString(1, searchKey.string());
			final ResultSet resultSet = preparedStatement.executeQuery();

			// only zero or one record can match the unique search key
			if (resultSet.next())
			{
				return graveyardMapper.map(resultSet);
			}
		}
		catch (SQLException sqlException)
		{
			logger.warning(SQLiteNotice.GRAVEYARD_RECORD_NOT_FOUND.toString());
			logger.warning(sqlException.getLocalizedMessage());
		}

		return new Graveyard.Invalid(searchKey.toDisplayName(), "∅", GraveyardReason.GRAVEYARD_MATCH_NOT_FOUND);
	}


	/**
	 * Select all graveyard records from the datastore, maintaining order returned by the query.
	 *
	 * @return a {@link List} containing all graveyard records in the order they were returned by the query
	 */
	@Override
	public List<Graveyard> getAll()
	{
		final List<Graveyard> returnList = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectAllGraveyards")))
		{
			final ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next())
			{
				returnList.add(graveyardMapper.map(resultSet));
			}
		}
		catch (SQLException sqlException)
		{
			logger.warning(SQLiteNotice.SELECT_ALL_GRAVEYARDS_FAILED.toString());
			logger.warning(sqlException.getLocalizedMessage());
		}

		return returnList;
	}


	/**
	 * Select all valid graveyard records from the datastore, maintaining order returned by the query. Records
	 * that produce an invalid graveyard are not included in the returned collection. Invalid graveyards are most
	 * likely a result of the graveyard's location world not loaded at the time of query.
	 *
	 * @return a {@link List} containing all graveyard records in the order they were returned by the query
	 */
	@Override
	public List<Graveyard.Valid> getAllValid()
	{
		final List<Graveyard.Valid> returnList = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectAllGraveyards")))
		{
			final ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next())
			{
				switch (graveyardMapper.map(resultSet))
				{
					case Graveyard.Valid valid -> returnList.add(valid);
					case Graveyard.Invalid(DisplayName displayName, String ignored, GraveyardReason graveyardReason) ->
							logger.warning("A valid graveyard '" + displayName.colorString() + "' could not be created: " + graveyardReason);
				}
			}
		}
		catch (SQLException sqlException)
		{
			logger.warning(SQLiteNotice.SELECT_ALL_VALID_GRAVEYARDS_FAILED.toString());
			logger.warning(sqlException.getLocalizedMessage());
		}

		return returnList;
	}


	/**
	 * Returns a list of enabled, valid graveyards in the player's current world for which
	 * the player has permission, returned in order of proximity to the player's location.
	 *
	 * @param player the player whose location is used as the origin, and permissions are checked
	 * @return a list of graveyards that match the criteria
	 */
	@Override
	public List<Graveyard.Valid> getNearestList(final Player player)
	{
		if (player == null) { return Collections.emptyList(); }

		final List<Graveyard.Valid> returnList = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectNearestGraveyards")))
		{
			ResultSet resultSet = queryHandler.SelectNearestGraveyards(player, preparedStatement);

			while (resultSet.next())
			{
				if (graveyardMapper.map(resultSet) instanceof Graveyard.Valid valid)
				{
					// check if graveyard has group and player is in group
					if (valid.attributes().group() == null
							|| valid.attributes().group().value().isBlank()
							|| player.hasPermission("group." + valid.attributes().group()))
					{
						returnList.add(valid);
					}
				}
			}
		}
		catch (SQLException sqlException)
		{
			logger.warning(SQLiteNotice.SELECT_NEAREST_GRAVEYARDS_FAILED.toString());
			logger.warning(sqlException.getLocalizedMessage());
		}

		return returnList;
	}


	/**
	 * Gets closest graveyard to player's current location
	 *
	 * @param player the player for whom to retrieve the nearest Valid
	 * @return Valid object
	 */
	@Override
	public Optional<Graveyard.Valid> getNearest(final Player player)
	{
		if (player == null) { return Optional.empty(); }

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectNearestGraveyards")))
		{
			ResultSet resultSet = queryHandler.selectNearestGraveyard(player, preparedStatement);

			while (resultSet.next())
			{
				if (graveyardMapper.map(resultSet) instanceof Graveyard.Valid valid)
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
			this.logger.warning(SQLiteNotice.SELECT_NEAREST_GRAVEYARD_FAILED.toString());
			this.logger.warning(e.getLocalizedMessage());
		}

		return Optional.empty();
	}


	/**
	 * Performs a sql query to retrieve a list of graveyard names that match a given prefix.
	 * Matches are case-insensitive, and match against stored searchKeys while treating
	 * spaces and underscores as equivalent.
	 * <p>
	 * This method is used by command TabCompleter methods th return a list of graveyard
	 * names that match a partially completed name prefix.
	 * <p>
	 * This is currently (11-Jul_2025) the only method that uses the <em>SelectGraveyardNamesMatchingPrefix</em> query.
	 *
	 * @param prefix the prefix to match
	 * @return List of Strings containing graveyard names matched by prefix
	 */
	@Override
	public List<String> getMatchingNames(final String prefix)
	{
		if (prefix == null) return Collections.emptyList();

		final List<String> returnList = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectGraveyardNamesMatchingPrefix")))
		{
			ResultSet resultSet = queryHandler.selectMatchingGraveyardNames(prefix, preparedStatement);
			while (resultSet.next())
			{
				returnList.add(resultSet.getString("SearchKey").replace("_", " "));
			}
		}
		catch (SQLException sqlException)
		{
			logger.warning(SQLiteNotice.SELECT_MATCHING_GRAVEYARD_NAMES_FAILED.toString());
			logger.warning(sqlException.getLocalizedMessage());
		}

		return returnList;
	}


	/**
	 * Get graveyard searchKeys that prefix match string
	 *
	 * @param prefix the prefix to match
	 * @return String collection of names with matching prefix
	 */
	@Override
	public List<String> getMatchingKeys(final String prefix)
	{
		if (prefix == null) return Collections.emptyList();

		final List<String> returnList = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectGraveyardNamesMatchingPrefix")))
		{
			ResultSet resultSet = queryHandler.selectMatchingGraveyardKeys(prefix, preparedStatement);

			while (resultSet.next())
			{
				returnList.add(resultSet.getString("SearchKey"));
			}
		}
		catch (SQLException e)
		{
			logger.warning(SQLiteNotice.SELECT_MATCHING_GRAVEYARD_KEYS_FAILED.toString());
			logger.warning(e.getLocalizedMessage());
		}

		return returnList;
	}


	/**
	 * Select a count of graveyards in the datastore
	 *
	 * @return the count of graveyard records in the datastore
	 */
	@Override
	public int getCount()
	{
		int count = 0;

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectGraveyardCount")))
		{
			final ResultSet resultSet = preparedStatement.executeQuery();

			if (resultSet.next())
			{
				count = resultSet.getInt("GraveyardCount");
			}
		}
		catch (SQLException e)
		{
			logger.warning("An error occurred while attempting to retrieve a count of all graveyard records.");
			logger.warning(e.getLocalizedMessage());
		}

		return count;
	}


	/**
	 * Get undiscovered graveyards for player
	 *
	 * @param player the player for whom to retrieve undiscovered Graveyards
	 * @return Stream of Valid objects that are undiscovered for player
	 */
	@Override
	public Stream<Graveyard.Valid> getUndiscoveredGraveyards(Player player)
	{
		if (player == null) return Stream.empty();

		final Set<Graveyard.Valid> returnSet = new HashSet<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectUndiscoveredGraveyards")))
		{
			ResultSet resultSet = queryHandler.selectUndiscoveredGraveyards(player, preparedStatement);

			while (resultSet.next())
			{
				switch (graveyardMapper.map(resultSet))
				{
					case Graveyard.Valid valid -> returnSet.add(valid);
					case Graveyard.Invalid(DisplayName displayName, String ignored, GraveyardReason reason) ->
							logger.warning("A valid graveyard named " + displayName.colorString() + " could not be created: " + reason);
				}
			}
		}
		catch (SQLException sqlException)
		{
			logger.warning(SQLiteNotice.SELECT_UNDISCOVERED_GRAVEYARD_RECORDS.toString());
			logger.warning(sqlException.getLocalizedMessage());
		}

		return returnSet.stream();
	}


	/**
	 * Get undiscovered graveyard keys for player
	 *
	 * @param player the player for whom to retrieve undiscovered Valid keys
	 * @return HashSet of Valid search keys that are undiscovered for player
	 */
	@Override
	public List<String> getUndiscoveredKeys(final Player player)
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
		catch (SQLException sqlException)
		{
			logger.warning(SQLiteNotice.SELECT_UNDISCOVERED_GRAVEYARD_KEYS.toString());
			logger.warning(sqlException.getLocalizedMessage());
		}

		return returnSet;
	}


	@Override
	public Graveyard save(final Graveyard.Valid graveyard)
	{
		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertGraveyard")))
		{
			// synchronize on connection
			synchronized (this)
			{
				queryHandler.insertGraveyard(graveyard, preparedStatement);
			}
		}
		catch (SQLException sqlException)
		{
			logger.warning("An error occurred while inserting a Valid record "
					+ "into the SQLite datastore.");
			logger.warning(sqlException.getLocalizedMessage());
			return new Graveyard.Invalid(graveyard.displayName(), "∅", GraveyardReason.GRAVEYARD_INSERT_FAILED);
		}

		return graveyard;
	}


	/**
	 * Insert a collection of records
	 *
	 * @param graveyards a collection of graveyard records
	 * @return int - the number of records successfully inserted
	 */
	@Override
	public int saveAll(final Collection<Graveyard.Valid> graveyards)
	{
		if (graveyards == null) return 0;

		int count = 0;

		for (Graveyard.Valid graveyard : graveyards)
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertGraveyard")))
			{
				synchronized (this)
				{
					count += queryHandler.insertGraveyard(graveyard, preparedStatement);
				}
			}
			catch (SQLException sqlException)
			{
				logger.warning("An error occurred while inserting a Valid record "
						+ "into the SQLite datastore.");
				logger.warning(sqlException.getLocalizedMessage());
			}
		}
		return count;
	}


	/**
	 * Update record
	 *
	 * @param graveyard the Valid to update in the datastore
	 * @return the graveyard
	 */
	@Override
	public Graveyard update(final Graveyard.Valid graveyard)
	{
		return update(graveyard.displayName(), graveyard);
	}


	@Override
	public Graveyard update(final DisplayName.Valid oldDisplayName, final Graveyard.Valid graveyard)
	{
		try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("UpdateGraveyard")))
		{
			synchronized (this)
			{
				queryHandler.updateGraveyard(oldDisplayName, graveyard, preparedStatement);
			}
		}
		catch (SQLException sqlException)
		{
			logger.warning(SQLiteNotice.UPDATE_GRAVEYARD_RECORD_FAILED.toString());
			logger.warning(sqlException.getLocalizedMessage());
		}

		return graveyard;
	}


	/**
	 * Delete record
	 *
	 * @param searchKey display name or search key of record to be deleted
	 * @return Deleted graveyard record
	 */
	@Override
	public Graveyard delete(final SearchKey.Valid searchKey)
	{
		// return deleted record or invalid if not found
		if (get(searchKey) instanceof Graveyard.Valid valid)
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("DeleteGraveyard")))
			{
				queryHandler.deleteGraveyard(searchKey, preparedStatement);
				preparedStatement.setString(1, searchKey.string());
				preparedStatement.executeUpdate();
			}
			catch (SQLException sqlException)
			{
				logger.warning(SQLiteNotice.DELETE_GRAVEYARD_RECORD_FAILED.toString());
				logger.warning(sqlException.getLocalizedMessage());
			}
			return valid;
		}
		else
		{
			return new Graveyard.Invalid(DisplayName.of(searchKey.string()), "∅", GraveyardReason.GRAVEYARD_DELETE_FAILED);
		}
	}

}
