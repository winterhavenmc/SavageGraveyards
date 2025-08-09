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

package com.winterhavenmc.savagegraveyards.plugin.models.discovery;

import com.winterhavenmc.savagegraveyards.plugin.util.LocalizedMessage;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public enum DiscoveryReason implements LocalizedMessage
{
	GRAVEYARD_NULL("The parameter 'graveyard' was null."),
	PLAYER_NULL("The parameter 'player' was null."),
	SEARCH_KEY_NULL("The parameter 'searchKey' was null."),
	PLAYER_UID_NULL("The parameter 'playerUid' was null."),
	SEARCH_KEY_INVALID("The parameter 'searchKey' was invalid."),
	;

	private final String defaultMessage;


	DiscoveryReason(String defaultMessage)
	{
		this.defaultMessage = defaultMessage;
	}


	public String getLocalizedMessage(final Locale locale)
	{
		try
		{
			ResourceBundle bundle = ResourceBundle.getBundle(getClass().getSimpleName(), locale);
			return bundle.getString(name());
		}
		catch (MissingResourceException exception)
		{
			return this.defaultMessage;
		}
	}


	@Override
	public String toString()
	{
		return this.defaultMessage;
	}

}
