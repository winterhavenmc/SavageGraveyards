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

package com.winterhavenmc.savagegraveyards.core.ports.listeners;

import com.winterhavenmc.savagegraveyards.core.context.ListenerCtx;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public interface PlayerEventListener extends Listener
{
	PlayerEventListener init(ListenerCtx ctx);

	/**
	 * Player death event handler
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	void onPlayerDeath(PlayerDeathEvent event);

	/**
	 * Player respawn event handler for LOWEST priority
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	void onPlayerRespawnLOWEST(PlayerRespawnEvent event);

	/**
	 * Player respawn event handler for LOW priority
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.LOW)
	void onPlayerRespawnLOW(PlayerRespawnEvent event);

	/**
	 * Player respawn event handler for NORMAL priority
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	void onPlayerRespawnNORMAL(PlayerRespawnEvent event);

	/**
	 * Player respawn event handler for HIGH priority
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.HIGH)
	void onPlayerRespawnHIGH(PlayerRespawnEvent event);

	/**
	 * Player respawn event handler for HIGHEST priority
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	void onPlayerRespawnHIGHEST(PlayerRespawnEvent event);

	/**
	 * Cancel mob targeting of a player for configured time period following death respawn
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event);
}
