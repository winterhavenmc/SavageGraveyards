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

package com.winterhavenmc.savagegraveyards.plugin.util;

import com.winterhavenmc.library.TimeUnit;
import com.winterhavenmc.savagegraveyards.plugin.PluginMain;
import com.winterhavenmc.savagegraveyards.plugin.tasks.DiscoveryTask;
import org.bukkit.scheduler.BukkitTask;


public final class DiscoveryManager
{
	private final PluginMain plugin;
	private BukkitTask discoveryTask;


	public DiscoveryManager(final PluginMain plugin)
	{
		this.plugin = plugin;
		this.runDiscoveryTask();
	}


	public void runDiscoveryTask()
	{
		int discoveryInterval = Config.DISCOVERY_INTERVAL.getInt(plugin.getConfig());

		if (discoveryInterval > 0)
		{
			discoveryTask = new DiscoveryTask(this.plugin)
					.runTaskTimer(plugin, 0L, TimeUnit.SECONDS.toTicks(discoveryInterval));
		}
	}


	public void cancel()
	{
		if (this.discoveryTask != null)
		{
			this.discoveryTask.cancel();
		}
	}


	public void reload()
	{
		this.cancel();
		this.runDiscoveryTask();
	}
}
