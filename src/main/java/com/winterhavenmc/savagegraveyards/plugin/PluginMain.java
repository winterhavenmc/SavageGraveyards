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

package com.winterhavenmc.savagegraveyards.plugin;

import com.winterhavenmc.savagegraveyards.plugin.commands.CommandManager;
import com.winterhavenmc.savagegraveyards.plugin.listeners.PlayerEventListener;
import com.winterhavenmc.savagegraveyards.plugin.storage.DataStore;
import com.winterhavenmc.savagegraveyards.plugin.tasks.DiscoveryTask;
import com.winterhavenmc.savagegraveyards.plugin.util.*;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.library.soundconfig.SoundConfiguration;
import com.winterhavenmc.library.soundconfig.YamlSoundConfiguration;
import com.winterhavenmc.library.worldmanager.WorldManager;
import com.winterhavenmc.library.time.TimeUnit;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;


/**
 * Bukkit plugin to allow creation of graveyard locations where players
 * will respawn on death. The nearest graveyard location that is valid
 * for the player will be chosen at the time of death.
 */
public class PluginMain extends JavaPlugin
{
	public MessageBuilder messageBuilder;
	public DataStore dataStore;
	public WorldManager worldManager;
	public SoundConfiguration soundConfig;
	public SafetyManager safetyManager;
	private BukkitTask discoveryTask;


	@Override
	public void onEnable()
	{
		// install default config.yml if not present
		saveDefaultConfig();

		// instantiate message builder
		messageBuilder = MessageBuilder.create(this);

		// instantiate sound configuration
		soundConfig = new YamlSoundConfiguration(this);

		// instantiate world manager
		worldManager = new WorldManager(this);

		// connect to storage object
		dataStore = DataStore.connect(this);

		// instantiate safety manager
		safetyManager = new SafetyManager(this);

		// instantiate player event listener
		new PlayerEventListener(this);

		// instantiate command manager
		new CommandManager(this);

		// run discovery task
		discoveryTask = new DiscoveryTask(this)
			.runTaskTimer(this, 0L, TimeUnit.SECONDS.toTicks(Config.DISCOVERY_INTERVAL.getLong(getConfig())));

		// bStats
		new MetricsHandler(this);
	}


	@Override
	public void onDisable()
	{
		discoveryTask.cancel();
		dataStore.close();
	}

}
