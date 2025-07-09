/*
 * Copyright (c) 2022 Tim Savage.
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

package com.winterhavenmc.savagegraveyards.tasks;

import com.winterhavenmc.savagegraveyards.PluginMain;
import com.winterhavenmc.savagegraveyards.events.DiscoveryEvent;
import com.winterhavenmc.savagegraveyards.models.discovery.Discovery;
import com.winterhavenmc.savagegraveyards.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.util.Macro;
import com.winterhavenmc.savagegraveyards.util.MessageId;
import com.winterhavenmc.savagegraveyards.util.SoundId;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


/**
 * Repeating task that checks if any players are
 * within discovery distance of undiscovered graveyard locations
 */
public final class DiscoveryTask extends BukkitRunnable
{
	// reference to plugin main class
	private final PluginMain plugin;

	// config setting key
	private final static String DISCOVERY_RANGE = "discovery-range";


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
		// iterate through online players
		for (Player player : plugin.getServer().getOnlinePlayers())
		{
			// if player does not have discover permission, skip to next player
			if (player.hasPermission("graveyard.discover"))
			{
				// iterate through player's undiscovered graveyards
				for (Graveyard.Valid graveyard : plugin.dataStore.selectUndiscoveredGraveyards(player))
				{
					// check if player is in graveyard group
					if (graveyard.attributes().group() == null
							|| graveyard.attributes().group().isEmpty()
							|| player.hasPermission("group." + graveyard.attributes().group()))
					{
						// get graveyard discovery range, or config default if negative
						int discoveryRange = graveyard.attributes().discoveryRange();
						if (discoveryRange < 0)
						{
							discoveryRange = plugin.getConfig().getInt(DISCOVERY_RANGE);
						}

						// check if player is within discovery range of graveyard
						if (graveyard.getLocation().distanceSquared(player.getLocation()) < Math.pow(discoveryRange, 2))
						{
							// create discovery record
							Discovery discovery = Discovery.of(graveyard.searchKey(), player.getUniqueId());
							if (discovery instanceof Discovery.Valid validDiscovery)
							{
								plugin.dataStore.insertDiscovery(validDiscovery);

								// send player message
								plugin.messageBuilder.compose(player, MessageId.DEFAULT_DISCOVERY)
										.setAltMessage(graveyard.attributes().discoveryMessage())
										.setMacro(Macro.GRAVEYARD, graveyard.displayName())
										.setMacro(Macro.LOCATION, graveyard.getLocation())
										.send();

								// call discovery event
								DiscoveryEvent event = new DiscoveryEvent(player, graveyard);
								plugin.getServer().getPluginManager().callEvent(event);

								// play discovery sound
								plugin.soundConfig.playSound(player, SoundId.ACTION_DISCOVERY);
							}
						}
					}
				}
			}
		}
	}

}
