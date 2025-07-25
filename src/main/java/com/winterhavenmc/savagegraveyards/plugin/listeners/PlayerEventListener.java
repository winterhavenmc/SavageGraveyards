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

package com.winterhavenmc.savagegraveyards.plugin.listeners;

import com.winterhavenmc.savagegraveyards.plugin.PluginMain;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.plugin.util.Config;
import com.winterhavenmc.savagegraveyards.plugin.util.Macro;
import com.winterhavenmc.savagegraveyards.plugin.util.MessageId;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Implements Listener for player events
 */
public final class PlayerEventListener implements Listener
{
	// reference to main class
	private final PluginMain plugin;

	// player death respawn hash set, prevents setting respawn location to graveyards on non-death respawn events
	private final Set<UUID> deathTriggeredRespawn = ConcurrentHashMap.newKeySet();

	// unmodifiable set of entity target cancel reasons
	private final static Set<TargetReason> CANCEL_REASONS = Set.of(
			TargetReason.CLOSEST_PLAYER,
			TargetReason.RANDOM_TARGET);

	private final static String RESPAWN_PRIORITY = "respawn-priority";


	/**
	 * constructor method for {@code PlayerEventListener} class
	 *
	 * @param plugin A reference to this plugin's main class
	 */
	public PlayerEventListener(final PluginMain plugin)
	{
		// reference to main
		this.plugin = plugin;

		// register events in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	private String getConfigSetting()
	{
		return plugin.getConfig().getString(RESPAWN_PRIORITY);
	}


	/**
	 * Player death event handler
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
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
	void onPlayerRespawnLOWEST(final PlayerRespawnEvent event)
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
	void onPlayerRespawnLOW(final PlayerRespawnEvent event)
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
	void onPlayerRespawnNORMAL(final PlayerRespawnEvent event)
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
	void onPlayerRespawnHIGH(final PlayerRespawnEvent event)
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
	void onPlayerRespawnHIGHEST(final PlayerRespawnEvent event)
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

		// if deathTriggeredRespawn set does not contain user uuid, do nothing and return
		if (!deathTriggeredRespawn.contains(player.getUniqueId()))
		{
			return;
		}

		// remove player uuid from deathTriggeredRespawn set
		deathTriggeredRespawn.remove(player.getUniqueId());

		// check that player world is enabled
		if (!plugin.worldManager.isEnabled(player.getWorld()))
		{
			return;
		}

		// check that player has graveyard.respawn permission
		if (!player.hasPermission("graveyard.respawn"))
		{
			return;
		}

		// get nearest valid graveyard for player
		//TODO: consider using List returned by 'selectNearestGraveyards()' method
		Optional<Graveyard.Valid> optionalGraveyard = plugin.dataStore.graveyards().getNearest(player);

		// if graveyard was found in data store and graveyard location is valid, set respawn location
		if (optionalGraveyard.isPresent()
				&& optionalGraveyard.get().getLocation().getWorld() != null)
		{
			// unwrap optional graveyard
			Graveyard.Valid graveyard = optionalGraveyard.get();

			// unwrap optional location
			Location location = graveyard.getLocation();

			// if bedspawn is closer, set respawn location to bedspawn
			if (Config.CONSIDER_BEDSPAWN.getBoolean(plugin.getConfig()))
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

			// send player message
			plugin.messageBuilder.compose(player, MessageId.DEFAULT_RESPAWN)
					.setAltMessage(graveyard.attributes().respawnMessage().value())
					.setMacro(Macro.GRAVEYARD, graveyard.displayName())
					.setMacro(Macro.LOCATION, location)
					.send();

			// put player in safety cooldown map
			plugin.safetyManager.putPlayer(player, graveyard);
		}
	}


	/**
	 * Cancel mob targeting of a player for configured time period following death respawn
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	void onEntityTargetLivingEntity(final EntityTargetLivingEntityEvent event)
	{
		// check that target is a player, in the safety cooldown and event is in CANCEL_REASONS set
		if (event.getTarget() != null && event.getTarget() instanceof Player player
				&& plugin.safetyManager.isPlayerProtected(player)
				&& CANCEL_REASONS.contains(event.getReason()))
		{
			event.setCancelled(true);
		}
	}

}
