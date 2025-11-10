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


import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.GraveyardRepository;
import org.bukkit.plugin.Plugin;


public sealed interface DiscoveryObserver permits ValidDiscoveryObserver, InvalidDiscoveryObserver
{
	static DiscoveryObserver create(final Plugin plugin,
	                                final MessageBuilder messageBuilder,
	                                final DiscoveryRepository discoveries,
	                                final GraveyardRepository graveyards,
	                                final DiscoveryTask discoveryTask)
	{
		if (plugin == null) { return new InvalidDiscoveryObserver("The parameter 'plugin' was null."); }
		else if (messageBuilder == null) { return new InvalidDiscoveryObserver("The parameter 'messageBuilder' was null."); }
		else if (discoveries == null) { return new InvalidDiscoveryObserver("The parameter 'discoveries' was null."); }
		else if (graveyards == null) { return new InvalidDiscoveryObserver("The parameter 'graveyards' was null."); }
		else if (discoveryTask == null) { return new InvalidDiscoveryObserver("The parameter 'discoveryTask was null."); }
		else
		{
			return new ValidDiscoveryObserver(plugin, messageBuilder, discoveries, graveyards, discoveryTask);
		}
	}
}
