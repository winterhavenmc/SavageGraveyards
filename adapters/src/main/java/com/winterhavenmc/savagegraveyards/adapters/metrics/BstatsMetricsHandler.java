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

package com.winterhavenmc.savagegraveyards.adapters.metrics;

import com.winterhavenmc.savagegraveyards.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.datastore.GraveyardRepository;

import com.winterhavenmc.savagegraveyards.models.Config;
import com.winterhavenmc.savagegraveyards.metrics.MetricsHandler;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

import org.bukkit.plugin.Plugin;


public class BstatsMetricsHandler implements MetricsHandler
{
	public BstatsMetricsHandler(final Plugin plugin, final ConnectionProvider connectionProvider)
	{
		final GraveyardRepository graveyards = connectionProvider.graveyards();
		final Metrics metrics = new Metrics(plugin, 13924);

		// get total number of graveyards as single line chart
		metrics.addCustomChart(new SingleLineChart("total_graveyards", graveyards::getCount));

		// total number of graveyards as pie chart
		metrics.addCustomChart(new SimplePie("graveyard_count", () -> String.valueOf(graveyards.getCount())));

		// pie chart of default enabled
		metrics.addCustomChart(new SimplePie("default_enabled", () -> Config.DEFAULT_ENABLED.getString(plugin.getConfig())));

		// pie chart of default hidden
		metrics.addCustomChart(new SimplePie("default_hidden", () -> Config.DEFAULT_HIDDEN.getString(plugin.getConfig())));

		// pie chart of safety time
		metrics.addCustomChart(new SimplePie("safety_time", () -> Config.SAFETY_TIME.getString(plugin.getConfig())));

		// pie chart of discovery range
		metrics.addCustomChart(new SimplePie("discovery_range", () -> Config.DISCOVERY_RANGE.getString(plugin.getConfig())));

		// pie chart of discovery interval
		metrics.addCustomChart(new SimplePie("discovery_interval", () -> Config.DISCOVERY_INTERVAL.getString(plugin.getConfig())));

		// pie chart of respawn listener priority
		metrics.addCustomChart(new SimplePie("respawn_listener_priority", () -> Config.RESPAWN_PRIORITY.getString(plugin.getConfig())));

		// pie chart of sound effects enabled
		metrics.addCustomChart(new SimplePie("sound_effects_enabled", () -> Config.SOUND_EFFECTS.getString(plugin.getConfig())));

		// pie chart of titles enabled
		metrics.addCustomChart(new SimplePie("titles_enabled", () -> Config.TITLES_ENABLED.getString(plugin.getConfig())));

		// pie chart of configured language
		metrics.addCustomChart(new SimplePie("language", () -> Config.LANGUAGE.getString(plugin.getConfig())));
	}

}
