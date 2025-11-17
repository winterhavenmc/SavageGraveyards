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

import com.winterhavenmc.savagegraveyards.models.graveyard.Graveyard;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface GraveyardRowMapper
{
	/**
	 * Maps columns of a database query ResultSet to fields of a newly created graveyard object
	 *
	 * @param resultSet the query result set
	 * @return an instance of {@code ValidGraveyard} if field mapping was successful, or {@code InvalidGraveyard} if not
	 * @throws SQLException if the sql query fails
	 */
	Graveyard map(ResultSet resultSet) throws SQLException;

	String selectAllQueryKey();
}
