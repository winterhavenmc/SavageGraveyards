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

package com.winterhavenmc.savagegraveyards.adapters.tasks.discovery;

import com.winterhavenmc.library.messagebuilder.models.time.TimeUnit;

import com.winterhavenmc.savagegraveyards.core.ports.tasks.discovery.DiscoveryObserver;
import com.winterhavenmc.savagegraveyards.core.ports.tasks.discovery.DiscoveryTask;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Supplier;


/**
 * A wrapper class for managing the lifecycle of DiscoveryTasks
 */
public final class BukkitDiscoveryObserver implements DiscoveryObserver
{
	private final Plugin plugin;
	private final Supplier<DiscoveryTask> discoveryTaskSupplier;
	private BukkitTask discoveryTask;


	/**
	 * Create an instance of a DiscoveryObserver
	 */
	public BukkitDiscoveryObserver(final Plugin plugin,
	                               final Supplier<DiscoveryTask> discoveryTaskSupplier)
	{
		this.plugin = plugin;
		this.discoveryTaskSupplier = discoveryTaskSupplier;

		this.run();
	}


	/**
	 * Start a DiscoveryTask, using the interval defined in the plugin configuration file
	 */
	@Override
	public void run()
	{
		int discoveryInterval = plugin.getConfig().getInt("discovery-interval");

		if (discoveryInterval > 0)
		{
			this.discoveryTask = discoveryTaskSupplier.get().runTaskTimer(plugin, 0L, TimeUnit.SECONDS.toTicks(discoveryInterval));
		}
	}


	/**
	 * Cancel a running DiscoveryTask
	 */
	@Override
	public void cancel()
	{
		if (this.discoveryTask != null && !this.discoveryTask.isCancelled())
		{
			this.discoveryTask.cancel();
		}
	}


	/**
	 * Cancel and restart a DiscoveryTask, re-reading the interval setting from the plugin configuration file
	 * in case of changes to the setting
	 */
	@Override
	public void reload()
	{
		this.cancel();
		this.run();
	}
}
