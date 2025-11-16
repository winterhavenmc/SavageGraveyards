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

import com.winterhavenmc.savagegraveyards.models.FailReason;
import com.winterhavenmc.savagegraveyards.models.Parameter;
import com.winterhavenmc.savagegraveyards.models.displayname.DisplayName;
import com.winterhavenmc.savagegraveyards.models.displayname.ValidDisplayName;
import com.winterhavenmc.savagegraveyards.models.graveyard.attributes.Attributes;
import com.winterhavenmc.savagegraveyards.models.location.ConfirmedLocation;
import com.winterhavenmc.savagegraveyards.models.location.ValidLocation;

import com.winterhavenmc.library.messagebuilder.core.ports.pipeline.accessors.displayname.DisplayNameable;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;


/**
 * Represents a graveyard as an algebraic data type, implemented using a sealed interface
 * with permitted types of {@link ValidGraveyard} or {@link InvalidGraveyard}.
 * <p>
 * <img src="doc-files/Graveyard_structure.svg" alt="Graveyard Structure"/>
 */
public sealed interface Graveyard extends DisplayNameable permits ValidGraveyard, InvalidGraveyard
{
	DisplayName displayName();
	String worldName();

	default String getDisplayName()
	{
		return this.displayName().colorString();
	}


	/**
	 * Creates a graveyard of the appropriate subtype when passed a minimal set of parameters.
	 * Used primarily in response to events or commands. Additional field values are retrieved
	 * from the plugin configuration, or default values are provided.
	 */
	static Graveyard of(final Plugin plugin,
	                    final ValidDisplayName displayName,
	                    final Player player)
	{
		if (plugin == null) throw new IllegalArgumentException("The parameter 'plugin' cannot be null.");
		else if (player == null) return new InvalidGraveyard(displayName, "ø", FailReason.PARAMETER_NULL, Parameter.PLAYER);
		else return Graveyard.of(displayName, UUID.randomUUID(), ConfirmedLocation.of(player), new Attributes(plugin));
	}


	/**
	 * Creates a graveyard of the appropriate subtype when passed a full set of parameters.
	 * Used primarily for creating objects from a persistent storage record.
	 */
	static Graveyard of(final ValidDisplayName displayName, final UUID graveyardUid,
	                    final ValidLocation location, final Attributes attributes)
	{
		if (displayName == null) return new InvalidGraveyard(DisplayName.NULL(), location.world().name(), FailReason.PARAMETER_NULL, Parameter.DISPLAY_NAME);
		else if (graveyardUid == null) return new InvalidGraveyard(displayName, location.world().name(), FailReason.PARAMETER_NULL, Parameter.GRAVEYARD_UID);
		else return new ValidGraveyard(graveyardUid, displayName, attributes, location);
	}

}
