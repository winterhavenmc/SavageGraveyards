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

import com.winterhavenmc.savagegraveyards.core.SavageGraveyardsPluginController;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.GraveyardRepository;
import com.winterhavenmc.savagegraveyards.core.util.Config;
import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.library.soundconfig.SoundConfiguration;
import com.winterhavenmc.library.time.TimeUnit;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;


/**
 * A wrapper class for managing the lifecycle of DiscoveryTasks
 */
public final class GraveyardDiscoveryObserver implements DiscoveryObserver
{
	private final Plugin plugin;
	private final MessageBuilder messageBuilder;
	private final SoundConfiguration soundConfig;
	private final DiscoveryRepository discoveries;
	private final GraveyardRepository graveyards;
	private BukkitTask discoveryTask;


	GraveyardDiscoveryObserver()
	{
		plugin = null;
		messageBuilder = null;
		soundConfig = null;
		discoveries = null;
		graveyards = null;
	}


	/**
	 * Create an instance of a DiscoveryObserver
	 */
	private GraveyardDiscoveryObserver(final Plugin plugin,
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
		this.run();
	}


	@Override
	public GraveyardDiscoveryObserver init(final SavageGraveyardsPluginController.DiscoveryContextContainer ctx)
	{
		return new GraveyardDiscoveryObserver(ctx.plugin(), ctx.messageBuilder(), ctx.soundConfig(), ctx.discoveries(), ctx.graveyards());
	}


	/**
	 * Start a DiscoveryTask, using the interval defined in the plugin configuration file
	 */
	@Override
	public void run()
	{
		int discoveryInterval = Config.DISCOVERY_INTERVAL.getInt(plugin.getConfig());

		if (discoveryInterval > 0)
		{
			discoveryTask = new DiscoveryTask(plugin, messageBuilder, soundConfig, discoveries, graveyards)
					.runTaskTimer(plugin, 0L, TimeUnit.SECONDS.toTicks(discoveryInterval));
		}
	}


	/**
	 * Cancel a running DiscoveryTask
	 */
	@Override
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
	@Override
	public void reload()
	{
		this.cancel();
		this.run();
	}
}
