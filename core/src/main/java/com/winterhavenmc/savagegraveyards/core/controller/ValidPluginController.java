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

package com.winterhavenmc.savagegraveyards.core.controller;

import com.winterhavenmc.savagegraveyards.core.context.*;
import com.winterhavenmc.savagegraveyards.core.ports.commands.CommandDispatcher;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.GraveyardRepository;
import com.winterhavenmc.savagegraveyards.core.ports.listeners.PlayerEventListener;
import com.winterhavenmc.savagegraveyards.core.tasks.discovery.DiscoveryObserver;
import com.winterhavenmc.savagegraveyards.core.tasks.discovery.InitializedObserver;
import com.winterhavenmc.savagegraveyards.core.tasks.discovery.InvalidDiscoveryObserver;
import com.winterhavenmc.savagegraveyards.core.tasks.discovery.UninitializedObserver;
import com.winterhavenmc.savagegraveyards.core.tasks.safety.InitializedSafetyManager;
import com.winterhavenmc.savagegraveyards.core.tasks.safety.InvalidSafetyManager;
import com.winterhavenmc.savagegraveyards.core.tasks.safety.SafetyManager;
import com.winterhavenmc.savagegraveyards.core.tasks.safety.UninitializedSafetyManager;
import com.winterhavenmc.savagegraveyards.core.util.MetricsHandler;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.library.soundconfig.SoundConfiguration;
import com.winterhavenmc.library.soundconfig.YamlSoundConfiguration;
import com.winterhavenmc.library.worldmanager.WorldManager;

import org.bukkit.plugin.java.JavaPlugin;


/**
 * Bukkit plugin to allow creation of graveyard locations where players
 * will respawn on death. The nearest graveyard location that is valid
 * for the player will be chosen at the time of death.
 */
public non-sealed class ValidPluginController implements PluginController
{
	private final JavaPlugin plugin;
	private final MessageBuilder messageBuilder;
	private final SoundConfiguration soundConfig;
	private final WorldManager worldManager;

	public CommandDispatcher commandDispatcher;
	public PlayerEventListener playerEventListener;
	public DiscoveryObserver discoveryObserver;
	public SafetyManager safetyManager;
	public ConnectionProvider datastore;
	public GraveyardRepository graveyards;
	public DiscoveryRepository discoveries;


	public ValidPluginController(final JavaPlugin plugin)
	{
		this.plugin = plugin;

		// install default config.yml if not present
		plugin.saveDefaultConfig();

		// instantiate message builder
		this.messageBuilder = MessageBuilder.create(plugin);

		// instantiate sound configuration
		this.soundConfig = new YamlSoundConfiguration(plugin);

		// instantiate world manager
		this.worldManager = new WorldManager(plugin);
	}


	public void startUp(final ConnectionProvider connectionProvider,
	                    final CommandDispatcher commandDispatcher,
	                    final PlayerEventListener playerEventListener,
	                    final DiscoveryObserver discoveryObserver,
	                    final SafetyManager safetyManager)
	{
		// connect to storage object
		this.datastore = connectionProvider.connect();

		// initialize discovery observer
		final DiscoveryCtx discoveryCtx = new DiscoveryCtx(plugin, messageBuilder, soundConfig, datastore.discoveries(), datastore.graveyards());
		this.discoveryObserver = switch (discoveryObserver)
		{
			case UninitializedObserver uninitialized -> uninitialized.init(discoveryCtx);
			case InitializedObserver initialized -> initialized;
			case InvalidDiscoveryObserver ignored -> new InvalidDiscoveryObserver("DiscoveryObserver could not be initialized!");
		};

		// initialize safety manager
		final SafetyCtx safetyCtx = new SafetyCtx(plugin, messageBuilder);
		this.safetyManager = switch (safetyManager)
		{
			case UninitializedSafetyManager uninitialized -> uninitialized.init(safetyCtx);
			case InitializedSafetyManager initialized -> initialized;
			case InvalidSafetyManager ignored -> new InvalidSafetyManager("SafetyManager could not be initialized!");
		};

		// initialize command dispatcher (depends on initialized discovery observer)
		final CommandCtx commandCtx = new CommandCtx(plugin, messageBuilder, soundConfig, worldManager,
				datastore.graveyards(), datastore.discoveries(), discoveryObserver);
		this.commandDispatcher = commandDispatcher.init(commandCtx);

		// initialize player event listener depends on initialized safety manager
		if (safetyManager instanceof InitializedSafetyManager initializedSafetyManager)
		{
			final ListenerCtx listenerCtx = new ListenerCtx(plugin, messageBuilder, worldManager, datastore.graveyards(), initializedSafetyManager);
			this.playerEventListener = playerEventListener.init(listenerCtx);
		}
		else
		{
			plugin.getLogger().severe("EventListener could not be initialized!");
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}

		// instantiate metrics handler
		final MetricsCtx metricsCtx = new MetricsCtx(plugin, datastore.graveyards());
		new MetricsHandler(metricsCtx);
	}


	public void shutDown()
	{
		if (this.discoveryObserver instanceof InitializedObserver initializedObserver)
		{
			initializedObserver.cancel();
		}
		datastore.close();
	}

}
