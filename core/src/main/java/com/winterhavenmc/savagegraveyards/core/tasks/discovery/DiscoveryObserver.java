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

public interface DiscoveryObserver
{
	/**
	 * Start a DiscoveryTask, using the interval defined in the plugin configuration file
	 */
	void run();

	/**
	 * Cancel a running DiscoveryTask
	 */
	void cancel();

	/**
	 * Cancel and restart a DiscoveryTask, re-reading the interval setting from the plugin configuration file
	 * in case of changes to the setting
	 */
	void reload();
}
