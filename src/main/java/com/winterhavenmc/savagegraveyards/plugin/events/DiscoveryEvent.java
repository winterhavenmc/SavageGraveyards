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

package com.winterhavenmc.savagegraveyards.plugin.events;

import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;


@SuppressWarnings("unused")
public final class DiscoveryEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private final Graveyard.Valid graveyard;


	public DiscoveryEvent(final Player player, final Graveyard.Valid graveyard)
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
	public Graveyard.Valid getGraveyard()
	{
		return this.graveyard;
	}

}
