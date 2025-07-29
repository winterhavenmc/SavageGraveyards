/*
 * Copyright (c) 2022-2025 Tim Savage.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Fetch database queries from properties file
 */
final class SQLiteQueries
{
	private static final String propFileName = "queries.properties";
	private static Properties properties;


	/**
	 * Private class constructor to prevent instantiation
	 */
	private SQLiteQueries()
	{
		throw new AssertionError();
	}

	private static Properties getQueries()
	{
		// singleton
		if (properties == null)
		{
			properties = new Properties();
			InputStream inputStream = SQLiteQueries.class.getResourceAsStream("/" + propFileName);
			try
			{
				properties.load(inputStream);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e); //TODO: more appropriate exception?
			}
		}

		return properties;
	}

	static String getQuery(final String query)
	{
		return getQueries().getProperty(query);
	}

}
