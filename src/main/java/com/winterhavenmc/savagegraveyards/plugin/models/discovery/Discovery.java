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

import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.SearchKey;
import org.bukkit.entity.Player;

import java.util.UUID;


public sealed interface Discovery permits Discovery.Valid, Discovery.Invalid
{
	record Valid(SearchKey.Valid searchKey, UUID playerUid) implements Discovery { }
	record Invalid(String reason) implements Discovery { }


	static Discovery of(final Graveyard.Valid graveyard, final Player player)
	{
		if (graveyard == null) return new Discovery.Invalid("The graveyard was null.");
		else if (player == null) return new Discovery.Invalid("The player was null.");
		else return new Discovery.Valid(graveyard.searchKey(), player.getUniqueId());
	}


	static Discovery of(final SearchKey.Valid searchKey, final UUID playerUid)
	{
		if (playerUid == null) return new Discovery.Invalid("The player UUID was null.");
		else return new Discovery.Valid(searchKey, playerUid);
	}

}
