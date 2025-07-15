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

package com.winterhavenmc.savagegraveyards.plugin.ports.in.discovery;

import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;


public interface GetDiscoveryTaskGraveyardsUseCase
{
	List<Graveyard> selectUndiscoveredGraveyards(Player player);
	boolean insertDiscovery(Graveyard.Valid graveyard, UUID playerUid);
}
