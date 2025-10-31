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

package com.winterhavenmc.savagegraveyards.models;

public enum Parameter
{
	DISPLAY_NAME("displayName"),
	GRAVEYARD("graveyard"),
	LOCATION("location"),
	PLAYER("player"),
	PLAYER_UID("playerUid"),
	SEARCH_KEY("searchKey"),
	STRING("string"),
	WORLD("world"),
	WORLD_NAME("worldName"),
	WORLD_UID("worldUid"),
	;

	private final String string;


	Parameter(final String string)
	{
		this.string = string;
	}

	@Override
	public String toString()
	{
		return string;
	}

}
