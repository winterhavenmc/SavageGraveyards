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


public enum SQLiteNotice implements Notice
{
	ALREADY_INITIALIZED("The SQLite datastore is already initialized."),
	SCHEMA_VERSION_NOT_FOUND("Could not read schema version."),
	TABLE_NOT_FOUND("An error occurred while trying to check the existence of a table."),
	SCHEMA_UPDATE_FAILED("An error occurred while trying to update the SQLite datastore schema."),
	SCHEMA_UPDATE_V1_FAILED("An error occurred while trying to update the datastore to schema v1."),
	DATABASE_CLOSE_SUCCESS("The SQLite datastore connection was successfully closed."),
	DATABASE_CLOSE_FAILED("An error occurred while closing the SQLite datastore."),
	GRAVEYARD_RECORD_NOT_FOUND("An error occurred while trying to select a graveyard record from the SQLite database."),
	SELECT_NEAREST_GRAVEYARD_FAILED("An error occurred while trying to fetch the nearest graveyard record from the SQLite datastore."),
	SELECT_NEAREST_GRAVEYARDS_FAILED("An error occurred while trying to fetch the nearest graveyard records from the SQLite datastore."),
	SELECT_MATCHING_GRAVEYARD_KEYS_FAILED("An error occurred while trying to fetch matching graveyard records from the SQLite datastore."),
	SELECT_MATCHING_GRAVEYARD_NAMES_FAILED("An error occurred while trying to fetch matching graveyard records from the SQLite datastore."),
	SELECT_ALL_GRAVEYARDS_FAILED("An error occurred while trying to select all graveyard records from the SQLite datastore."),
	SELECT_ALL_VALID_GRAVEYARDS_FAILED("An error occurred while trying to select all graveyard graveyard records from the SQLite datastore."),
	SELECT_UNDISCOVERED_GRAVEYARD_RECORDS("An error occurred while trying to select undiscovered graveyard records from the SQLite datastore."),
	SELECT_UNDISCOVERED_GRAVEYARD_KEYS("An error occurred while trying to select undiscovered graveyard keys from the SQLite datastore."),
	INSERT_DISCOVERY_FAILED("An error occurred while trying to insert a discovery record into the SQLite datastore."),
	UPDATE_GRAVEYARD_RECORD_FAILED("An error occurred while trying to update a graveyard record into the SQLite datastore."),
	DELETE_GRAVEYARD_RECORD_FAILED("An error occurred while attempting to delete a graveyard record from the SQLite datastore."),
	ENABLE_FOREIGN_KEYS_FAILED("An error occurred while attempting to enable foreign keys in the SQLite datastore.");

	private final String message;

	SQLiteNotice(String message)
	{
		this.message = message;
	}

	@Override
	public String toString()
	{
		return message;
	}
}
