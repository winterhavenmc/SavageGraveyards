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

package com.winterhavenmc.savagegraveyards.plugin.models.discovery;

public enum Reason
{
	GRAVEYARD_NULL("The graveyard parameter was null."),
	PLAYER_NULL("The player parameter was null."),
	SEARCH_KEY_NULL("The searchKey parameter was null."),
	PLAYER_UID_NULL("The playerUid parameter was null."),
	;

	private final String message;

	Reason(String message)
	{
		this.message = message;
	}

	@Override
	public String toString()
	{
		return this.message;
	}
}
