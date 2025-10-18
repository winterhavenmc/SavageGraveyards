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

package com.winterhavenmc.savagegraveyards.core.tasks.discovery;

import com.winterhavenmc.savagegraveyards.core.context.DiscoveryCtx;
import com.winterhavenmc.savagegraveyards.core.events.DiscoveryEvent;
import com.winterhavenmc.savagegraveyards.core.util.Config;
import com.winterhavenmc.savagegraveyards.core.util.Macro;
import com.winterhavenmc.savagegraveyards.core.util.MessageId;
import com.winterhavenmc.savagegraveyards.core.util.SoundId;

import com.winterhavenmc.savagegraveyards.models.discovery.Discovery;
import com.winterhavenmc.savagegraveyards.models.discovery.ValidDiscovery;
import com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Predicate;


/**
 * Repeating task that checks if any players are
 * within discovery distance of undiscovered graveyard locations
 */
public final class DiscoveryTask extends BukkitRunnable
{
	private final Plugin plugin;
	private final DiscoveryCtx ctx;

	/**
	 * Class constructor
	 */
	public DiscoveryTask(final Plugin plugin, final DiscoveryCtx ctx)
	{
		this.plugin = plugin;
		this.ctx = ctx;
	}


	@Override
	public void run()
	{
		this.plugin.getServer().getOnlinePlayers().stream()
				.filter(player -> player.hasPermission("graveyard.discover"))
				.forEach(player -> ctx.graveyards().getUndiscoveredGraveyards(player)
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
		Discovery discovery = Discovery.of(graveyard, player);

		if (discovery instanceof ValidDiscovery validDiscovery && ctx.discoveries().save(validDiscovery))
		{
			ctx.messageBuilder().sounds().play(player, SoundId.ACTION_DISCOVERY);
			ctx.messageBuilder().compose(player, MessageId.DEFAULT_DISCOVERY)
					.setMacro(Macro.GRAVEYARD, graveyard.displayName().colorString())
					.setMacro(Macro.LOCATION, graveyard.getLocation())
					.send();

			DiscoveryEvent event = new DiscoveryEvent(player, graveyard);
			this.plugin.getServer().getPluginManager().callEvent(event);
		}
	}

}
