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

package com.winterhavenmc.savagegraveyards.adapters.listeners.bukkit;

import com.winterhavenmc.savagegraveyards.core.context.ListenerCtx;
import com.winterhavenmc.savagegraveyards.core.ports.listeners.EventListener;
import com.winterhavenmc.savagegraveyards.core.util.Config;
import com.winterhavenmc.savagegraveyards.core.util.Macro;
import com.winterhavenmc.savagegraveyards.core.util.MessageId;
import com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * Implements Listener for player events
 */
public final class BukkitEventListener implements EventListener
{
	private final ListenerCtx ctx;
	private final Set<UUID> deathTriggeredRespawn = new HashSet<>();
	private final static Set<TargetReason> CANCEL_REASONS = Set.of(
			TargetReason.CLOSEST_PLAYER,
			TargetReason.RANDOM_TARGET);
	private final static String RESPAWN_PRIORITY = "respawn-priority";


	/**
	 * Private no param constructor
	 */
	private BukkitEventListener()
	{
		this.ctx = null;
	}


	/**
	 * Private constructor for {@code BukkitEventListener} class
	 *
	 * @param ctx a listener context container
	 */
	private BukkitEventListener(final ListenerCtx ctx)
	{
		this.ctx = ctx;
		ctx.plugin().getServer().getPluginManager().registerEvents(this, ctx.plugin());
	}


	/**
	 * Static factory method to instantiate this class. Note that the class is in an uninitialized state
	 * until the init method has been called
	 *
	 * @return a new instance of this class, uninitialized
	 */
	public static EventListener create()
	{
		return new BukkitEventListener();
	}


	/**
	 * Initialization method for this class, accepts a ListenerCtx
	 *
	 * @param ctx a listener context container
	 * @return an initialized instance of this class
	 */
	public EventListener init(ListenerCtx ctx)
	{
		return new BukkitEventListener(ctx);
	}


	private String getConfigSetting()
	{
		return ctx.plugin().getConfig().getString(RESPAWN_PRIORITY);
	}


	/**
	 * Player death event handler
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	@Override
	public void onPlayerDeath(final PlayerDeathEvent event)
	{
		// put player uuid in deathTriggeredRespawn set
		deathTriggeredRespawn.add(event.getEntity().getUniqueId());
	}


	/**
	 * Player respawn event handler for LOWEST priority
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	@Override
	public void onPlayerRespawnLOWEST(final PlayerRespawnEvent event)
	{
		if ("LOWEST".equalsIgnoreCase(getConfigSetting()))
		{
			onPlayerRespawnHandler(event);
		}
	}


	/**
	 * Player respawn event handler for LOW priority
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.LOW)
	@Override
	public void onPlayerRespawnLOW(final PlayerRespawnEvent event)
	{
		if ("LOW".equalsIgnoreCase(getConfigSetting()))
		{
			onPlayerRespawnHandler(event);
		}
	}


	/**
	 * Player respawn event handler for NORMAL priority
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	@Override
	public void onPlayerRespawnNORMAL(final PlayerRespawnEvent event)
	{
		if ("NORMAL".equalsIgnoreCase(getConfigSetting()))
		{
			onPlayerRespawnHandler(event);
		}
	}


	/**
	 * Player respawn event handler for HIGH priority
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.HIGH)
	@Override
	public void onPlayerRespawnHIGH(final PlayerRespawnEvent event)
	{
		if ("HIGH".equalsIgnoreCase(getConfigSetting()))
		{
			onPlayerRespawnHandler(event);
		}
	}


	/**
	 * Player respawn event handler for HIGHEST priority
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	@Override
	public void onPlayerRespawnHIGHEST(final PlayerRespawnEvent event)
	{
		if ("HIGHEST".equalsIgnoreCase(getConfigSetting()))
		{
			onPlayerRespawnHandler(event);
		}
	}


	/**
	 * Player respawn handler, called by the registered event listener with configured priority
	 *
	 * @param event the player respawn event handled by this method
	 */
	private void onPlayerRespawnHandler(final PlayerRespawnEvent event)
	{
		// get event player
		Player player = event.getPlayer();

		// if deathTriggeredRespawn set contains user uuid, handle player respawn event
		if (deathTriggeredRespawn.contains(player.getUniqueId()))
		{
			// remove player uuid from deathTriggeredRespawn set
			deathTriggeredRespawn.remove(player.getUniqueId());

			// check that player world is enabled
			// check that player has graveyard.respawn permission
			if (ctx.worldManager().isEnabled(player.getWorld()) && player.hasPermission("graveyard.respawn"))
			{
				// get nearest valid graveyard for player
				List<ValidGraveyard> nearestGraveyards = ctx.graveyards().getNearestGraveyards(player);

				if (!nearestGraveyards.isEmpty())
				{
					ValidGraveyard nearestGraveyard = nearestGraveyards.getFirst();

					// unwrap optional location
					Location location = nearestGraveyard.getLocation();

					// if bedspawn is closer, set respawn location to bedspawn
					if (Config.CONSIDER_BEDSPAWN.getBoolean(ctx.plugin().getConfig()))
					{
						// get player bedspawn location
						Location bedSpawnLocation = player.getRespawnLocation();

						// check bedspawn world is same as current world and closer than graveyard
						if (bedSpawnLocation != null
								&& bedSpawnLocation.getWorld() != null
								&& bedSpawnLocation.getWorld().equals(player.getWorld())
								&& bedSpawnLocation.distanceSquared(player.getLocation()) < location.distanceSquared(player.getLocation()))
						{
							// set respawn location to bedspawn location
							event.setRespawnLocation(bedSpawnLocation);
							return;
						}
					}

					event.setRespawnLocation(location);

					ctx.safetyManager().put(player, nearestGraveyard);
					//TODO: get rid of this message, and BukkitEventListener can drop MessageBuilder dependency
					ctx.messageBuilder().compose(player, MessageId.DEFAULT_RESPAWN)
							.setMacro(Macro.GRAVEYARD, nearestGraveyard.displayName())
							.setMacro(Macro.LOCATION, location)
							.send();
				}
			}
		}
	}


	/**
	 * Cancel mob targeting of a player for configured time period following death respawn
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	@Override
	public void onEntityTargetLivingEntity(final EntityTargetLivingEntityEvent event)
	{
		// check that target is a player, in the safety cooldown and event is in CANCEL_REASONS set
		if (event.getTarget() != null && event.getTarget() instanceof Player player
				&& ctx.safetyManager().isProtected(player)
				&& CANCEL_REASONS.contains(event.getReason()))
		{
			event.setCancelled(true);
		}
	}

}
