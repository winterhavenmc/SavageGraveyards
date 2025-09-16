/*
 * Copyright (c) 2022-2025 Tim Savage.
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

package com.winterhavenmc.savagegraveyards.core.events;

import com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;


@SuppressWarnings("unused")
public final class DiscoveryEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private final ValidGraveyard graveyard;


	public DiscoveryEvent(final Player player, final ValidGraveyard graveyard)
	{
		this.player = player;
		this.graveyard = graveyard;
	}


	@Override @Nonnull
	public HandlerList getHandlers()
	{
		return handlers;
	}


	/**
	 * Get player that triggered graveyard discovery
	 *
	 * @return player
	 */
	public Player getPlayer()
	{
		return this.player;
	}


	/**
	 * Get graveyard discovered by player
	 *
	 * @return graveyard
	 */
	public ValidGraveyard getGraveyard()
	{
		return this.graveyard;
	}

}
