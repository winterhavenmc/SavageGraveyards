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

package com.winterhavenmc.savagegraveyards.plugin.models.graveyard.attributes;

import com.winterhavenmc.savagegraveyards.plugin.util.Config;
import org.bukkit.plugin.Plugin;

import java.time.Duration;


public record Attributes(Enabled enabled,
                         Hidden hidden,
                         DiscoveryRange discoveryRange,
                         DiscoveryMessage discoveryMessage,
                         RespawnMessage respawnMessage,
                         Group group,
                         SafetyRange safetyRange,
                         SafetyTime safetyTime)
{
	public Attributes(Plugin plugin)
	{
		this(Enabled.of(Config.DEFAULT_ENABLED.getBoolean(plugin.getConfig())),
				Hidden.of(Config.DEFAULT_HIDDEN.getBoolean(plugin.getConfig())),
				DiscoveryRange.of(Config.DISCOVERY_RANGE.getInt(plugin.getConfig())),
				DiscoveryMessage.of(""),
				RespawnMessage.of(""),
				Group.of(""),
				SafetyRange.of(50),
				SafetyTime.of(Duration.ofSeconds(15)));
	}

	public Attributes withEnabled(boolean newValue)
	{
		return new Attributes(enabled.with(newValue), hidden, discoveryRange, discoveryMessage, respawnMessage, group, safetyRange, safetyTime);
	}

	public Attributes withHidden(boolean newValue)
	{
		return new Attributes(enabled, hidden.with(newValue), discoveryRange, discoveryMessage, respawnMessage, group, safetyRange, safetyTime);
	}

	public Attributes withDiscoveryRange(int newValue)
	{
		return new Attributes(enabled, hidden, discoveryRange.with(newValue), discoveryMessage, respawnMessage, group, safetyRange, safetyTime);
	}

	public Attributes withDiscoveryMessage(String newValue)
	{
		return new Attributes(enabled, hidden, discoveryRange, discoveryMessage.with(newValue), respawnMessage, group, safetyRange, safetyTime);
	}

	public Attributes withRespawnMessage(String newValue)
	{
		return new Attributes(enabled, hidden, discoveryRange, discoveryMessage, respawnMessage.with(newValue), group, safetyRange, safetyTime);
	}

	public Attributes withGroup(String newValue)
	{
		return new Attributes(enabled, hidden, discoveryRange, discoveryMessage, respawnMessage, group.with(newValue), safetyRange, safetyTime);
	}

	public Attributes withSafetyRange(int newValue)
	{
		return new Attributes(enabled, hidden, discoveryRange, discoveryMessage, respawnMessage, group, safetyRange.with(newValue), safetyTime);
	}

	public Attributes withSafetyTime(Duration newValue)
	{
		return new Attributes(enabled, hidden, discoveryRange, discoveryMessage, respawnMessage, group, safetyRange, safetyTime.with(newValue));
	}
}
