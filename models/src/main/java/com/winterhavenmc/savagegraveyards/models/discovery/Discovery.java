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

package com.winterhavenmc.savagegraveyards.models.discovery;

import com.winterhavenmc.savagegraveyards.models.FailReason;
import com.winterhavenmc.savagegraveyards.models.Parameter;

import java.time.Instant;
import java.util.UUID;


/**
 * Represents a graveyard discovery as an algebraic data type, implemented using a sealed interface
 * with permitted types of {@link ValidDiscovery} or {@link InvalidDiscovery}.
 * <p>
 * <img src="doc-files/Discovery_structure.svg" alt="Discovery Structure"/>
 */
public sealed interface Discovery permits ValidDiscovery, InvalidDiscovery
{
	/**
 	 * Creates a graveyard discovery
	 *
	 * @param graveyardUid the uid of the graveyard discovered
	 * @param playerUid the uid of the player discovering
	 * @return a graveyard discovery
	 */
	static Discovery of(final UUID graveyardUid, final UUID playerUid)
	{
		if (graveyardUid == null) return new InvalidDiscovery(FailReason.PARAMETER_NULL, Parameter.GRAVEYARD_UID);
		else if (playerUid == null) return new InvalidDiscovery(FailReason.PARAMETER_NULL, Parameter.PLAYER_UID);
		else return new ValidDiscovery(graveyardUid, playerUid, Instant.now());
	}


	/**
	 * Creates a graveyard discovery
	 *
	 * @param graveyardUid the search key of the graveyard discovered
	 * @param playerUid    the uid of the player discovering
	 * @param timeStamp    the discovery creation time, as {@link Instant}
	 * @return a graveyard discovery
	 */
	static Discovery of(final UUID graveyardUid, final UUID playerUid, final Instant timeStamp)
	{
		if (graveyardUid == null) return new InvalidDiscovery(FailReason.PARAMETER_NULL, Parameter.SEARCH_KEY);
		else if (playerUid == null) return new InvalidDiscovery(FailReason.PARAMETER_NULL, Parameter.PLAYER_UID);
		else if (timeStamp == null) return new ValidDiscovery(graveyardUid, playerUid, Instant.now());
		else return new ValidDiscovery(graveyardUid, playerUid, timeStamp);
	}

}
