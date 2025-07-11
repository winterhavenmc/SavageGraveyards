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

package com.winterhavenmc.savagegraveyards.plugin.storage.graveyard;

import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.plugin.ports.in.graveyard.GetRespawnGraveyardUseCase;
import com.winterhavenmc.savagegraveyards.plugin.storage.DataStore;
import org.bukkit.entity.Player;

import java.util.Optional;

public class GetRespawnGraveyardService implements GetRespawnGraveyardUseCase
{
	private final DataStore dataStore;


	public GetRespawnGraveyardService(DataStore dataStore)
	{
		this.dataStore = dataStore;
	}


	/**
	 * Gets closest graveyard to player's current location.
	 *
	 * @param player the player for whom to retrieve the nearest Valid
	 * @return Valid object
	 */
	@Override
	public Optional<Graveyard.Valid> selectNearestGraveyard(final Player player)
	{
		if (player == null) throw new IllegalArgumentException("The player cannot be null.");

		return dataStore.selectNearestGraveyard(player);
	}

}
