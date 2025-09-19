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

package com.winterhavenmc.savagegraveyards.core;

import com.winterhavenmc.savagegraveyards.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.core.ports.commands.CommandManager;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.GraveyardRepository;
import com.winterhavenmc.savagegraveyards.core.ports.listeners.PlayerEventListener;
import com.winterhavenmc.savagegraveyards.core.tasks.DiscoveryObserver;
import com.winterhavenmc.savagegraveyards.core.tasks.GraveyardDiscoveryObserver;
import com.winterhavenmc.savagegraveyards.core.tasks.SafetyManager;
import com.winterhavenmc.savagegraveyards.core.util.*;

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
public class SavageGraveyardsPluginController implements PluginController
{
	public MessageBuilder messageBuilder;
	public ConnectionProvider datastore;
	public WorldManager worldManager;
	public SoundConfiguration soundConfig;
	public SafetyManager safetyManager;
	public DiscoveryObserver discoveryObserver;
	public CommandManager commandManager;
	public PlayerEventListener playerEventListener;


	@Override
	public void startUp(final JavaPlugin plugin, final ConnectionProvider connectionProvider,
	                    final CommandManager commandManager, final PlayerEventListener playerEventListener,
	                    final DiscoveryObserver discoveryObserver)
	{
		// install default config.yml if not present
		plugin.saveDefaultConfig();

		// instantiate message builder
		this.messageBuilder = MessageBuilder.create(plugin);

		// instantiate sound configuration
		this.soundConfig = new YamlSoundConfiguration(plugin);

		// instantiate world manager
		this.worldManager = new WorldManager(plugin);

		// connect to storage object
		this.datastore = connectionProvider.connect();

		// instantiate safety manager
		this.safetyManager = new SafetyManager(plugin, messageBuilder);

		// initialize discovery manager
		this.discoveryObserver = discoveryObserver.init(plugin, messageBuilder, soundConfig, datastore);

		// bStats
		new MetricsHandler(plugin, datastore);

		// instantiate context containers
		CommandContextContainer commandCtx = new CommandContextContainer(plugin, messageBuilder, soundConfig,
				worldManager, datastore.graveyards(), datastore.discoveries(), discoveryObserver);
		ListenerContextContainer listenerCtx = new ListenerContextContainer(plugin, messageBuilder, worldManager,
				datastore.graveyards(), safetyManager);

		// initialize command manager
		this.commandManager = commandManager.init(commandCtx);

		// initialize player event listener
		this.playerEventListener = playerEventListener.init(listenerCtx);
	}


	@Override
	public void shutDown()
	{
		discoveryObserver.cancel();
		datastore.close();
	}


	public record CommandContextContainer(JavaPlugin plugin, MessageBuilder messageBuilder,
	                                      SoundConfiguration soundConfig, WorldManager worldManager,
	                                      GraveyardRepository graveyards, DiscoveryRepository discoveries,
	                                      DiscoveryObserver discoveryObserver) { }


	public record ListenerContextContainer(JavaPlugin plugin, MessageBuilder messageBuilder, WorldManager worldManager,
	                                       GraveyardRepository graveyards, SafetyManager safetyManager) { }
}
