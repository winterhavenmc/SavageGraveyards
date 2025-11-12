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

package com.winterhavenmc.savagegraveyards.adapters.events.bukkit;

import com.winterhavenmc.savagegraveyards.ports.events.DiscoveryEvent;
import com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;


@SuppressWarnings("unused")
public final class BukkitDiscoveryEvent extends Event implements DiscoveryEvent
{
	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private final ValidGraveyard graveyard;


	public BukkitDiscoveryEvent(final Player player, final ValidGraveyard graveyard)
	{
		this.player = player;
		this.graveyard = graveyard;
	}


	@Nonnull
	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}


	/**
	 * Get player that triggered graveyard discovery
	 *
	 * @return player
	 */
	@Override
	public Player getPlayer()
	{
		return this.player;
	}


	/**
	 * Get graveyard discovered by player
	 *
	 * @return graveyard
	 */
	@Override
	public ValidGraveyard getGraveyard()
	{
		return this.graveyard;
	}
}
