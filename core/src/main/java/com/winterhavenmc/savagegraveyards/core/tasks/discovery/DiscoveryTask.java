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

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.library.soundconfig.SoundConfiguration;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.GraveyardRepository;
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
	private final MessageBuilder messageBuilder;
	private final SoundConfiguration soundConfig;
	private final DiscoveryRepository discoveries;
	private final GraveyardRepository graveyards;

	/**
	 * Class constructor
	 */
	public DiscoveryTask(final Plugin plugin,
	                     final MessageBuilder messageBuilder,
	                     final SoundConfiguration soundConfig,
						 final DiscoveryRepository discoveries,
	                     final GraveyardRepository graveyards)
	{
		this.plugin = plugin;
		this.messageBuilder = messageBuilder;
		this.soundConfig = soundConfig;
		this.discoveries = discoveries;
		this.graveyards = graveyards;
	}


	@Override
	public void run()
	{
		plugin.getServer().getOnlinePlayers().stream()
				.filter(player -> player.hasPermission("graveyard.discover"))
				.forEach(player -> graveyards.getUndiscoveredGraveyards(player)
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
				? Config.DISCOVERY_RANGE.getInt(plugin.getConfig())
				: graveyard.attributes().discoveryRange().value();
	}


	private void createDiscoveryRecord(final ValidGraveyard graveyard, final Player player)
	{
		Discovery discovery = Discovery.of(graveyard, player);

		if (discovery instanceof ValidDiscovery validDiscovery && discoveries.save(validDiscovery))
		{
			soundConfig.playSound(player, SoundId.ACTION_DISCOVERY);
			messageBuilder.compose(player, MessageId.DEFAULT_DISCOVERY)
					.setMacro(Macro.GRAVEYARD, graveyard.displayName().colorString())
					.setMacro(Macro.LOCATION, graveyard.getLocation())
					.send();

			DiscoveryEvent event = new DiscoveryEvent(player, graveyard);
			plugin.getServer().getPluginManager().callEvent(event);
		}
	}

}
