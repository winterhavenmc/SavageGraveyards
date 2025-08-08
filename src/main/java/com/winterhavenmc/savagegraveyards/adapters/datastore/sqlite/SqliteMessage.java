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

import com.winterhavenmc.savagegraveyards.plugin.util.Notice;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public enum SqliteMessage implements Notice
{
	ALREADY_INITIALIZED_NOTICE("The SQLite datastore is already initialized."),
	NO_SCHEMA_VERSION_ERROR("Could not read schema version."),
	SCHEMA_UPDATE_ERROR("An error occurred while trying to update the SQLite datastore schema."),
	SCHEMA_UPDATE_V1_ERROR("An error occurred while trying to update the datastore to schema v1."),
	DATASTORE_CLOSED_NOTICE("The SQLite datastore connection was successfully closed."),
	DATASTORE_CLOSE_ERROR("An error occurred while closing the SQLite datastore."),
	SCHEMA_UP_TO_DATE_NOTICE("Current schema is up to date."),
	DATASTORE_INITIALIZED_NOTICE("SQLite datastore initialized."),
	CREATE_GRAVEYARD_TABLE_ERROR("An error occurred while trying to create the Graveyard table in the SQLite datastore."),
	CREATE_DISCOVERY_TABLE_ERROR("An error occurred while trying to create the Discovery table in the SQLite datastore."),
	SELECT_GRAVEYARD_RECORD_ERROR("An error occurred while trying to select a graveyard record from the SQLite database."),
	SELECT_NEAREST_GRAVEYARD_ERROR("An error occurred while trying to fetch the nearest graveyard record from the SQLite datastore."),
	SELECT_NEAREST_GRAVEYARDS_ERROR("An error occurred while trying to fetch the nearest graveyard records from the SQLite datastore."),
	SELECT_MATCHING_GRAVEYARD_KEYS_ERROR("An error occurred while trying to fetch matching graveyard records from the SQLite datastore."),
	SELECT_MATCHING_GRAVEYARD_NAMES_ERROR("An error occurred while trying to fetch matching graveyard records from the SQLite datastore."),
	SELECT_ALL_DISCOVERIES_ERROR("An error occurred while trying to select all discovery records from the SQLite datastore."),
	SELECT_ALL_GRAVEYARDS_ERROR("An error occurred while trying to select all graveyard records from the SQLite datastore."),
	SELECT_ALL_VALID_GRAVEYARDS_ERROR("An error occurred while trying to select all graveyard graveyard records from the SQLite datastore."),
	SELECT_UNDISCOVERED_RECORDS_ERROR("An error occurred while trying to select undiscovered graveyard records from the SQLite datastore."),
	SELECT_UNDISCOVERED_KEYS_ERROR("An error occurred while trying to select undiscovered graveyard keys from the SQLite datastore."),
	INSERT_DISCOVERY_ERROR("An error occurred while trying to insert a discovery record into the SQLite datastore."),
	UPDATE_GRAVEYARD_RECORD_FAILED("An error occurred while trying to update a graveyard record into the SQLite datastore."),
	DELETE_GRAVEYARD_RECORD_FAILED("An error occurred while attempting to delete a graveyard record from the SQLite datastore."),
	ENABLE_FOREIGN_KEYS_ERROR("An error occurred while attempting to enable foreign keys in the SQLite datastore."),
	INSERT_GRAVEYARD_ERROR("An error occurred while inserting a graveyard record into the SQLite datastore."),
	;

	private final String defaultMessage;


	SqliteMessage(String defaultMessage)
	{
		this.defaultMessage = defaultMessage;
	}

	@Override
	public String toString()
	{
		return defaultMessage;
	}


	public String getLocalizeMessage(final Locale locale)
	{
		try
		{
			ResourceBundle bundle = ResourceBundle.getBundle(getClass().getSimpleName(), locale);
			return bundle.getString(name());
		}
		catch (MissingResourceException exception)
		{
			return this.defaultMessage;
		}
	}
}
