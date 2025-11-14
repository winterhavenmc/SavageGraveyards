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
import com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard;
import com.winterhavenmc.savagegraveyards.models.searchkey.ValidSearchKey;

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
	 * @param graveyard the graveyard discovered
	 * @param playerUid the uid of the player discovering
	 * @return a graveyard discovery
	 */
	static Discovery of(final ValidGraveyard graveyard, final UUID playerUid)
	{
		if (graveyard == null) return new InvalidDiscovery(FailReason.PARAMETER_NULL, Parameter.GRAVEYARD);
		else if (playerUid == null) return new InvalidDiscovery(FailReason.PARAMETER_NULL, Parameter.PLAYER_UID);
		else return Discovery.of(graveyard.searchKey(), playerUid, Instant.now());
	}


	/**
 	 * Creates a graveyard discovery
	 *
	 * @param searchKey the search key of the graveyard discovered
	 * @param playerUid the uid of the player discovering
	 * @return a graveyard discovery
	 */
	static Discovery of(final ValidSearchKey searchKey, final UUID playerUid)
	{
		if (searchKey == null) return new InvalidDiscovery(FailReason.PARAMETER_NULL, Parameter.SEARCH_KEY);
		else if (playerUid == null) return new InvalidDiscovery(FailReason.PARAMETER_NULL, Parameter.PLAYER_UID);
		else return new ValidDiscovery(searchKey, playerUid, Instant.now());
	}


	/**
	 * Creates a graveyard discovery
	 *
	 * @param searchKey the search key of the graveyard discovered
	 * @param playerUid the uid of the player discovering
	 * @param timeStamp the discovery creation time, as {@link Instant}
	 * @return a graveyard discovery
	 */
	static Discovery of(final ValidSearchKey searchKey, final UUID playerUid, final Instant timeStamp)
	{
		if (searchKey == null) return new InvalidDiscovery(FailReason.PARAMETER_NULL, Parameter.SEARCH_KEY);
		else if (playerUid == null) return new InvalidDiscovery(FailReason.PARAMETER_NULL, Parameter.PLAYER_UID);
		else if (timeStamp == null) return new ValidDiscovery(searchKey, playerUid, Instant.now());
		else return new ValidDiscovery(searchKey, playerUid, timeStamp);
	}

}
