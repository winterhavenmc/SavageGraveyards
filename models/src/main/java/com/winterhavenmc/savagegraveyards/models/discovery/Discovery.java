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
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.UUID;


public sealed interface Discovery permits ValidDiscovery, InvalidDiscovery
{
	static Discovery of(final ValidGraveyard graveyard, final Player player)
	{
		if (graveyard == null) return new InvalidDiscovery(FailReason.PARAMETER_NULL, Parameter.GRAVEYARD);
		else if (player == null) return new InvalidDiscovery(FailReason.PARAMETER_NULL, Parameter.PLAYER);
		else return Discovery.of(graveyard.searchKey(), player.getUniqueId(), Instant.now());
	}


	static Discovery of(final ValidSearchKey searchKey, final UUID playerUid)
	{
		if (searchKey == null) return new InvalidDiscovery(FailReason.PARAMETER_NULL, Parameter.SEARCH_KEY);
		else if (playerUid == null) return new InvalidDiscovery(FailReason.PARAMETER_NULL, Parameter.PLAYER_UID);
		else return new ValidDiscovery(searchKey, playerUid, Instant.now());
	}


	static Discovery of(final ValidSearchKey searchKey, final UUID playerUid, final Instant timeStamp)
	{
		if (searchKey == null) return new InvalidDiscovery(FailReason.PARAMETER_NULL, Parameter.SEARCH_KEY);
		else if (playerUid == null) return new InvalidDiscovery(FailReason.PARAMETER_NULL, Parameter.PLAYER_UID);
		else if (timeStamp == null) return new ValidDiscovery(searchKey, playerUid, Instant.now());
		else return new ValidDiscovery(searchKey, playerUid, timeStamp);
	}

}
