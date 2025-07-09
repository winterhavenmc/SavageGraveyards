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

package com.winterhavenmc.savagegraveyards.models.graveyard;

import com.winterhavenmc.savagegraveyards.util.Config;
import org.bukkit.plugin.Plugin;

import java.time.Duration;

public record Attributes(boolean enabled,
                         boolean hidden,
                         int discoveryRange,
                         String discoveryMessage,
                         String respawnMessage,
                         String group,
                         int safetyRange,
                         Duration safetyTime)
{
	public Attributes(Plugin plugin)
	{
		this(Config.DEFAULT_ENABLED.getBoolean(plugin.getConfig()),
				Config.DEFAULT_HIDDEN.getBoolean(plugin.getConfig()),
				Config.DISCOVERY_RANGE.getInt(plugin.getConfig()),
				"", "", "", 50, Duration.ofSeconds(15));
	}

	public Attributes withEnabled(boolean newValue)
	{
		return new Attributes(newValue, hidden, discoveryRange, discoveryMessage, respawnMessage, group, safetyRange, safetyTime);
	}

	public Attributes withHidden(boolean newValue)
	{
		return new Attributes(enabled, newValue, discoveryRange, discoveryMessage, respawnMessage, group, safetyRange, safetyTime);
	}

	public Attributes withDiscoveryRange(int newValue)
	{
		return new Attributes(enabled, hidden, newValue, discoveryMessage, respawnMessage, group, safetyRange, safetyTime);
	}

	public Attributes withDiscoveryMessage(String newValue)
	{
		return new Attributes(enabled, hidden, discoveryRange, newValue, respawnMessage, group, safetyRange, safetyTime);
	}

	public Attributes withRespawnMessage(String newValue)
	{
		return new Attributes(enabled, hidden, discoveryRange, discoveryMessage, newValue, group, safetyRange, safetyTime);
	}

	public Attributes withGroup(String newValue)
	{
		return new Attributes(enabled, hidden, discoveryRange, discoveryMessage, respawnMessage, newValue, safetyRange, safetyTime);
	}

	public Attributes withSafetyTime(Duration newValue)
	{
		return new Attributes(enabled, hidden, discoveryRange, discoveryMessage, respawnMessage, group, safetyRange, newValue);
	}
}
