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

package com.winterhavenmc.savagegraveyards.tasks.discovery;

import com.winterhavenmc.savagegraveyards.events.BukkitDiscoveryEvent;
import com.winterhavenmc.savagegraveyards.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.models.Config;
import com.winterhavenmc.savagegraveyards.models.Macro;
import com.winterhavenmc.savagegraveyards.models.MessageId;

import com.winterhavenmc.savagegraveyards.models.discovery.Discovery;
import com.winterhavenmc.savagegraveyards.models.discovery.ValidDiscovery;
import com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Predicate;


/**
 * Repeating task that checks if any players are
 * within discovery distance of undiscovered graveyard locations
 */
public final class BukkitDiscoveryTask extends BukkitRunnable implements DiscoveryTask
{
	private final Plugin plugin;
	private final MessageBuilder messageBuilder;
	private final ConnectionProvider connectionProvider;
	private final static String PERMISSION_NODE = "graveyard.discover";


	/**
	 * Class constructor
	 */
	public BukkitDiscoveryTask(final Plugin plugin,
	                           final MessageBuilder messageBuilder,
							   final ConnectionProvider connectionProvider)
	{
		this.plugin = plugin;
		this.messageBuilder = messageBuilder;
		this.connectionProvider = connectionProvider;
	}


	public static DiscoveryTask create(final Plugin plugin,
	                                   final MessageBuilder messageBuilder,
	                                   final ConnectionProvider connectionProvider)
	{
		return new BukkitDiscoveryTask(plugin, messageBuilder, connectionProvider);
	}


	@Override
	public void run()
	{
		this.plugin.getServer().getOnlinePlayers().stream()
				.filter(player -> player.hasPermission(PERMISSION_NODE))
				.forEach(player -> connectionProvider.graveyards().getUndiscoveredGraveyards(player)
						.filter(withinRange(player))
						.filter(groupMatches(player))
						.forEach(graveyard -> createDiscoveryRecord(graveyard, player)));
	}


	private Predicate<ValidGraveyard> groupMatches(final Player player)
	{
		return graveyard ->
				graveyard.attributes().group() == null
						|| graveyard.attributes().group().value().isBlank()
						|| player.hasPermission("group." + graveyard.attributes().group().value());
	}


	private Predicate<ValidGraveyard> withinRange(final Player player)
	{
		return graveyard -> graveyard.getLocation()
				.distanceSquared(player.getLocation()) < Math.pow(getDiscoveryRange(graveyard), 2);
	}


	/**
	 * Get graveyard discovery range, or config default if graveyard attribute is negative
	 *
	 * @param graveyard the graveyard to retrieve discovery range
	 * @return the discovery range of the graveyard, or default if negative
	 */
	private int getDiscoveryRange(final ValidGraveyard graveyard)
	{
		return (graveyard.attributes().discoveryRange().value() < 0)
				? Config.DISCOVERY_RANGE.getInt(this.plugin.getConfig())
				: graveyard.attributes().discoveryRange().value();
	}


	private void createDiscoveryRecord(final ValidGraveyard graveyard, final Player player)
	{
		Discovery discovery = Discovery.of(graveyard.uid(), player.getUniqueId());

		if (discovery instanceof ValidDiscovery validDiscovery && connectionProvider.discoveries().save(validDiscovery))
		{
			messageBuilder.compose(player, MessageId.EVENT_DISCOVERY_DEFAULT)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.send();

			Event event = new BukkitDiscoveryEvent(player, graveyard);
			this.plugin.getServer().getPluginManager().callEvent(event);
		}
	}


	@Override
	public void cancel() throws IllegalStateException
	{
		super.cancel();
	}

}
