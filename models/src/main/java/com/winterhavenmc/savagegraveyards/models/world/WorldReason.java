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

package com.winterhavenmc.savagegraveyards.models.world;

import java.util.Locale;


public enum WorldReason
{
	WORLD_NULL("The world was null."),
	WORLD_NAME_NULL("The world name was null."),
	WORLD_NAME_BLANK("The world name was blank."),
	WORLD_UUID_NULL("The world UUID was null."),
	;


	private final String defaultMessage;

	WorldReason(String defaultMessage)
	{
		this.defaultMessage = defaultMessage;
	}


	@Override
	public String toString()
	{
		return this.defaultMessage;
	}


	public String getLocalizedMessage(Locale locale)
	{
		return this.defaultMessage;
	}

}
