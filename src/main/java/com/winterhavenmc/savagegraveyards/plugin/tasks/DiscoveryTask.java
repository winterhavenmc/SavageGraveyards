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

package com.winterhavenmc.savagegraveyards.plugin.tasks;

import com.winterhavenmc.savagegraveyards.plugin.PluginMain;
import com.winterhavenmc.savagegraveyards.plugin.events.DiscoveryEvent;
import com.winterhavenmc.savagegraveyards.plugin.models.discovery.Discovery;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.plugin.util.Config;
import com.winterhavenmc.savagegraveyards.plugin.util.Macro;
import com.winterhavenmc.savagegraveyards.plugin.util.MessageId;
import com.winterhavenmc.savagegraveyards.plugin.util.SoundId;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Predicate;


/**
 * Repeating task that checks if any players are
 * within discovery distance of undiscovered graveyard locations
 */
public final class DiscoveryTask extends BukkitRunnable
{
	private final PluginMain plugin;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public DiscoveryTask(final PluginMain plugin)
	{
		this.plugin = plugin;
	}


	@Override
	public void run()
	{
		plugin.getServer().getOnlinePlayers().stream()
				.filter(player -> player.hasPermission("graveyard.discover"))
				.forEach(player -> plugin.dataStore.selectUndiscoveredGraveyards(player)
						.filter(withinRange(player))
						.filter(groupMatches(player))
						.forEach(graveyard -> createDiscoveryRecord(graveyard, player)));
	}


	private Predicate<Graveyard.Valid> groupMatches(Player player)
	{
		return graveyard ->
				graveyard.attributes().group() == null
						|| graveyard.attributes().group().value().isBlank()
						|| player.hasPermission("group." + graveyard.attributes().group().value());
	}


	private Predicate<Graveyard.Valid> withinRange(Player player)
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
	private int getDiscoveryRange(Graveyard.Valid graveyard)
	{
		return (graveyard.attributes().discoveryRange().value() < 0)
				? Config.DISCOVERY_RANGE.getInt(plugin.getConfig())
				: graveyard.attributes().discoveryRange().value();
	}


	private void createDiscoveryRecord(Graveyard.Valid graveyard, Player player)
	{
		Discovery discovery = Discovery.of(graveyard, player);

		if (discovery instanceof Discovery.Valid validDiscovery && plugin.dataStore.insertDiscovery(validDiscovery))
		{
			plugin.soundConfig.playSound(player, SoundId.ACTION_DISCOVERY);
			plugin.messageBuilder.compose(player, MessageId.DEFAULT_DISCOVERY)
					.setAltMessage(graveyard.attributes().discoveryMessage().value())
					.setMacro(Macro.GRAVEYARD, graveyard.displayName().colorString())
					.setMacro(Macro.LOCATION, graveyard.getLocation())
					.send();

			DiscoveryEvent event = new DiscoveryEvent(player, graveyard);
			plugin.getServer().getPluginManager().callEvent(event);
		}
	}

}
