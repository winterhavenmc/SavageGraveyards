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

package com.winterhavenmc.savagegraveyards.core.tasks.discovery;

import com.winterhavenmc.library.messagebuilder.models.time.TimeUnit;

import com.winterhavenmc.savagegraveyards.core.context.DiscoveryCtx;
import org.bukkit.scheduler.BukkitTask;


/**
 * A wrapper class for managing the lifecycle of DiscoveryTasks
 */
public final class InitializedDiscoveryObserver implements ValidDiscoveryObserver
{
	private final DiscoveryCtx ctx;
	private BukkitTask discoveryTask;


	/**
	 * Create an instance of a DiscoveryObserver
	 */
	InitializedDiscoveryObserver(final DiscoveryCtx ctx)
	{
		this.ctx = ctx;
		this.run();
	}


	/**
	 * Start a DiscoveryTask, using the interval defined in the plugin configuration file
	 */
	public void run()
	{
		int discoveryInterval = ctx.plugin().getConfig().getInt("discovery-interval");

		if (discoveryInterval > 0)
		{
			discoveryTask = new DiscoveryTask(ctx.plugin(), ctx)
					.runTaskTimer(ctx.plugin(), 0L, TimeUnit.SECONDS.toTicks(discoveryInterval));
		}
	}


	/**
	 * Cancel a running DiscoveryTask
	 */
	public void cancel()
	{
		if (this.discoveryTask != null)
		{
			this.discoveryTask.cancel();
		}
	}


	/**
	 * Cancel and restart a DiscoveryTask, re-reading the interval setting from the plugin configuration file
	 * in case of changes to the setting
	 */
	public void reload()
	{
		this.cancel();
		this.run();
	}
}
