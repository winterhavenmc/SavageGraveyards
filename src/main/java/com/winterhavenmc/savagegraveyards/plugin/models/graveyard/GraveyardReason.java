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

package com.winterhavenmc.savagegraveyards.plugin.models.graveyard;

import com.winterhavenmc.savagegraveyards.plugin.util.Notice;


public enum GraveyardReason implements Notice
{
	DISPLAY_NAME_NULL("The display name parameter was null."),
	DISPLAY_NAME_BLANK("The display name was blank."),
	PLAYER_NULL("The parameter 'player' cannot be null."),
	DISPLAY_NAME_INVALID("The DisplayName was invalid."),
	DISPLAY_NAME_STRING_NULL("The string parameter was null."),
	DISPLAY_NAME_STRING_BLANK("The string parameter was blank."),
	GRAVEYARD_MATCH_NOT_FOUND("No matching graveyard found."),
	GRAVEYARD_INSERT_FAILED("Could not insert graveyard in datastore."),
	GRAVEYARD_DELETE_FAILED("No graveyard was found to delete."),
	GRAVEYARD_STORED_LOCATION_INVALID("The stored location is invalid."),
	GRAVEYARD_STORED_DISPLAY_NAME_INVALID("The stored display name is invalid."),
	;

	private final String message;

	GraveyardReason(String message)
	{
		this.message = message;
	}

	@Override
	public String toString()
	{
		return this.message;
	}
}
